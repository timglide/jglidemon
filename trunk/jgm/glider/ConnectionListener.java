package jgm.glider;

public interface ConnectionListener {
	public GliderConn getConn();
	public void connectionEstablished();
	//public void connectionDied();
}
