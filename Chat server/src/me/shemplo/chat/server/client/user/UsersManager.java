package me.shemplo.chat.server.client.user;

import java.util.Map;

import me.shemplo.chat.server.exceptions.UserException;

public class UsersManager {
	
	public static enum AuthorizedUserFields {
		ID, LOGIN, NAME, LAST_NAME, RIGHTS
	}
	
	public static Map <AuthorizedUserFields, String> authorize (String id, String password) throws UserException {
		return null;
	}
	
	public static boolean register (String login, String password) {
		return false;
	}
	
	public static void delete (String id) {
		
	}
	
	/* ===| USERS ACCESS (SECURITY) |=== */
	
	public enum AccessFields {
		
		LOGIN ("login", 'l', 0),
		NAME ("name", 'n', 1),
		LAST_NAME ("last name", 'f', 2),
		RIGHTS ("rights", 'r', 3),
		SERVER ("server", 's', 4);
		
		private final String what;
		private final char key;
		private final int pos;
		
		private AccessFields (String what, char need, int pos) {
			this.what = what; this.key = need;
			this.pos = pos;
		}
		
		public String filed () { return what; }
		
		public char key () { return key; }
		
		public int index () { return pos; }
		
	}
	
	public static boolean hasAccess (AccessFields field, User user) {
		if (field == null || user == null) {
			// No rights for null users
			return false;
		}
		
		if (user.getRights ().length () <= field.index ()) {
			// Necessary property doesn't exits
			return false;
		}
		
		return user.getRights ().charAt (field.index ()) == field.key ();
	}
	
}
