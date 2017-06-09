package me.shemplo.chat.server.properties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Properties {

	private static final String FILE_NAME = "properties.xml";
	private static final int FAILED_READ = 10; // Failed to read properties file
	
	private static Map <String, String> properties;
	
	static {
		properties = new HashMap <> ();
		
		File file = new File (FILE_NAME);
		try {
			if (!file.exists ()) { file.createNewFile (); }
		} catch (IOException ioe) {
			System.err.println ("[ERROR] Failed to create proprties file" 
									+ (ioe.getMessage () != null && ioe.getMessage ().length () > 0
											? ": " + ioe.getMessage ()
											: ""));
			System.exit (FAILED_READ);
		}
		
		System.out.println ("Done");
	}
	
	public static String getProperty (String name) {
		System.getProperty (FILE_NAME);
		return properties.get (name);
	}
	
}
