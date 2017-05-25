package me.shemplo.chat.server.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.IllegalCharsetNameException;

import me.shemplo.chat.server.ifs.Client;
import me.shemplo.chat.server.ifs.ClientsPool;

public class ChatClient implements Client {
	
	private ClientsPool pool;
	private Socket socket;
	private int ID;
	
	private BufferedReader br;
	private PrintWriter pw;
	
	public ChatClient (Socket socket) {
		this.socket = socket;
		boolean flag = true;
		
		String encoding = "UTF-8";
		
		try {			
			br = new BufferedReader (
					new InputStreamReader (
						socket.getInputStream (), encoding));
		} catch (IOException | IllegalCharsetNameException ioe) {
			System.out.println ("[ERROR] Failed to get input stream "
									+ "from client: " + ioe.getMessage ());
			flag = false;
		}
		
		try {
			pw = new PrintWriter (
					new OutputStreamWriter (
						socket.getOutputStream (), encoding));
			if (!flag) {
				pw.println ("[WARNING] Failed to get input stream! Listening mode only");
				pw.flush ();
			}
		} catch (IOException ioe) {
			System.out.println ("[ERROR] Failed to get output stream "
									+ "from client: " + ioe.getMessage ());
			if (!flag) { kill (); }
		}
	}
	
	public int getID () {
		return ID;
	}
	
	public void setID (int id) {
		this.ID = id;
	}
	
	public void setPool (ClientsPool pool) {
		this.pool = pool;
	}
	
	private int failCounter = 0;
	
	private void checkFails () {
		if ((failCounter ++) >= 10) {
			kill ();
		}
	}
	
	public boolean hasInputData () {
		if (br == null) {
			return false;
		}
		
		try {
			failCounter = 0;
			return br.ready ();
		} catch (IOException ioe) { checkFails (); }
		
		return false;
	}
	
	public synchronized String read () {
		if (!hasInputData ()) {
			return null;
		}
		
		try {
			failCounter = 0;
			return br.readLine ();
		} catch (IOException e) { checkFails (); }
		
		return null;
	}
	
	public synchronized void send (String message) {
		if (pw == null) {
			return;
		}
		
		pw.println (message);
		pw.flush ();
	}
	
	public void kill () {
		int counter = 0;
		while (!socket.isClosed () && (counter ++) < 10) {
			try {
				socket.close ();
			} catch (IOException ioe) {}
		}
		socket = null;
		
		if (pool != null && pool.exists (ID)) {
			pool.remove (ID);
		}
	}
	
}
