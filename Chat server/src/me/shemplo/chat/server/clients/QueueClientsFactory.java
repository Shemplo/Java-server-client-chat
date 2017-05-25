package me.shemplo.chat.server.clients;

import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import me.shemplo.chat.server.ifs.ClientsFactory;
import me.shemplo.chat.server.ifs.ClientsPool;

public class QueueClientsFactory implements ClientsFactory {
	
	private final Queue <Socket> queue;
	private final Thread [] threads;
	
	public QueueClientsFactory (ClientsPool pool, int threads) {
		queue = new LinkedList <> ();
		
		this.threads = new Thread [threads];
		Arrays.asList (this.threads).stream ().map (t -> new Thread (() -> {
			while (true) {
				synchronized (queue) {
					while (queue.isEmpty ()) {
						try {
							queue.wait ();
						} catch (InterruptedException ie) {
							return; // Stopping thread
						}
					}
					
					//System.out.println ("[LOG] New connection");
					pool.add (new ChatClient (queue.poll ()));
				}
			}
		})).forEach (Thread::start); 
	}
	
	public void register (Socket socket) {
		synchronized (queue) {
			queue.add (socket);
			queue.notify ();
		}
	}
	
	public void stop () {
		for (int i = 0; i < threads.length; i ++) {
			threads [i].interrupt ();
		}
		
		synchronized (queue) {
			for (int i = 0; i < threads.length; i ++) {
				queue.add (null);
				queue.notify ();
			}
		}
	}
	
}
