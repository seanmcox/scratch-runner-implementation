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
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JPanel;

import com.shtick.utils.scratch.runner.core.GraphicEffect;
import com.shtick.utils.scratch.runner.core.GraphicEffectRegistry;
import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.core.elements.Stage;
import com.shtick.utils.scratch.runner.core.elements.StageMonitor;
import com.shtick.utils.scratch.runner.impl.BubbleImage;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.elements.CostumeImplementation.ImageAndArea;
import com.shtick.utils.scratch.runner.impl.elements.ListImplementation;
import com.shtick.utils.scratch.runner.impl.elements.SpriteImplementation;
import com.shtick.utils.scratch.runner.impl.elements.StageMonitorImplementation;

/**
 * @author sean.cox
 *
 */
public class StagePanel extends JPanel {
	private static Object LAYER_LOCK = new Object();
	private BufferedImage penLayer;
	private BufferedImage buffer;
	private HashMap<RenderableChild,Component> renderableChildComponents;
	private LinkedList<Component> otherComponents = new LinkedList<>();
	private ScratchRuntimeImplementation runtime;
	
	// Reused painting objects
	private Rectangle paintingRectangle = new Rectangle();

	/**
	 * @param runtime 
	 * 
	 */
	public StagePanel(ScratchRuntimeImplementation runtime) {
		super(null,true);
		this.runtime = runtime;
		this.setFocusable(true);
		
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
					component = new MonitorNormal((StageMonitorImplementation)stageMonitor, runtime);
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
				
				Component component = ((ListImplementation)list).getMonitor();
				if(component!=null) {
					renderableChildComponents.put(renderableChild,component);
					add(component);
				}
				continue;
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		Graphics finalGraphics = g;
		if(buffer==null) {
			buffer = new BufferedImage(runtime.getStageWidth(),runtime.getStageHeight(), java.awt.Transparency.TRANSLUCENT);
		}
		g = buffer.getGraphics();
		
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
		for(RenderableChild child:children) {
			if(child instanceof Sprite) {
				if(!child.isVisible())
					continue;
				SpriteImplementation sprite = (SpriteImplementation) child;
				synchronized(sprite.getSpriteLock()) {
					ImageAndArea imageAndArea = sprite.getScaledAndRotatedImage();
					if(imageAndArea==null)
						continue;
					
					// Contrary to the usual Scratch mangling of standard practices,
					// centerX and centerY have the usual meaning of being values
					// relative to the upper-left corner of the image, with values
					// increasing left to right and top to bottom respectively.
					int centerX = imageAndArea.rotationCenterX;
					int centerY = imageAndArea.rotationCenterY;

					// TODO Move effect application to image pool.
					Map<String,Double> effects = ((SpriteImplementation)sprite).getEffects();
					BufferedImage img = imageAndArea.image;
					if(effects.size()>0) {
						img =  new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
						img.getGraphics().drawImage(imageAndArea.image, 0, 0, null);
						GraphicEffectRegistry registry = runtime.getGraphicEffectRegistry();
						for(String name:effects.keySet()) {
							GraphicEffect effect = registry.getGraphicEffect(name);
							if(effect==null) {
								System.err.println("WARNING: Effect not found: "+name);
								continue;
							}
							img = effect.getAffectedImage(img, effects.get(name));
						}
					}
					g2Scratch.drawImage(img, (int)(sprite.getScratchX()-centerX), (int)(-sprite.getScratchY()-centerY), null);
					
				}
			}
			else {
				Component component = renderableChildComponents.get(child);
				if(component == null)
					continue;
				if(!child.isVisible())
					continue;
				synchronized(getTreeLock()) {
					Rectangle cr;

					cr = component.getBounds(paintingRectangle);
	
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
		
		for(Component component:otherComponents) {
			synchronized(getTreeLock()) {
				Rectangle cr;

				cr = component.getBounds(paintingRectangle);

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

		finalGraphics.drawImage(buffer,0,0,null);
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
			retval.translate(runtime.getStageWidth()/2, runtime.getStageHeight()/2);
			return retval;
		}
	}
	
	/**
	 * Clears all marks on the pen layer.
	 */
	public void clearPenLayer() {
		synchronized(LAYER_LOCK) {
			penLayer = new BufferedImage(runtime.getStageWidth(),runtime.getStageHeight(), java.awt.Transparency.TRANSLUCENT);
		}
	}
	
	/**
	 * Used to add a component to the top layer (above all the sprites) of the stage.
	 * Uses a coordinate system similar to scratch's, but with the y-axis reversed.
	 * ie. centered on the middle, with x increasing to the right and y increasing to the bottom.
	 * The width and the height units are stage units.
	 * 
	 * @param component
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void addComponent(Component component, int x, int y, int width, int height) {
		removeComponent(component);
		int w = this.getWidth();
		int h = this.getHeight();
		double scaleW = runtime.getStageWidth()/(double)w;
		double scaleH = runtime.getStageHeight()/(double)h;
		width*=scaleW;
		height*=scaleH;
		x+=runtime.getStageWidth()/2;
		y+=runtime.getStageHeight()/2;
		x+=scaleW;
		y*=scaleH;
		component.setBounds(x, y, width, height);
		otherComponents.add(component);
		add(component);
	}
	
	/**
	 * Removes the given component from the stage.
	 * 
	 * @param component
	 */
	public void removeComponent(Component component) {
		otherComponents.remove(component);
		remove(component);
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
