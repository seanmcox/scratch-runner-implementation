/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.shtick.utils.scratch.runner.core.FeatureLibrary;
import com.shtick.utils.scratch.runner.core.FeatureSet;
import com.shtick.utils.scratch.runner.core.FeatureSetGenerator;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.impl.AmalgamatedFeatureSet;
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
		this(480, 360, getStandardFeatureSet());
	}

	/**
	 * 
	 * @param stageWidth
	 * @param stageHeight
	 * @param featureSet
	 * @throws IOException
	 */
	public DummyScratchRuntimeImplementation(int stageWidth, int stageHeight,
			FeatureSet featureSet) throws IOException {
		super(null, stageWidth, stageHeight, featureSet);
	}
	
	private static FeatureSet getStandardFeatureSet() {
		Collection<FeatureSetGenerator> generators = FeatureLibrary.getFeatureSetGenerators();
		FeatureSet[] featureSets = new FeatureSet[generators.size()];
		int i = 0;
		for(FeatureSetGenerator generator:generators){
			featureSets[i] = generator.generateFeatureSet();
			i++;
		}
		return new AmalgamatedFeatureSet(featureSets);
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
