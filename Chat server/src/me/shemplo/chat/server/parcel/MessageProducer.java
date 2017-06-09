package me.shemplo.chat.server.parcel;

import static me.shemplo.chat.server.client.listener.ClientsListener.CHANNEL_SIGN;
import static me.shemplo.chat.server.client.listener.ClientsListener.DEFAULT_CHANNEL;

import me.shemplo.chat.server.client.Client;

public class MessageProducer {
	
	public static Message makeMessage (Object data, long timestamp, Client client) {
		if (data instanceof String) {
			String message = (String) data;
			String channel = fetchChannel (message);
			
			int offset = channel.length ();
			offset += message.indexOf (CHANNEL_SIGN) != -1 
						? CHANNEL_SIGN.length ()
						: 0;
			offset += message.indexOf (';') != -1
						? 1
						: 0;
			channel = channel.length () == 0 
						? DEFAULT_CHANNEL
						: channel;
			
			String text = message.substring (offset);
			return new TextMessage (text, timestamp, client, channel);
		}
		
		return null;
	}
	
	public static String fetchChannel (String data) {
		int index = data.indexOf (";");
		if (index > 0) {
			String left = data.substring (0, index);
			int signIndex = left.indexOf (CHANNEL_SIGN);
			if (signIndex == 0 && left.length () > 0) {
				return left.substring (signIndex);
			}
		}
		
		return "";
	}
	
}
