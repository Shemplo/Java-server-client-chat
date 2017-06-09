package me.shemplo.chat.server.client;

import me.shemplo.chat.server.client.user.User;
import me.shemplo.chat.server.parcel.Message;

public interface Client extends Comparable <Client> {
	
	public User getUser ();
	
	public boolean hasInputData ();
	
	public Message read ();
	
	public void send (Message message);
	
	public void kill ();
	
	@Override
	default
	public int compareTo (Client o) {
		return this.hashCode () - o.hashCode ();
	}
	
}
