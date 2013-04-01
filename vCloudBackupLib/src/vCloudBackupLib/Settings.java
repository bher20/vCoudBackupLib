/**
 * 
 */
package vCloudBackupLib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Brad Herring
 * Holds the settings for the library, read in from an xml file.
 */
public class Settings
{
	/**
	 * The default path to the settings.xml file.
	 */
	public static final String DEFAULT_XML_PATH = "lib_settings.xml";
	
	/**
	 * The path to the settings file.
	 */
	private String settingsFilePath;
	
	
	/**
	 * An ArrayList of Templates.
	 */
	private ArrayList<Template> templates;
	/**
	 * An ArrayList of Environments.
	 */
	private ArrayList<Environment> environments;
	
	
	/**
	 * Set the path to the settings file.
	 * @param settingsFilePath The path to the settings file.
	 */
	private void setSettingsFilePath(String settingsFilePath)
	{
		if (SettingsFileExists(settingsFilePath))
			this.settingsFilePath = settingsFilePath;
		
		else
			this.settingsFilePath = DEFAULT_XML_PATH;
	}

	/**
	 * Get an ArrayList of Templates.
	 * @return An ArrayList of Templates.
	 */
	public ArrayList<Template> getTemplates()
	{
		return templates;
	}

	/**
	 * Get an ArrayList of Environments.
	 * @return An ArrayList of Environments.
	 */
	public ArrayList<Environment> getEnvironments()
	{
		return environments;
	}



















	/**
	 * Default constructor
	 */
	public Settings()
	{
		setSettingsFilePath(DEFAULT_XML_PATH);
		
		templates = new ArrayList<Template>();
		environments = new ArrayList<Environment>();
	}
	
	
	/**
	 * Main constructor
	 * @param pathToXML The path to the settings file.
	 */
	public Settings(String pathToXML)
	{
		setSettingsFilePath(pathToXML);
		
		templates = new ArrayList<Template>();
		environments = new ArrayList<Environment>();
	}
	
	
	/**
	 * Check if the settings file exists.
	 * @param pathToXML The path to the settings file.
	 * @return True, if the file exists, false otherwise.
	 */
	private boolean SettingsFileExists(String pathToXML)
	{
		File f = new File(pathToXML);
		
		
		if(f.exists()) 
		 return true;
		
		else
			return false;
	}
	
	
	/**
	 * Read in the settings file.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public void ReadSettings() throws SAXException, IOException, ParserConfigurationException
	{
		NodeList nList;
		
		File fXmlFile = new File(this.settingsFilePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		
		//Template Settings
		nList = doc.getElementsByTagName(Template.TEMPLATE);
 	
		//Process each Template
		for (int i = 0; i < nList.getLength(); i++) 
		{			
			Node nNode = nList.item(i);
			
			
			
			  
			if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				Element eElement = (Element) nNode;
				int cpuNumber = Integer.parseInt(GetTagValue(Template.CPU, eElement));
				int memory = Integer.parseInt(GetTagValue(Template.MEMORY, eElement));
				
				
				templates.add(new Template(cpuNumber, memory));
			}
		}
		
		
		//Environment
		nList = doc.getElementsByTagName("environment");
	 	
		//Process each environment
		for (int i = 0; i < nList.getLength(); i++) 
		{			
			Node nNode = nList.item(i);
			
			
			
			  
			if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			{
				Element eElement = (Element) nNode;
				String name = eElement.getAttribute("name");
				String tag = eElement.getTextContent();
				
				
				environments.add(new Environment(name, tag));
			}
		}
	}
	
	
	/**
	 * Get the passed in tags value.
	 * @param sTag The tag to get value for.
	 * @param eElement The element to read the tags value from.
	 * @return The value of the tag.
	 */
	private static String GetTagValue(String sTag, Element eElement) 
	{
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
	 
	    Node nValue = (Node) nlList.item(0);
	 
		return nValue.getNodeValue();
	}
	
	
	
	
	
	/**
	 * @author Brad Herring
	 * Represents a Environment.
	 */
	public class Environment
	{
		/**
		 * The name of the Environment name tag in the settings.xml file.
		 */
		public static final String NAME = "name";
		
		/**
		 * The name of this Environment.
		 */
		private String name;
		/**
		 * The tag of this Environment.
		 */
		private String tag;
		
		
		/**
		 * Get the name of this Environment.
		 * @return The tag of this Environment.
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Get the tag of this Environment.
		 * @return The tag of this Environment.
		 */
		public String getTag()
		{
			return tag;
		}
		
		
		
		
		
		
		/**
		 * Main constructor
		 * @param name The name of this Environment.
		 * @param tag The tag of this Environment.
		 */
		public Environment (String name, String tag)
		{
			this.name = name;
			this.tag = tag;
		}
	}
	
	
	/**
	 * @author Brad Herring
	 * Represents a Template.
	 */
	public class Template
	{
		/**
		 * The name of the template tag in the settings.xml file.
		 */
		public static final String TEMPLATE = "template";
		/**
		 * The name of the cpu tag in the settings.xml file.
		 */
		public static final String CPU = "cpu";
		/**
		 * The name of the memory tag in the settings.xml file.
		 */
		public static final String MEMORY = "memory";
		
		/**
		 * The number of cpus of this Template.
		 */
		private int numOfCPU;
		/**
		 * The default amount of memory of this Template.
		 */
		private int defaultMemory;
		
		/**
		 * Get the number of cpus for this Template.
		 * @return The number of cpus for this template.
		 */
		public int getNumOfCPU()
		{
			return numOfCPU;
		}
		
		/**
		 * Get the default amount of memory for this Template.
		 * @return The default amount of memory for this Template.
		 */
		public int getDefaultMemory()
		{
			return defaultMemory;
		}
		
		
		
		
		
		
		/**
		 * Main constructor
		 * @param numOfCPU The number of cpus for this Template.
		 * @param defaultMemory The default amount of memory for this Template.
		 */
		public Template(int numOfCPU, int defaultMemory)
		{
			this.numOfCPU = numOfCPU;
			this.defaultMemory = defaultMemory;
		}
	}
}