package me.shemplo.chat.server.client.listener;

import me.shemplo.chat.server.client.Client;

public interface ClientsListener {

	public static final String CHANNEL_SIGN = "@";
	public static final String DEFAULT_CHANNEL = "";
	
	public void bind (Client client);
	
	public void unbind (Client client);
	
	public void unbind (String id);
	
	public void stop ();
	
}
