/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.util.ArrayList;
import java.util.Arrays;

import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.ScriptTuple;

/**
 * @author sean.cox
 *
 */
public class ScriptTupleImplementation implements ScriptTuple {
	private BlockTuple[] blockTuples;
	private ScriptContext context;

	/**
	 * @param context 
	 * @param blockTuples
	 */
	public ScriptTupleImplementation(ScriptContext context, BlockTuple[] blockTuples) {
		super();
		this.context = context;
		this.blockTuples = blockTuples;
	}

	@Override
	public java.util.List<BlockTuple> getBlockTuples() {
		return new ArrayList<>(Arrays.asList(blockTuples));
	}
	
	@Override
	public BlockTuple getBlockTuple(int index) {
		return blockTuples[index];
	}
	
	@Override
	public int getBlockTupleCount() {
		return blockTuples.length;
	}

	@Override
	public ScriptContext getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Tuple#toArray()
	 */
	@Override
	public Object[] toArray() {
		return Arrays.copyOf(blockTuples, blockTuples.length);
	}
}
