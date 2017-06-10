package me.shemplo.chat.server.client.listener;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import me.shemplo.chat.server.Server;
import me.shemplo.chat.server.channel.ChatChannel;
import me.shemplo.chat.server.client.Client;
import me.shemplo.chat.server.client.user.RootUser;
import me.shemplo.chat.server.client.user.User;
import me.shemplo.chat.server.client.user.UsersManager;
import me.shemplo.chat.server.client.user.UsersManager.AuthorizedUserFields;
import me.shemplo.chat.server.exceptions.ChannelException;
import me.shemplo.chat.server.exceptions.UserException;
import me.shemplo.chat.server.parcel.Message;
import me.shemplo.chat.server.parcel.TextMessage;

public class CycleClientsListener implements ClientsListener {
	
	private ConcurrentLinkedQueue <Message> writeQueue;
	private ConcurrentLinkedQueue <Client> readQueue;
	private ConcurrentSkipListSet <Client> inQueue;
	
	private ConcurrentMap <String, ConcurrentSkipListSet <Client>> users;
	private ConcurrentMap <String, ChatChannel> channels;
	private ConcurrentMap <User, Client> clients;
	
	private Server server;
	private User root;
	
	private AtomicLong sleep; // Time to sleep for threads
	private boolean running = false;
	private Thread [] pool;
	private Thread scanner;
	
	public CycleClientsListener (Server server, int threads) {
		if (server == null) { throw new NullPointerException ("Null given as ServerSocket argument"); }
		this.server = server;
		this._init ();
		
		try {
			this.root = RootUser.getInstance ();
		} catch (UserException ue) {
			System.err.println ("[ERROR] Failed to create root user: " + ue.getMessage ());
			System.out.println ("[WARNING] Root operations with server disabled"
									+ " (to fix this try to restart server)");
		}
		
		if (threads <= 0) { throw new IllegalArgumentException ("The number of threads must be positive"); }
		this.pool = new Thread [threads];
		this.running = true;
		
		for (int i = 0; i < pool.length; i ++) {
			pool [i] = new Thread (() -> {
						while (running) {
							long delay = sleep.get ();
							long tasks = delay * readQueue.size () + delay * writeQueue.size ();
							if (tasks >= 200) { sleep.updateAndGet (v -> Math.max (0, v - 1)); } 
							else if (tasks <= 10) { sleep.updateAndGet (v -> Math.min (500, v + 1)); }
							// DEBUG: System.out.println ("[LOG] Current delay: " + sleep.get ());
							
							try {
								Thread.sleep (sleep.get ());
							} catch (InterruptedException ie) {
								// Just handle exception
							}
							
							Client client = readQueue.poll ();
							
							if (client != null) {
								while (client.hasInputData ()) {
									Message message = client.read ();
									if (message != null && !_executeCommand (message)) {
										writeQueue.add (message);
									}
								}
								
								inQueue.remove (client);
							}
							
							Message message = writeQueue.poll ();
							if (message != null) {
								String destination = message.getDestination ();
								if (destination.equals (ClientsListener.DEFAULT_CHANNEL)) { _sendToAll (message); }
								else {
									if (clients.keySet ().contains (destination)) {
										clients.get (destination).send (message);
									} // else ... check for the group of several clients
								}
							}
						}
					});
			pool [i].start ();
		}
		
		scanner = new Thread (() -> {
			while (running) {
				try {
					Thread.sleep (10);
				} catch (InterruptedException ie) {
					continue; // Just handle exception
				}
				
				for (Client client : clients.values ()) {
					if (client != null 
							&& client.hasInputData () 
							&& !inQueue.contains (client)) {
						readQueue.add (client);
						inQueue.add (client);
					}
				}
			}
		}, "Listener thread");
		scanner.start ();
	}
	
	private void _init () {
		writeQueue = new ConcurrentLinkedQueue <> ();
		readQueue = new ConcurrentLinkedQueue <> ();
		inQueue = new ConcurrentSkipListSet <> ();
		channels = new ConcurrentHashMap <> ();
		clients = new ConcurrentHashMap <> ();
		users = new ConcurrentHashMap <> ();
		
		channels.putIfAbsent (DEFAULT_CHANNEL, new ChatChannel (DEFAULT_CHANNEL, true, null, null));
		sleep = new AtomicLong (10L);
	}
	
	private boolean _executeCommand (Message message) {
		if (message instanceof TextMessage) {
			String content = (String) message.getContent ();
			if (content.indexOf ("/auth") == 0) {
				StringTokenizer st = new StringTokenizer (content);
				st.nextToken (); // This is "/auth" string
				String login = "", password = "";
				
				try {
					login = st.nextToken ();
					password = st.nextToken ();
				} catch (NoSuchElementException nsee) {
					// TODO: Write to client that not enough arguments
					return true;
				}
				
				User real = message.getClient ().getUser ();
				Map <AuthorizedUserFields, String> user;
				
				try {
					user = UsersManager.authorize (login, password);
				} catch (UserException ue) {
					// TODO: Write to client that authorization failed
					return true;
				}
				
				try {
					real.changeName (user.get (AuthorizedUserFields.NAME), root);
					real.changeLogin (user.get (AuthorizedUserFields.LOGIN), root);
					real.changeRights (user.get (AuthorizedUserFields.RIGHTS), root);
					real.changeLastName (user.get (AuthorizedUserFields.LAST_NAME), root);
				} catch (UserException ue) {
					// Just handle exception
					// This issue is impossible due to root access
				}
				
				return true;
			}
			
			if (content.equals ("/exit")) {
				unbind (message.getClient ());
				return true;
			}
			
			if (content.equals ("/stop")) {
				System.out.println ("[LOG] Stopping server...");
				server.stop ();
				return true;
			}
		}
		
		return false;
	}
	
	private void _sendToAll (Message message) {
		channels.get (DEFAULT_CHANNEL).sendMessage (message);
	}

	@Override
	public void bind (Client client) {
		clients.putIfAbsent (client.getUser (), client);
		users.putIfAbsent (client.getUser ().getID (), new ConcurrentSkipListSet <> ());
		users.get (client.getUser ().getID ()).add (client);
		
		try {
			channels.get (DEFAULT_CHANNEL).join (client, null);
		} catch (ChannelException ce) {
			System.out.println (ce.getMessage ());
			// TODO: send a message "failed to join to public channel"
			unbind (client);
		}
	}
	
	@Override
	public void unbind (Client client) {
		client.kill ();
		
		for (ChatChannel channel : channels.values ()) {
			if (channel.checkFollower (client)) {
				channel.leave (client);
			}
		}
		
		users.get (client.getUser ().getID ()).remove (client);
		clients.remove (client.getUser ());
	}
	
	@Override
	public void unbind (String id) {
		
	}

	@Override
	public void stop () {
		this.running = false;
		scanner.interrupt ();
		
		/*for (Thread thread : pool) {
			thread.interrupt ();
		}*/
		
		for (int i = 0; i < pool.length; i ++) {
			try {
				pool [i].interrupt ();
				pool [i].join ();
			} catch (InterruptedException ie) {
				// Just handle exception
			} finally {
				pool [i] = null;
			}
		}
		
		for (Client client : clients.values ()) { unbind (client); }
	}
	
}
