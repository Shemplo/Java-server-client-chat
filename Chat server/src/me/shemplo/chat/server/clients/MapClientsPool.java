package me.shemplo.chat.server.clients;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.util.Pair;
import me.shemplo.chat.server.exceptions.NoAvailableIDs;
import me.shemplo.chat.server.ifs.Client;
import me.shemplo.chat.server.ifs.ClientsPool;

public class MapClientsPool implements ClientsPool {

	private final ConcurrentLinkedQueue <Pair <String, Integer>> messages;
	private final Map <Integer, Client> clients;
	private final Thread [] senders;
	private final Thread scanner;
	private final int POOL_SIZE;
	
	public MapClientsPool (int threads) {
		POOL_SIZE = Integer.MAX_VALUE / 2;
		clients = new HashMap <> ();
		
		this.messages = new ConcurrentLinkedQueue <> ();
		this.senders = new Thread [threads];
		
		Arrays.asList (this.senders)
				.stream ()
				.map (t -> new Thread (() -> {
					while (true) {
						synchronized (messages) {
							while (messages.isEmpty ()) {
								try {
									messages.wait ();
								} catch (Exception e) { return; }
							}
							
							Pair <String, Integer> message = messages.poll ();
							sendAll (message.getKey ());
						}
					}
				})).forEach (Thread::start);
		
		
		this.scanner = new Thread (() -> {
			while (true) {
				try {
					Thread.sleep (10);
				} catch (InterruptedException ie) { return; }
				
				Iterator <Client> iterator = clients.values ().iterator ();
				while (iterator.hasNext ()) {
					Client client = iterator.next ();
					while (client != null && client.hasInputData ()) {
						String message = client.read ();
						if (message != null) {
							synchronized (messages) {
								messages.add (new Pair <String, Integer> (message, client.getID ()));
								messages.notify ();
							}
						}
					}
				}
			}
		}, "Ready scanner");
		scanner.start ();
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
		Iterator <Client> iterator = clients.values ().iterator ();
		while (iterator.hasNext ()) {
			Client client = iterator.next ();
			synchronized (client) {
				if (client != null) { client.send ("Message: " + message); }
			}
		}
	}
	
}
