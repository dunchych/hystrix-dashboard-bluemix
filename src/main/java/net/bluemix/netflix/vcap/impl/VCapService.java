package net.bluemix.netflix.vcap.impl;

import net.bluemix.netflix.vcap.IVCapService;
import net.bluemix.netflix.vcap.IVCapServiceCredentials;
import net.bluemix.netflix.vcap.IVCapServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VCapService implements IVCapService {
	
	private final JSONObject _json;
	private final String _serviceType;
	private final VCapServiceCredentials _cred;
	private final boolean _parseSysProperty;
	
	/**
	 * @param serviceType
	 * @param service
	 * @param parseSysProperty true when json values that follow the pattern ${sys_prop_key,_default_value}
	 * should be substituted.
	 * @throws JSONException
	 */
	public VCapService(String serviceType, JSONObject service,
			boolean parseSysProperty) throws JSONException {
		_json = service;
		_serviceType = serviceType;
		_cred = new VCapServiceCredentials(service, parseSysProperty);
		_parseSysProperty = parseSysProperty;
	}
	
	/**
	 * @return The type of the service. For example mysql-5.1 or mongodb-1.8
	 */
	public String getServiceType() {
		return _serviceType;
	}
	
	/**
	 * @return The name of the service.
	 */
	public String getName() {
		try {
			return _json.getString("name");
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * @return The name of the service.
	 */
	public String getLabel() {
		try {
			return _json.getString("label");
		} catch (JSONException e) {
			return null;
		}
	}
	/**
	 * @return The name of the plan.
	 */
	public String getPlan() {
		try {
			return _json.getString("label");
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * @return The tags
	 */
	public String[] getTags() {
		try {
			JSONArray arr = _json.getJSONArray("tags");
			String[] res = new String[arr.length()];
			for (int i = 0; i < res.length; i++) {
				res[i] = arr.getString(i);
			}
			return res;
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * @return The connection parameters.
	 */
	public IVCapServiceCredentials getCredentials() {
		return _cred;
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
			if (_parseSysProperty) {
				str = resolvePropertyValue(str);
			}
			return str;
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * recursively substitute the ${sysprop} by their actual system property.
	 * ${sysprop,defaultvalue} will use 'defaultvalue' as the value if no sysprop is defined.
	 * Not the most efficient code but we are shooting for simplicity and speed of development here.
	 * 
	 * @param value
	 * @return
	 */
	public static String resolvePropertyValue(String value)
	{	
		int ind = value.indexOf("${");
		if (ind == -1) {
			return value;
		}
		int ind2 = value.indexOf('}', ind);
		if (ind2 == -1) {
			return value;
		}
		String sysprop = value.substring(ind+2, ind2);
		String defaultValue = null;
		int comma = sysprop.indexOf(',');
		if (comma != -1 && comma+1 != sysprop.length())
		{
			defaultValue = sysprop.substring(comma+1);
			defaultValue = resolvePropertyValue(defaultValue);
			sysprop = sysprop.substring(0,comma);
		}
		else
		{
			defaultValue = "${" + sysprop + "}";
		}
		
		String v = System.getProperty(sysprop);
		if (v == null) {
			v = System.getenv(sysprop);
		}
		
		String reminder = value.length() > ind2 + 1 ? value.substring(ind2+1) : "";
		reminder = resolvePropertyValue(reminder);
		if (v != null)
		{
			return value.substring(0, ind) + v + reminder;
		}
		else
		{
			return value.substring(0, ind) + defaultValue + reminder;
		}
	}

}
