package me.shemplo.chat.server.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import me.shemplo.chat.server.client.listener.ClientsListener;
import me.shemplo.chat.server.client.user.DefaultUser;
import me.shemplo.chat.server.client.user.User;

public class QueueClientProducer implements ClientProducer {

	private ClientsListener listener;
	private ServerSocket server;
	
	private boolean running = false;
	private Thread [] pool;
	
	/**
	 * 
	 * 
	 * @param server server socket object
	 * @param listener clients' listener object
	 * @param threads number of threads in producer
	 * 
	 * @throws NullPointerException in case of null given as argument
	 * @throws IllegalArgumentException in case of wrong threads number
	 * 
	 */
	public QueueClientProducer (ServerSocket server, ClientsListener listener, int threads) {
		if (server == null) { throw new NullPointerException ("Null given as ServerSocket argument"); }
		if (listener == null) { throw new NullPointerException ("Null given as ClientsListener argument"); }
		this.server = server; this.listener = listener;
		
		if (threads <= 0) { throw new IllegalArgumentException ("The number of threads must be positive"); }
		this.pool = new Thread [threads];
		this.running = true;
		
		for (int i = 0; i < pool.length; i ++) {
			pool [i] = new Thread (() -> {
						while (running) {
							Socket socket;
							
							try {
								socket = this.server.accept ();
							} catch (SocketTimeoutException ste) {
								continue; // Just handle exception
							} catch (IOException ioe) {
								System.err.println ("[ERROR] Failed to accept socket: " + ioe.getMessage ());
								continue;
							} catch (SecurityException se) {
								System.err.println ("[ERROR] Unsecured socket: " + se.getMessage ());
								continue;
							}
							
							try {
								Client client = makeClient (socket);
								if (client != null) { this.listener.bind (client); }
							} catch (NullPointerException npe) {
								System.err.println ("[ERROR] Failed to make client: " + npe.getMessage ());
								npe.printStackTrace ();
								continue;
							} catch (Exception e) {
								// Change this
								e.printStackTrace ();
							}
						}
					});
			pool [i].start ();
		}
	}

	@Override
	public void stop () {
		try {
			this.running = false;
			for (Thread thread : pool) {
				thread.join ();
			}
		} catch (InterruptedException ie) {
			// Just handle exception
		} finally {
			for (int i = 0; i < pool.length; i ++) {
				pool [i] = null;
			}
		}
	}

	@Override
	public Client makeClient (Socket socket) throws Exception {
		if (socket == null) { throw new NullPointerException ("Null given as Socket argument"); }
		// TODO: handshake to set up encoding and other initial things
		InputStreamReader isr = new InputStreamReader (socket.getInputStream ());
		OutputStreamWriter osr = new OutputStreamWriter (socket.getOutputStream ());
		
		User user = new DefaultUser (); // Authorization on spot in future
		return new ConsoleClient (user, isr, osr, socket);
	}
	
}
