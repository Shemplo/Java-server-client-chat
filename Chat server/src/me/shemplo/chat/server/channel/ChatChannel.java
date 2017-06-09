package me.shemplo.chat.server.channel;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import me.shemplo.chat.server.client.Client;
import me.shemplo.chat.server.client.user.User;
import me.shemplo.chat.server.exceptions.ChannelException;
import me.shemplo.chat.server.parcel.Message;

public class ChatChannel {
	
	private String name, password;
	private User creator;
	
	private ConcurrentSkipListSet <Client> followers;
	
	public ChatChannel (String name, boolean open, String password, Client creator) {
		followers = new ConcurrentSkipListSet <> ();
		this.name = name;
		
		if (creator != null) {
			this.creator = creator.getUser ();
			followers.add (creator);
		}
		
		if (!open) { this.password = password; }
	}
	
	public String getName () { return name; }
	
	public User getCreator () { return creator; }
	
	public Set <Client> getFollowers () { return followers.clone (); }
	
	public boolean checkFollower (Client client) { return followers.contains (client); }
	
	public boolean join (Client client, String password) throws ChannelException {
		if (this.password != null && !this.password.equals (password)) {
			throw new ChannelException ("Not enough rights to join channel");
		}
		
		return followers.add (client);
	}
	
	public void leave (Client client) {
		if (followers.contains (client)) {
			followers.remove (client);
		}
	}
	
	public void sendMessage (Message message) {
		if (!message.getDestination ().equals (name)) { return; }
		for (Client client : followers) { client.send (message); }
	}
	
}
