package me.shemplo.chat.server.client.user;

import me.shemplo.chat.server.client.user.UsersManager.AccessFields;
import me.shemplo.chat.server.exceptions.UserException;

public class RootUser implements User {
	
	public static final String ID = ".root";
	private static boolean wasCreated = false;
	
	public static RootUser getInstance () throws UserException {
		if (wasCreated) { throw new UserException ("Only one root user can be instantiated"); }
		RootUser.wasCreated = true;
		return new RootUser ();
	}
	
	/* ===| IMPLEMENTATIONS |===  */
	
	private static String rights = "";
	
	private RootUser () {
		AccessFields [] fields = AccessFields.values ();
		char [] buffer = new char [fields.length];
		
		for (AccessFields field : fields) {
			buffer [field.index ()] = field.key ();
		}
		
		rights = new String (buffer);
	}
	
	@Override
	public String getID () { return ID; }

	@Override
	public String getName () { return this.getClass ().getSimpleName (); }

	@Override
	public String getLastName () { return getName (); }

	@Override
	public String getLogin () { return getName (); }
	
	@Override
	public String getRights () { return rights; }

	@Override
	public void changeName (String name, User user) throws UserException {}
	
	@Override
	public void changeLastName (String lastName, User user) throws UserException {}
	
	@Override
	public void changeLogin (String login, User user) throws UserException {}

	@Override
	public void changeRights (AccessFields fields, char value, User user) throws UserException {}

	@Override
	public void changeRights (String rights, User user) throws UserException {}
	
}
