package me.shemplo.chat.server.exceptions;

public class NoAvailableIDs extends Exception {

	private static final long serialVersionUID = 1166594591588054563L;
	
	private String message;
	
	public NoAvailableIDs () {
		this.message = "";
	}
	
	public NoAvailableIDs (String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage () {
		return "No available IDs are in pool" + message != null && message.length () > 0
													? ": " + message
													: "";
	}
	
}
