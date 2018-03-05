/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.util.ArrayList;
import java.util.Collections;

import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.List;

/**
 * @author sean.cox
 *
 */
public class BlockTupleImplementation implements BlockTuple{
	private String opcode;
	private java.util.List<Object> arguments;
	
	/**
	 * @param opcode
	 * @param arguments
	 */
	public BlockTupleImplementation(String opcode, ArrayList<Object> arguments) {
		super();
		this.opcode = opcode;
		this.arguments = Collections.unmodifiableList(arguments);
	}

	@Override
	public String getOpcode() {
		return opcode;
	}

	@Override
	public java.util.List<Object> getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Tuple#toArray()
	 */
	@Override
	public Object[] toArray() {
		Object[] retval = new Object[arguments.size()+1];
		retval[0] = opcode;
		System.arraycopy(arguments, 0, retval, 1, arguments.size());
		return retval;
	}
}
