/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import java.util.List;

import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.ScriptTuple;

/**
 * @author Sean
 *
 */
public class AllBadScriptTuple implements ScriptTuple{

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Tuple#toArray()
	 */
	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Called toArray when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptTuple#getBlockTuples()
	 */
	@Override
	public List<BlockTuple> getBlockTuples() {
		throw new UnsupportedOperationException("Called getBlockTuples when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptTuple#getBlockTuple(int)
	 */
	@Override
	public BlockTuple getBlockTuple(int index) {
		throw new UnsupportedOperationException("Called getBlockTuple when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptTuple#getBlockTupleCount()
	 */
	@Override
	public int getBlockTupleCount() {
		throw new UnsupportedOperationException("Called getBlockTupleCount when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptTuple#getContext()
	 */
	@Override
	public ScriptContext getContext() {
		throw new UnsupportedOperationException("Called getContext when not expected.");
	}

	@Override
	public ScriptTuple clone(ScriptContext context) {
		throw new UnsupportedOperationException("Called clone when not expected.");
	}

}
