/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import java.io.File;
import java.io.IOException;

import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;

/**
 * @author scox
 *
 */
public class DummyScratchRuntimeImplementation extends ScratchRuntimeImplementation {

	/**
	 * 
	 * @throws IOException
	 */
	public DummyScratchRuntimeImplementation() throws IOException {
		super(null, 480, 360);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation#loadProject(java.io.File)
	 */
	@Override
	protected void loadProject(File file) throws IOException {
	}
}
