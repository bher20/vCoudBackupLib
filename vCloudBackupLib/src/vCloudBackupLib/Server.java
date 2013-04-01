/**
 * 
 */
package vCloudBackupLib;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import vCloudBackupLib.Settings.Template;

import com.vmware.vcloud.sdk.Organization;
import com.vmware.vcloud.sdk.VCloudException;
import com.vmware.vcloud.sdk.Vapp;
import com.vmware.vcloud.sdk.Vdc;

/**
 * @author Brad Herring
 *
 */
public class Server 
{	
	private static final String PRODUCTION_STRING = "production";
	private static final String STAGING_STRING = "staging";
	private static final String DEVELOPMENT_STRING = "development";
	
	private Organization organization;
	private Vdc vdc;
	private String serverName;
	private Vapp vapp;
	private Environment environment;
	private String dataCenter;
	private ArrayList<Settings.Environment> environments;
	
	public static enum Environment 
	{
		PRODUCTION, STAGING, DEVELOPMENT, UNKNOWN
	}
	
	
	
	public Organization getOrganization()
	{
		return organization;
	}
	
	
	public Vdc getVdc()
	{
		return vdc;
	}
	
	
	public String getServerName() 
	{
		return serverName;
	}
	
	public Vapp getVapp() 
	{
		return vapp;
	}
	
	public Environment getEnvironment() 
	{
		return environment;
	}
	
	public String getDataCenter() 
	{
		return dataCenter;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	public Server()
	{
	}
	
	
	public Server(Organization organization, Vdc vdc, String serverName, Vapp vapp, String dataCenter, ArrayList<Settings.Environment> environments)
	{
		this.organization = organization;
		this.vdc = vdc;
		this.serverName = serverName;
		this.vapp = vapp;
		this.dataCenter = dataCenter;
		this.environments = environments;
		
		this.environment = GetEnvironmentType();
	}
	
	
	private Environment GetEnvironmentType()
	{
		Pattern r;
		Matcher m;
		
		
		
		for (Settings.Environment env : environments)
		{
			r = Pattern.compile(env.getTag());
			m = r.matcher(serverName);
			
			if (m.find( )) 
			{				
				switch (env.getName())
				{
					case Server.PRODUCTION_STRING:
						return Environment.PRODUCTION;
						
					case Server.STAGING_STRING:
						return Environment.STAGING;
						
					case Server.DEVELOPMENT_STRING:
						return Environment.DEVELOPMENT;
						
					default:
						return Environment.UNKNOWN;
				}
			}
		}
		
		
		return Environment.UNKNOWN;
	}
	
	
	@Override public String toString()
	{
		return this.serverName;
	}
	
	
	public class ServerEnvironmentComparator implements Comparator<Server> 
	{
	    @Override
	    public int compare(Server o1, Server o2) 
	    {
	        return o1.getEnvironment().compareTo(o2.getEnvironment());
	    }
	}
	
	
	public class ServerNameComparator implements Comparator<Server> 
	{
	    @Override
	    public int compare(Server o1, Server o2) 
	    {
	        return o1.getServerName().compareTo(o2.getServerName());
	    }
	}
	
	
	public boolean PowerOff()
	{
		try
		{
			vapp.powerOff().waitForTask(0);
		} 
		
		catch (VCloudException | TimeoutException e)
		{
			return false;
		}
		
		return true;
	}
	
	
	public boolean PowerOn()
	{
		try
		{
			vapp.powerOn().waitForTask(0);
		} 
		
		catch (VCloudException | TimeoutException e)
		{
			return false;
		}
		
		return true;
	}
	
	
	public static int GetBaseMemory (int numberOfCPUs, ArrayList<Template> templates)
	{
		for (Template template : templates)
		{
			if (template.getNumOfCPU() == numberOfCPUs)
			{
				return template.getDefaultMemory();
			}
		}
		
		return 0;
	}
}
