/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;

/**
 * @author Sean
 *
 */
public class AllBadRunner implements ScriptTupleRunner {

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#flagStop()
	 */
	@Override
	public void flagStop() {
		throw new UnsupportedOperationException("Called flagStop when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#isStopFlagged()
	 */
	@Override
	public boolean isStopFlagged() {
		throw new UnsupportedOperationException("Called isStopFlagged when not expected.");
	}

	@Override
	public boolean isStopped() {
		throw new UnsupportedOperationException("Called isStopped when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#getCurrentOpcode()
	 */
	@Override
	public Opcode getCurrentOpcode() {
		throw new UnsupportedOperationException("Called getCurrentOpcode when not expected.");
	}

	@Override
	public ScriptContext getContext() {
		throw new UnsupportedOperationException("Called getContext when not expected.");
	}

	@Override
	public String getStackTrace() {
		throw new UnsupportedOperationException("Called getStackTrace when not expected.");
	}
}
