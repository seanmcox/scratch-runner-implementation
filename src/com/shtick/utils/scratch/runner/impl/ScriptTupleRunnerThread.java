/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import com.shtick.utils.scratch.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.Opcode.DataType;
import com.shtick.utils.scratch.runner.core.OpcodeAction;
import com.shtick.utils.scratch.runner.core.OpcodeControl;
import com.shtick.utils.scratch.runner.core.OpcodeHat;
import com.shtick.utils.scratch.runner.core.OpcodeUtils;
import com.shtick.utils.scratch.runner.core.OpcodeValue;
import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.ScriptTuple;
import com.shtick.utils.scratch.runner.core.elements.control.BasicJumpBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.ChangeLocalVarByBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.ControlBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.FalseJumpBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.JumpBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.LocalVarBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.ReadLocalVarBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.SetLocalVarBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.TestBlockTuple;
import com.shtick.utils.scratch.runner.core.elements.control.TrueJumpBlockTuple;
import com.shtick.utils.scratch.runner.impl.bundle.Activator;

/**
 * @author sean.cox
 *
 */
public class ScriptTupleRunnerThread extends Thread {
	private static final HashMap<ScriptTuple,BlockTuple[]> resolvedScripts = new HashMap<>();
	private static final HashMap<ScriptTuple,Integer> resolvedScriptLocalVariableCounts = new HashMap<>();
	private ScriptTuple scriptTuple;
	private boolean stop = false;
	private int instructionDelayMillis;
	private boolean isAtomic=false;
	private Opcode currentOpcode = null;
	private Object[] localVariables;
	private boolean testResult;
	
	/**
	 * @param threadGroup 
	 * @param scriptTuple
	 * @param instructionDelayMillis 
	 * @param isAtomic 
	 */
	public ScriptTupleRunnerThread(ThreadGroup threadGroup, ScriptTuple scriptTuple, int instructionDelayMillis, boolean isAtomic) {
		super(threadGroup,"ScriptTupleRunner");
		this.scriptTuple = scriptTuple;
		this.instructionDelayMillis = instructionDelayMillis;
		this.isAtomic = isAtomic;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		try {
			Object methodRetval = scriptTuple.getContext().getClass().getMethod("getProcName").invoke(scriptTuple.getContext());
			if(methodRetval.toString().equals("Fill Random %n")) {
				System.out.println("***** Filling random Start");
				System.out.flush();
			}
		}
		catch(InvocationTargetException|NoSuchMethodException|IllegalAccessException t) {}
		try {
			if(!resolvedScripts.containsKey(scriptTuple)) {
				resolveScript(scriptTuple);
			}
			localVariables = new Object[resolvedScriptLocalVariableCounts.get(scriptTuple)];
			runBlockTuples(scriptTuple.getContext(), resolvedScripts.get(scriptTuple));
		}
		catch(InvalidScriptDefinitionException t) {
			throw new RuntimeException(t);
		}
		try {
			Object methodRetval = scriptTuple.getContext().getClass().getMethod("getProcName").invoke(scriptTuple.getContext());
			if(methodRetval.toString().equals("Fill Random %n")) {
				System.out.println("***** Fill random runtime (ms): "+(System.currentTimeMillis()-startTime));
				System.out.flush();
			}
		}
		catch(InvocationTargetException|NoSuchMethodException|IllegalAccessException t) {}
	}
	
	/**
	 * 
	 */
	public void flagStop() {
		stop=true;
	}
	
	/**
	 * 
	 * @return true if this thread is running atomically, and false otherwise.
	 */
	public boolean isAtomic() {
		return isAtomic;
	}
	
	private void resolveScript(ScriptTuple scriptTuple) throws InvalidScriptDefinitionException {
		LinkedList<BlockTuple> resolvedScript = new LinkedList<>();
		int largestLocalVariableIndex = resolveScript(scriptTuple.getBlockTuples(),resolvedScript,0,0);
		resolvedScripts.put(scriptTuple, resolvedScript.toArray(new BlockTuple[resolvedScript.size()]));
		resolvedScriptLocalVariableCounts.put(scriptTuple, largestLocalVariableIndex+1);
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
		}
		int largestRemappedLocalVarIncludingChildren = largestRemappedLocalVar;

		// Make note of all jumps in order to facilitate index adjustments.
		for(BlockTuple blockTuple:blockTuples) {
			if(blockTuple instanceof JumpBlockTuple)
				jumpBlockTuples.add((JumpBlockTuple)blockTuple);
		}
		// Make initial start index adjustment
		for(JumpBlockTuple jumpBlockTuple:jumpBlockTuples)
			jumpBlockTuple.setIndex(jumpBlockTuple.getIndex()+startIndex);
		
		int i = startIndex;
		// Process block tuples, inflating control blocks.
		for(BlockTuple blockTuple:blockTuples) {
			String opcode = blockTuple.getOpcode();
			Opcode opcodeImplementation = Activator.OPCODE_TRACKER.getOpcode(opcode);
			if(opcodeImplementation instanceof OpcodeControl) {
				BlockTuple[] resolvedControl = ((OpcodeControl)opcodeImplementation).execute(blockTuple.getArguments());
				largestRemappedLocalVarIncludingChildren = Math.max(largestRemappedLocalVarIncludingChildren, resolveScript(Arrays.asList(resolvedControl),resolvedScript,i, largestRemappedLocalVar+1));
				int adjustment = resolvedControl.length-1;
				// Adjust jumps to points after this block inflation.
				for(JumpBlockTuple jumpBlockTuple:jumpBlockTuples) {
					if(jumpBlockTuple.getIndex()>i) {
						jumpBlockTuple.setIndex(jumpBlockTuple.getIndex()+adjustment);
					}
				}
				i+=adjustment;
			}
			else if(opcodeImplementation instanceof OpcodeHat) {
				if(i==0)
					continue;
				throw new InvalidScriptDefinitionException("OpcodeHat found in the middle of a script.");
			}
			else {
				resolvedScript.add(blockTuple);
			}
			i++;
		}
		return largestRemappedLocalVarIncludingChildren;
	}

	/**
	 * 
	 * @param context
	 * @param blockTuples The results of resolveScript(), so this shouldn't include any ControlBlockTuple elements.
	 * @throws InvalidScriptDefinitionException
	 */
	private void runBlockTuples(ScriptContext context, BlockTuple[] blockTuples) throws InvalidScriptDefinitionException{
		ScriptTupleRunnerImpl scriptRunner = new ScriptTupleRunnerImpl(this);
		int blockTupleIndex=0;
		try {
			while((blockTupleIndex<blockTuples.length)&&(!stop)) {
				if((!isAtomic)&&(instructionDelayMillis>0)) {
					try {
						Thread.sleep(instructionDelayMillis);
					}
					catch(InterruptedException t) {}
				}
				BlockTuple tuple = blockTuples[blockTupleIndex];
				if(tuple instanceof ControlBlockTuple) {
					if(tuple instanceof TestBlockTuple) {
						testResult = (Boolean)getValue(context,tuple.getArguments()[0]);
					}
					else if(tuple instanceof BasicJumpBlockTuple) {
						blockTupleIndex = ((JumpBlockTuple)tuple).getIndex();
						continue;
					}
					else if(tuple instanceof TrueJumpBlockTuple) {
						if(testResult) {
							blockTupleIndex = ((JumpBlockTuple)tuple).getIndex();
							continue;
						}
					}
					else if(tuple instanceof FalseJumpBlockTuple) {
						if(!testResult) {
							blockTupleIndex = ((JumpBlockTuple)tuple).getIndex();
							continue;
						}
					}
					else if(tuple instanceof SetLocalVarBlockTuple) {
						localVariables[((LocalVarBlockTuple)tuple).getLocalVarIdentifier()] = tuple.getArguments()[1];
					}
					else if(tuple instanceof ChangeLocalVarByBlockTuple) {
						Number value = OpcodeUtils.getNumericValue(localVariables[((LocalVarBlockTuple)tuple).getLocalVarIdentifier()]);
						Number change = OpcodeUtils.getNumericValue(getValue(context,tuple.getArguments()[1]));
						if((value instanceof Double)||(value instanceof Double))
							value = value.doubleValue()+change.doubleValue();
						else
							value = value.longValue()+change.longValue();
						localVariables[((LocalVarBlockTuple)tuple).getLocalVarIdentifier()] = value;
					}
					blockTupleIndex++;
					continue;
				}
				// TODO Move type/safety checking below to resolveScript. (Opcode value implementations will need to report a return type.)
				//      The type checking at that point would be less comprehensive, probably, but this seems to be the direction I need to go to improve performance.
				String opcode = tuple.getOpcode();
				try {
					Object methodRetval = context.getClass().getMethod("getProcName").invoke(context);
					if(methodRetval.toString().equals("Fill Horizontal Line %s %n %n %n %n")||methodRetval.toString().startsWith("Fill Circle")||methodRetval.toString().startsWith("Init Caves")||methodRetval.toString().startsWith("Make Seams")) {
						System.out.println("***** "+opcode);
						System.out.flush();
					}
				}
				catch(InvocationTargetException|NoSuchMethodException|IllegalAccessException t) {}
				Object[] arguments = tuple.getArguments();
				ScratchRuntime runtime = ScratchRuntimeImplementation.getScratchRuntime();
				Opcode opcodeImplementation = Activator.OPCODE_TRACKER.getOpcode(opcode);
				if(opcodeImplementation == null)
					throw new InvalidScriptDefinitionException("Unrecognized opcode: "+opcode);
				DataType[] types = opcodeImplementation.getArgumentTypes();
				if(types.length!=arguments.length)
					if(!((types.length-1<=arguments.length)&&(types[types.length-1]==Opcode.DataType.OBJECTS)))
						throw new InvalidScriptDefinitionException("Invalid arguments found for opcode, "+opcode);
				if(opcodeImplementation instanceof OpcodeValue)
					throw new InvalidScriptDefinitionException("Attempted to execute value opcode: "+opcode);
				if(opcodeImplementation instanceof OpcodeAction) {
					for(int i=0;i<types.length;i++) {
						switch(types[i]) {
						case BOOLEAN:
							arguments[i] = getValue(context,arguments[i]);
							
							if(!(arguments[i] instanceof Boolean))
								throw new InvalidScriptDefinitionException("Non-tuple provided where tuple expected.");
							break;
						case NUMBER:
							arguments[i] = getValue(context,arguments[i]);
							arguments[i] = OpcodeUtils.getNumericValue(arguments[i]);
							break;
						case OBJECT:
							arguments[i] = getValue(context,arguments[i]);
							if(!((arguments[i] instanceof Boolean)||(arguments[i] instanceof Number)||(arguments[i] instanceof String)))
								throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
							break;
						case OBJECTS:
							Object[] newArguments = new Object[types.length];
							System.arraycopy(arguments, 0, newArguments, 0, types.length-1);
							Object[] objects = new Object[arguments.length-types.length+1];
							for(int j=0;j<objects.length;j++) {
								objects[j] = getValue(context,arguments[i+j]);
								if(!((objects[j] instanceof Boolean)||(objects[j] instanceof Number)||(objects[j] instanceof String)))
									throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
							}
							arguments = newArguments;
							arguments[i] = objects;
							break;
						case STRING:
							arguments[i] = getValue(context,arguments[i]);
							arguments[i] = OpcodeUtils.getStringValue(arguments[i]);
							break;
						case SCRIPT:
							if(!(arguments[i] instanceof ScriptTuple))
								throw new InvalidScriptDefinitionException("Non-script-tuple provided where script tuple expected.");
							break;
						default:
							throw new RuntimeException("Unhandled DataType, "+types[i].name()+", in method signature for opcode, "+opcode);
						}
					}
					currentOpcode = opcodeImplementation;
					((OpcodeAction)opcodeImplementation).execute(runtime, scriptRunner, context, arguments);
					blockTupleIndex++;
				}
			}
			currentOpcode = null;
		}
		finally {
			scriptRunner.thread = null;
		}
	}
	
	private Object getValue(ScriptContext context, Object object) throws InvalidScriptDefinitionException {
		if(object instanceof BlockTuple)
			return getBlockTupleValue(context, (BlockTuple)object);
		try {
			Object methodRetval = context.getClass().getMethod("getProcName").invoke(context);
			if(methodRetval.toString().equals("Fill Horizontal Line %s %n %n %n %n")||methodRetval.toString().startsWith("Fill Circle")||methodRetval.toString().startsWith("Init Caves")||methodRetval.toString().startsWith("Make Seams")) {
				System.out.println("VVVVV "+object);
				System.out.flush();
			}
		}
		catch(InvocationTargetException|NoSuchMethodException|IllegalAccessException t) {}
		return object;
	}
	
	private Object getBlockTupleValue(ScriptContext context, BlockTuple tuple) throws InvalidScriptDefinitionException{
		if(tuple instanceof ReadLocalVarBlockTuple)
			return localVariables[((ReadLocalVarBlockTuple)tuple).getLocalVarIdentifier()];
		ScriptTupleRunnerImpl scriptRunner = new ScriptTupleRunnerImpl(this);
		Object[] arguments = tuple.getArguments();
		ScratchRuntime runtime = ScratchRuntimeImplementation.getScratchRuntime();
		Opcode opcodeImplementation = getOpcode(tuple);
		if(opcodeImplementation == null)
			throw new InvalidScriptDefinitionException("Unrecognized value resolving opcode: "+tuple.getOpcode());
		if(!(opcodeImplementation instanceof OpcodeValue))
			throw new InvalidScriptDefinitionException("Attempted to evaluate non-value opcode: "+tuple.getOpcode());
		DataType[] types = opcodeImplementation.getArgumentTypes();
		if(types.length!= arguments.length)
			throw new InvalidScriptDefinitionException("Invalid arguments found for opcode, "+tuple.getOpcode());
		for(int i=0;i<arguments.length;i++) {
			if(types[i]!=Opcode.DataType.TUPLE)
				arguments[i] = getValue(context,arguments[i]);
			switch(types[i]) {
			case BOOLEAN:
				if(!(arguments[i] instanceof Boolean))
					throw new InvalidScriptDefinitionException("Non-tuple provided where tuple expected.");
				break;
			case NUMBER:
				arguments[i] = OpcodeUtils.getNumericValue(arguments[i]);
				break;
			case OBJECT:
				if(!((arguments[i] instanceof Boolean)||(arguments[i] instanceof Number)||(arguments[i] instanceof String)))
					throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
				break;
			case STRING:
				arguments[i] = OpcodeUtils.getStringValue(arguments[i]);
				break;
			default:
				throw new RuntimeException("Unhandled DataType, "+types[i].name()+", in method signature for opcode, "+opcodeImplementation.getOpcode());
			}
		}
		Object retval = ((OpcodeValue)opcodeImplementation).execute(runtime, scriptRunner, context, arguments);
		try {
			Object methodRetval = context.getClass().getMethod("getProcName").invoke(context);
			if(methodRetval.toString().equals("Fill Horizontal Line %s %n %n %n %n")||methodRetval.toString().startsWith("Fill Circle")||methodRetval.toString().startsWith("Init Caves")||methodRetval.toString().startsWith("Make Seams")) {
				System.out.println("rrrrr "+retval+" | "+tuple.getOpcode());
				System.out.flush();
			}
		}
		catch(InvocationTargetException|NoSuchMethodException|IllegalAccessException t) {}
		return retval;
	}

	private static Opcode getOpcode(BlockTuple blockTuple) {
		return Activator.OPCODE_TRACKER.getOpcode(blockTuple.getOpcode());
	}

	/**
	 * 
	 * @return A ScriptTupleRunner object for accessing this Thread.
	 */
	public ScriptTupleRunner getScriptTupleRunner() {
		return new ScriptTupleRunnerImpl(this);
	}
	
	/**
	 * A class for helping to encapsulate the thread. (Not completely doable, since the thread is always accessible as Thread.currentThread())
	 * 
	 * @author sean.cox
	 *
	 */
	private static class ScriptTupleRunnerImpl implements ScriptTupleRunner{
		private ScriptTupleRunnerThread thread;
		
		/**
		 * @param thread
		 */
		public ScriptTupleRunnerImpl(ScriptTupleRunnerThread thread) {
			super();
			this.thread = thread;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#flagStop()
		 */
		@Override
		public void flagStop() {
			thread.flagStop();
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#isStopFlagged()
		 */
		@Override
		public boolean isStopFlagged() {
			return thread.stop;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#runScript(com.shtick.utils.scratch.runner.core.elements.ScriptTuple)
		 */
		@Override
		public void runBlockTuples(ScriptContext context, java.util.List<BlockTuple> script) throws InvalidScriptDefinitionException{
			// TODO Completely remove this. Scripts are now reporting their child code blocks and logic rather than calling this function. ScriptTupleRunner may be eliminated as a core object passed in to opcodes.
//			if((script==null)||(script.size()==0)) {
//				if((!thread.isAtomic)&&(thread.instructionDelayMillis>0)) {
//					try {
//						Thread.sleep(thread.instructionDelayMillis);
//					}
//					catch(InterruptedException t) {}
//				}
//				return;
//			}
//			thread.runBlockTuples(context, script.iterator());
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#getOpcode(com.shtick.utils.scratch.runner.core.elements.BlockTuple)
		 */
		@Override
		public Opcode getOpcode(BlockTuple blockTuple) {
			return ScriptTupleRunnerThread.getOpcode(blockTuple);
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#getCurrentOpcode()
		 */
		@Override
		public Opcode getCurrentOpcode() {
			return thread.currentOpcode;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#isAtomic()
		 */
		@Override
		public boolean isAtomic() {
			return thread.isAtomic;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#join(long, int)
		 */
		@Override
		public void join(long millis, int nanos) throws InterruptedException {
			thread.join(millis,nanos);
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#join()
		 */
		@Override
		public void join() throws InterruptedException {
			thread.join();
		}
	}
}
