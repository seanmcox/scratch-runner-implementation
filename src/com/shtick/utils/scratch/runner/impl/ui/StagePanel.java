/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.shtick.utils.scratch.runner.core.GraphicEffect;
import com.shtick.utils.scratch.runner.core.elements.Costume;
import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.core.elements.Stage;
import com.shtick.utils.scratch.runner.core.elements.StageMonitor;
import com.shtick.utils.scratch.runner.impl.BubbleImage;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.bundle.Activator;
import com.shtick.utils.scratch.runner.impl.bundle.GraphicEffectTracker;
import com.shtick.utils.scratch.runner.impl.elements.SpriteImplementation;
import com.shtick.utils.scratch.runner.impl.elements.StageMonitorImplementation;

/**
 * @author sean.cox
 *
 */
public class StagePanel extends JPanel {
	private static Object LAYER_LOCK = new Object();
	private BufferedImage penLayer;
	private HashMap<RenderableChild,Component> renderableChildComponents;

	/**
	 * 
	 */
	public StagePanel() {
		super(null,true);
		
		ScratchRuntimeImplementation runtime = ScratchRuntimeImplementation.getScratchRuntime();
		RenderableChild[] renderableChildren = runtime.getAllRenderableChildren();
		renderableChildComponents = new HashMap<>(renderableChildren.length);
		for(RenderableChild renderableChild:renderableChildren) {
			if(renderableChild instanceof Sprite)
				continue;
			if(renderableChild instanceof StageMonitor) {
				StageMonitor stageMonitor = (StageMonitor)renderableChild;
				Component component = null;
				switch(stageMonitor.getMode()) {
				case StageMonitor.MODE_NORMAL:
					component = new MonitorNormal((StageMonitorImplementation)stageMonitor);
					break;
				case StageMonitor.MODE_LARGE:
				case StageMonitor.MODE_SLIDER:
				default:
					System.out.println("Stage monitor mode not yet implemented: "+stageMonitor.getMode());
					continue;
				}
				
				if(component!=null) {
					renderableChildComponents.put(stageMonitor,component);
				}
				continue;
			}
			if(renderableChild instanceof List) {
				List list = (List)renderableChild;
				ScriptContext context = null;
				if(runtime.getCurrentStage().getContextListByName(list.getListName())!=null) {
					context = runtime.getCurrentStage();
				}
				else {
					for(RenderableChild child:runtime.getAllRenderableChildren()) {
						if(child instanceof Sprite) {
							Sprite sprite = (Sprite)child;
							if(sprite.getContextListByName(list.getListName())!=null) {
								context = sprite;
								break;
							}
						}
					}
				}
				
				if(context == null)
					continue;
				Component component = new ListMonitor(list,context);
				renderableChildComponents.put(renderableChild,component);
				add(component);
				continue;
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		ScratchRuntimeImplementation runtime = ScratchRuntimeImplementation.getScratchRuntime();
		int width = getWidth();
		int height = getHeight();
		
		{
			Stage stage = runtime.getCurrentStage();
			BufferedImage image = stage.getCurrentCostume().getImage();
			int imageWidth = image.getWidth(null);
			int imageHeight = image.getHeight(null);
			
			Graphics2D g2 = (Graphics2D)g.create();
			g2.scale(width/(double)imageWidth, height/(double)imageHeight);
			g2.drawImage(image, 0, 0, null);
		}
		
		if(penLayer!=null){
			int imageWidth = penLayer.getWidth(null);
			int imageHeight = penLayer.getHeight(null);
			
			Graphics2D g2 = (Graphics2D)g.create();
			g2.scale(width/(double)imageWidth, height/(double)imageHeight);
			g2.drawImage(penLayer, 0, 0, null);
		}

		Graphics2D g2Scratch = (Graphics2D)g.create();
		g2Scratch.scale(width/(double)runtime.getStageWidth(), height/(double)runtime.getStageHeight());
		g2Scratch.translate(runtime.getStageWidth()/2,runtime.getStageHeight()/2);
		
		BubbleImage bubbleImage = runtime.getBubbleImage();
		RenderableChild[] children = runtime.getAllRenderableChildren();
		Rectangle rectangle = new Rectangle();
		for(RenderableChild child:children) {
			if(!child.isVisible())
				continue;
			if(child instanceof Sprite) {
				Sprite sprite = (Sprite) child;
				synchronized(sprite.getSpriteLock()) {
					Costume costume = sprite.getCurrentCostume();
					BufferedImage image = deepCopy(costume.getImage());
					
					// Contrary to the usual Scratch mangling of standard practices,
					// centerX and centerY have the usual meaning of being values
					// relative to the upper-left corner of the image, with values
					// increasing left to right and top to bottom respectively.
					int centerX = costume.getRotationCenterX();
					int centerY = costume.getRotationCenterY();
					
					// TODO Scale the image using a smoother algorithm rather than scaling the Graphics2D. (Current scaling method doesn't look good.)
					Graphics2D g2 = (Graphics2D)g2Scratch.create();
					g2.translate(sprite.getScratchX(), -sprite.getScratchY());
					g2.scale(sprite.getScale()/costume.getBitmapResolution(), sprite.getScale()/costume.getBitmapResolution());
					switch(sprite.getRotationStyle()) {
					case "normal":
						g2.rotate((sprite.getDirection()-90)*Math.PI/180);
						break;
					case "leftRight":
						if(sprite.getDirection()<0)
							g2.scale(-1.0, 1.0);
						break;
					default:
						break;
					}
					Map<String,Double> effects = ((SpriteImplementation)sprite).getEffects();
					GraphicEffectTracker tracker = Activator.GRAPHIC_EFFECT_TRACKER;
					for(String name:effects.keySet()) {
						GraphicEffect effect = tracker.getGraphicEffect(name);
						if(effect==null) {
							System.err.println("WARNING: Effect not found: "+name);
							continue;
						}
						image = effect.getAffectedImage(image, effects.get(name));
					}
					g2.drawImage(image, -centerX, -centerY, null);
					
				}
			}
			else {
				Component component = renderableChildComponents.get(child);
				if(component == null)
					continue;
				synchronized(getTreeLock()) {
					Rectangle cr;

					cr = component.getBounds(rectangle);
	
					boolean hitClip = g.hitClip(cr.x, cr.y, cr.width, cr.height);
					if (hitClip) {
	                    Graphics cg = g.create(cr.x, cr.y, cr.width,
	                            cr.height);
					    cg.setColor(component.getForeground());
					    cg.setFont(component.getFont());
					    component.paint(cg);
					}
				}
			}
		}

		if((bubbleImage!=null)&&(bubbleImage.getSprite()!=null)) {
			Graphics2D g2 = (Graphics2D)g2Scratch.create();
			g2.translate(bubbleImage.getSprite().getScratchX(), -bubbleImage.getSprite().getScratchY());

			Area shape = bubbleImage.getSprite().getSpriteShape();
			Rectangle bounds = shape.getBounds();
			g2.drawImage(bubbleImage.getImage(), bounds.x+bounds.width, bounds.y-bubbleImage.getImage().getHeight(null), null);
		}
		
//		super.paintChildren(g);
	}

	/**
	 * 
	 * @return A Graphics2D object that will let you paint on the background using a coordinate system similar to scratch's, but with the y-axis reversed. (ie. centered on the middle, with x increasing to the right and y increasing to the bottom. The width and the height are the stage width and height.)
	 */
	public Graphics2D getPenLayerGraphics() {
		synchronized(LAYER_LOCK) {
			if(penLayer==null)
				clearPenLayer();
			Graphics2D retval = (Graphics2D)penLayer.getGraphics();
			ScratchRuntimeImplementation runtime = ScratchRuntimeImplementation.getScratchRuntime();
			retval.translate(runtime.getStageWidth()/2, runtime.getStageHeight()/2);
			return retval;
		}
	}
	
	/**
	 * Clears all marks on the pen layer.
	 */
	public void clearPenLayer() {
		synchronized(LAYER_LOCK) {
			ScratchRuntimeImplementation runtime = ScratchRuntimeImplementation.getScratchRuntime();
			penLayer = new BufferedImage(runtime.getStageWidth(),runtime.getStageHeight(), java.awt.Transparency.TRANSLUCENT);
		}
	}
	
	/**
	 * Used to add a component to the top layer (above all the sprites) of the stage.
	 * Uses a coordinate system similar to scratch's, but with the y-axis reversed.
	 * ie. centered on the middle, with x increasing to the right and y increasing to the bottom.
	 * The width and the height are the stage width and height.
	 * 
	 * @param component
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void addComponent(Component component, int x, int y, int width, int height) {
		// TODO
	}
	
	/**
	 * Removes the given component from the stage.
	 * 
	 * @param component
	 */
	public void removeComponent(Component component) {
		// TODO
	}
	
	/**
	 * 
	 * @param bi
	 * @return
	 */
	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}
