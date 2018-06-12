/**
 * 
 */
package com.shtick.utils.data.json;

/**
 * @author sean.cox
 * @param <T> 
 *
 */
public interface SupplementalJSONEncoder<T> {
	/**
	 * 
	 * @return The type this encoder handles.
	 */
	Class<T> getEncodedType();
	
	/**
	 * 
	 * @param object
	 * @return The encoded object.
	 */
	String encode(Object object);
}
