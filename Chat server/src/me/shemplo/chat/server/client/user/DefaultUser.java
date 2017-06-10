package me.shemplo.chat.server.client.user;

import java.util.Map;

import me.shemplo.chat.server.client.user.UsersManager.AccessFields;
import me.shemplo.chat.server.client.user.UsersManager.AuthorizedUserFields;
import me.shemplo.chat.server.exceptions.UserException;

public class DefaultUser implements User {

	private String id, name, 
					lastName, 
					login, 
					rights;
	
	public DefaultUser () {
		this.id = User.GUEST_ID;
		this.rights = "00000000";
		this.login = "::guest::";
		this.name = this.lastName = ":gst:";
	}
	
	public DefaultUser (String id, String password) throws UserException {
		Map <AuthorizedUserFields, String> user = UsersManager.authorize (id, password);
		
		this.id = user.get (AuthorizedUserFields.ID);
		this.name = user.get (AuthorizedUserFields.NAME);
		this.login = user.get (AuthorizedUserFields.LOGIN);
		this.rights = user.get (AuthorizedUserFields.RIGHTS);
		this.lastName = user.get (AuthorizedUserFields.LAST_NAME);
	}
	
	@Override
	public String getID () { return id; }
	
	@Override
	public String getName () { return name; }

	@Override
	public String getLastName () { return lastName; }
	
	@Override
	public String getLogin () { return login; }
	
	@Override
	public String getRights () { return rights; }
	
	@Override
	public void changeLogin (String login, User user) throws UserException {
		_checkRights (AccessFields.LOGIN, user);
	}
	
	@Override
	public void changeName (String name, User user) throws UserException {

	}

	@Override
	public void changeLastName (String lastName, User user) throws UserException {
		
	}

	@Override
	public void changeRights (AccessFields fields, char value, User user) {
		
	}
	
	@Override
	public void changeRights (String rights, User user) throws UserException {
		
	}
	
	private boolean _checkRights (AccessFields filed, User user) throws UserException {
		if (!UsersManager.hasAccess (filed, user)) {
			throw new UserException ("Not enough rights to change " 
										+ filed.filed () + " for user " 
										+ user.getID ());
		}
		
		return true;
	}
	
}
