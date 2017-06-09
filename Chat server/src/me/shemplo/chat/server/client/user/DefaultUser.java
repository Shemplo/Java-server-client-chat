package me.shemplo.chat.server.client.user;

import me.shemplo.chat.server.exceptions.UserException;

public class DefaultUser implements User {

	private String id, name, 
					lastName, 
					login, 
					rights;
	
	public DefaultUser (String id, String password) {
		this.id = _authorize (id, password);
		
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
		_checkRights (Fields.LOGIN, user);
	}
	
	@Override
	public void changeName (String name, User user) throws UserException {

	}

	@Override
	public void changeLastName (String lastName, User user) throws UserException {
		
	}

	@Override
	public String changeRights (Fields fields, char value, User user) {
		return null;
	}
	
	private String _authorize (String id, String password) {
		name = lastName = ":gt:";
		login = ":: guest ::";
		rights = "l000";
		
		return id;
	}
	
	private boolean _checkRights (Fields filed, User user) throws UserException {
		if (!checkRights (filed, user)) {
			throw new UserException ("Not enough rights to change " 
										+ filed.filed () + " for user " 
										+ user.getID ());
		}
		
		return true;
	}
	
}
