/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import com.shtick.utils.scratch.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.OpcodeControl;
import com.shtick.utils.scratch.runner.core.OpcodeHat;
import com.shtick.utils.scratch.runner.core.OpcodeValue;
import com.shtick.utils.scratch.runner.core.Opcode.DataType;
import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.ScriptTuple;
import com.shtick.utils.scratch.runner.core.elements.control.ControlBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.JumpBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.LocalVarBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.ReadLocalVarBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.TestBlockTuple;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class ScriptTupleImplementation implements ScriptTuple {
	private ScriptContext context;
	private CloneableData cloneableData;
	private ScratchRuntimeImplementation runtime;

	/**
	 * @param context 
	 * @param blockTuples
	 * @param runtime 
	 * @throws InvalidScriptDefinitionException 
	 */
	public ScriptTupleImplementation(ScriptContext context, BlockTuple[] blockTuples, ScratchRuntimeImplementation runtime) throws InvalidScriptDefinitionException{
		super();
		this.context = context;
		this.cloneableData = new CloneableData();
		this.cloneableData.blockTuples = blockTuples;
		this.runtime = runtime;
		resolveScript();
	}
	
	private ScriptTupleImplementation(ScriptContext context, CloneableData cloneableData) {
		this.context = context;
		this.cloneableData = cloneableData;
	}

	@Override
	public ScriptTuple clone(ScriptContext context) {
		return new ScriptTupleImplementation(context,cloneableData);
	}
	
	/**
	 * 
	 * @return A representation of the script in which all control opcodes have been resolved.
	 */
	public BlockTuple[] getResolvedBlockTuples() {
		return cloneableData.resolvedBlockTuples;
	}

	/**
	 * 
	 * @return The number of local variables needed to run the resolved script.
	 */
	public int getLocalVariableCount() {
		return cloneableData.localVariableCount;
	}

	private void resolveScript() throws InvalidScriptDefinitionException {
		LinkedList<BlockTuple> resolvedScript = new LinkedList<>();
		try {
			int largestLocalVariableIndex = resolveScript(Arrays.asList(cloneableData.blockTuples),resolvedScript,0,0);
			cloneableData.resolvedBlockTuples = resolvedScript.toArray(new BlockTuple[resolvedScript.size()]);
			cloneableData.localVariableCount = largestLocalVariableIndex+1;
		}
		catch(InvalidScriptDefinitionException t) {
			throw new InvalidScriptDefinitionException(t.getMessage()+", hat="+cloneableData.blockTuples[0].getOpcode(),t);
		}
	}

	/**
	 * @param blockTuples
	 * @param resolvedScript
	 * @param startIndex
	 * @param firstAvailableLocalVar The next thread-local variable index available to this block of code.
	 * @return The index of the largest local variable used. (firstAvailableLocalVar-1 if no local variables were used.)
	 * @throws InvalidScriptDefinitionException
	 */
	private int resolveScript(java.util.List<BlockTuple> blockTuples, LinkedList<BlockTuple> resolvedScript, int startIndex, int firstAvailableLocalVar) throws InvalidScriptDefinitionException {
		// TODO Do some type safety checking.
		LinkedList<JumpBlockTuple> jumpBlockTuples = new LinkedList<>();
		HashMap<Integer,Integer> localVariableMap = new HashMap<>();
		
		// Adjust local variable indexes and return the largest index
		int largestRemappedLocalVar = firstAvailableLocalVar-1;
		for(BlockTuple blockTuple:blockTuples) {
			if(blockTuple instanceof LocalVarBlockTuple) {
				int varIndex = ((LocalVarBlockTuple)blockTuple).getLocalVarIdentifier();
				if(!localVariableMap.containsKey(varIndex)) {
					largestRemappedLocalVar++;
					localVariableMap.put(varIndex, largestRemappedLocalVar);
				}
				((LocalVarBlockTuple)blockTuple).setLocalVarIdentifier(localVariableMap.get(varIndex));
			}
			else if(blockTuple instanceof TestBlockTuple) {
				HashSet<LocalVarBlockTuple> localVarBlockTuples = new HashSet<>();
				HashSet<BlockTuple> newBlockTuples = new HashSet<>();
				HashSet<BlockTuple> oldBlockTuples = new HashSet<>();
				oldBlockTuples.add(blockTuple);
				while(oldBlockTuples.size()>0) {
					for(BlockTuple oldBlockTuple:oldBlockTuples) {
						for(Object argument:oldBlockTuple.getArguments()) {
							if(argument instanceof ReadLocalVarBlockTuple) {
								localVarBlockTuples.add((ReadLocalVarBlockTuple)argument);
							}
							else if(argument instanceof BlockTuple) {
								newBlockTuples.add((BlockTuple)argument);
							}
						}
					}
					oldBlockTuples = newBlockTuples;
					newBlockTuples = new HashSet<>();
				}
				for(LocalVarBlockTuple localVarBlockTuple:localVarBlockTuples) {
					int varIndex = localVarBlockTuple.getLocalVarIdentifier();
					if(!localVariableMap.containsKey(varIndex)) {
						largestRemappedLocalVar++;
						localVariableMap.put(varIndex, largestRemappedLocalVar);
					}
					localVarBlockTuple.setLocalVarIdentifier(localVariableMap.get(varIndex));
				}
			}
		}
		int largestRemappedLocalVarIncludingChildren = largestRemappedLocalVar;

		// Make note of all jumps in order to facilitate index adjustments.
		for(BlockTuple blockTuple:blockTuples) {
			if(blockTuple instanceof JumpBlockTuple)
				jumpBlockTuples.add((JumpBlockTuple)blockTuple);
		}
		// Make initial start index adjustment
		for(JumpBlockTuple jumpBlockTuple:jumpBlockTuples) {
			jumpBlockTuple.setIndex(jumpBlockTuple.getIndex()+startIndex);
		}
		
		int i = startIndex;
		// Process block tuples, inflating control blocks.
		for(BlockTuple blockTuple:blockTuples) {
			String opcode = blockTuple.getOpcode();
			Opcode opcodeImplementation = runtime.getFeatureSet().getOpcode(opcode);
			if(opcodeImplementation instanceof OpcodeControl) {
				BlockTuple[] resolvedControl = ((OpcodeControl)opcodeImplementation).execute(blockTuple.getArguments());
				largestRemappedLocalVarIncludingChildren = Math.max(
						largestRemappedLocalVarIncludingChildren,
						resolveScript(Arrays.asList(resolvedControl),resolvedScript,i, largestRemappedLocalVar+1)
				);
				int adjustment = resolvedScript.size()-i-1;
				// Adjust jumps to points after this block inflation.
				for(JumpBlockTuple jumpBlockTuple:jumpBlockTuples) {
					if(jumpBlockTuple.getIndex()>i) {
						jumpBlockTuple.setIndex(jumpBlockTuple.getIndex()+adjustment);
					}
				}
				i=resolvedScript.size();
			}
			else if(opcodeImplementation instanceof OpcodeHat) {
				if(i==0)
					continue;
				throw new InvalidScriptDefinitionException("OpcodeHat found in the middle of a script. Context = "+context.getObjName());
			}
			else if(blockTuple instanceof ControlBlockTuple) {
				// There is not implementation to check against.
				resolvedScript.add(blockTuple);
				i++;
			}
			else {
				if(opcodeImplementation == null)
					throw new InvalidScriptDefinitionException("Unrecognized opcode: "+opcode);
				if(opcodeImplementation instanceof OpcodeValue)
					throw new InvalidScriptDefinitionException("Attempted to execute value opcode. Opcode = "+opcode+", Context = "+context.getObjName());
				DataType[] types = opcodeImplementation.getArgumentTypes();
				java.util.List<Object> arguments = blockTuple.getArguments();
				if(types.length!=arguments.size())
					if(!((types.length-1<=arguments.size())&&(types[types.length-1]==Opcode.DataType.OBJECTS)))
						throw new InvalidScriptDefinitionException("Invalid arguments found for opcode. Opcode = "+opcode+", Context = "+context.getObjName());
				resolvedScript.add(blockTuple);
				i++;
			}
		}
		return largestRemappedLocalVarIncludingChildren;
	}

	@Override
	public java.util.List<BlockTuple> getBlockTuples() {
		return new ArrayList<>(Arrays.asList(cloneableData.blockTuples));
	}
	
	@Override
	public BlockTuple getBlockTuple(int index) {
		return cloneableData.blockTuples[index];
	}
	
	@Override
	public int getBlockTupleCount() {
		return cloneableData.blockTuples.length;
	}

	@Override
	public ScriptContext getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Tuple#toArray()
	 */
	@Override
	public Object[] toArray() {
		return Arrays.copyOf(cloneableData.blockTuples, cloneableData.blockTuples.length);
	}
	
	private class CloneableData {
		private BlockTuple[] blockTuples;
		private BlockTuple[] resolvedBlockTuples;
		private int localVariableCount;
	}
}
