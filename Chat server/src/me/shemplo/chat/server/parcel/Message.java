package me.shemplo.chat.server.parcel;

import java.io.Serializable;

import me.shemplo.chat.server.client.Client;

public interface Message extends Serializable {
	
	public Object getContent ();
	
	public Client getClient ();
	
	public String getDestination ();
	
	public String getTime ();
	
	public long getTimestamp ();
	
	public Class <?> getType ();
	
}
