package jgm.glider;

public class ConnectionAdapter implements ConnectionListener {
	public GliderConn getConn() {
		return null;
	}
	
	public void connecting() {}
	public void connectionEstablished() {}
	public void disconnecting() {}
	public void connectionDied() {}
}
