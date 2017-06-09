package me.shemplo.chat.server.parcel;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.shemplo.chat.server.client.Client;

public class TextMessage implements Message {
	
	private static final long serialVersionUID = 6393530456307441492L;

	private SimpleDateFormat formatter;
	
	private String text, destination;
	private long timestamp;
	private Client client;
	
	public TextMessage (String text, long timestamp, Client client, String destination) {
		this.text = text; this.client = client; this.destination = destination;
		this.timestamp = timestamp;
		
		// TODO: getting locale and time zone from user
		formatter = new SimpleDateFormat ("HH:mm:ss");
	}

	@Override
	public String getContent () {
		return text;
	}

	public int getContentSize () {
		return text.length ();
	}

	@Override
	public Client getClient () {
		return client;
	}

	@Override
	public String getDestination () {
		return destination;
	}

	@Override
	public String getTime () {
		return formatter.format (new Date (getTimestamp ()));
	}

	@Override
	public long getTimestamp () {
		return timestamp;
	}

	@Override
	public Class <?> getType () {
		return String.class;
	}
	
}
