package me.shemplo.chat.server.clients;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.shemplo.chat.server.exceptions.NoAvailableIDs;
import me.shemplo.chat.server.ifs.Client;
import me.shemplo.chat.server.ifs.ClientsPool;

public class MapClientsPool implements ClientsPool {

	private final Map <Integer, Client> clients;
	private final int POOL_SIZE;
	
	public MapClientsPool () {
		POOL_SIZE = Integer.MAX_VALUE / 2;
		clients = new HashMap <> ();
		
		Thread t = new Thread (() -> {
			while (true) {
				Iterator <Client> cls = clients.values ().iterator ();
				while (cls.hasNext ()) {
					Client cl = cls.next ();
					while (cl != null && cl.hasInputData ()) {
						String read = cl.read ();
						System.out.println (read);
						cl.send (read);
					}
				}
				
				try {
					Thread.sleep (10);
				} catch (Exception e) { return; }
			}
		});
		
		t.start ();
	}
	
	public void add (Client client) {
		synchronized (clients) {
			try {
				int ID = nextID ();
				clients.put (ID, client);
				client.setPool (this);
				client.setID (ID);
				
				client.send ("Welcome to chat");
			} catch (NoAvailableIDs naie) {
				System.err.println ("[ERROR] Failed to add client: " + naie.getMessage ());
				client.kill ();
			}
		}
	}
	
	private int index = 0;
	
	private int nextID () throws NoAvailableIDs {
		int counter = 0;
		
		while (exists (index)) {
			if ((counter ++) >= POOL_SIZE) {
				throw new NoAvailableIDs ();
			}
			
			index = (index + 1) % POOL_SIZE;
		}
		
		return index;
	}
	
	public boolean exists (int id) {
		synchronized (clients) {
			return clients.containsKey (id);
		}
	}
	
	public void remove (int id) {
		
	}
	
	public void removeAll () {
		
	}
	
	public void sendAll (String message) {
		
	}
	
}
