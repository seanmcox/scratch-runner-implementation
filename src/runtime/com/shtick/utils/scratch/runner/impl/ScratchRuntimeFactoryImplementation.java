/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.io.File;
import java.io.IOException;

import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory;

/**
 * @author scox
 *
 */
public class ScratchRuntimeFactoryImplementation implements ScratchRuntimeFactory {

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#createScratchRuntime(java.io.File)
	 */
	@Override
	public ScratchRuntime createScratchRuntime(File projectFile) throws IOException{
		return new ScratchRuntimeImplementation(projectFile, 480, 360);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#createScratchRuntime(java.io.File, int, int)
	 */
	@Override
	public ScratchRuntime createScratchRuntime(File projectFile, int stageWidth, int stageHeight) throws IOException{
		return new ScratchRuntimeImplementation(projectFile, stageWidth, stageHeight);
	}

}
