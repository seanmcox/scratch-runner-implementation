/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.shtick.utils.scratch.runner.core.AbstractSpriteListener;
import com.shtick.utils.scratch.runner.core.GraphicEffect;
import com.shtick.utils.scratch.runner.core.Opcode;
import com.shtick.utils.scratch.runner.core.OpcodeHat;
import com.shtick.utils.scratch.runner.core.OpcodeUtils;
import com.shtick.utils.scratch.runner.core.SoundMonitor;
import com.shtick.utils.scratch.runner.core.SpriteListener;
import com.shtick.utils.scratch.runner.core.ValueListener;
import com.shtick.utils.scratch.runner.core.Opcode.DataType;
import com.shtick.utils.scratch.runner.core.elements.BlockTuple;
import com.shtick.utils.scratch.runner.core.elements.Costume;
import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.core.elements.Tuple;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.ScriptTupleThread;
import com.shtick.utils.scratch.runner.impl.bundle.Activator;
import com.shtick.utils.scratch.runner.impl.bundle.GraphicEffectTracker;
import com.shtick.utils.scratch.runner.impl.elements.CostumeImplementation.ImageAndArea;

/**
 * @author sean.cox
 *
 */
public class SpriteImplementation implements Sprite{
	private final Object LOCK = new Object();

	private String objName;
	private java.util.List<ScriptTupleImplementation> scripts;
	private SoundImplementation[] sounds;
	private Map<String,SoundImplementation> soundsByName;
	private CostumeImplementation[] costumes;
	private int currentCostumeIndex;
	private double scratchX;
	private double scratchY;
	private double scale;
	private double direction;
	private String rotationStyle;
	private boolean isDraggable;
	private int indexInLibrary;
	private boolean visible;
	private Map<String,Object> spriteInfo;
	private ScriptContext parentContext;
	private Map<String,Double> effectValues = new HashMap<>(7);
	private LinkedList<SpriteListener> spriteListeners = new LinkedList<>();
	private ScratchRuntimeImplementation runtime;

	// Variable and list properties.
	private Map<String,Object> variableValuesByName;
	private HashMap<String,LinkedList<ValueListener>> varListeners = new HashMap<>();
	private HashMap<String,HashMap<ValueListener,SpriteListener>> propertyListeners = new HashMap<>(5); // Only tracks listeners that aren't a simple pass-through for the valueListener.
	private Map<String,ListImplementation> listsByName;

	// Clone properties
	private SpriteImplementation cloneOf = null;
	private LinkedList<SpriteImplementation> clones = new LinkedList<>();
	
	// Sound properties
	private double volume = 100;
	private LinkedList<SoundMonitor> activeSoundMonitors = new LinkedList<>();
	private LinkedList<ValueListener> volumeListeners = new LinkedList<>();
	
	// Pen properties
	private final Object PEN_LOCK = new Object();
	private boolean penDown = false;
	/**'
	 * Documentation describes the initial color (hue) as red.
	 * See: https://wiki.scratch.mit.edu/wiki/Pen_Color_(value)
	 * 
	 * However, when I ran a test of this myself, using a copy of Scratch installed shortly after Christmas 2017, the initial color (hue) was blue.
	 * In fact, that copy of scratch was universally saving blank images as the pen layer image, even when I did have pen marking on my background.
	 * 
	 * tl;dr?
	 * The start color probably doean't matter as there is a discrepancy between documentation and behavior, and support seems to be incomplete.
	 */
	private int penHue = 0;
	private double penBrightness = 100;
	private Color penColor = Color.getHSBColor(0, 1.0f, 1.0f);
	private double penSize = 1;
	
	/**
	 * @param objName
	 * @param variables
	 * @param listsByName
	 * @param sounds
	 * @param costumes
	 * @param currentCostumeIndex
	 * @param scratchX
	 * @param scratchY
	 * @param scale
	 * @param direction
	 * @param rotationStyle
	 * @param isDraggable
	 * @param indexInLibrary
	 * @param visible
	 * @param spriteInfo
	 * @param parentContext 
	 * @param runtime 
	 */
	public SpriteImplementation(String objName, VariableImplementation[] variables, Map<String, ListImplementation> listsByName,
			SoundImplementation[] sounds, CostumeImplementation[] costumes, int currentCostumeIndex, double scratchX,
			double scratchY, double scale, double direction, String rotationStyle, boolean isDraggable,
			int indexInLibrary, boolean visible, Map<String, Object> spriteInfo, ScriptContext parentContext, ScratchRuntimeImplementation runtime) {
		super();
		this.objName = objName;
		this.runtime = runtime;
		if(variables==null) {
			variableValuesByName = new HashMap<>();
		}
		else {
			variableValuesByName = new HashMap<>(variables.length);
			for(VariableImplementation variable:variables)
				variableValuesByName.put(variable.getName(), variable.getValue());
		}
		if(listsByName==null)
			this.listsByName = new HashMap<>();
		else
			this.listsByName = listsByName;
		this.scripts = new LinkedList<>();
		this.sounds = sounds;
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
		this.scratchX = scratchX;
		this.scratchY = scratchY;
		this.scale = scale;
		direction%=360;
		if(direction>180)
			direction-=360;
		else if(direction<-180)
			direction+=360;
		this.direction = direction;
		this.rotationStyle = rotationStyle;
		this.isDraggable = isDraggable;
		this.indexInLibrary = indexInLibrary;
		this.visible = visible;
		this.spriteInfo = spriteInfo;
		this.parentContext = parentContext;
		synchronized(LOCK) {
			registerWithCostumeImagePool();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getContextObject()
	 */
	@Override
	public ScriptContext getContextObject() {
		return this;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		synchronized(LOCK) {
			if(this.visible==visible)
				return;
			this.visible = visible;
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.visibilityChanged(visible);
				}
			});
		}
	}

	@Override
	public String getObjName() {
		return objName;
	}

	@Override
	public Map<String, Object> getSpriteInfo() {
		return spriteInfo;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#playSoundByName(java.lang.String, boolean)
	 */
	@Override
	public SoundMonitor playSoundByName(String soundName) {
		if(!soundsByName.containsKey(soundName))
			return null;
		SoundImplementation sound = soundsByName.get(soundName);
		return playSound(sound);
	}

	@Override
	public SoundMonitor playSoundByIndex(int index) {
		if(index<0)
			return null;
		if(index>=sounds.length)
			return null;
		SoundImplementation sound = sounds[index];
		return playSound(sound);
	}
	
	private SoundMonitor playSound(SoundImplementation sound) {
		try {
			final SoundMonitor monitor = runtime.playSound(sound,volume);
			if(monitor != null) {
				activeSoundMonitors.add(monitor);
				monitor.addCloseListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						activeSoundMonitors.remove(monitor);
					}
				});
				if(monitor.isDone())
					activeSoundMonitors.remove(monitor);
				return monitor;
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		return new SoundMonitor() {
			
			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public void addCloseListener(ActionListener listener) {}

			@Override
			public void removeCloseListener(ActionListener listener) {}

			@Override
			public void setVolume(double volume) {}

			@Override
			public void stop() {}
		};
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#setVolume(int)
	 */
	@Override
	public void setVolume(double volume) {
		synchronized(LOCK) {
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
			synchronized(activeSoundMonitors) {
				for(SoundMonitor soundMonitor:activeSoundMonitors) {
					soundMonitor.setVolume(volume);
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

	@Override
	public Costume getCurrentCostume() {
		return costumes[currentCostumeIndex];
	}
	
	@Override
	public boolean setCurrentCostumeIndex(int i) {
		synchronized(LOCK) {
			if(currentCostumeIndex == i)
				return true;
			if(i<0)
				return false;
			if(i>=costumes.length)
				return false;
			final int oldIndex = currentCostumeIndex+1;
			final int newIndex = i+1;
			final String oldName = costumes[currentCostumeIndex].getCostumeName();
			final String newName = costumes[i].getCostumeName();
			costumes[currentCostumeIndex].unregisterSprite(this);
			currentCostumeIndex = i;
			registerWithCostumeImagePool();
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.costumeChanged(oldIndex,oldName,newIndex,newName);
				}
			});
		}
		return true;
	}
	
	private void registerWithCostumeImagePool() {
		switch(getRotationStyle()) {
		case Sprite.ROTATION_STYLE_NORMAL:
			costumes[currentCostumeIndex].registerSprite(this, scale,((getDirection()-90+360)%360)*Math.PI/180,false);
			break;
		case Sprite.ROTATION_STYLE_LEFT_RIGHT:
			costumes[currentCostumeIndex].registerSprite(this, scale,0,getDirection()<0);
			break;
		default:
			costumes[currentCostumeIndex].registerSprite(this, scale,0,false);
			break;
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCurrentCostumeIndex()
	 */
	@Override
	public int getCurrentCostumeIndex() {
		return currentCostumeIndex;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCostumeCount()
	 */
	@Override
	public int getCostumeCount() {
		return costumes.length;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setCostumeByName(java.lang.String)
	 */
	@Override
	public boolean setCostumeByName(String name) {
		if(name==null)
			return false;
		for(int i=0;i<costumes.length;i++) {
			if(name.equals(costumes[i].getCostumeName())) {
				if(i==currentCostumeIndex)
					return true;
				final int oldIndex = currentCostumeIndex+1;
				final int newIndex = i+1;
				final String oldName = costumes[currentCostumeIndex].getCostumeName();
				final String newName = name;
				costumes[currentCostumeIndex].unregisterSprite(this);
				currentCostumeIndex = i;
				registerWithCostumeImagePool();
				runtime.repaintStage();
				SwingUtilities.invokeLater(()->{
					synchronized(spriteListeners) {
						for(SpriteListener listener:spriteListeners)
							listener.costumeChanged(oldIndex,oldName,newIndex,newName);
					}
				});
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return An object containing the appropriate image, center, and bounds information for the identified sprite.
	 */
	public ImageAndArea getScaledAndRotatedImage() {
		synchronized(LOCK) {
			return costumes[currentCostumeIndex].getScaledAndRotatedImage(this);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#stampSprite()
	 */
	@Override
	public void stampSprite() {
		synchronized(LOCK) {
			ImageAndArea imageAndArea = getScaledAndRotatedImage();
			if(imageAndArea==null)
				return;
			
			// Contrary to the usual Scratch mangling of standard practices,
			// centerX and centerY have the usual meaning of being values
			// relative to the upper-left corner of the image, with values
			// increasing left to right and top to bottom respectively.
			int centerX = imageAndArea.rotationCenterX;
			int centerY = imageAndArea.rotationCenterY;
		
			Graphics2D g2 = runtime.getPenLayerGraphics();
			BufferedImage img = imageAndArea.image;
			if(effectValues.size()>0) {
				img =  new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
				img.getGraphics().drawImage(imageAndArea.image, 0, 0, null);
				GraphicEffectTracker tracker = Activator.GRAPHIC_EFFECT_TRACKER;
				for(String name:effectValues.keySet()) {
					GraphicEffect effect = tracker.getGraphicEffect(name);
					if(effect==null) {
						System.err.println("WARNING: Effect not found: "+name);
						continue;
					}
					img = effect.getAffectedImage(img, effectValues.get(name));
				}
			}
			g2.drawImage(img, (int)(getScratchX()-centerX), (int)(-getScratchY()-centerY), null);
		}
		runtime.repaintStage();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#gotoXY(double, double)
	 */
	@Override
	public void gotoXY(double scratchX, double scratchY) {
		synchronized(LOCK) {
			if(penDown) {
				Graphics2D g2 = runtime.getPenLayerGraphics();
				g2.setStroke(getPenStroke());
				g2.setColor(getPenColor());
				g2.drawLine((int)this.scratchX, (int)-this.scratchY, (int)scratchX, (int)-scratchY);
			}
			
			if((this.scratchX == scratchX)&&(this.scratchY == scratchY))
				return;
			final double oldX = this.scratchX;
			final double newX = scratchX;
			final double oldY = this.scratchY;
			final double newY = scratchY;
			this.scratchX = scratchX;
			this.scratchY = scratchY;
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				Point2D oldPoint = new Point2D.Double(oldX, oldY);
				Point2D newPoint = new Point2D.Double(newX, newY);
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.positionChanged(oldPoint,newPoint);
				}
			});
		}
	}

	@Override
	public double getScratchX() {
		return scratchX;
	}

	@Override
	public void setScratchX(double scratchX) {
		synchronized(LOCK) {
			if(penDown) {
				Graphics2D g2 = runtime.getPenLayerGraphics();
				g2.setStroke(getPenStroke());
				g2.setColor(getPenColor());
				g2.drawLine((int)this.scratchX, (int)-scratchY, (int)scratchX, (int)-scratchY);
			}

			if(this.scratchX == scratchX)
				return;
			final double oldX = this.scratchX;
			final double newX = scratchX;
			final double y = scratchY;
			this.scratchX = scratchX;
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				Point2D oldPoint = new Point2D.Double(oldX, y);
				Point2D newPoint = new Point2D.Double(newX, y);
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.positionChanged(oldPoint,newPoint);
				}
			});
		}
	}

	@Override
	public double getScratchY() {
		return scratchY;
	}

	@Override
	public void setScratchY(double scratchY) {
		synchronized(LOCK) {
			if(penDown) {
				Graphics2D g2 = runtime.getPenLayerGraphics();
				g2.setStroke(getPenStroke());
				g2.setColor(getPenColor());
				g2.drawLine((int)scratchX, (int)-this.scratchY, (int)scratchX, (int)-scratchY);
			}
			
			if(this.scratchY == scratchY)
				return;
			final double oldY = this.scratchY;
			final double newY = scratchY;
			final double x = scratchX;
			this.scratchY = scratchY;
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				Point2D oldPoint = new Point2D.Double(x, oldY);
				Point2D newPoint = new Point2D.Double(x, newY);
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.positionChanged(oldPoint,newPoint);
				}
			});
		}
	}

	@Override
	public double getScale() {
		return scale;
	}

	@Override
	public void setScale(double scale) {
		synchronized(LOCK) {
			if(this.scale == scale)
				return;
			final double oldScale = this.scale;
			final double newScale = scale;
			this.scale = scale;
			registerWithCostumeImagePool();
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.headingChanged(oldScale,newScale);
				}
			});
		}
	}

	@Override
	public double getDirection() {
		return direction;
	}

	@Override
	public void setDirection(double direction) {
		synchronized(LOCK) {
			direction%=360;
			if(direction>180)
				direction-=360;
			else if(direction<-180)
				direction+=360;
			if(this.direction == direction)
				return;
			final double oldDirection = this.direction;
			final double newDirection = direction;
			this.direction = direction;
			registerWithCostumeImagePool();
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.headingChanged(oldDirection,newDirection);
				}
			});
		}
	}

	@Override
	public String getRotationStyle() {
		return rotationStyle;
	}

	@Override
	public void setRotationStyle(String rotationStyle) {
		synchronized(LOCK) {
			if(this.rotationStyle.equals(rotationStyle))
				return;
			final String old = this.rotationStyle;
			this.rotationStyle = rotationStyle;
			registerWithCostumeImagePool();
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.rotationStyleChanged(old,rotationStyle);
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getEffect(java.lang.String)
	 */
	@Override
	public double getEffect(String name) {
		if(effectValues.containsKey(name))
			return effectValues.get(name);
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setEffect(java.lang.String, double)
	 */
	@Override
	public void setEffect(String name, double value) {
		synchronized(LOCK) {
			final double old = effectValues.containsKey(name)?effectValues.get(name).doubleValue():0.0;
			if(old==value)
				return;
			effectValues.put(name, value);
			runtime.repaintStage();
			SwingUtilities.invokeLater(()->{
				synchronized(spriteListeners) {
					for(SpriteListener listener:spriteListeners)
						listener.effectChanged(name, old, value);
				}
			});
		}
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#clearEffects()
	 */
	@Override
	public void clearEffects() {
		if(effectValues.size()==0)
			return;
		synchronized(LOCK) {
			HashMap<String,Double> oldEffectValues = new HashMap<>(effectValues);
			effectValues.clear();
			for(String key:oldEffectValues.keySet()) {
				final double old = oldEffectValues.get(key).doubleValue();
				if(old!=0.0) {
					SwingUtilities.invokeLater(()->{
						synchronized(spriteListeners) {
							for(SpriteListener listener:spriteListeners)
								listener.effectChanged(key, old, 0.0);
						}
					});
				}
			}
		}
	}

	/**
	 * 
	 * @return A map of all currently set effects.
	 */
	public Map<String,Double> getEffects(){
		synchronized(LOCK) {
			return new HashMap<>(effectValues);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#addSpriteListener(com.shtick.utils.scratch.runner.core.SpriteListener)
	 */
	@Override
	public void addSpriteListener(SpriteListener listener) {
		synchronized(spriteListeners) {
			spriteListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#removeSpriteListener(com.shtick.utils.scratch.runner.core.SpriteListener)
	 */
	@Override
	public void removeSpriteListener(SpriteListener listener) {
		synchronized(spriteListeners) {
			spriteListeners.remove(listener);
		}
	}

	@Override
	public List getContextListByName(String name) {
		if(listsByName.containsKey(name))
			return listsByName.get(name);
		return parentContext.getContextListByName(name);
	}

	@Override
	public Object getContextVariableValueByName(String name) throws IllegalArgumentException {
		if(variableValuesByName.containsKey(name))
			return variableValuesByName.get(name);
		return parentContext.getContextVariableValueByName(name);
	}

	@Override
	public void setContextVariableValueByName(String name, Object value) throws IllegalArgumentException {
		synchronized(LOCK) {
			if(variableValuesByName.containsKey(name)) {
				final Object old = variableValuesByName.get(name);
				variableValuesByName.put(name, value);
				if(old.equals(value))
					return;
				SwingUtilities.invokeLater(()->{
					synchronized(LOCK) {
						LinkedList<ValueListener> valueListeners = varListeners.get(name);
						if(valueListeners == null)
							return;
						for(ValueListener listener:valueListeners)
							listener.valueUpdated(old, value);
					}
				});
				return;
			}
			try {
				parentContext.setContextVariableValueByName(name, value);
			}
			catch(IllegalArgumentException t) {
				throw new IllegalArgumentException("No veriable with the name, "+name+", can be found on the sprite, "+objName+".", t);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addVariableListener(String var, ValueListener listener) {
		synchronized(LOCK) {
			if(!variableValuesByName.containsKey(var)) {
				parentContext.addVariableListener(var, listener);
				return;
			}
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
		synchronized(LOCK) {
			if(!varListeners.containsKey(var)) {
				parentContext.addVariableListener(var, listener);
				return;
			}
			varListeners.get(var).remove(listener);
			if(varListeners.get(var).isEmpty())
				varListeners.remove(var);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addContextPropertyListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addContextPropertyListener(String property, ValueListener listener) {
		SpriteListener spriteListener = null;
		switch(property) {
		case "x position":
			spriteListener = new AbstractSpriteListener() {
				@Override
				public void positionChanged(Point2D oldPoint, Point2D newPoint) {
					if(oldPoint.getX()==newPoint.getX())
						return;
					listener.valueUpdated(oldPoint.getX(), newPoint.getX());
				}
			};
			break;
		case "y position":
			spriteListener = new AbstractSpriteListener() {
				@Override
				public void positionChanged(Point2D oldPoint, Point2D newPoint) {
					if(oldPoint.getY()==newPoint.getY())
						return;
					listener.valueUpdated(oldPoint.getY(), newPoint.getY());
				}
			};
			break;
		case "direction":
			spriteListener = new AbstractSpriteListener() {
				@Override
				public void headingChanged(double oldValue, double newValue) {
					listener.valueUpdated(oldValue, newValue);
				}
			};
			break;
		case "costume#":
			spriteListener = new AbstractSpriteListener() {
				@Override
				public void costumeChanged(int oldSceneIndex, String oldSceneName, int newSceneIndex,
						String newSceneName) {
					listener.valueUpdated(oldSceneIndex, newSceneIndex);
				}
			};
			break;
		case "size":
			spriteListener = new AbstractSpriteListener() {
				@Override
				public void scaleChanged(double oldValue, double newValue) {
					listener.valueUpdated(oldValue*100, newValue*100);
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
		synchronized(spriteListeners) {
			if(!propertyListeners.containsKey(property))
				propertyListeners.put(property, new HashMap<>());
			propertyListeners.get(property).put(listener, spriteListener);
			spriteListeners.add(spriteListener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeContextPropertyListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeContextPropertyListener(String property, ValueListener listener) {
		switch(property) {
		case "x position":
		case "y position":
		case "direction":
		case "costume#":
		case "size":
			synchronized(spriteListeners) {
				if(!propertyListeners.containsKey(property))
					return;
				HashMap<ValueListener,SpriteListener> valueToSpriteListeners = propertyListeners.get(property);
				SpriteListener spriteListener = valueToSpriteListeners.get(listener);
				if(spriteListener == null)
					return;
				valueToSpriteListeners.remove(listener);
				spriteListeners.remove(spriteListener);
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
	public Object getContextPropertyValueByName(String name) throws IllegalArgumentException {
		switch(name) {
		case "x position":
			return scratchX;
		case "y position":
			return scratchY;
		case "direction":
			return direction;
		case "costume#":
			return currentCostumeIndex+1; // Translate from 0-index to 1-index
		case "size":
			return scale*100;
		case "volume":
			return volume;
		}
		if(variableValuesByName.containsKey(name))
			return variableValuesByName.get(name);
		throw new IllegalArgumentException();
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getSpriteLock()
	 */
	@Override
	public Object getSpriteLock() {
		return LOCK;
	}

	@Override
	public void stopScripts() {
		ScriptTupleThread scriptTupleThread = runtime.getScriptTupleThread();
		scriptTupleThread.stopScriptsByContext(this);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#isClone()
	 */
	@Override
	public boolean isClone() {
		return cloneOf!=null;
	}
	
	/**
	 * 
	 * @param sprite
	 * @return Returns true if this Sprite is a clone of the given Sprite. (Even if a multi-generational clone.) Returns false otherwise.
	 */
	public boolean isCloneOf(Sprite sprite) {
		if(cloneOf == null)
			return false;
		if(cloneOf == sprite)
			return true;
		return cloneOf.isCloneOf(sprite);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCloneParent()
	 */
	@Override
	public Sprite getCloneParent() {
		return cloneOf;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#createClone()
	 */
	@Override
	public void createClone() {
		synchronized(LOCK) {
			synchronized(this.listsByName) {
				VariableImplementation[] variables = new VariableImplementation[variableValuesByName.size()];
				int i = 0;
				for(String name:variableValuesByName.keySet()) {
					variables[i] = new VariableImplementation(name, variableValuesByName.get(name));
					i++;
				}
	
				HashMap<String, ListImplementation> listsByName = new HashMap<>(this.listsByName.size());
				for(String name:this.listsByName.keySet()) {
					try {
						listsByName.put(name,  (ListImplementation)this.listsByName.get(name).clone());
					}
					catch(CloneNotSupportedException t) {
						throw new RuntimeException(t);
					}
				}
	
				SpriteImplementation clone = new SpriteImplementation(
						objName,
						variables,
						listsByName,
						soundsByName.values().toArray(new SoundImplementation[soundsByName.size()]),
						costumes,
						currentCostumeIndex,
						scratchX,
						scratchY,
						scale,
						direction,
						rotationStyle,
						isDraggable,
						indexInLibrary,
						visible,
						spriteInfo,
						parentContext,
						runtime);
				
				// Set clone scripts.
				for(ScriptTupleImplementation script:scripts) {
					BlockTuple[] blockTuples = script.getBlockTuples().toArray(new BlockTuple[script.getBlockTupleCount()]);
					ScriptTupleImplementation scriptTupleImplementation = (ScriptTupleImplementation)script.clone(clone);
					clone.addScript(scriptTupleImplementation);
					if(blockTuples.length>0) {
						BlockTuple maybeHat = blockTuples[0];
						String opcode = maybeHat.getOpcode();
						Opcode opcodeImplementation = Activator.OPCODE_TRACKER.getOpcode(opcode);
						if((opcodeImplementation != null)&&(opcodeImplementation instanceof OpcodeHat)) {
							java.util.List<Object> arguments = maybeHat.getArguments();
							Object[] executableArguments = new Object[arguments.size()];
							DataType[] types = opcodeImplementation.getArgumentTypes();
							for(i=0;i<arguments.size();i++) {
								switch(types[i]) {
								case BOOLEAN:
									executableArguments[i] = arguments.get(i);
									break;
								case NUMBER:
									executableArguments[i] = OpcodeUtils.getNumericValue(arguments.get(i));
									break;
								case OBJECT:
									executableArguments[i] = arguments.get(i);
									break;
								case STRING:
									executableArguments[i] = OpcodeUtils.getStringValue(arguments.get(i));
									break;
								case TUPLE:
									executableArguments[i] = ((Tuple)arguments.get(i)).toArray();
									break;
								default:
									throw new RuntimeException("Unhandled DataType, "+types[i].name()+", in method signature for opcode, "+opcode);
								}
							}
							((OpcodeHat)opcodeImplementation).registerListeningScript(scriptTupleImplementation, executableArguments);
						}
					}
				}
				
				// Clone properties that aren't part of an initial clone definition in a .SB2 file
				clone.setVolume(volume);
				clone.setPenSize(penSize);
				clone.setPenColor(getPenColor());
				clone.setPenHue(penHue);
				clone.setPenShade(penBrightness);
				clone.setPenDown(penDown);
				clone.effectValues.putAll(effectValues);
				clone.cloneOf = this;
				
				runtime.addClone(clone);
				clones.add(clone);
				for(ScriptTupleImplementation script:clone.scripts) {
					if(script.getBlockTupleCount()<=0)
						continue;
					BlockTuple firstTuple = script.getBlockTuple(0);
					if("whenCloned".equals(firstTuple.getOpcode()))
						runtime.startScript(script);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getClones()
	 */
	@Override
	public Set<Sprite> getClones() {
		synchronized(LOCK) {
			HashSet<Sprite> retval = new HashSet<>(clones);
			for(SpriteImplementation clone:clones)
				retval.addAll(clone.getClones());
			return retval;
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#deleteClone()
	 */
	@Override
	public void deleteClone() {
		if(!isClone()) {
			System.err.println("WARNING: deleteClone() called on non-clone.");
			return;
		}
		// Set clone scripts.
		stopScripts();
		synchronized(LOCK) {
			costumes[currentCostumeIndex].unregisterSprite(this);
			for(ScriptTupleImplementation script:scripts) {
				BlockTuple[] blockTuples = script.getBlockTuples().toArray(new BlockTuple[script.getBlockTupleCount()]);
				if(blockTuples.length>0) {
					BlockTuple maybeHat = blockTuples[0];
					String opcode = maybeHat.getOpcode();
					java.util.List<Object> arguments = maybeHat.getArguments();
					Opcode opcodeImplementation = Activator.OPCODE_TRACKER.getOpcode(opcode);
					if((opcodeImplementation != null)&&(opcodeImplementation instanceof OpcodeHat)) {
						((OpcodeHat)opcodeImplementation).unregisterListeningScript(script, arguments.toArray(new Object[arguments.size()]));
					}
				}
			}
		}
		synchronized(cloneOf.getSpriteLock()) {
			cloneOf.clones.remove(this);
		}
		runtime.deleteClone(this);
	}

	/**
	 * 
	 * @param script
	 */
	public void addScript(ScriptTupleImplementation script) {
		scripts.add(script);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getSpriteShape()
	 */
	@Override
	public Area getSpriteShape() {
		synchronized(LOCK) {
			if(!visible)
				return new Area();
			ImageAndArea imageAndArea = costumes[currentCostumeIndex]
					.getScaledAndRotatedImage(this);
			if(imageAndArea==null)
				return new Area();
			return (Area)imageAndArea
					.getCostumeArea()
					.clone(); // Don't give the original to external entities. (Might have to simply depend on external entities to play nice if this becomes an important performance issue.)
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#isPenDown()
	 */
	@Override
	public boolean isPenDown() {
		return penDown;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenDown(boolean)
	 */
	@Override
	public void setPenDown(boolean penDown) {
		this.penDown = penDown;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenSize()
	 */
	@Override
	public double getPenSize() {
		return penSize;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenSize(double)
	 */
	@Override
	public void setPenSize(double penSize) {
		if(penSize<0)
			penSize = 0;
		this.penSize = penSize;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenHue()
	 */
	@Override
	public int getPenHue() {
		return penHue;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenHue(int)
	 */
	@Override
	public void setPenHue(int hue) {
		synchronized(PEN_LOCK) {
			this.penHue = hue;
			updatePenColorForHSBChange();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenShade()
	 */
	@Override
	public double getPenShade() {
		return penBrightness;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenShade(double)
	 */
	@Override
	public void setPenShade(double shade) {
		synchronized(PEN_LOCK) {
			this.penBrightness = shade;
			updatePenColorForHSBChange();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenColor()
	 */
	@Override
	public Color getPenColor() {
		return penColor;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenColor(java.awt.Color)
	 */
	@Override
	public void setPenColor(Color penColor) {
		synchronized(PEN_LOCK) {
			if(penColor!=null) {
				this.penColor = penColor;
				float[] hsb = Color.RGBtoHSB(penColor.getRed(), penColor.getGreen(), penColor.getBlue(), null);
				penHue = Math.round(hsb[0]*200)%200;
				penBrightness = hsb[2]*100;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenStroke()
	 */
	@Override
	public Stroke getPenStroke() {
		return new BasicStroke((float)penSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}
	
	private void updatePenColorForHSBChange() {
		float[] hsb = Color.RGBtoHSB(penColor.getRed(), penColor.getGreen(), penColor.getBlue(), null);
		hsb[0] = (((penHue%200)+200)%200)/200f;
		hsb[2] = 1-(Math.abs((((penHue%200)+200)%200)-100)/100f);
		Color hsbColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		penColor = new Color(hsbColor.getRed(),hsbColor.getGreen(),hsbColor.getBlue(),penColor.getAlpha());
	}
}
