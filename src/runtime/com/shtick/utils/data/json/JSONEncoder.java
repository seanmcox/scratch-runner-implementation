/**
 * 
 */
package com.shtick.utils.data.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sean.cox
 *
 */
public class JSONEncoder {
	private static HashMap<Class<?>,SupplementalJSONEncoder<?>> supplementalEncoders = new HashMap<>();
	
	/**
	 * 
	 * @param encoder
	 */
	public static void registerSupplementalJSONEncoder(SupplementalJSONEncoder<?> encoder){
		supplementalEncoders.put(encoder.getEncodedType(), encoder);
	}

	/**
	 * @param value Must be a String, Map&lt;String,?>, array, Boolean, Integer, Float, null, or Double.
	 * @return JSON-encoded version of value
	 */
	public static String encode(Object value){
		if(value == null)
			return "null";
		if(value instanceof String)
			return encode((String)value);
		if(value instanceof Map<?,?>)
			return encode((Map<?,?>)value);
		if(value instanceof Object[])
			return encode((Object[])value);
		if(value instanceof Collection<?>)
			return encode(((Collection<?>)value).toArray());
		if((value instanceof Boolean)||(value instanceof Integer)||(value instanceof Long)||(value instanceof Float)||(value instanceof Double))
			return value.toString();
		SupplementalJSONEncoder<?> supplemental = supplementalEncoders.get(value.getClass());
		if(supplemental!=null)
			return supplemental.encode(value);
		throw new IllegalArgumentException("Type of value to encode not recognized.");
	}

	/**
	 * @param value A string
	 * @return JSON-encoded version of value
	 */
	public static String encode(String value){
		return "\""+value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\b", "\\b").replace("\f", "\\f").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")+"\"";
	}

	/**
	 * @param value Must be a Map&lt;String,?>
	 * @return JSON-encoded version of value
	 */
	public static String encode(Map<?,?> value){
		String retval = "";
		for(Object key:value.keySet()){
			if(!(key instanceof String))
				throw new IllegalArgumentException("Map used non-string as key.");
			if(retval.length()>0)
				retval+=",";
			retval+=encode((String)key)+":"+encode(value.get(key));
		}
		return "{"+retval+"}";
	}

	/**
	 * @param value
	 * @return JSON-encoded version of value
	 */
	public static String encode(Object[] value){
		String retval = "";
		for(Object val:value){
			if(retval.length()>0)
				retval+=",";
			retval+=encode(val);
		}
		return "["+retval+"]";
	}
}
