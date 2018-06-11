/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.util.Map;
import java.util.Set;

import com.shtick.utils.scratch.runner.core.SoundMonitor;
import com.shtick.utils.scratch.runner.core.SpriteListener;
import com.shtick.utils.scratch.runner.core.ValueListener;
import com.shtick.utils.scratch.runner.core.elements.Costume;
import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.Sprite;

/**
 * @author sean.cox
 *
 */
public class AllBadSprite implements Sprite {
	@Override
	public void stopScripts() {
		fail("stopScripts called unnecessarily");
	}
	
	@Override
	public SoundMonitor playSoundByName(String soundName) {
		fail("playSoundByName called unnecessarily");
		return null;
	}
	
	@Override
	public SoundMonitor playSoundByIndex(int index) {
		fail("playSoundByIndex called unnecessarily");
		return null;
	}

	@Override
	public void setVolume(double volume) {
		fail("setVolume called unnecessarily");
	}

	@Override
	public double getVolume() {
		fail("getVolume called unnecessarily");
		return 100;
	}
	
	@Override
	public void setContextVariableValueByName(String name, Object value) throws IllegalArgumentException {
		fail("setContextVariableValueByName called unnecessarily");
	}
	
	@Override
	public Object getContextVariableValueByName(String name) throws IllegalArgumentException {
		fail("getContextVariableValueByName called unnecessarily");
		return null;
	}
	
	@Override
	public Object getContextPropertyValueByName(String name) throws IllegalArgumentException {
		fail("getContextPropertyValueByName called unnecessarily");
		return null;
	}
	
	@Override
	public List getContextListByName(String name) {
		fail("getContextListByName called unnecessarily");
		return null;
	}
	
	@Override
	public ScriptContext getContextObject() {
		fail("getContextObject called unnecessarily");
		return this;
	}

	@Override
	public void setVisible(boolean visible) {
		fail("setVisible called unnecessarily");
	}
	
	@Override
	public void setScratchY(double scratchY) {
		fail("setScratchY called unnecessarily");
	}
	
	@Override
	public void setScratchX(double scratchX) {
		fail("setScratchX called unnecessarily");
	}
	
	@Override
	public void setScale(double scale) {
		fail("setScale called unnecessarily");
	}
	
	@Override
	public void setRotationStyle(String rotationStyle) {
		fail("setRotationStyle called unnecessarily");
	}
	
	@Override
	public void setEffect(String name, double value) {
		fail("setEffect called unnecessarily");
	}
	
	@Override
	public void setDirection(double direction) {
		fail("setDirection called unnecessarily");
	}
	
	@Override
	public boolean setCurrentCostumeIndex(int i) {
		fail("setCurrentCostumeIndex called unnecessarily");
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCurrentCostumeIndex()
	 */
	@Override
	public int getCurrentCostumeIndex() {
		fail("getCurrentCostumeIndex called unnecessarily");
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCostumeCount()
	 */
	@Override
	public int getCostumeCount() {
		fail("getCostumeCount called unnecessarily");
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setCostumeByName(java.lang.String)
	 */
	@Override
	public boolean setCostumeByName(String name) {
		fail("setCostumeByName called unnecessarily");
		return false;
	}

	@Override
	public boolean isVisible() {
		fail("isVisible called unnecessarily");
		return true;
	}
	
	@Override
	public boolean isClone() {
		fail("isClone called unnecessarily");
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCloneParent()
	 */
	@Override
	public Sprite getCloneParent() {
		fail("getCloneParent called unnecessarily");
		return null;
	}

	@Override
	public void gotoXY(double scratchX, double scratchY) {
		fail("gotoXY called unnecessarily");
	}
	
	@Override
	public Area getSpriteShape() {
		fail("getSpriteShape called unnecessarily");
		return null;
	}
	
	@Override
	public Object getSpriteLock() {
		fail("getSpriteLock called unnecessarily");
		return null;
	}
	
	@Override
	public Map<String, Object> getSpriteInfo() {
		fail("getSpriteInfo called unnecessarily");
		return null;
	}
	
	@Override
	public double getScratchY() {
		fail("getScratchY called unnecessarily");
		return 0;
	}
	
	@Override
	public double getScratchX() {
		fail("getScratchX called unnecessarily");
		return 0;
	}
	
	@Override
	public double getScale() {
		fail("getScale called unnecessarily");
		return 1.0;
	}
	
	@Override
	public String getRotationStyle() {
		fail("getRotationStyle called unnecessarily");
		return "none";
	}
	
	@Override
	public String getObjName() {
		fail("getObjName called unnecessarily");
		return "testObject";
	}
	
	@Override
	public double getEffect(String name) {
		fail("getEffect called unnecessarily");
		return 0;
	}
	
	@Override
	public double getDirection() {
		fail("getDirection called unnecessarily");
		return 90;
	}
	
	@Override
	public Costume getCurrentCostume() {
		fail("getCurrentCostume called unnecessarily");
		return null;
	}
	
	@Override
	public void deleteClone() {
		fail("deleteClone called unnecessarily");
	}
	
	@Override
	public void createClone() {
		fail("createClone called unnecessarily");
	}
	
	@Override
	public void clearEffects() {
		fail("clearEffects called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getClones()
	 */
	@Override
	public Set<Sprite> getClones() {
		fail("getClones called unnecessarily");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#isPenDown()
	 */
	@Override
	public boolean isPenDown() {
		fail("isPenDown called unnecessarily");
		return false;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenDown(boolean)
	 */
	@Override
	public void setPenDown(boolean penDown) {
		fail("setPenDown called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenSize()
	 */
	@Override
	public double getPenSize() {
		fail("getPenSize called unnecessarily");
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenSize(double)
	 */
	@Override
	public void setPenSize(double penSize) {
		fail("setPenSize called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenHue()
	 */
	@Override
	public int getPenHue() {
		fail("getPenHue called unnecessarily");
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenHue(int)
	 */
	@Override
	public void setPenHue(int hue) {
		fail("setPenHue called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenShade()
	 */
	@Override
	public double getPenShade() {
		fail("getPenShade called unnecessarily");
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenShade(double)
	 */
	@Override
	public void setPenShade(double shade) {
		fail("setPenShade called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenColor()
	 */
	@Override
	public Color getPenColor() {
		fail("getPenColor called unnecessarily");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setPenColor(java.awt.Color)
	 */
	@Override
	public void setPenColor(Color penColor) {
		fail("setPenColor called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getPenStroke()
	 */
	@Override
	public Stroke getPenStroke() {
		fail("getPenStroke called unnecessarily");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#addSpriteListener(com.shtick.utils.scratch.runner.core.SpriteListener)
	 */
	@Override
	public void addSpriteListener(SpriteListener listener) {
		fail("addSpriteListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#removeSpriteListener(com.shtick.utils.scratch.runner.core.SpriteListener)
	 */
	@Override
	public void removeSpriteListener(SpriteListener listener) {
		fail("removeSpriteListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addVariableListener(String var, ValueListener listener) {
		fail("addVariableListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeVariableListener(String var, ValueListener listener) {
		fail("removeVariableListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addContextPropertyListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addContextPropertyListener(String property, ValueListener listener) {
		fail("addContextPropertyListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeContextPropertyListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeContextPropertyListener(String property, ValueListener listener) {
		fail("removeContextPropertyListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addVolumeListener(com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addVolumeListener(ValueListener listener) {
		fail("addVolumeListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeVolumeListener(com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeVolumeListener(ValueListener listener) {
		fail("removeVolumeListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#stampSprite()
	 */
	@Override
	public void stampSprite() {
		fail("stampSprite called unnecessarily");
	}
}
