/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import com.shtick.utils.scratch.runner.core.SoundMonitor;

/**
 * @author scox
 *
 */
public class ClipSoundMonitor implements SoundMonitor{
	private Clip clip;
	private LinkedList<ActionListener> closeListeners = new LinkedList<>();

	/**
	 * 
	 * @param clip
	 */
	public ClipSoundMonitor(Clip clip) {
		this.clip = clip;
		clip.start();
		clip.addLineListener(new LineListener() {
			@Override
			public void update(LineEvent event) {
				if((event.getType()==LineEvent.Type.STOP)||(event.getType()==LineEvent.Type.CLOSE)) {
					synchronized(closeListeners) {
						if(event.getType()==LineEvent.Type.STOP) {
							for(ActionListener closeListener:closeListeners)
								closeListener.actionPerformed(new ActionEvent(ClipSoundMonitor.this, 0, "stop"));
							clip.close();
						}
						else {
							for(ActionListener closeListener:closeListeners)
								closeListener.actionPerformed(new ActionEvent(ClipSoundMonitor.this, 0, "close"));
						}
					}
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#isDone()
	 */
	@Override
	public boolean isDone() {
		return !clip.isRunning();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#addCloseListener(java.awt.event.ActionListener)
	 */
	@Override
	public void addCloseListener(ActionListener listener) {
		closeListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#removeCloseListener(java.awt.event.ActionListener)
	 */
	@Override
	public void removeCloseListener(ActionListener listener) {
		closeListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#setVolume(int)
	 */
	@Override
	public void setVolume(double volume) {
	    if(clip.isControlSupported(FloatControl.Type.VOLUME)) {
		    FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.VOLUME);
		    if(control!=null)
		    		control.setValue(control.getMinimum()+(float)volume*(control.getMaximum()-control.getMinimum())/100);
	    }
	    else if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
		    FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		    if(control!=null)
	    		control.setValue(Math.min(control.getMinimum()-(float)volume*control.getMinimum()/100,control.getMaximum()));
	    }
	    else {
	    		System.err.println("Neither VOLUME nor MASTER_GAIN controls supported for clip.");
	    }
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.SoundMonitor#stop()
	 */
	@Override
	public void stop() {
		clip.stop();
		clip.close();
	}
}
