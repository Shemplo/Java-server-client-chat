package me.shemplo.chat.server.exceptions;

public class ServerException extends Exception {
	
	private static final long serialVersionUID = 6745017724757369253L;

	public ServerException (String reason) {
		super (reason);
	}
	
	public ServerException (String reason, int port) {
		super (reason + " (port: " + port + ")");
	}
	
}
