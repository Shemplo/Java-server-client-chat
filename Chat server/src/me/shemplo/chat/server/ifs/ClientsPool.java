package me.shemplo.chat.server.ifs;

public interface ClientsPool {
	
	public void add (Client client);
	
	public boolean exists (int id);
	
	public void remove (int id);
	
	public void removeAll ();
	
	public void sendAll (String message);
	
}
