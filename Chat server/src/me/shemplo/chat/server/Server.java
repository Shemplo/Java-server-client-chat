package me.shemplo.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import me.shemplo.chat.server.clients.MapClientsPool;
import me.shemplo.chat.server.clients.QueueClientsFactory;
import me.shemplo.chat.server.ifs.SocketAcceptor;
import me.shemplo.chat.server.ifs.ClientsFactory;
import me.shemplo.chat.server.ifs.ClientsPool;

public class Server {
	
	private static final int DEFAULT_PORT = 0;
	private static final int START_FAILED = 1;
	
	/* ===| STATIC |=== */
	
	public static void main (String... args) {
		int customPort = DEFAULT_PORT;
		
		int argsIndex = 0;
		while (argsIndex < args.length) {
			String flag = args [argsIndex];
			if (flag.indexOf ("-") == -1) {
				System.err.println ("[ERROR] Flag `-[name]` expected but `" + flag + "` given");
				System.exit (1);
			}
			
			argsIndex ++;
			switch (flag) {
				case "-p":
					if (argsIndex >= args.length) {
						System.err.println ("[ERROR] Port number expected but arguments ended");
						System.exit (START_FAILED);
					}
					
					try {
						customPort = Integer.parseInt (args [argsIndex ++]);
						System.out.println ("[LOG] Default port changed on " + customPort);
					} catch (NumberFormatException nfe) {
						System.err.println ("[ERROR] Port number expected but `" 
												+ args [argsIndex - 1] + "` given");
						System.exit (START_FAILED);
					}
					
					break;
			}
		}
		
		// Here must be check for updates
		
		Server server = new Server ();
		server.start (customPort);
	}
	
	/* ===| CLASS |=== */
	
	private boolean isRunning;
	private int PORT;
	
	private ServerSocket server;
	private SocketAcceptor acceptor;
	private ClientsFactory factory;
	
	private Thread acceptorThread;
	
	public Server () {
		
	}
	
	public void start (int port) {
		try {
			server = new ServerSocket (port);
		} catch (IOException ioe) {
			System.err.println ("[ERROR] Failed to start server: " + ioe.getMessage ());
			System.exit (START_FAILED);
		} catch (IllegalArgumentException iae) {
			System.err.println ("[ERROR] Port number must be between 0 and 65535");
			System.exit (START_FAILED);
		} catch (Exception e) {
			System.err.println ("[ERROR] Message: " + e.getMessage ());
			System.exit (START_FAILED);
		}
		
		this._init ();
		
		isRunning = true;
		acceptorThread = new Thread (acceptor, "Client acceptor");
		acceptorThread.start ();
		
		System.out.println ("[LOG] Server started on port " + PORT);
	}
	
	private void _init () {
		PORT = server.getLocalPort ();
		acceptor = new ClientAcceptor ();
		
		ClientsPool pool = new MapClientsPool (2);
		factory = new QueueClientsFactory (pool, 1);
	}
	
	public void stop () {
		
	}
	
	/* ===| ACCEPTOR |=== */
	
	private class ClientAcceptor implements SocketAcceptor {
		
		public void run () {
			while (isRunning) {
				try {
					Socket socket = server.accept ();
					factory.register (socket);
					// Any other actions with sc	
				} catch (SocketTimeoutException ste) {
					// Ignoring it
				} catch (SecurityException | IOException e) {
					System.err.println ("[ERROR] Fatal error: " + e.getMessage ());
					stop ();
				}
			}
		}
		
	}
	
}
