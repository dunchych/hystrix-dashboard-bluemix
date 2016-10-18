package net.bluemix.vcap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Parses a JSON string into a collection of vcap services.
 * <p>
 * Database connection parameters and other application services 
 * are provided by cloudfoundry in the environment variable VCAP_SERVICES.
 * </p>
 * <p>
 * The value of this variable is a JSON string that contains the name of the wired
 * app services and the connection parameters for each one of them.
 * </p>
 * <p>
 * This helper class parses JSON and provides the value of those connection
 * parameters.
 * </p>
 */

public interface IVCapServices {

    /**
     * This reflects directly the way the vcap services are described in the environment variable
     * VCAP_SERVICES.
     * VCAP_SERVICES are an ordered bag of vcap services where the index is the service type
     * (for example mongodb-1.8).
     * @return
     */
    public LinkedHashMap<String,ArrayList<IVCapService>> getVCapServices();
    
    /**
     * @param serviceType
     * @return The services of a given type.
     */
    public ArrayList<IVCapService> getVCapServicesByType(String serviceType);
    
    /**
     * Helper method: traverses the vcap services and
     * returns the first one that name matches the argument.
     * @param name
     * @return The first service with this name or null.
     */
    public IVCapService getVCapServiceByName(String name);
    
    /**
     * @param serviceType
     * @param index The 0-based index
     * @return
     */
    public IVCapService getVCapService(String serviceType, int index);
   
    
}