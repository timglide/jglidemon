using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class Help : Command {
		public static readonly Command Instance = new Help();

		private static readonly string[] lines = new string[] {
			string.Format("Connected to {0}'s {1} v{2}", Class1.Instance.Author, Class1.Instance.Name, Class1.Instance.Version),
			//"Connected to timglide's GliderRemoteCompat",
			"* = not yet implemented",
			"† = partial implementation",
			"/exit                         - shut down this connection",
			"/exitglider*                  - shut down Glider completely",
			"/status                       - return current status of the game/char",
			"/version*                     - return Glider and game version info",
			"/log [none|all|status|chatraw|",
			"      gliderlog|chat|combat]  - add logging of data on this channel",
			"",
			"/nolog [all|status|chatraw|",
			"      gliderlog|chat|combat]  - remove logging of data on this channel",
			"",
			"/say [message]*               - queue chat for sending",
			"/queuekeys [keys]†            - queue string for injection, | = CR, #VK# = VK",
			"/clearsay*                    - clear queue of message, if pending",
			"/forcekeys [keys]*            - force keys in right now (dangerous!)",
			"/holdkey [VK code]*           - press and hold this VK code (dangerous!)",
			"/releasekey [VK code]*        - release this VK code (dangerous!)",
			"/grabmouse [true/false]*      - tell driver to grab/release mouse for bg ops",
			"/setmouse [X/Y]*              - position mouse, use 0 - .999 for coord",
			"/getmouse*                    - return current mouse position in percentages",
			"/clickmouse [left|right]*     - click mouse button",
			"/attach*                      - attach to the game",
			"/startglide                   - start gliding",
			"/stopglide                    - stop gliding",
			"/loadprofile [filename]*      - load a profile",
			"/capture                      - capture screen and send as JPG stream",
			"/capturecache [ms]*           - set capture caching time in milliseconds",
			"/capturescale [10-100]        - set capture scaling from 10% to 100%",
			"/capturequality [10-100]      - set capture image quality from 10% to 100%",
			"/queryconfig [name]*          - retrieve a config value from Glider.config.xml",
			"                                (name is case-sensitive!)",
			"/config*                      - reload configuration",
			"/selectgame*                  - bring the game window to the foreground",
			"/getgamews*                   - get the game window state",
			"/setgamews [normal|hidden|",
			"            shrunk]*          - set the game window state",
			"/escapehi [on/off]*           - escape hi-bit (intl) characters with &&#...;"
		};

		public override void Execute(Server server, Client client, string args) {
			client.Send(lines);
		}
	}
}
