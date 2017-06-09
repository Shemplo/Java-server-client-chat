package me.shemplo.chat.server.client.user;

import me.shemplo.chat.server.exceptions.UserException;

public interface User {
	
	public static final String GUEST_ID = ".guest";
	
	public enum Fields {
		
		LOGIN ("login", 'l', 0),
		NAME ("name", 'n', 1),
		LAST_NAME ("last name", 'f', 2),
		RIGHTS ("rights", 'r', 3);
		
		private final String what;
		private final char key;
		private final int pos;
		
		private Fields (String what, char need, int pos) {
			this.what = what; this.key = need;
			this.pos = pos;
		}
		
		public String filed () { return what; }
		
		public char key () { return key; }
		
		public int index () { return pos; }
		
	}
	
	public String getID ();
	
	public String getName ();
	
	public void changeName (String name, User user) throws UserException;
	
	public String getLastName ();
	
	public void changeLastName (String lastName, User user) throws UserException;
	
	public String getLogin ();
	
	public void changeLogin (String login, User user) throws UserException;
	
	public String getRights ();
	
	public String changeRights (Fields fields, char value, User user) throws UserException;
	
	default
	public boolean isAuthorized () { return !GUEST_ID.equals (getID ()); }
	
	default
	public boolean checkRights (Fields field, User user) {
		if (user == null) {
			return false; // No rights for null user
		}
		
		if (this.equals (user)
				&& field != Fields.RIGHTS) {
			return true; // Just because it's the same user
		}
		
		if (user.getRights ().length () <= field.index ()) {
			return false; // Necessary property doesn't exits
		}
		
		return user.getRights ().charAt (field.index ()) == field.key ();
	}
	
}
