/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Player;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.TransitionEvent;

import com.shtick.utils.scratch.runner.core.SoundMonitor;

/**
 * @author scox
 *
 */
public class JMFSoundMonitor implements SoundMonitor{
	private final Object BLOCKER = new Object();
	private Player player;
	private boolean done = false;
	private LinkedList<ActionListener> closeListeners = new LinkedList<>();
	private Thread timerThread;

	/**
	 * 
	 * @param player
	 */
	public JMFSoundMonitor(Player player) {
		this.player = player;
		player.prefetch();
		player.addControllerListener(new ControllerListener() {
			
			@Override
			public void controllerUpdate(ControllerEvent arg0) {
				if(arg0 instanceof TransitionEvent) {
					TransitionEvent event = (TransitionEvent)arg0;
					if(event.getCurrentState()>=player.Realized) {
						synchronized(BLOCKER) {
							BLOCKER.notifyAll();
						}
					}
				}
				else if(arg0 instanceof ResourceUnavailableEvent) {
					ResourceUnavailableEvent event = (ResourceUnavailableEvent)arg0;
					System.err.println(event.getMessage());
					done = true;
					synchronized(BLOCKER) {
						BLOCKER.notifyAll();
					}
				}
			}
		});
		synchronized(BLOCKER) {
			player.start();
			try {
				BLOCKER.wait(1000);
			}
			catch(InterruptedException t) {}
		}
		Thread thread = new Thread(()->{
			double secondsLeft = player.getDuration().getSeconds()-player.getMediaTime().getSeconds();
			if((secondsLeft>0)&&(!done)) {
				try {
					Thread.sleep((long)(secondsLeft*1000));
				}
				catch(InterruptedException t) {
				}
				secondsLeft = player.getDuration().getSeconds()-player.getMediaTime().getSeconds();
			}
			player.stop();
			player.close();
			player.deallocate();
			done = true;
			for(ActionListener closeListener:closeListeners)
				closeListener.actionPerformed(new ActionEvent(JMFSoundMonitor.this, 0, "close"));
		});
		thread.start();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#isDone()
	 */
	@Override
	public boolean isDone() {
		return done;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#addCloseListener(java.awt.event.ActionListener)
	 */
	@Override
	public void addCloseListener(ActionListener listener) {
		synchronized(closeListeners) {
			closeListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#removeCloseListener(java.awt.event.ActionListener)
	 */
	@Override
	public void removeCloseListener(ActionListener listener) {
		synchronized(closeListeners) {
			closeListeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#setVolume(int)
	 */
	@Override
	public void setVolume(double volume) {
		if(volume<0)
		player.getGainControl().setLevel((float)volume/100);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#stop()
	 */
	@Override
	public void stop() {
		player.stop();
		player.close();
		player.deallocate();
		done = true;
	}
}
