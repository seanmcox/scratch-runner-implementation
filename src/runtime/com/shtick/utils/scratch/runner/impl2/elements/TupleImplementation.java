/**
 * 
 */
package com.shtick.utils.scratch.runner.impl2.elements;

import java.util.Arrays;

import com.shtick.utils.scratch.runner.core.elements.Tuple;

/**
 * This is intended to be a catch-all Tuple which will be used to encode tuple data that does not meet the requirements of any other defined Tuple. 
 * 
 * @author sean.cox
 *
 */
public class TupleImplementation implements Tuple{
	private Object[] data;

	/**
	 * @param data
	 */
	public TupleImplementation(Object[] data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Tuple#toArray()
	 */
	@Override
	public Object[] toArray() {
		return Arrays.copyOf(data, data.length);
	}
}
