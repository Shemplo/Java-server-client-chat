package me.shemplo.chat.server.client;

import java.net.Socket;

public interface ClientProducer {
	
	public void stop ();
	
	public Client makeClient (Socket socket) throws Exception;
	
}
