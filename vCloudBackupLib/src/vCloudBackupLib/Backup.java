/**
 * 
 */
package vCloudBackupLib;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.xml.sax.SAXException;

import com.vmware.vcloud.api.rest.schema.CaptureVAppParamsType;
import com.vmware.vcloud.api.rest.schema.CatalogItemType;
import com.vmware.vcloud.api.rest.schema.ReferenceType;
import com.vmware.vcloud.api.rest.schema.TaskType;
import com.vmware.vcloud.api.rest.schema.TasksInProgressType;
import com.vmware.vcloud.sdk.Catalog;
import com.vmware.vcloud.sdk.CatalogItem;
import com.vmware.vcloud.sdk.Metadata;
import com.vmware.vcloud.sdk.Organization;
import com.vmware.vcloud.sdk.Task;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.VM;
import com.vmware.vcloud.sdk.Vapp;
import com.vmware.vcloud.sdk.VappTemplate;
import com.vmware.vcloud.sdk.VcloudClient;
import com.vmware.vcloud.sdk.Vdc;
import com.vmware.vcloud.sdk.VirtualDisk;
import com.vmware.vcloud.sdk.VirtualNetworkCard;
import com.vmware.vcloud.sdk.admin.extensions.VcloudAdminExtension;
import com.vmware.vcloud.sdk.constants.Version;

/**
 * @author Brad Herring
 *
 */
public class Backup 
{
	private static final String SHEET_TARGET_VMS = "Target VMs";
	private static final int COLUMN_DATA_CENTER = 0;
	private static final String COLUMN_DATA_CENTER_NAME = "Data Center";
	private static final int COLUMN_CPUS = 1;
	private static final String COLUMN_CPUS_NAME = "CPUs";
	private static final int COLUMN_MEMORY = 2;
	private static final String COLUMN_MEMORY_NAME = "Memory";
	private static final int COLUMN_EXTRA_STORAGE = 3;
	private static final String COLUMN_EXTRA_STORAGE_NAME = "Extra Storage";
	private static final int COLUMN_EXTRA_MEMORY = 4;
	private static final String COLUMN_EXTRA_MEMORY_NAME = "Extra Memory";
	private static final int COLUMN_ENVRIONMENT = 5;
	private static final String COLUMN_ENVRIONMENT_NAME = "Environment";
	private static final int COLUMN_IP_ADDRESS = 6;
	private static final String COLUMN_IP_ADDRESS_NAME = "IP Address";
	private static final int COLUMN_DNS_NAME = 7;
	private static final String COLUMN_DNS_NAME_NAME = "DNS Name";
	private static final int COLUMN_HASH = 8;
	private static final String COLUMN_HASH_NAME = "Hash";
	private static final int COLUMN_ORGANIZATION = 9;
	private static final String COLUMN_ORGANIZATION_NAME = "Organization";
	private static final int COLUMN_VAPP_NAME = 10;
	private static final String COLUMN_VAPP_NAME_NAME = "vAPP Name";
	private static final int COLUMN_VM_DESCRIPTION = 11;
	private static final String COLUMN_VM_DESCRIPTION_NAME = "VM Description";
	private static final int COLUMN_TEMPLATE = 12;
	private static final String COLUMN_TEMPLATE_NAME = "Template";
	private static final int COLUMN_ZONE = 13;
	private static final String COLUMN_ZONE_NAME = "Zone";
	private static final int COLUMN_NUMBER = 14;
	
	private static final int DEFAULT_DRIVE_COUNT = 2;
	
	
	/**
	 * Users username used to access the vCloud API.
	 */
	private String username;
	/**
	 * Users password used to access the vCloud API.
	 */
	private String password;
	/**
	 * The API url for the vCloud server.
	 */
	private String vCloudURL;
	/**
	 * The users vCloud organization.
	 */
	private String organization;
	/**
	 * The VcloudClient used to access the vCloud API.
	 */
	private VcloudClient _client;
	/**
	 * A HashMap of organizations in the vCloud.
	 */
	private HashMap<String, ReferenceType> organizations;
	/**
	 * The ArrayList of Servers retrieved from the vCloud API.
	 */
	private ArrayList<Server> servers;
	/**
	 * Whether or not the user is logged into the vCloud API.
	 */
	private boolean loggedIn;
	/**
	 * The data center name.
	 */
	private String dataCenterName;
	/**
	 * The Settings associated with this Backup object.
	 */
	private Settings settings;
	/**
	 * The name of the private catalog
	 */
	private String catalogName;
	
	private static VcloudAdminExtension extension;

	/**
	 * Get the vCloud API url that this Backup object is using.
	 * @return The url to the vCloud API.
	 */
	public String getvCloudURL() 
	{
		return vCloudURL;
	}

	/**
	 * Get an ArrayList of Servers that is associated with 
	 * 	this Backup object.
	 * @return An ArrayList of Servers.
	 */
	public ArrayList<Server> getServers() 
	{
		return servers;
	}

	/**
	 * Get whether the user is logged in or not.
	 * @return True, if the user is logged in, false otherwise.
	 */
	public boolean getLoggedIn() 
	{
		return loggedIn;
	}
	
	
	
	
	/**
	 * Default Constructor
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public Backup() throws SAXException, IOException, ParserConfigurationException
	{		
		settings = new Settings();
		settings.ReadSettings();
		loggedIn = false;

		VcloudClient.setLogLevel(Level.ALL);
	}
	
	
	/**
	 * Main Constructor
	 * 
	 * @param username The username of the user to login to the API as.
	 * @param password The password of the user to login to the API as.
	 * @param organization The vCloud Organization the user belongs to.
	 * @param vCloudURL The URL of the vCloud Director server.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public Backup(String username, String password, String organization, String vCloudURL, String dataCenterName, String catalogName) throws SAXException, IOException, ParserConfigurationException
	{
		this.username = username;
		this.password = password;
		this.organization = organization;
		this.vCloudURL = vCloudURL;	
		this.dataCenterName = dataCenterName;
		this.catalogName = catalogName;
		
		settings = new Settings();
		settings.ReadSettings();
		loggedIn = false;

		VcloudClient.setLogLevel(Level.ALL);
	}
	
	
	/**
	 * Get the login name string
	 * 
	 * @return The username plus the organization used to login to the API.
	 */
	private String GetLoginName()
	{
		return username + "@" + this.organization;
	}

	
	/**
	 * Login to the vCloud API.
	 * 
	 * @return True if the login was successful, false otherwise.
	 */
	public boolean Login()
	{
		boolean success = false;
		
		try 
		{
			_client = new VcloudClient(vCloudURL, Version.V1_5);
			_client.login(GetLoginName(), password);

			extension = _client.getVcloudAdminExtension();
			organizations = GetOrgs();
			servers = ProcessServers();
			
			this.loggedIn = true;
			success = true;
		} 
		catch (VCloudException e) 
		{
			loggedIn = false;
		}
		
		return success;
	}
	
	
	/**
	 * Logout of the vCloud API.
	 * 
	 * @return True if the logout was successful, false otherwise.
	 */
	public boolean Logout()
	{
		boolean success = false;
		
		
		try 
		{
			_client.logout();
			
			loggedIn = false;
			
			success = true;
		} 
		catch (VCloudException e) 
		{
			e.printStackTrace();
		}	
		
		return success;
	}


	/**
	 * Get all of the user's Organizations.
	 * 
	 * @return A HashMap of all of the user's organizations.
	 * @throws VCloudException
	 */
	private HashMap<String, ReferenceType> GetOrgs() throws VCloudException
	{		
		HashMap<String, ReferenceType> orgs = _client.getOrgRefsByName();
		HashMap<String, ReferenceType> returnOrgs = new HashMap<String, ReferenceType>();
		
		
		for (String organizationName : orgs.keySet())
		{
			if (organizationName != null)
			{
				returnOrgs.put(organizationName, orgs.get(organizationName));
			}
		}
		
		return returnOrgs;
	}
	
	
	/**
	 * Get all of the server's associated with the user's Organizations.
	 * 
	 * @return An Array of the servers.
	 * @throws VCloudException 
	 */
	private ArrayList<Server> ProcessServers() throws VCloudException
	{
		ArrayList<Server> servers = new ArrayList<Server>();
		
		
		for (ReferenceType orgRef : this.organizations.values()) 
		{
			for (ReferenceType vdcRef : Organization.getOrganizationByReference(_client, orgRef).getVdcRefs()) 
			{				
				for (ReferenceType vAppRef : Vdc.getVdcByReference(_client, vdcRef).getVappRefs()) 
				{					
					Vapp vapp = Vapp.getVappByReference(_client, vAppRef);
					
					
					servers.add(new Server (Organization.getOrganizationByReference(_client, orgRef), Vdc.getVdcByReference(_client, vdcRef), 
							vAppRef.getName(), vapp, dataCenterName, settings.getEnvironments()));
				}
			}
		}
		
		return servers;
	}
	
	
	/**
	 * Backup the passed in <code>Server</code> to the private catalog, and add the passed in description.
	 * @param server The <code>Server</code> to backup.
	 * @param description The description to use when backing up the Server.
	 * @return <code>true</code> if the backup was successful, <code>false</code> otherwise.
	 */
	public boolean BackupServer(Server server, String description)
	{
		//Setup the templates information
		CaptureVAppParamsType parms = new CaptureVAppParamsType();
		
		parms.setSource(server.getVapp().getReference());
		parms.setName(server.getServerName());
		parms.setDescription(description);
		
		
		Vdc vdc = server.getVdc();
		
		if (vdc == null)
			return false;
		
		else
			return true;
		
		/*else
		{
			//Backup server
			try
			{
				server.PowerOff();
				
				
				
				VappTemplate vTemplate = vdc.captureVapp(parms);
				
				
				ReferenceType catalogRef = null;
				for (ReferenceType ref : server.getOrganization().getCatalogRefs()) {
					if (ref.getName().equals(catalogName))
						catalogRef = ref;
				}
				
				Catalog cat = Catalog.getCatalogByReference(_client, catalogRef);
				CatalogItemType catItem = createNewCatalogItem(vTemplate.getReference(), server.getServerName(), description); 
				CatalogItem catalogItem = cat.addCatalogItem(catItem);
				
				Task task = returnTask(catalogItem);
				
				if (task != null)
				{
					try
					{
						task.waitForTask(0);
					} 
					
					catch (TimeoutException e)
					{
						server.PowerOn();
						return false;
					}
				}
			} 
			
			catch (VCloudException e)
			{
				server.PowerOn();
				return false;
			}
			

			server.PowerOn();
			return true;
		}*/
	}
	
	
	/**
	 * Export the passed in <code>ArrayList</code> of <code>Server</code> objects to a Excel spreadsheet.
	 * @param servers The <code>ArrayList</code> of <code>Server</code> objects to export.
	 * @param pathToSave The path that the Excel spreadsheet should be saved to.
	 * @return <code>true</code> if the <code>ArrayList</code> of <code>Server</code> objects was able to be exported, code>false</code> otherwise.
	 */
	public boolean ServerDetailExcel(ArrayList<Server> servers, String pathToSave)
	{
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(Backup.SHEET_TARGET_VMS);
		
		
		//Create the row headers
		HSSFRow row = sheet.createRow(0);
		row.createCell(Backup.COLUMN_DATA_CENTER).setCellValue(Backup.COLUMN_DATA_CENTER_NAME);
		row.createCell(Backup.COLUMN_CPUS).setCellValue(Backup.COLUMN_CPUS_NAME);
		row.createCell(Backup.COLUMN_MEMORY).setCellValue(Backup.COLUMN_MEMORY_NAME);
		row.createCell(Backup.COLUMN_EXTRA_STORAGE).setCellValue(Backup.COLUMN_EXTRA_STORAGE_NAME);
		row.createCell(Backup.COLUMN_EXTRA_MEMORY).setCellValue(Backup.COLUMN_EXTRA_MEMORY_NAME);
		row.createCell(Backup.COLUMN_ENVRIONMENT).setCellValue(Backup.COLUMN_ENVRIONMENT_NAME);
		row.createCell(Backup.COLUMN_IP_ADDRESS).setCellValue(Backup.COLUMN_IP_ADDRESS_NAME);
		row.createCell(Backup.COLUMN_DNS_NAME).setCellValue(Backup.COLUMN_DNS_NAME_NAME);
		row.createCell(Backup.COLUMN_HASH).setCellValue(Backup.COLUMN_HASH_NAME);
		row.createCell(Backup.COLUMN_ORGANIZATION).setCellValue(Backup.COLUMN_ORGANIZATION_NAME);
		row.createCell(Backup.COLUMN_VAPP_NAME).setCellValue(Backup.COLUMN_VAPP_NAME_NAME);
		row.createCell(Backup.COLUMN_VM_DESCRIPTION).setCellValue(Backup.COLUMN_VM_DESCRIPTION_NAME);
		row.createCell(Backup.COLUMN_TEMPLATE).setCellValue(Backup.COLUMN_TEMPLATE_NAME);
		row.createCell(Backup.COLUMN_ZONE).setCellValue(Backup.COLUMN_ZONE_NAME);
		
		
		try
		{
			for (int i = 1; i < servers.size(); i++)
			{
				Server server = servers.get(i);
				VM vm = server.getVapp().getChildrenVms().get(0);
				ArrayList<VirtualDisk> vds = new ArrayList<VirtualDisk>();
				int cpus = vm.getCpu().getNoOfCpus();
				int memory = vm.getMemory().getMemorySize().intValue();
				VirtualNetworkCard primaryNic = vm.getNetworkCards().get(0);
				
				
				
				//Extra Storage
				int extraStorage = 0;
				try
				{	
					
					for (VirtualDisk vd : vm.getDisks())
					{
						if (vd.isHardDisk())
							vds.add(vd);
					}
					
					
					
					
					int numOfDrives = vds.size();
					if (numOfDrives > DEFAULT_DRIVE_COUNT)
					{
						for (int j = DEFAULT_DRIVE_COUNT; j < numOfDrives; j++)
						{
							VirtualDisk vd = vds.get(j);
							int hdSize = vd.getHardDiskSize().intValue();
							
							extraStorage += hdSize;
						}
					}
				} 
				catch (Exception e)
				{
					return false;
				}
				


				//Extra Memory
				int extraMemory = 0;	
				int tempExtraMemory = 0;
				int baseMemory = Server.GetBaseMemory(cpus, settings.getTemplates()) * 1024;
				
				if (baseMemory != memory)
					tempExtraMemory = memory - baseMemory;
				
				if (tempExtraMemory!= 0)
				{
					extraMemory = tempExtraMemory;
				}
				
				
				for (VirtualNetworkCard nc : vm.getNetworkCards())
				{
					if (nc.isPrimaryNetworkConnection())
					{
						primaryNic = nc;
						break;
					}
				}
				
				
				Map<QName, String> md = server.getVapp().getReference().getOtherAttributes();
				
				for (QName mdKey : md.keySet())
				{
					String mdStr = md.get(mdKey);
					
					System.out.println(mdStr);
				}
				
				
				
				
				row = sheet.createRow(i);
				row.createCell(Backup.COLUMN_DATA_CENTER).setCellValue(Character.toUpperCase(server.getDataCenter().charAt(0)) + server.getDataCenter().substring(1));
				row.createCell(Backup.COLUMN_CPUS).setCellValue(cpus);
				row.createCell(Backup.COLUMN_MEMORY).setCellValue((vm.getMemory().getMemorySize().intValue() / 1024) + "GB");
				row.createCell(Backup.COLUMN_EXTRA_STORAGE).setCellValue((extraStorage / 1024) + "GB");
				row.createCell(Backup.COLUMN_EXTRA_MEMORY).setCellValue((extraMemory / 1024) + "GB");
				row.createCell(Backup.COLUMN_ENVRIONMENT).setCellValue(server.getEnvironment().toString());
				row.createCell(Backup.COLUMN_IP_ADDRESS).setCellValue(primaryNic.getIpAddress());
				row.createCell(Backup.COLUMN_DNS_NAME).setCellValue(vm.getGuestCustomizationSection().getComputerName().toUpperCase());
				row.createCell(Backup.COLUMN_HASH).setCellValue("#");
				row.createCell(Backup.COLUMN_ORGANIZATION).setCellValue(server.getOrganization().getReference().getName());
				row.createCell(Backup.COLUMN_VAPP_NAME).setCellValue(server.getVapp().getReference().getName());
				row.createCell(Backup.COLUMN_VM_DESCRIPTION).setCellValue("");
				row.createCell(Backup.COLUMN_TEMPLATE).setCellValue("");
				row.createCell(Backup.COLUMN_ZONE).setCellValue(primaryNic.getNetwork().replaceAll(server.getOrganization().getReference().getName() + "-", ""));
			}
			
			
			
			
			
			for(int i = 0; i <= Backup.COLUMN_NUMBER; i++)
			{
				sheet.autoSizeColumn(i);
			}
			
			

			FileOutputStream fileOut = new FileOutputStream(pathToSave);
			wb.write(fileOut);
			fileOut.close();
		} 
		
		catch (VCloudException | IOException e)
		{
			return false;
		}
		
		
		return true;
	}
	
	
	
	/**
	 * Create a new catalog item type with the specified vapp template reference
	 *
	 * @param vAppTemplatereference
	 *            {@link ReferenceType}
	 * @return {@link CatalogItemType}
	 */
	private static CatalogItemType createNewCatalogItem(ReferenceType vAppTemplatereference, String name, String description) 
	{
		CatalogItemType catalogItem = new CatalogItemType();
		catalogItem.setName(name);
		catalogItem.setDescription(description);
		catalogItem.setEntity(vAppTemplatereference);

		
		return catalogItem;
	}
	
	
	/**
	 * Check for tasks if any
	 *
	 * @param catalogItem
	 * @return {@link Task}
	 * @throws VCloudException
	 */
	private Task returnTask(CatalogItem catalogItem)
			throws VCloudException {
		TasksInProgressType tasksInProgress = ((CatalogItemType) catalogItem
				.getResource()).getTasks();
		if (tasksInProgress != null)
			for (TaskType task : tasksInProgress.getTask()) {
				return new Task(_client, task);
			}
		return null;
	}
}
