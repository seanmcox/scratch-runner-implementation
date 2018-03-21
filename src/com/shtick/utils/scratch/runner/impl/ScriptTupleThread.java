/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.impl.elements.ScriptTupleImplementation;

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
	private Thread mainThread = new Thread(new MainProcess());
	private boolean stopFlagged = false;
	
	/**
	 * 
	 */
	public ScriptTupleThread() {
		mainThread.setDaemon(true);
		mainThread.start();
	}
	
	/**
	 * 
	 * @return true if the current Thread is the mainThread for this TaskQueue.
	 */
	public boolean isQueueThread() {
		return Thread.currentThread()==mainThread;
	}
	
	public ScriptTupleRunner startScriptTuple(ScriptTupleImplementation scriptTuple) {
		synchronized(scriptsToStart) {
			// A script added here will be terminated if it is already running. Eliminate the ambiguity of intention by overriding any current stop order.
			scriptsToStop.remove(scriptTuple);
			ScriptTupleRunnable runnable = new ScriptTupleRunnable(scriptTuple);
			scriptsToStart.put(scriptTuple,runnable);
			return runnable.getScriptTupleRunner();
		}
	}
	
	public void stopScript(ScriptTupleImplementation scriptTuple) {
		synchronized(scriptsToStart) {
			// A script set to start/restart will no longer be expected to start.
			scriptsToStart.remove(scriptTuple);
			scriptsToStop.add(scriptTuple);
		}
	}
	
	public void stopAllScripts() {
		stopFlagged = true;
		synchronized(scriptsToStart) {
			scriptsToStart.clear();
			scriptsToStop.clear();
		}
	}
	
	public void stopScriptsByContext(ScriptContext context) {
		synchronized(scriptsToStart) {
			contextsToStop.add(context);
		}
	}
	
	private class MainProcess implements Runnable{

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			long startTime, delay;
			while(true) {
				startTime = System.currentTimeMillis();
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
					ScriptTupleRunnable runnable = taskQueue.get(key);
					// The ScriptTupleRunnable should run until it yields, and then continue when called again.
					runnable.run();
					// If the runnable is done, then remove it from the taskQueue.
					if(runnable.isFinished())
						iter.remove();
				}
				if(stopFlagged) {
					taskQueue.clear();
					stopFlagged = false;
				}
				// Trigger painting if painting is needed.
				ScratchRuntimeImplementation.getScratchRuntime().repaintStageFinal();
				delay = System.currentTimeMillis() - startTime;
				if(delay<DEFAULT_FRAME_DELAY_MS) {
					// Current painting mechanism to only flag that painting should be triggered.
					// See: https://en.scratch-wiki.info/wiki/Single_Frame#Effects_of_Turbo_Speed
					try {
						Thread.sleep(DEFAULT_FRAME_DELAY_MS-delay);
					}
					catch(InterruptedException t) {
					}
				}
			}
		}
	}
}
