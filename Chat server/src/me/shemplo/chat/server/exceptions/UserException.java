package me.shemplo.chat.server.exceptions;

public class UserException extends Exception {

	private static final long serialVersionUID = -7323286411347359324L;
	
	public UserException (String reason) {
		super (reason);
	}
	
}
