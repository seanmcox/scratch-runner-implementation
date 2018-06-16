/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.io.File;
import java.io.IOException;

import com.shtick.utils.scratch.runner.core.GraphicEffectRegistry;
import com.shtick.utils.scratch.runner.core.OpcodeRegistry;
import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory;
import com.shtick.utils.scratch.runner.core.StageMonitorCommandRegistry;

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
		return createScratchRuntime(projectFile, 480, 360);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#createScratchRuntime(java.io.File, int, int)
	 */
	@Override
	public ScratchRuntime createScratchRuntime(File projectFile, int stageWidth, int stageHeight) throws IOException{
		return createScratchRuntime(projectFile, stageWidth, stageHeight, OpcodeRegistry.getOpcodeRegistry(), GraphicEffectRegistry.getGraphicEffectRegistry(), StageMonitorCommandRegistry.getStageMonitorCommandRegistry());
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#createScratchRuntime(java.io.File, int, int, com.shtick.utils.scratch.runner.core.OpcodeRegistry, com.shtick.utils.scratch.runner.core.GraphicEffectRegistry, com.shtick.utils.scratch.runner.core.StageMonitorCommandRegistry)
	 */
	@Override
	public ScratchRuntime createScratchRuntime(File projectFile, int stageWidth, int stageHeight,
			OpcodeRegistry opcodeRegistry, GraphicEffectRegistry graphicEffectRegistry,
			StageMonitorCommandRegistry stageMonitorCommandRegistry) throws IOException {
		return new ScratchRuntimeImplementation(projectFile, stageWidth, stageHeight, opcodeRegistry, graphicEffectRegistry, stageMonitorCommandRegistry);
	}

}
