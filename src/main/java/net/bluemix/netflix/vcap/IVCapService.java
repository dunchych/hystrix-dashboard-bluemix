package net.bluemix.netflix.vcap;


public interface IVCapService {

	/**
	 * @return The type of the service. For example mysql-5.1 or mongodb-1.8
	 */
	public String getServiceType();
	
	/**
	 * @return The name of the service.
	 */
	public String getName();
	
	/**
	 * @return The name of the service.
	 */
	public String getLabel();
	/**
	 * @return The name of the plan.
	 */
	public String getPlan();
	
	/**
	 * @return The tags
	 */
	public String[] getTags();
	
	/**
	 * @return The connection parameters.
	 */
	public IVCapServiceCredentials getCredentials();
}