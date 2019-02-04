/**
 * 
 */
package com.shtick.utils.scratch.runner.impl2;

import java.util.LinkedList;

/**
 * Inspired by the AWT Event Dispatch Thread.
 * 
 * @author sean.cox
 *
 */
public class ThreadTaskQueue {
	private LinkedList<Runnable> taskQueue=new LinkedList<>();
	private Thread mainThread = new Thread(new MainProcess());
	
	/**
	 * 
	 */
	public ThreadTaskQueue() {
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
	
	/**
	 * 
	 * @param r
	 */
	public void invokeAndWait(Runnable r) {
		synchronized(r) {
			boolean[] done = new boolean[] {false};
			synchronized(taskQueue) {
				taskQueue.addFirst(()->{
					r.run();
					done[0]=true;
					synchronized(r) {
						r.notifyAll();
					}
				});
				taskQueue.notifyAll();
			}
			while(!done[0]) {
				try {
					r.wait();
				}
				catch(InterruptedException t) {}
			}
		}
	}
	
	/**
	 * 
	 * @param r
	 */
	public void invokeLater(Runnable r) {
		synchronized(taskQueue) {
			taskQueue.add(r);
			taskQueue.notifyAll();
		}
	}
	
	private class MainProcess implements Runnable{

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			Runnable runnable;
			while(true) {
				synchronized(taskQueue) {
					while(taskQueue.size()==0) {
						try {
							taskQueue.wait();
						}
						catch(InterruptedException t) {}
					}
					runnable = taskQueue.removeFirst();
				}
				runnable.run();
			}
		}
	}
}
