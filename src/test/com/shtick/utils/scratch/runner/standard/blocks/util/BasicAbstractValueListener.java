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
public abstract class BasicAbstractValueListener implements ValueListener {
	private ScratchRuntime runtime;
	private ScriptTupleRunner runner;
	private ScriptContext context;
	private Object[] arguments;

	/**
	 * @param runtime
	 * @param runner
	 * @param context
	 * @param arguments
	 */
	public BasicAbstractValueListener(ScratchRuntime runtime, ScriptTupleRunner runner, ScriptContext context,
			Object[] arguments) {
		super();
		this.runtime = runtime;
		this.runner = runner;
		this.context = context;
		this.arguments = arguments;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getRuntime()
	 */
	@Override
	public ScratchRuntime getRuntime() {
		return runtime;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getScriptRunner()
	 */
	@Override
	public ScriptTupleRunner getScriptRunner() {
		return runner;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getScriptContext()
	 */
	@Override
	public ScriptContext getScriptContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#getArguments()
	 */
	@Override
	public Object[] getArguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ValueListener#valueUpdated(java.lang.Object, java.lang.Object)
	 */
	@Override
	public abstract void valueUpdated(Object oldValue, Object newValue);
}
