package me.shemplo.chat.server.client;

import static me.shemplo.chat.server.parcel.MessageProducer.makeMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import me.shemplo.chat.server.client.user.User;
import me.shemplo.chat.server.parcel.Message;
import me.shemplo.chat.server.parcel.TextMessage;

public class ConsoleClient implements Client {
	
	private BufferedReader br;
	private PrintWriter pw;
	private User user;
	
	private boolean killed = false;
	
	public ConsoleClient (User user, Reader reader, Writer writer) {
		if (reader != null) {
			br = new BufferedReader (reader);
		}
		
		if (writer != null) {
			pw = new PrintWriter (writer);
			
			pw.println ("Hello");
			pw.flush ();
		}
		
		this.user = user;
	}
	
	@Override
	public User getUser () { return user; }
	
	@Override
	public boolean hasInputData () {
		if (br == null || killed) { return false; }
		
		try {
			synchronized (br) { return br.ready (); }
		} catch (IOException ioe) { ioe.printStackTrace (); }
		
		return false;
	}
	
	@Override
	public Message read () {
		if (!hasInputData ()) { return null; }
		
		try {
			String message;
			synchronized (br) { message = br.readLine (); }
			long timestamp = System.currentTimeMillis ();
			return makeMessage (message, timestamp, this);
		} catch (IOException ioe) {}
		
		return null;
	}

	@Override
	public void send (Message message) {
		if (pw == null || killed) { return; }
		
		// !!! Possibly it's necessary !!!
		/*String destination = message.getDestination ();
		if (!destination.equals (ClientsListener.DEFAULT_CHANNEL)
				&& !channels.contains (destination)) { return; }*/
		
		if (message instanceof TextMessage) {
			synchronized (pw) {
				pw.println (message.getContent ());
				pw.flush ();
			}
		}
	}
	
	@Override
	public void kill () {
		try {
			this.killed = true;
			if (br != null) { br.close (); }
			if (pw != null) { pw.close (); }
		} catch (IOException ioe) {
			// Just handle exception
		} finally {
			br = null;
			pw = null;
		}
	}
	
}
