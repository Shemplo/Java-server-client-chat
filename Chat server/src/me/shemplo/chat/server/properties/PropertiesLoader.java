package me.shemplo.chat.server.properties;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PropertiesLoader {

	private static final String FILE_NAME = "properties.xml";
	private static final int FAILED_READ = 10; // Failed to read properties file
	
	private static Set <String> loaded;
	
	public static void load (String fileName) {
		File file = new File (fileName);
		loaded = new HashSet <> ();
		
		try {
			if (!file.exists ()) { file.createNewFile (); }
		} catch (IOException ioe) {
			System.err.println ("[ERROR] Failed to create properties file" 
									+ (ioe.getMessage () != null && ioe.getMessage ().length () > 0
											? ": " + ioe.getMessage ()
											: ""));
			System.exit (FAILED_READ);
		}
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
		
		DocumentBuilder builder = null;
		try {
			builder = dbf.newDocumentBuilder ();
		} catch (ParserConfigurationException pce) {
			System.err.println ("[ERROR] Failed to create Document builder" 
									+ (pce.getMessage () != null && pce.getMessage ().length () > 0
											? ": " + pce.getMessage ()
											: ""));
			System.exit (FAILED_READ);
		}
		
		Document document = null;
		try {
			document = builder.parse (file);
		} catch (IOException ioe) {
			System.err.println ("[ERROR] Failed to parse document" 
									+ (ioe.getMessage () != null && ioe.getMessage ().length () > 0
											? ": " + ioe.getMessage ()
											: ""));
			System.exit (FAILED_READ);
		} catch (SAXException saxe) {
			System.err.println ("[ERROR] Failed to parse document" 
									+ (saxe.getMessage () != null && saxe.getMessage ().length () > 0
											? ": " + saxe.getMessage ()
											: ""));
			System.exit (FAILED_READ);
		}
		
		NodeList root = document.getChildNodes ();
		for (int i = 0; i < root.getLength (); i ++) {
			_loadProperties (root.item (i), "");
		}
	}
	
	private static void _loadProperties (Node node, String prefix) {
		if (node.getNodeType () == 1 /* ELEMENT NODE */) {
			String nodeName = node.getNodeName ();
			prefix = prefix != null ? prefix : "";
			prefix = prefix.length () > 0 
						? prefix + "."
						: "";
			prefix += nodeName;
			
			NodeList list = node.getChildNodes ();
			for (int i = 0; i < list.getLength (); i ++) {
				_loadProperties (list.item (i), prefix);
			}
		}
		
		if (node.getNodeType () == 3 /* TEXT NODE */) {
			if (!_isEmptyNode (node) && !loaded.contains (prefix)) {
				//System.out.println (prefix + "=" + node.getTextContent ());
				System.setProperty (prefix, node.getTextContent ());
				loaded.add (prefix);
			}
		}
	}
	
	private static boolean _isEmptyNode (Node node) {
		String value = node.getTextContent ();
		for (int i = 0; i < value.length (); i ++) {
			if (!Character.isWhitespace (value.charAt (i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static void load () { load (FILE_NAME); }
	
}
