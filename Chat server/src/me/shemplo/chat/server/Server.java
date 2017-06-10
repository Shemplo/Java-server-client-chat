package me.shemplo.chat.server;

import static java.lang.Integer.*;
import static java.lang.System.*;

import java.io.IOException;
import java.net.ServerSocket;

import me.shemplo.chat.server.client.ClientProducer;
import me.shemplo.chat.server.client.QueueClientProducer;
import me.shemplo.chat.server.client.listener.ClientsListener;
import me.shemplo.chat.server.client.listener.CycleClientsListener;
import me.shemplo.chat.server.exceptions.ServerException;
import me.shemplo.chat.server.properties.PropertiesLoader;

public class Server {
	
	/* ===| STATIC |=== */
	
	/*
	 * server.port              = 0
	 * server.threads.producers = 2
	 * server.threads.listeners = 2
	 * server.limits.clients    = 100
	 * server.limits.users      = 100
	 * server.updates.src       = http://yandex.ru
	 * server.updates.check     = false
	 * server.codes.startfailed = 1
	 * server.codes.stopfailed  = 2
	 * 
	 */
	
	static {
		PropertiesLoader.load ();
	}
	
	public static final int DEFAULT_PORT = parseInt (getProperty ("server.port"));
	public static final int START_FAILED_CODE = parseInt (getProperty ("server.codes.startfailed"));
	public static final int STOP_FAILED_CODE = parseInt (getProperty ("server.codes.stopfailed"));
	
	public static void main (String [] args) {
		int runPort = DEFAULT_PORT;
		runPort = 43;
		
		int pointer = 0;
		while (pointer < args.length) {
			pointer ++;
		}
		
		// Update checker must be here
		
		try {
			Server server = new Server ();
			
			int producers = parseInt (getProperty ("server.threads.producers"));
			int listeners = parseInt (getProperty ("server.threads.producers"));
			server.start (runPort, producers, listeners);
		} catch (ServerException e) {
			System.err.println ("[ERROR] Failed to start server: " + e.getMessage ());
			System.exit (START_FAILED_CODE);
		}
	}
	
	/* ===| CLASS |=== */
	
	private ServerSocket server;
	private boolean running = false;
	
	private ClientProducer producer;
	private ClientsListener listener;
	
	public void start (int port, int prodThreads, int listenThreads) throws  ServerException {
		if (server != null && running) { throw new ServerException ("Server is already started"); }
		
		try {
			server = new ServerSocket (port);
			server.setSoTimeout (1000);
		} catch (IOException ioe) {
			String message = ioe.getMessage () != null 
								? ioe.getMessage () 
								: "Unknown I/O reason";
			throw new ServerException (message);
		} catch (SecurityException se) {
			String message = se.getMessage () != null 
								? se.getMessage () 
								: "Unknown security reason";
			throw new ServerException (message);
		} catch (IllegalArgumentException iae) {
			String message = "Port value must be between 0 and 65535 (" + port + " given)";
			throw new ServerException (message);
		}
		
		// TODO: wrap with try/catch when API will be finished
		listener = new CycleClientsListener (this, listenThreads);
		producer = new QueueClientProducer (server, listener, prodThreads);
		System.out.println ("[LOG] Server started on port " + server.getLocalPort ());
	}
	
	public void stop () {
		producer.stop ();
		
		try {
			server.close ();
		} catch (IOException ioe) {
			System.err.println ("[ERROR] Failed to stop server correctly: " + ioe.getMessage ());
			
			listener.stop ();
			System.exit (STOP_FAILED_CODE);
		}
		
		listener.stop ();
	}
	
}
