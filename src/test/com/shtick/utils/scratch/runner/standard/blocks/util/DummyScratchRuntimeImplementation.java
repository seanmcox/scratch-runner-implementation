/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import java.io.File;
import java.io.IOException;

import com.shtick.utils.scratch.runner.core.GraphicEffectRegistry;
import com.shtick.utils.scratch.runner.core.OpcodeRegistry;
import com.shtick.utils.scratch.runner.core.StageMonitorCommandRegistry;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
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
		super(null, 480, 360, OpcodeRegistry.getOpcodeRegistry(), GraphicEffectRegistry.getGraphicEffectRegistry(), StageMonitorCommandRegistry.getStageMonitorCommandRegistry());
	}

	/**
	 * 
	 * @param stageWidth
	 * @param stageHeight
	 * @param opcodeRegistry
	 * @param graphicEffectRegistry
	 * @param stageMonitorCommandRegistry
	 * @throws IOException
	 */
	public DummyScratchRuntimeImplementation(int stageWidth, int stageHeight,
			OpcodeRegistry opcodeRegistry, GraphicEffectRegistry graphicEffectRegistry,
			StageMonitorCommandRegistry stageMonitorCommandRegistry) throws IOException {
		super(null, stageWidth, stageHeight, opcodeRegistry, graphicEffectRegistry, stageMonitorCommandRegistry);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation#loadProject(java.io.File)
	 */
	@Override
	protected void loadProject(File file) throws IOException {
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation#getAllRenderableChildren()
	 */
	@Override
	public RenderableChild[] getAllRenderableChildren() {
		return new RenderableChild[0];
	}
}
