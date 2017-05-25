package me.shemplo.chat.server.exceptions;

public class NoAvailableIDs extends Exception {

	private static final long serialVersionUID = 1166594591588054563L;
	
	@Override
	public String getMessage () { return "No available IDs are in pool"; }
	
}
