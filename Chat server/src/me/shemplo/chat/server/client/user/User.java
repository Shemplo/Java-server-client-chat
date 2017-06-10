package me.shemplo.chat.server.client.user;

import me.shemplo.chat.server.client.user.UsersManager.AccessFields;
import me.shemplo.chat.server.exceptions.UserException;

public interface User {
	
	public static final String GUEST_ID = ".guest";
	
	public String getID ();
	
	public String getName ();
	
	public void changeName (String name, User user) throws UserException;
	
	public String getLastName ();
	
	public void changeLastName (String lastName, User user) throws UserException;
	
	public String getLogin ();
	
	public void changeLogin (String login, User user) throws UserException;
	
	public String getRights ();
	
	public void changeRights (AccessFields fields, char value, User user) throws UserException;
	
	public void changeRights (String rights, User user) throws UserException;
	
	default
	public boolean isAuthorized () { return !GUEST_ID.equals (getID ()); }
	
}
