/**
 * 
 */
package com.shtick.utils.scratch.runner.standard.blocks.util;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D.Double;

import javax.swing.JPanel;

import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.ScriptTuple;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.core.elements.Stage;

/**
 * @author Sean
 *
 */
public class AllBadRuntime implements ScratchRuntime {

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getCurrentStage()
	 */
	@Override
	public Stage getCurrentStage() {
		throw new UnsupportedOperationException("Called getCurrentStage when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getScriptContextByName(java.lang.String)
	 */
	@Override
	public ScriptContext getScriptContextByName(String name) {
		throw new UnsupportedOperationException("Called getScriptContextByName when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getSpriteByName(java.lang.String)
	 */
	@Override
	public Sprite getSpriteByName(String name) {
		throw new UnsupportedOperationException("Called getSpriteByName when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#setSpriteBubbleImage(com.shtick.utils.scratch.runner.core.elements.Sprite, java.awt.Image)
	 */
	@Override
	public void setSpriteBubbleImage(Sprite sprite, Image image) {
		throw new UnsupportedOperationException("Called setSpriteBubbleImage when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getAllRenderableChildren()
	 */
	@Override
	public RenderableChild[] getAllRenderableChildren() {
		throw new UnsupportedOperationException("Called getAllRenderableChildren when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getStageWidth()
	 */
	@Override
	public int getStageWidth() {
		throw new UnsupportedOperationException("Called getStageWidth when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getStageHeight()
	 */
	@Override
	public int getStageHeight() {
		throw new UnsupportedOperationException("Called getStageHeight when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getStagePanel()
	 */
	@Override
	public JPanel getStagePanel() {
		throw new UnsupportedOperationException("Called getStagePanel when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#start()
	 */
	@Override
	public void start() {
		throw new UnsupportedOperationException("Called start when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isRunning()
	 */
	@Override
	public boolean isRunning() {
		throw new UnsupportedOperationException("Called isRunning when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#stop()
	 */
	@Override
	public void stop() {
		throw new UnsupportedOperationException("Called stop when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isStopped()
	 */
	@Override
	public boolean isStopped() {
		throw new UnsupportedOperationException("Called isStopped when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#getMouseStagePosition()
	 */
	@Override
	public Double getMouseStagePosition() {
		throw new UnsupportedOperationException("Called getMouseStagePosition when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isMouseDown()
	 */
	@Override
	public boolean isMouseDown() {
		throw new UnsupportedOperationException("Called isMouseDown when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#startScript(com.shtick.utils.scratch.runner.core.elements.ScriptTuple)
	 */
	@Override
	public ScriptTupleRunner startScript(ScriptTuple script) {
		throw new UnsupportedOperationException("Called startScript when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#clearPenLayer()
	 */
	@Override
	public void clearPenLayer() {
		throw new UnsupportedOperationException("Called clearPenLayer when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#bringToFront(com.shtick.utils.scratch.runner.core.elements.Sprite)
	 */
	@Override
	public void bringToFront(Sprite sprite) {
		throw new UnsupportedOperationException("Called bringToFront when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#sendToBack(com.shtick.utils.scratch.runner.core.elements.Sprite)
	 */
	@Override
	public void sendToBack(Sprite sprite) {
		throw new UnsupportedOperationException("Called sendToBack when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#sendBackNLayers(com.shtick.utils.scratch.runner.core.elements.Sprite, int)
	 */
	@Override
	public void sendBackNLayers(Sprite sprite, int n) {
		throw new UnsupportedOperationException("Called sendBackNLayers when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addComponent(java.awt.Component, int, int, int, int)
	 */
	@Override
	public void addComponent(Component component, int x, int y, int width, int height) {
		throw new UnsupportedOperationException("Called addComponent when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeComponent(java.awt.Component)
	 */
	@Override
	public void removeComponent(Component component) {
		throw new UnsupportedOperationException("Called removeComponent when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#repaintStage()
	 */
	@Override
	public void repaintStage() {
		throw new UnsupportedOperationException("Called repaintStage when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#stopAllSounds()
	 */
	@Override
	public void stopAllSounds() {
		throw new UnsupportedOperationException("Called stopAllSounds when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		throw new UnsupportedOperationException("Called addKeyListener when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void removeKeyListener(KeyListener listener) {
		throw new UnsupportedOperationException("Called removeKeyListener when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addStageMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void addStageMouseListener(MouseListener listener) {
		throw new UnsupportedOperationException("Called addStageMouseListener when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeStageMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void removeStageMouseListener(MouseListener listener) {
		throw new UnsupportedOperationException("Called removeStageMouseListener when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addStageMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void addStageMouseMotionListener(MouseMotionListener listener) {
		throw new UnsupportedOperationException("Called addStageMouseMotionListener when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeStageMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void removeStageMouseMotionListener(MouseMotionListener listener) {
		throw new UnsupportedOperationException("Called removeStageMouseMotionListener when not expected.");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isKeyPressed(java.lang.String)
	 */
	@Override
	public boolean isKeyPressed(String keyID) {
		throw new UnsupportedOperationException("Called isKeyPressed when not expected.");
	}

}
