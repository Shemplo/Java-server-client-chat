package me.shemplo.chat.server.ifs;

public interface Client {

	public int getID ();
	
	public void setID (int id);
	
	public void setPool (ClientsPool pool);
	
	public boolean hasInputData ();
	
	public String read ();
	
	public void send (String message);
	
	public void kill ();
	
}
