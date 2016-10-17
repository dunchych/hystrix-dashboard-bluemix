package net.bluemix.vcap;

/**
 * Credentials to connect to a data service. As modeled in VCAP_SERVICES.
 * 
 */

 public interface IVCapServiceCredentials {

    /**
     * @return The name of the app service.
     * For example, the database name of a relational database.
     */
    public String getName();
    /**
     * @return The IP or hostname where the service runs.
     */
    public String getHostname();
    /**
     * @return The port where it is accessible.
     */
    public int getPort();
    /**
     * @return The name of the user.
     */
    public String getUser();
    /**
     * @return The password.
     */
    public String getPassword();
    
    /**
     * @return The url value or null when not defined. Used in rabbitmq.
     * For a relational database, the name of the database is returned by IVCapCredentials#getName
     */
    public String getURI();
    
    /**
     * @return The vhost value or null when not defined. Used in rabbitmq.
     * For a relational database, the name of the database is returned by IVCapCredentials#getName
     */
    public String getURL();

     /**
     * @return The vhost value or null when not defined. Used in rabbitmq.
     * For a relational database, the name of the database is returned by IVCapCredentials#getName
     */
    public String getVhost();
    
}