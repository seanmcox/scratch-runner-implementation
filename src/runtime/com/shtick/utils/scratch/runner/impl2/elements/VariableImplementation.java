package com.shtick.utils.scratch.runner.impl2.elements;

import com.shtick.utils.scratch.runner.core.elements.Variable;

/**
 * 
 * @author sean.cox
 *
 */
public class VariableImplementation implements Variable{
	private String name;
	private Object value;
	
	/**
	 * @param name
	 * @param value
	 */
	public VariableImplementation(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getValue() {
		return value;
	}
}
