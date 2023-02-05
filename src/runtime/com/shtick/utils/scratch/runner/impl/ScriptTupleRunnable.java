/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.util.Arrays;
import java.util.Stack;

import com.shtick.utils.scratch.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.Opcode.DataType;
import com.shtick.utils.scratch.runner.core.OpcodeAction;
import com.shtick.utils.scratch.runner.core.OpcodeSubaction;
import com.shtick.utils.scratch.runner.core.OpcodeUtils;
import com.shtick.utils.scratch.runner.core.OpcodeValue;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
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
import com.shtick.utils.scratch.runner.impl.elements.ScriptTupleImplementation;

/**
 * @author sean.cox
 *
 */
public class ScriptTupleRunnable implements Runnable {
	private ScriptTupleImplementation scriptTuple;
	private Opcode currentOpcode = null;
	private boolean testResult;
	
	private Stack<YieldingScript> callStack = new Stack<>();
	private OpcodeSubaction yieldCheck = null;

	private final Object STOP_LOCK = new Object();
	/**
	 * Used to flag that the currently running script (top of the stack) should be aborted.
	 */
	private boolean stopProcedure = false;
	/**
	 * Used to flag that the entire call stack needs to be aborted, rather than just the currently running script on the top of the stack.
	 */
	private boolean totalStop = false;
	private boolean stopped = false;
	/**
	 * Indicated that the script is actively executing now. (ie. false if yielded or stopped)
	 */
	private boolean running = false;

	private ScriptTupleRunnerImpl scriptRunner;
	private ScratchRuntimeImplementation runtime;

	/**
	 * @param scriptTuple
	 * @param runtime 
	 */
	public ScriptTupleRunnable(ScriptTupleImplementation scriptTuple, ScratchRuntimeImplementation runtime) {
		this.scriptTuple = scriptTuple;
		this.runtime = runtime;
		callStack.push(new YieldingScript(scriptTuple.getContext(), scriptTuple.getResolvedBlockTuples(), scriptTuple.getLocalVariableCount(), false, Arrays.toString(scriptTuple.getBlockTuples().get(0).toArray())));
		this.scriptRunner = new ScriptTupleRunnerImpl(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			runBlockTuples();
		}
		catch(InvalidScriptDefinitionException t) {
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * 
	 * @return A String describing where the process is currently, with newlines separating stack layers.
	 */
	public String getStackTrace() {
		String retval = scriptTuple.getContext().getObjName();
		int i = 0;
		synchronized(callStack) {
			for(YieldingScript task:callStack) {
				i++;
				retval+="\n"+i+": "+task.description+" "+task.index+"/"+task.blockTuples.length+" "+((task.index>=task.blockTuples.length)?"Done":task.blockTuples[task.index].getOpcode())+" "+task.isAtomic;
			}
		}
		return retval;
	}
	
	/**
	 * 
	 * @param totalStop If true, then the entire call stack is aborted. Otherwise, only the top procedure in the stack aborts and returns.
	 */
	public void flagStop(boolean totalStop) {
		this.totalStop = totalStop;
		stopProcedure=true;
		if(totalStop) {
			new Thread(()->{
				synchronized(STOP_LOCK) {
					while(running) {
						try {
							STOP_LOCK.wait(100);
						}
						catch(InterruptedException t) {}
					}
					if(!stopped) {
						stopped = true;
						STOP_LOCK.notifyAll();
						scriptRunner.runnable = null;
					}
				}
			}).start();
		}
	}
	
	/**
	 * 
	 * @return true if the script being run has completed, and false otherwise.
	 */
	public boolean isFinished() {
		return stopped;
	}
	
	/**
	 * 
	 * @param context
	 * @param blockTuples The results of resolveScript(), so this shouldn't include any ControlBlockTuple elements.
	 * @throws InvalidScriptDefinitionException
	 */
	private void runBlockTuples() throws InvalidScriptDefinitionException{
		if(callStack.size()==0)
			return;
		
		// Handle yieldCheck.
		if(yieldCheck!=null) {
			if(yieldCheck.shouldYield())
				return;
			yieldCheck = null;
		}
		
		try {
			synchronized(STOP_LOCK) {
				running = true;
			}
		
			// Run the next script segment.
			while((callStack.size()>0)&&(!totalStop)) {
				YieldingScript yieldingScript = callStack.peek();
				while((yieldingScript.index<yieldingScript.blockTuples.length)&&(!stopProcedure)) {
					BlockTuple tuple = yieldingScript.blockTuples[yieldingScript.index];
					if(yieldingScript.debugFlag)
						System.out.println("Index: "+yieldingScript.index+"/"+yieldingScript.blockTuples.length+" - "+tuple.getOpcode());
					if(tuple instanceof ControlBlockTuple) {
						if(tuple instanceof TestBlockTuple) {
							testResult = (Boolean)getValue(yieldingScript.context,tuple.getArguments().get(0),yieldingScript.localVariables);
							yieldingScript.index++;
							continue;
						}
						else if(tuple instanceof SetLocalVarBlockTuple) {
							yieldingScript.localVariables[((LocalVarBlockTuple)tuple).getLocalVarIdentifier()] = getValue(yieldingScript.context,tuple.getArguments().get(1),yieldingScript.localVariables);
							yieldingScript.index++;
							continue;
						}
						else if(tuple instanceof ChangeLocalVarByBlockTuple) {
							Number value = OpcodeUtils.getNumericValue(yieldingScript.localVariables[((LocalVarBlockTuple)tuple).getLocalVarIdentifier()]);
							Number change = OpcodeUtils.getNumericValue(getValue(yieldingScript.context,tuple.getArguments().get(1),yieldingScript.localVariables));
							if((value instanceof Double)||(value instanceof Double))
								value = value.doubleValue()+change.doubleValue();
							else
								value = value.longValue()+change.longValue();
							yieldingScript.localVariables[((LocalVarBlockTuple)tuple).getLocalVarIdentifier()] = value;
							yieldingScript.index++;
							continue;
						}
						if(tuple instanceof BasicJumpBlockTuple) {
							if((!yieldingScript.isAtomic)&&(((JumpBlockTuple)tuple).getIndex()<yieldingScript.index)) {
								// If the new index is before the old index, then yield after updating the index, unless this function is atomic.
								yieldingScript.index = ((JumpBlockTuple)tuple).getIndex();
								return;
							}
							yieldingScript.index = ((JumpBlockTuple)tuple).getIndex();
							continue;
						}
						else if(tuple instanceof TrueJumpBlockTuple) {
							if(testResult) {
								if((!yieldingScript.isAtomic)&&(((JumpBlockTuple)tuple).getIndex()<yieldingScript.index)) {
									// If the new index is before the old index, then yield after updating the index, unless this function is atomic.
									yieldingScript.index = ((JumpBlockTuple)tuple).getIndex();
									return;
								}
								yieldingScript.index = ((JumpBlockTuple)tuple).getIndex();
								continue;
							}
						}
						else if(tuple instanceof FalseJumpBlockTuple) {
							if(!testResult) {
								if((!yieldingScript.isAtomic)&&(((JumpBlockTuple)tuple).getIndex()<yieldingScript.index)) {
									// If the new index is before the old index, then yield after updating the index, unless this function is atomic.
									yieldingScript.index = ((JumpBlockTuple)tuple).getIndex();
									return;
								}
								yieldingScript.index = ((JumpBlockTuple)tuple).getIndex();
								continue;
							}
						}
						else {
							throw new InvalidScriptDefinitionException("Unrecognized control block: "+tuple.getClass().getCanonicalName());
						}
						yieldingScript.index++;
						continue;
					}
					// TODO Move type/safety checking below to resolveScript. (Opcode value implementations will need to report a return type.)
					//      The type checking at that point would be less comprehensive, probably, but this seems to be the direction I need to go to improve performance.
					String opcode = tuple.getOpcode();
					java.util.List<Object> arguments = tuple.getArguments();
					Opcode opcodeImplementation = runtime.getFeatureSet().getOpcode(opcode);
					currentOpcode = opcodeImplementation;
					DataType[] types = opcodeImplementation.getArgumentTypes();
					Object[] executableArguments = new Object[Math.max(arguments.size(),types.length)];
					for(int i=0;i<types.length;i++) {
						switch(types[i]) {
						case BOOLEAN:
							executableArguments[i] = getValue(yieldingScript.context,arguments.get(i),yieldingScript.localVariables);
							
							if(!(executableArguments[i] instanceof Boolean))
								throw new InvalidScriptDefinitionException("Non-tuple provided where tuple expected.");
							break;
						case NUMBER:
							executableArguments[i] = OpcodeUtils.getNumericValue(getValue(yieldingScript.context,arguments.get(i),yieldingScript.localVariables));
							break;
						case OBJECT:
							executableArguments[i] = getValue(yieldingScript.context,arguments.get(i),yieldingScript.localVariables);
							if(!((executableArguments[i] instanceof Boolean)||(executableArguments[i] instanceof Number)||(executableArguments[i] instanceof String)))
								throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
							break;
						case OBJECTS:
							Object[] objects = new Object[arguments.size()-types.length+1];
							for(int j=0;j<objects.length;j++) {
								objects[j] = getValue(yieldingScript.context,arguments.get(i+j),yieldingScript.localVariables);
								if(!((objects[j] instanceof Boolean)||(objects[j] instanceof Number)||(objects[j] instanceof String)))
									throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
							}
							executableArguments[i] = objects;
							break;
						case STRING:
							executableArguments[i] = OpcodeUtils.getStringValue(getValue(yieldingScript.context,arguments.get(i),yieldingScript.localVariables));
							break;
						default:
							throw new RuntimeException("Unhandled DataType, "+types[i].name()+", in method signature for opcode, "+opcode);
						}
					}
					if(yieldingScript.debugFlag) {
						String description = currentOpcode.getOpcode()+" "+Arrays.toString((Object[])executableArguments);
						System.out.println("</>"+description);
					}
					OpcodeSubaction subaction = ((OpcodeAction)opcodeImplementation).execute(runtime, scriptRunner, yieldingScript.context, executableArguments);
					yieldingScript.index++;
					if(subaction!=null) {
						switch(subaction.getType()) {
						case YIELD_CHECK:
							if(!yieldingScript.isAtomic) {
								yieldCheck = subaction;
								return;
							}
							break;
						case SUBSCRIPT:
							String description = currentOpcode.getOpcode()+" "+executableArguments[0]+((executableArguments.length>1)?(" "+Arrays.toString((Object[])executableArguments[1])):"");
							synchronized(callStack){
								if(!stopProcedure) {
									callStack.push(new YieldingScript(subaction.getSubscript().getContext(), ((ScriptTupleImplementation)subaction.getSubscript()).getResolvedBlockTuples(), ((ScriptTupleImplementation)subaction.getSubscript()).getLocalVariableCount(), subaction.isSubscriptAtomic(), description));
								}
							}
							if(!yieldingScript.isAtomic) {
								return;
							}
							yieldingScript = callStack.peek();
							break;
						}
					}
				}
				currentOpcode = null;
				synchronized(callStack){
					callStack.pop();
				}
				stopProcedure = false;
				if(callStack.size() > 0) {
					yieldingScript = callStack.peek();
					if(!yieldingScript.isAtomic)
						return;
				}
			}

			synchronized(STOP_LOCK) {
				stopped = true;
				STOP_LOCK.notifyAll();
				scriptRunner.runnable = null;
			}
		}
		finally{
			synchronized(STOP_LOCK) {
				running = false;
			}
		}
	}
	
	private Object getValue(ScriptContext context, Object object, Object[] localVariables) throws InvalidScriptDefinitionException {
		if(object instanceof BlockTuple)
			return getBlockTupleValue(context, (BlockTuple)object, localVariables);
		return object;
	}
	
	private Object getBlockTupleValue(ScriptContext context, BlockTuple tuple, Object[] localVariables) throws InvalidScriptDefinitionException{
		if(tuple instanceof ReadLocalVarBlockTuple)
			return localVariables[((ReadLocalVarBlockTuple)tuple).getLocalVarIdentifier()];
		java.util.List<Object> arguments = tuple.getArguments();
		Opcode opcodeImplementation = getOpcode(tuple);
		if(opcodeImplementation == null)
			throw new InvalidScriptDefinitionException("Unrecognized value resolving opcode: "+tuple.getOpcode());
		if(!(opcodeImplementation instanceof OpcodeValue))
			throw new InvalidScriptDefinitionException("Attempted to evaluate non-value opcode: "+tuple.getOpcode());
		DataType[] types = opcodeImplementation.getArgumentTypes();
		if(types.length!= arguments.size())
			throw new InvalidScriptDefinitionException("Invalid arguments found for opcode, "+tuple.getOpcode());
		Object[] executableArguments = new Object[arguments.size()];
		for(int i=0;i<arguments.size();i++) {
			if(types[i]!=Opcode.DataType.TUPLE)
				executableArguments[i] = getValue(context,arguments.get(i), localVariables);
			switch(types[i]) {
			case BOOLEAN:
				if(!(executableArguments[i] instanceof Boolean))
					throw new InvalidScriptDefinitionException("Non-boolean provided where boolean expected: "+executableArguments[i]);
				break;
			case NUMBER:
				executableArguments[i] = OpcodeUtils.getNumericValue(executableArguments[i]);
				break;
			case OBJECT:
				if(!((executableArguments[i] instanceof Boolean)||(executableArguments[i] instanceof Number)||(executableArguments[i] instanceof String)))
					throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
				break;
			case STRING:
				executableArguments[i] = OpcodeUtils.getStringValue(executableArguments[i]);
				break;
			default:
				throw new RuntimeException("Unhandled DataType, "+types[i].name()+", in method signature for opcode, "+opcodeImplementation.getOpcode());
			}
		}
		Object retval = ((OpcodeValue)opcodeImplementation).execute(runtime, scriptRunner, context, executableArguments);
		return retval;
	}

	private Opcode getOpcode(BlockTuple blockTuple) {
		return runtime.getFeatureSet().getOpcode(blockTuple.getOpcode());
	}

	/**
	 * 
	 * @return A ScriptTupleRunner object for accessing this Thread.
	 */
	public ScriptTupleRunner getScriptTupleRunner() {
		return scriptRunner;
	}
	
	private static class YieldingScript{
		public ScriptContext context;
		public BlockTuple[] blockTuples;
		public Object[] localVariables;
		public int index;
		public boolean isAtomic;
		public String description;
		public boolean debugFlag = false;
		
		public YieldingScript(ScriptContext context, BlockTuple[] blockTuples, int localVarCount, boolean isAtomic, String description) {
			super();
			this.context = context;
			this.blockTuples = blockTuples;
			this.isAtomic = isAtomic;
			this.localVariables = new Object[localVarCount];
			this.description = description;
			index = 0;
		}
	}
	
	/**
	 * A class for helping to encapsulate the thread. (Not completely doable, since the thread is always accessible as Thread.currentThread())
	 * 
	 * @author sean.cox
	 *
	 */
	private static class ScriptTupleRunnerImpl implements ScriptTupleRunner{
		private ScriptTupleRunnable runnable;
		
		/**
		 * @param runnable
		 */
		public ScriptTupleRunnerImpl(ScriptTupleRunnable runnable) {
			super();
			this.runnable = runnable;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#flagStop()
		 */
		@Override
		public void flagStop() {
			if(runnable!=null)
				runnable.flagStop(false);
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#isStopFlagged()
		 */
		@Override
		public boolean isStopFlagged() {
			return (runnable!=null)||runnable.stopProcedure;
		}

		@Override
		public boolean isStopped() {
			return (runnable==null)||runnable.stopped;
		}

		@Override
		public ScriptContext getContext() {
			return runnable.scriptTuple.getContext();
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#getCurrentOpcode()
		 */
		@Override
		public Opcode getCurrentOpcode() {
			return runnable.currentOpcode;
		}

		@Override
		public String getStackTrace() {
			return runnable.getStackTrace();
		}
	}
}
