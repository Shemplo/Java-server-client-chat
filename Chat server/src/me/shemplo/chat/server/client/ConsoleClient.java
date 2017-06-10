package me.shemplo.chat.server.client;

import static me.shemplo.chat.server.parcel.MessageProducer.makeMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.shemplo.chat.server.client.user.User;
import me.shemplo.chat.server.parcel.Message;
import me.shemplo.chat.server.parcel.TextMessage;

public class ConsoleClient implements Client {
	
	private BufferedReader br;
	private PrintWriter pw;
	private User user;
	
	private ConcurrentLinkedQueue <String> strings;
	private StringBuilder builder;
	private char [] buffer;
	
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
		
		this.strings = new ConcurrentLinkedQueue <> ();
		this.builder = new StringBuilder ();
		this.buffer = new char [1 << 10];
		this.user = user;
	}
	
	@Override
	public User getUser () { return user; }
	
	@Override
	public boolean hasInputData () {
		if (br == null || killed) { return false; }
		if (!strings.isEmpty ()) { return true; }
		
		try {
			synchronized (br) {
				synchronized (builder) {
					if (!br.ready ()) { return !strings.isEmpty (); }
					
					int read = br.read (buffer);
					
					boolean wasSeparator = false;
					for (int i = 0; i < read; i ++) {
						char c = buffer [i];
						
						boolean isSeparator = c == '\n' || c == '\r';
						if (isSeparator && !wasSeparator) {
							strings.add (builder.toString ());
							builder = new StringBuilder ();
							wasSeparator = true;
							continue;
						} else if (isSeparator) {
							continue;
						}
						
						builder.append (c);
						wasSeparator = false;
					}
				}
			}
		} catch (IOException ioe) { ioe.printStackTrace (); }
		
		return false;
	}
	
	@Override
	public Message read () {
		if (!hasInputData ()) { return null; }
		
		String message = strings.poll ();
		long timestamp = System.currentTimeMillis ();
		System.out.println ("Message: " + message);
		return makeMessage (message, timestamp, this);
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
