package jgm.glider;

public interface ConnectionListener {
	public Conn getConn();
	public void connecting();
	public void connectionEstablished();
	public void disconnecting();
	public void connectionDied();
}
