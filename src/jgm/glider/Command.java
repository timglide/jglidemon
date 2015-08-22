/*
 * -----LICENSE START-----
 * JGlideMon - A Java based remote monitor for MMO Glider
 * Copyright (C) 2007 - 2008 Tim
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -----LICENSE END-----
 */
package jgm.glider;

/**
 * An abstraction of the available remote
 * Glider commands.
 * 
 * @author Tim
 * @since 0.15
 */
public final class Command {
	public final String slash;
	public final String args;
	
	private Command(String slash) {
		this(slash, null);
	}
	
	private Command(String slash, String args) {
		this.slash = slash;
		this.args = args;
	}
	
	@Override
	public String toString() {
		return slash + (args != null ? " " + args : "");
	}
	
	/**
	 * Sends this command to the supplied Conn object.
	 * It is up to the caller to then read from Conn
	 * as necessary.
	 * 
	 * @param conn
	 */
	public void send(Conn conn) {
		conn.send("/" + toString());
	}
	
	/**
	 * Sends this command to the supplied Conn object
	 * and reads the result returned by Glider
	 * 
	 * @param conn
	 */
	public String getResult(Conn conn) throws java.io.IOException {
		return conn.cmd(toString());
	}
	
	// factory methods
	
	static final Command EXIT = new Command("exit");
	public static Command getExitCommand() {
		return EXIT;
	}
	
	static final Command STATUS = new Command("status");
	public static Command getStatusCommand() {
		return STATUS;
	}
	
	static final Command VERSION = new Command("version");
	public static Command getVersionCommand() {
		return VERSION;
	}
	
	public static Command getLogCommand(String channel) {
		return new Command("log", channel);
	}
	
	public static Command getNoLogCommand(String channel) {
		return new Command("nolog", channel);
	}
	
	public static Command getChatCommand(String keys) {
		return new Command("queuekeys", keys);
	}
	
	public static Command getHoldKeyCommand(int keyCode) {
		return new Command("holdkey", Integer.toString(keyCode));
	}
	
	public static Command getReleaseKeyCommand(int keyCode) {
		return new Command("releasekey", Integer.toString(keyCode));
	}
	
	public static Command getSetMouseCommand(double x, double y) {
		if (!(0.0 <= x && x < 1.0 && 0.0 <= y && y < 1.0))
			throw new IllegalArgumentException("x and y must be in the range [0.0, 1.0)");
		
		return new Command("setmouse", String.format("%s/%s", x, y));
	}
	
	static final Command CLICK_MOUSE_LEFT = new Command("clickmouse", "left");
	static final Command CLICK_MOUSE_RIGHT = new Command("clickmouse", "right");
	public static final Command getClickMouseCommand(String button) {
		button = button.toLowerCase();
		
		if (button.equals("left"))
			return CLICK_MOUSE_LEFT;
		
		if (button.equals("right"))
			return CLICK_MOUSE_RIGHT;
		
		throw new IllegalArgumentException("Button must be left or right");
	}
	
	static final Command START = new Command("startglide"); 
	public static Command getStartCommand() {
		return START;
	}
	
	static final Command STOP = new Command("stopglide");
	public static Command getStopCommand() {
		return STOP;
	}
	
	public static Command getLoadProfileCommand(String profile) {
		profile = profile.trim();
		
		if (profile.length() < 5) // a.xml
			throw new IllegalArgumentException("Invalid profile specified");
		
		return new Command("loadprofile", profile);
	}
	
	static final Command CAPTURE = new Command("capture");
	public static Command getCaptureCommand() {
		return CAPTURE;
	}
	
	public static Command getCaptureScaleCommand(int i) {
		if (!(10 <= i && i <= 100))
			throw new IllegalArgumentException("Scale must be between 10 and 100");
		
		return new Command("capturescale", Integer.toString(i));
	}
	
	public static Command getCaptureQualityCommand(int i) {
		if (!(10 <= i && i <= 100))
			throw new IllegalArgumentException("Quality must be between 10 and 100");
		
		return new Command("capturequality", Integer.toString(i));
	}
	
	static final Command SELECT_GAME = new Command("selectgame");
	public static Command getSelectGameCommand() {
		return SELECT_GAME;
	}
	
	static final Command GET_GAME_WS = new Command("getgamews");
	public static Command getGetGameWSCommand() {
		return GET_GAME_WS;
	}
	
	static final Command WS_NORMAL = new Command("setgamews", "normal");
	static final Command WS_SHRUNK = new Command("setgamews", "shrunk");
	static final Command WS_HIDDEN = new Command("setgamews", "hidden");
	public static Command getSetGameWSCommand(String state) {
		state = state.toLowerCase();
		
		if (state.equals("normal"))
			return WS_NORMAL;
		if (state.equals("shrunk") || state.equals("shrink"))
			return WS_SHRUNK;
		if (state.equals("hidden") || state.equals("hide"))
			return WS_HIDDEN;
		
		throw new IllegalArgumentException("State must be one of normal, shrunk, shrink, hidden, hide");
	}
	
	static final Command ESCAPE_HI_ON = new Command("escapehi", "on");
	static final Command ESCAPE_HI_OFF = new Command("escapehi", "off");
	public static Command getEscapeHiCommand(String state) {
		state = state.toLowerCase();
		
		if (state.equals("on"))
			return ESCAPE_HI_ON;
		if (state.equals("off"))
			return ESCAPE_HI_OFF;
		
		throw new IllegalArgumentException("State must be on or off");
	}
	
	static final Command INVENTORY = new Command("inventory");
	public static Command getInventoryCommand() {
		return INVENTORY;
	}
	
	public static Command getInventoryCommand(String slot) {
		if (null == slot || slot.isEmpty())
			return INVENTORY;
		return new Command("inventory", slot);
	}
}

///exit                         - shut down this connection
///exitglider                   - shut down Glider completely
///status                       - return current status of the game/char
///version                      - return Glider and game version info
///log [none|all|status|chatraw|
//      gliderlog|chat|combat]  - add logging of data on this channel
//
///nolog [all|status|chatraw|
//      gliderlog|chat|combat]  - remove logging of data on this channel
//
///say [message]                - queue chat for sending
///queuekeys [keys]             - queue string for injection, | = CR, #VK# = VK
///clearsay                     - clear queue of message, if pending
///forcekeys [keys]             - force keys in right now (dangerous!)
///holdkey [VK code]            - press and hold this VK code (dangerous!)
///releasekey [VK code]         - release this VK code (dangerous!)
///grabmouse [true/false]       - tell driver to grab/release mouse for bg ops
///setmouse [X/Y]               - position mouse, use 0 - .999 for coord
///getmouse                     - return current mouse position in percentages
///clickmouse [left|right]      - click mouse button
///attach                       - attach to the game
///startglide                   - start gliding
///stopglide                    - stop gliding
///loadprofile [filename]       - load a profile
///capture                      - capture screen and send as JPG stream
///capturecache [ms]            - set capture caching time in milliseconds
///capturescale [10-100]        - set capture scaling from 10% to 100%
///capturequality [10-100]      - set capture image quality from 10% to 100%
///queryconfig [name]           - retrieve a config value from Glider.config.xml
//                                (name is case-sensitive!)
///config                       - reload configuration
///selectgame                   - bring the game window to the foreground
///getgamews                    - get the game window state
///setgamews [normal|hidden|
//            shrunk]           - set the game window state
///escapehi [on/off]            - escape hi-bit (intl) characters with &&#...;
