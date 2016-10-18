package com.rbc.wy20.vcap.impl;


import com.rbc.wy20.vcap.IVCapService;
import com.rbc.wy20.vcap.IVCapServiceCredentials;
import com.rbc.wy20.vcap.IVCapServices;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class VCapServiceCredentials implements IVCapServiceCredentials {
 
	private final JSONObject _json;
	private final boolean _resolveSysProperty;


	
	/**
	 * Factory method for Ioc like spring.
	 * @param services
	 * @param serviceType
	 * @return
	 */
	public static IVCapServiceCredentials getCredentialsOfFirstService(
			IVCapServices services, String serviceType) {
			return services.getVCapService(serviceType, 0).getCredentials();
	}

	
		
	/**
	 * @param service The service object. It contains a 'credentials' object.
	 * @throws JSONException
	 */
	public VCapServiceCredentials(JSONObject service, boolean resolveSysProperty) throws JSONException {
		_json = service.getJSONObject("credentials");
		_resolveSysProperty = resolveSysProperty;
		if (_json == null) {
			throw new IllegalArgumentException("Unable to find the 'credentials' object.");
		}
	}

	/**
	 * @return The name of the app service.
	 * For example, the database name or the top-level collection.
	 */
	public String getName() {
		return getString("name");
	}
	/**
	 * @return The IP or hostname where the service runs.
	 */
	public String getHostname() {
		return getString("hostname");
	}
	/**
	 * @return The port where it is accessible.
	 */
	public int getPort() {
		try {
			return _json.getInt("port");
		} catch (JSONException e) {
			if (_resolveSysProperty) {
				String portStr = getString("port");
				if (portStr != null) {
					return Integer.parseInt(portStr);
				}
			}
			return -1;
		}
	}
	/**
	 * @return The name of the user.
	 */
	public String getUser() {
		String result = getString("user");
		return result != null ? result : getString("username");
	}
	/**
	 * @return The name of the user.
	 */
	public String getUsername() {
		String result = getString("username");
		return result != null ? result : getString("user");
	}
	/**
	 * @return The password.
	 */
	public String getPassword() {
		return getString("password");
	}
	
    /**
     * @return The url value or null when not defined. Used in mongodb.
     */
    public String getURI() {
        return getString("uri");
    }
    
    /**
	 * @return The url value or null when not defined. Used in mongodb.
	 */
	public String getURL() {
		return getString("url");
	}

	/**
	 * @return The vhost value or null when not defined. Used in mongodb.
	 */
	public String getVhost() {
		return getString("vhost");
	}

       
	
	/**
	 * Returns the json string value for the given key.
	 * Takes care of resolving system properties if necessary.
	 * @param key
	 * @return
	 */
	private String getString(String key) {
		try {
			String str = _json.getString(key);
			if (str != null && _resolveSysProperty) {
				str = VCapService.resolvePropertyValue(str);
			}
			return str;
		} catch (JSONException e) {
			return null;
		}
	}
}