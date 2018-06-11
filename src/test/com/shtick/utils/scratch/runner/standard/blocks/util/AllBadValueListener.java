/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.ValueListener;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;

/**
 * @author Sean
 *
 */
public class AllBadValueListener implements ValueListener {
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getRuntime()
	 */
	@Override
	public ScratchRuntime getRuntime() {
		throw new UnsupportedOperationException("Called getRuntime when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getScriptRunner()
	 */
	@Override
	public ScriptTupleRunner getScriptRunner() {
		throw new UnsupportedOperationException("Called getScriptRunner when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getScriptContext()
	 */
	@Override
	public ScriptContext getScriptContext() {
		throw new UnsupportedOperationException("Called getScriptContext when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getArguments()
	 */
	@Override
	public Object[] getArguments() {
		throw new UnsupportedOperationException("Called getArguments when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#valueUpdated(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void valueUpdated(Object oldValue, Object newValue) {
		throw new UnsupportedOperationException("Called valueUpdated when not expected.");
	}
}
