/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.shtick.utils.scratch.runner.core.FeatureSet;
import com.shtick.utils.scratch.runner.core.GraphicEffect;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.StageMonitorCommand;

/**
 * @author scox
 *
 */
public class AmalgamatedFeatureSet implements FeatureSet {
	private HashMap<String, GraphicEffect> graphicEffects = new HashMap<>();
	private HashMap<String, Opcode> opcodes = new HashMap<>();
	private HashMap<String, StageMonitorCommand> stageMonitorCommands = new HashMap<>();
	
	/**
	 * 
	 * @param featureSets
	 */
	public AmalgamatedFeatureSet(FeatureSet ... featureSets) {
		for(FeatureSet featureSet:featureSets) {
			for(Opcode opcode:featureSet.getOpcodes())
				opcodes.put(opcode.getOpcode(), opcode);
			for(GraphicEffect graphicEffect:featureSet.getGraphicEffects())
				graphicEffects.put(graphicEffect.getName(), graphicEffect);
			for(StageMonitorCommand stageMonitorCommand:featureSet.getStageMonitorCommands())
				stageMonitorCommands.put(stageMonitorCommand.getCommand(), stageMonitorCommand);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getOpcode(java.lang.String)
	 */
	@Override
	public Opcode getOpcode(String opcode) {
		return opcodes.get(opcode);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getOpcodes()
	 */
	@Override
	public Set<Opcode> getOpcodes() {
		return new HashSet<>(opcodes.values());
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getGraphicEffect(java.lang.String)
	 */
	@Override
	public GraphicEffect getGraphicEffect(String id) {
		return graphicEffects.get(id);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getGraphicEffects()
	 */
	@Override
	public Set<GraphicEffect> getGraphicEffects() {
		return new HashSet<>(graphicEffects.values());
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getStageMonitorCommand(java.lang.String)
	 */
	@Override
	public StageMonitorCommand getStageMonitorCommand(String id) {
		return stageMonitorCommands.get(id);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.FeatureSet#getStageMonitorCommands()
	 */
	@Override
	public Set<StageMonitorCommand> getStageMonitorCommands() {
		return new HashSet<>(stageMonitorCommands.values());
	}

}
