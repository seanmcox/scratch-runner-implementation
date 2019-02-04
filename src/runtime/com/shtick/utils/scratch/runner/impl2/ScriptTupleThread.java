/**
 * 
 */
package com.shtick.utils.scratch.runner.impl2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.impl2.elements.ScriptTupleImplementation;

/**
 * Inspired by the AWT Event Dispatch Thread.
 * 
 * @author sean.cox
 *
 */
public class ScriptTupleThread {
	private static final int DEFAULT_FRAME_DELAY_MS = 1000/60;
	// TODO Use the total number of scripts to determine a good number to use for starting size of this HashMap.
	private HashMap<ScriptTupleImplementation,ScriptTupleRunnable> taskQueue = new HashMap<>();
	private HashMap<ScriptTupleImplementation,ScriptTupleRunnable> scriptsToStart = new HashMap<>();
	private HashSet<ScriptTupleImplementation> scriptsToStop = new HashSet<>();
	private HashSet<ScriptContext> contextsToStop = new HashSet<>();
	private Thread mainThread = new Thread(new MainProcess(), "Scratch Execution Thread");
	private boolean stopFlagged = false;
	private ScriptTupleRunnable currentlyRunning;
	private ScratchRuntimeImplementation runtime;
	
	/**
	 * 
	 * @param runtime
	 */
	public ScriptTupleThread(ScratchRuntimeImplementation runtime) {
		this.runtime = runtime;
	}
	
	/**
	 * Starts the script execution thread if it is not already running.
	 */
	public void start() {
		if(!mainThread.isAlive()) {
			mainThread.setDaemon(true);
			mainThread.start();
		}
	}
	
	/**
	 * 
	 * @return true if the current Thread is the mainThread for this TaskQueue.
	 */
	public boolean isQueueThread() {
		return Thread.currentThread()==mainThread;
	}
	
	/**
	 * 
	 * @param scriptTuple
	 * @return Adds the given scriptTuple to the list of scripts to run.
	 */
	public ScriptTupleRunner startScriptTuple(ScriptTupleImplementation scriptTuple) {
		synchronized(scriptsToStart) {
			// A script added here will be terminated if it is already running. Eliminate the ambiguity of intention by overriding any current stop order.
			scriptsToStop.remove(scriptTuple);
			ScriptTupleRunnable runnable = new ScriptTupleRunnable(scriptTuple, runtime);
			scriptsToStart.put(scriptTuple,runnable);
			return runnable.getScriptTupleRunner();
		}
	}
	
	/**
	 * Stops the specified script. (The entire call stack is aborted.)
	 * 
	 * @param scriptTuple
	 */
	public void stopScript(ScriptTupleImplementation scriptTuple) {
		synchronized(scriptsToStart) {
			// A script set to start/restart will no longer be expected to start.
			scriptsToStart.remove(scriptTuple);
			scriptsToStop.add(scriptTuple);
			ScriptTupleRunnable runnable = taskQueue.get(scriptTuple);
			if(runnable!=null)
				runnable.flagStop(true);
		}
	}
	
	/**
	 * Stops all currently running scripts.
	 */
	public void stopAllScripts() {
		stopFlagged = true;
		synchronized(scriptsToStart) {
			scriptsToStart.clear();
			scriptsToStop.clear();
		}
	}
	
	/**
	 * Stops all scripts running under the given ScriptContext.
	 * 
	 * @param context A ScriptContext
	 */
	public void stopScriptsByContext(ScriptContext context) {
		synchronized(scriptsToStart) {
			contextsToStop.add(context);
			for(ScriptTupleImplementation script:taskQueue.keySet()) {
				if(script.getContext()==context) {
					taskQueue.get(script).flagStop(true);
				}
			}
		}
	}
	
	/**
	 * @return the currentlyRunning
	 */
	public ScriptTupleRunnable getCurrentlyRunning() {
		return currentlyRunning;
	}

	private class MainProcess implements Runnable{

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			long frameStartTime, delay;
			frameStartTime = System.currentTimeMillis();
			while(true) {
				synchronized(scriptsToStart) {
					// A script set to start/restart will no longer be expected to start.
					for(ScriptTupleImplementation scriptToStop:scriptsToStart.keySet())
						taskQueue.remove(scriptToStop);
					for(ScriptTupleImplementation scriptToStart:scriptsToStart.keySet())
						taskQueue.put(scriptToStart,scriptsToStart.get(scriptToStart));
					for(ScriptTupleImplementation script:taskQueue.keySet())
						if(contextsToStop.contains(script.getContext()))
							scriptsToStop.add(script);
					for(ScriptTupleImplementation scriptToStop:scriptsToStop)
						taskQueue.remove(scriptToStop);
					scriptsToStart.clear();
					scriptsToStop.clear();
					contextsToStop.clear();
				}
				
				Set<ScriptTupleImplementation> keySet = taskQueue.keySet();
				Iterator<ScriptTupleImplementation> iter = keySet.iterator();
				while(iter.hasNext()&&!stopFlagged) {
					ScriptTupleImplementation key = iter.next();
					currentlyRunning = taskQueue.get(key);
					if(contextsToStop.contains(key.getContext())) {
						iter.remove();
						continue;
					}
					// The ScriptTupleRunnable should run until it yields, and then continue when called again.
					currentlyRunning.run();
					// If the runnable is done, then remove it from the taskQueue.
					if(currentlyRunning.isFinished())
						iter.remove();
				}
				if(stopFlagged) {
					taskQueue.clear();
					stopFlagged = false;
				}
				// Trigger painting if painting is needed.
				if(runtime.repaintStageFinal()) {
					delay = System.currentTimeMillis() - frameStartTime;
					if(delay<DEFAULT_FRAME_DELAY_MS) {
						// Current painting mechanism to only flag that painting should be triggered.
						// See: https://en.scratch-wiki.info/wiki/Single_Frame#Effects_of_Turbo_Speed
						try {
							Thread.sleep(DEFAULT_FRAME_DELAY_MS-delay);
						}
						catch(InterruptedException t) {
						}
					}
					frameStartTime = System.currentTimeMillis();
				}
			}
		}
	}
}
