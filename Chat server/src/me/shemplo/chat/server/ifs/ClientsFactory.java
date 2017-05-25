package me.shemplo.chat.server.ifs;

import java.net.Socket;

public interface ClientsFactory {
	
	public void register (Socket socket);
	
	//public void changePool (ClientsPool pool);
	
	public void stop ();
	
}
