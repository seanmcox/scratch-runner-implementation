/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.util.Arrays;

import com.shtick.utils.scratch.runner.core.elements.BlockTuple;

/**
 * @author sean.cox
 *
 */
public class BlockTupleImplementation implements BlockTuple{
	private String opcode;
	private Object[] arguments;
	
	/**
	 * @param opcode
	 * @param arguments
	 */
	public BlockTupleImplementation(String opcode, Object[] arguments) {
		super();
		this.opcode = opcode;
		this.arguments = arguments;
	}

	@Override
	public String getOpcode() {
		return opcode;
	}

	@Override
	public Object[] getArguments() {
		return Arrays.copyOf(arguments,arguments.length);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Tuple#toArray()
	 */
	@Override
	public Object[] toArray() {
		Object[] retval = new Object[arguments.length+1];
		retval[0] = opcode;
		System.arraycopy(arguments, 0, retval, 1, arguments.length);
		return retval;
	}
}
