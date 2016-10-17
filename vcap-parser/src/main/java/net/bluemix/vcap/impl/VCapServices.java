package net.bluemix.vcap.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.bluemix.vcap.IVCapService;
import net.bluemix.vcap.IVCapServiceCredentials;
import net.bluemix.vcap.IVCapServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * 
 */
public class VCapServices implements IVCapServices {
	
	/**
	 * The services.
	 */
	private final LinkedHashMap<String, ArrayList<IVCapService>> _services = new LinkedHashMap<String, ArrayList<IVCapService>>();
	
	private final Map<String,IVCapService> _servicesIndexedByName = new HashMap<String, IVCapService>();
	
	public VCapServices() throws JSONException {
		String vcapServices = System.getenv("VCAP_SERVICES");
		if (vcapServices == null) {
			vcapServices = System.getProperty("VCAP_SERVICES");
		}
		if (vcapServices != null) {
			this.setServices(vcapServices, false);
		}
	}
	
	/**
	 * @param defaultIfEnvValueNotDefined default value if the environment constant 'VCAP_SERVICES' is not defined.
	 * @throws JSONException
	 */
	public VCapServices(String defaultIfEnvValueNotDefined) throws JSONException {
		this(defaultIfEnvValueNotDefined, true);
	}
		
	/**
	 * @param services
	 * @throws JSONException
	 */
	public VCapServices(String services, boolean lookAtEnvFirst) throws JSONException {
		if (lookAtEnvFirst) {
			String envServices = System.getenv("VCAP_SERVICES");
			if (envServices != null && envServices.trim().length() != 0) {
				this.setServices(envServices, false);
				return;
			}
		}
		this.setServices(services, true);
	}
	
	protected void setServices(String services, boolean resolveSysProperty) throws JSONException {
		if (services == null) {
			throw new IllegalArgumentException("The services string description" +
					" must not be null. No VCAP_SERVICES to parse.");
		}
		JSONObject obj = new JSONObject(services);
		Iterator<?> it = obj.keys();
		while (it.hasNext()) {
			String serviceType = (String)it.next();
			JSONArray arr = obj.getJSONArray(serviceType);
			ArrayList<IVCapService> servicesArr = new ArrayList<IVCapService>(arr.length());
			if (_services.put(serviceType, servicesArr) != null) {
				throw new IllegalArgumentException("Duplicate service type arrays '" + serviceType + "'.");
			}
			for (int i = 0; i < arr.length(); i++) {
				JSONObject servOb = arr.getJSONObject(i);
				VCapService serv = new VCapService(serviceType, servOb, resolveSysProperty);
				servicesArr.add(serv);
				String name = serv.getName();
				if (name != null) {
					if (_servicesIndexedByName.put(name, serv) != null) {
						throw new IllegalArgumentException("Duplicate service with the name '" + name + "'.");
					}
				}
			}
		}
	}
	

	
	/**
	 * This reflects directly the way the vcap services are described in the environment variable
	 * VCAP_SERVICES.
	 * VCAP_SERVICES are an ordered bag of vcap services where the index is the service type (for example mongodb-1.8).
	 * @return
	 */
	public LinkedHashMap<String,ArrayList<IVCapService>> getVCapServices() {
		return _services;
	}
	
	/**
	 * @param serviceType
	 * @return The services of a given type.
	 */
	public ArrayList<IVCapService> getVCapServicesByType(String serviceType) {
		return _services.get(serviceType);
	}
	
	/**
	 * Helper method: traverses the vcap services and
	 * returns the first one that name matches the argument.
	 * @param name
	 * @return The first service with this name or null.
	 */
	public IVCapService getVCapServiceByName(String name) {
		return _servicesIndexedByName.get(name);
	}
	
	/**
	 * @param serviceType
	 * @param index The 0-based index
	 * @return
	 */
	public IVCapService getVCapService(String serviceType, int index) {
		ArrayList<IVCapService> servs = getVCapServicesByType(serviceType);
		if (servs == null || servs.size() > index+1) {
			return null;
		}
		return servs.get(index);
	}
	
	

	/**
	 * Helper method: traverses the vcap services and
	 * returns the first one that name matches the argument.
	 * @param name
	 * @return The first service with this name or null.
	 */
	public IVCapServiceCredentials getVCapServiceCredentialsByName(String name) {
		IVCapService serv = getVCapServiceByName(name);
		if (serv != null) {
			return serv.getCredentials();
		}
		return null;

	}
	
	/**
	 * @param serviceType
	 * @param index The 0-based index
	 * @return
	 */
	public IVCapServiceCredentials getVCapServiceCredentials(String serviceType, int index) {
		IVCapService serv = getVCapService(serviceType, index);
		if (serv != null) {
			return serv.getCredentials();
		}
		return null;
	}
	
	
		
}