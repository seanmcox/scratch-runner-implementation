/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.SwingUtilities;

import com.shtick.utils.scratch.runner.core.SoundMonitor;
import com.shtick.utils.scratch.runner.core.StageListener;
import com.shtick.utils.scratch.runner.core.ValueListener;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.core.elements.Stage;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.ScriptTupleThread;

/**
 * @author sean.cox
 *
 */
public class StageImplementation implements Stage{
	private String objName;
	private java.util.List<ScriptTupleImplementation> scripts;
	private Map<String,SoundImplementation> soundsByName;
	private long penLayerID;
	private String penLayerMD5;
	private long tempoBPM;
	private double videoAlpha;
	private LinkedList<RenderableChild> children;
	private Map<String,Object> info;
	private boolean stopAll = false;

	// Variable and list properties.
	private Map<String,Object> variableValuesByName;
	private HashMap<String,LinkedList<ValueListener>> varListeners = new HashMap<>();
	private HashMap<String,HashMap<ValueListener,StageListener>> propertyListeners = new HashMap<>(5); // Only tracks listeners that aren't a simple pass-through for the valueListener.
	private Map<String,ListImplementation> listsByName;

	// Costume properties
	private CostumeImplementation[] costumes;
	private int currentCostumeIndex;
	private LinkedList<StageListener> stageListeners = new LinkedList<>();

	// Sound properties
	private double volume = 100;
	private LinkedList<Clip> activeClips = new LinkedList<>();
	private LinkedList<ValueListener> volumeListeners = new LinkedList<>();
	
	/**
	 * @param objName
	 * @param variables
	 * @param listsByName
	 * @param sounds
	 * @param costumes
	 * @param currentCostumeIndex
	 * @param penLayerID
	 * @param penLayerMD5
	 * @param tempoBPM
	 * @param videoAlpha
	 * @param info
	 */
	public StageImplementation(String objName, VariableImplementation[] variables, Map<String, ListImplementation> listsByName,
			SoundImplementation[] sounds, CostumeImplementation[] costumes, int currentCostumeIndex, long penLayerID,
			String penLayerMD5, long tempoBPM, double videoAlpha, Map<String, Object> info) {
		super();
		this.objName = objName;
		if(variables==null) {
			variableValuesByName = new HashMap<>();
		}
		else {
			variableValuesByName = new HashMap<>(variables.length);
			for(VariableImplementation variable:variables)
				variableValuesByName.put(variable.getName(), variable.getValue());
		}
		this.listsByName = listsByName;
		this.scripts = new LinkedList<>();
		if(sounds==null) {
			soundsByName = new HashMap<>();
		}
		else {
			soundsByName = new HashMap<>(sounds.length);
			for(SoundImplementation sound:sounds)
				soundsByName.put(sound.getSoundName(), sound);
		}
		this.costumes = costumes;
		this.currentCostumeIndex = currentCostumeIndex;
		this.penLayerID = penLayerID;
		this.penLayerMD5 = penLayerMD5;
		this.tempoBPM = tempoBPM;
		this.videoAlpha = videoAlpha;
		this.children = new LinkedList<>();
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getContextObject()
	 */
	@Override
	public ScriptContext getContextObject() {
		return this;
	}

	@Override
	public String getObjName() {
		return objName;
	}

	@Override
	public long getPenLayerID() {
		return penLayerID;
	}

	@Override
	public String getPenLayerMD5() {
		return penLayerMD5;
	}

	@Override
	public long getTempoBPM() {
		return tempoBPM;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#playSoundByName(java.lang.String, boolean)
	 */
	@Override
	public SoundMonitor playSoundByName(String soundName) {
		if(!soundsByName.containsKey(soundName))
			throw new IllegalArgumentException("Could not find sound with name, "+soundName+", in "+objName+".");
		SoundImplementation sound = soundsByName.get(soundName);
		String resourceName = sound.getResourceName();
		try {
			final Clip clip = ScratchRuntimeImplementation.getScratchRuntime().playSound(resourceName,volume);
			if(clip==null) {
				return new SoundMonitor() {
					
					@Override
					public boolean isDone() {
						return true;
					}
				};
			}
			synchronized(activeClips) {
				activeClips.add(clip);
				clip.addLineListener(new LineListener() {
					
					@Override
					public void update(LineEvent event) {
						if((event.getType()==LineEvent.Type.STOP)||(event.getType()==LineEvent.Type.CLOSE)) {
							synchronized(activeClips) {
								activeClips.remove(clip);
							}
						}
					}
				});
				if(!clip.isRunning())
					activeClips.remove(clip);
			}
			return new SoundMonitor() {
				
				@Override
				public boolean isDone() {
					return !clip.isRunning();
				}
			};
		}
		catch(Throwable t) {
			t.printStackTrace();
			return new SoundMonitor() {
				
				@Override
				public boolean isDone() {
					return true;
				}
			};
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#setVolume(int)
	 */
	@Override
	public void setVolume(double volume) {
		synchronized(volumeListeners) {
			if(volume<0)
				volume = 0;
			else if(volume>100)
				volume = 100;
			final double oldVolume = this.volume;
			final double newVolume = volume;
			this.volume = volume;
			if(oldVolume==newVolume)
				return;
			SwingUtilities.invokeLater(()->{
				synchronized(volumeListeners) {
					for(ValueListener listener:volumeListeners)
						listener.valueUpdated(oldVolume,newVolume);
				}
			});
		
			// Adjust volume of active clips.
			synchronized(activeClips) {
				for(Clip clip:activeClips) {
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
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getVolume()
	 */
	@Override
	public double getVolume() {
		return volume;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addVolumeListener(com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addVolumeListener(ValueListener listener) {
		volumeListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeVolumeListener(com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeVolumeListener(ValueListener listener) {
		volumeListeners.remove(listener);
	}

	@Override
	public CostumeImplementation getCurrentCostume() {
		return costumes[currentCostumeIndex];
	}
	
	@Override
	public void setCurrentCostumeIndex(int i) {
		if(currentCostumeIndex == i)
			return;
		if(i<0)
			throw new IndexOutOfBoundsException();
		if(i>=costumes.length)
			throw new IndexOutOfBoundsException();
		synchronized(stageListeners) {
			final int oldIndex = currentCostumeIndex+1;
			final int newIndex = i+1;
			currentCostumeIndex = i;
			for(StageListener listener:stageListeners)
				listener.sceneChanged(oldIndex, costumes[oldIndex-1].getCostumeName(), newIndex, costumes[newIndex-1].getCostumeName());
		}
		ScratchRuntimeImplementation.getScratchRuntime().repaintStage();
	}

	@Override
	public int getCurrentCostumeIndex() {
		return currentCostumeIndex;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Stage#getCostumeCount()
	 */
	@Override
	public int getCostumeCount() {
		return costumes.length;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Stage#startScene(java.lang.String)
	 */
	@Override
	public void startScene(String name) {
		if(name==null)
			throw new IllegalArgumentException("Null costume name not allowed.");
		for(int i=0;i<costumes.length;i++) {
			if(name.equals(costumes[i].getCostumeName())) {
				if(i!=currentCostumeIndex) {
					final int oldIndex = currentCostumeIndex;
					final int newIndex = i;
					currentCostumeIndex = i;
					for(StageListener listener:stageListeners)
						listener.sceneChanged(oldIndex, costumes[oldIndex].getCostumeName(), newIndex, costumes[newIndex].getCostumeName());
					ScratchRuntimeImplementation.getScratchRuntime().repaintStage();
				}
				return;
			}
		}
		throw new IllegalArgumentException("Costume name not found: "+name);
	}

	@Override
	public ListImplementation getContextListByName(String name) {
		return listsByName.get(name);
	}

	@Override
	public Object getContextVariableValueByName(String name) throws IllegalArgumentException {
		if(!variableValuesByName.containsKey(name))
			throw new IllegalArgumentException();
		return variableValuesByName.get(name);
	}

	@Override
	public void setContextVariableValueByName(String name, Object value) throws IllegalArgumentException {
		if(!variableValuesByName.containsKey(name))
			throw new IllegalArgumentException("No veriable with the name, "+name+", can be found on the stage.");
		final Object old = variableValuesByName.get(name);
		variableValuesByName.put(name, value);
		if(old.equals(value))
			return;
		SwingUtilities.invokeLater(()->{
			synchronized(varListeners) {
				LinkedList<ValueListener> valueListeners = varListeners.get(name);
				if(valueListeners==null)
					return;
				for(ValueListener listener:valueListeners)
					listener.valueUpdated(old, value);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addVariableListener(String var, ValueListener listener) {
		synchronized(varListeners) {
			if(!variableValuesByName.containsKey(var))
				return;
			if(!varListeners.containsKey(var))
				varListeners.put(var,new LinkedList<>());
			varListeners.get(var).add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeVariableListener(String var, ValueListener listener) {
		synchronized(varListeners) {
			if(!varListeners.containsKey(var))
				return;
			varListeners.get(var).remove(listener);
			if(varListeners.get(var).isEmpty())
				varListeners.remove(var);
		}
	}
	
	@Override
	public Object getContextPropertyValueByName(String name) throws IllegalArgumentException {
		switch(name) {
		case "background#":
			return currentCostumeIndex+1; // Translate from 0-index to 1-index
		case "volume":
			return volume;
		}
		return getContextVariableValueByName(name);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addContextPropertyListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addContextPropertyListener(String property, ValueListener listener) {
		StageListener spriteListener = null;
		switch(property) {
		case "background#":
			spriteListener = new StageListener() {
				/* (non-Javadoc)
				 * @see com.shtick.utils.scratch.runner.core.StageListener#sceneChanged(int, java.lang.String, int, java.lang.String)
				 */
				@Override
				public void sceneChanged(int oldSceneIndex, String oldSceneName, int newSceneIndex,
						String newSceneName) {
					listener.valueUpdated(oldSceneIndex, newSceneIndex);
				}
			};
			break;
		case "volume":
			addVolumeListener(listener);
			return;
		default:
			addVariableListener(property, listener);
			return;
		}
		synchronized(stageListeners) {
			if(!propertyListeners.containsKey(property))
				propertyListeners.put(property, new HashMap<>());
			propertyListeners.get(property).put(listener, spriteListener);
			stageListeners.add(spriteListener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeContextPropertyListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeContextPropertyListener(String property, ValueListener listener) {
		switch(property) {
		case "background#":
			synchronized(stageListeners) {
				if(!propertyListeners.containsKey(property))
					return;
				HashMap<ValueListener,StageListener> valueToSpriteListeners = propertyListeners.get(property);
				StageListener stageListener = valueToSpriteListeners.get(listener);
				if(stageListener == null)
					return;
				valueToSpriteListeners.remove(listener);
				stageListeners.remove(stageListener);
				if(valueToSpriteListeners.size()==0)
					propertyListeners.remove(property);
			}
			return;
		case "volume":
			removeVolumeListener(listener);
			return;
		}
		removeVariableListener(property, listener);
	}

	@Override
	public void stopScripts() {
		ScratchRuntimeImplementation.getScratchRuntime().stopAllSounds();
		synchronized(children) {
			for(Object child:children) {
				if(!(child instanceof SpriteImplementation))
					continue;
				((SpriteImplementation)child).stopScripts();
			}
		}
		ScriptTupleThread scriptTupleThread = ScratchRuntimeImplementation.getScratchRuntime().getScriptTupleThread();
		scriptTupleThread.stopScriptsByContext(this);
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Stage#addStageListsner(com.shtick.utils.scratch.runner.core.StageListener)
	 */
	@Override
	public void addStageListener(StageListener listener) {
		stageListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Stage#removeStageListsner(com.shtick.utils.scratch.runner.core.StageListener)
	 */
	@Override
	public void removeStageListener(StageListener listener) {
		stageListeners.remove(listener);
	}

	/**
	 * 
	 * @param script
	 */
	public void addScript(ScriptTupleImplementation script) {
		scripts.add(script);
	}
	
	/**
	 * 
	 * @param child
	 */
	public void addChild(RenderableChild child) {
		synchronized(children) {
			if((child instanceof Sprite)&&((Sprite)child).isClone()) {
				Sprite cloneParent = ((Sprite)child).getCloneParent();
				int i = children.indexOf(cloneParent);
				children.add(i, child);
			}
			else {
				children.add(child);
			}
		}
	}
	
	/**
	 * 
	 * @param child
	 */
	public void removeChild(RenderableChild child) {
		synchronized(children) {
			children.remove(child);
		}
	}

	/**
	 * 
	 * @return An array of all RenderableChild objects in order from back to front.
	 */
	public RenderableChild[] getAllRenderableChildren() {
		synchronized(children) {
			return children.toArray(new RenderableChild[children.size()]);
		}
	}

	/**
	 * 
	 * @param sprite
	 */
	public void bringToFront(Sprite sprite) {
		synchronized(children) {
			if(!children.contains(sprite))
				throw new IllegalArgumentException("Unknown Sprite");
		}
	}

	/**
	 * 
	 * @param sprite
	 */
	public void sendToBack(Sprite sprite) {
		synchronized(children) {
			if(!children.contains(sprite))
				throw new IllegalArgumentException("Unknown Sprite");
			children.remove(sprite);
			children.addFirst(sprite);
		}		
	}

	/**
	 * 
	 * @param sprite
	 * @param n
	 */
	public void sendBackNLayers(Sprite sprite, int n) {
		synchronized(children) {
			if(!children.contains(sprite))
				throw new IllegalArgumentException("Unknown Sprite");
			int layer = children.indexOf(sprite);
			layer -= n;
			if(layer<0)
				layer=0;
			if(layer>children.size())
				layer=children.size();
			children.add(layer,sprite);
		}		
	}
}
