/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.TreeSet;

import com.shtick.utils.scratch.runner.core.elements.Costume;

/**
 * @author sean.cox
 *
 */
public class CostumeImplementation implements Costume{
	private String costumeName;
	private long baseLayerID;
	private String baseLayerMD5;
	private int bitmapResolution;
	private ImageAndArea imageAndArea;
	
	// Pooling manipulated images to conserve resources.
	private HashMap<String,ImageAndArea> imagePool = new HashMap<>();
	private HashMap<SpriteImplementation,String> spriteRegistry = new HashMap<>();
	
	/**
	 * @param costumeName
	 * @param baseLayerID
	 * @param baseLayerMD5
	 * @param bitmapResolution
	 * @param rotationCenterX
	 * @param rotationCenterY
	 * @param image 
	 */
	public CostumeImplementation(String costumeName, long baseLayerID, String baseLayerMD5, int bitmapResolution, int rotationCenterX,
			int rotationCenterY, BufferedImage image) {
		super();
		this.costumeName = costumeName;
		this.baseLayerID = baseLayerID;
		this.baseLayerMD5 = baseLayerMD5;
		this.bitmapResolution = bitmapResolution;
		imageAndArea = new ImageAndArea(image, rotationCenterX, rotationCenterY);
	}

	@Override
	public String getCostumeName() {
		return costumeName;
	}

	@Override
	public long getBaseLayerID() {
		return baseLayerID;
	}

	@Override
	public String getBaseLayerMD5() {
		return baseLayerMD5;
	}

	@Override
	public int getBitmapResolution() {
		return bitmapResolution;
	}

	@Override
	public int getRotationCenterX() {
		return imageAndArea.rotationCenterX;
	}

	@Override
	public int getRotationCenterY() {
		return imageAndArea.rotationCenterY;
	}

	@Override
	public BufferedImage getImage() {
		return imageAndArea.image;
	}
	
	Area getCostumeArea() {
		return imageAndArea.getCostumeArea();
	}
	
	ImageAndArea getScaledAndRotatedImage(SpriteImplementation sprite) {
		synchronized(imagePool) {
			String key = spriteRegistry.get(sprite);
			if(key==null)
				return null;
			return imagePool.get(key);
		}
	}
	
	/**
	 * 
	 * @param sprite 
	 * @param scale
	 * @param direction In radians, indicating a clockwise direction starting from the right. Value must be >=0 and &lt;2*Math.PI
	 * @param mirror For left/right style images, then when this is true, the direction should be set as if there were no rotation at all.
	 *               The image is flipped first, and then rotated. The overall affect allows for mirror images in any direction, although scratch does not support such a style.
	 */
	void registerSprite(SpriteImplementation sprite, double scale, double direction, boolean mirror) {
		String key = ""+ (int)(scale*1000)+"-"+(int)(direction*180/Math.PI)+"-"+(mirror?"t":"f");
		synchronized(imagePool) {
			String oldKey = spriteRegistry.get(sprite);
			if(oldKey!=null) {
				if(oldKey.equals(key))
					return;
				if((--imagePool.get(oldKey).usageCount)==0)
					imagePool.remove(oldKey);
			}
			ImageAndArea imageAndArea = imagePool.get(key);
			if(imageAndArea == null) {
				Area area = (Area)this.imageAndArea.getCostumeArea().clone();
				double compositeScale = scale/bitmapResolution;
				if(compositeScale!=1.0) {
					if(mirror)
						area.transform(AffineTransform.getScaleInstance(-compositeScale, compositeScale));
					else
						area.transform(AffineTransform.getScaleInstance(compositeScale, compositeScale));
				}
				if(direction>0)
					area.transform(AffineTransform.getRotateInstance(direction));
				Rectangle bounds = area.getBounds();
				if((bounds.width<=0)||(bounds.height<=0)) 
					return;

				// TODO Scale the image using a smoother algorithm rather than scaling the Graphics2D. (Current scaling method doesn't look good.)
				BufferedImage scaledImage = new BufferedImage(bounds.width,bounds.height,BufferedImage.TRANSLUCENT);
				Graphics2D g2 = (Graphics2D)scaledImage.getGraphics();
				g2.translate(-bounds.x, -bounds.y);
				if(mirror)
					g2.scale(-compositeScale,compositeScale);
				else
					g2.scale(compositeScale,compositeScale);
				if(direction>0)
					g2.rotate(direction);
				g2.drawImage(this.imageAndArea.image, -this.imageAndArea.rotationCenterX, -this.imageAndArea.rotationCenterY, null);

				imageAndArea = new ImageAndArea(scaledImage,-bounds.x,-bounds.y,area);
				
				imagePool.put(key, imageAndArea);
			}
			imageAndArea.usageCount++;
			spriteRegistry.put(sprite, key);
		}
	}
	
	/**
	 * Unregistering only needs to happen when a clone is destroyed or when a costume is changed.
	 * For all scale/direction/mirror updates, it is enough to call registerSprite repeatedly.
	 *  
	 * @param sprite
	 */
	void unregisterSprite(SpriteImplementation sprite) {
		synchronized(imagePool) {
			String key = spriteRegistry.remove(sprite);
			if(key==null)
				return;
			if((--imagePool.get(key).usageCount)==0)
				imagePool.remove(key);
		}
	}
	
	private static class PointAndSide implements Comparable<PointAndSide>{
		public static final int TOP = 0;
		public static final int BOTTOM = 1;
		public static final int LEFT = 2;
		public static final int RIGHT = 3;
		int x;
		int y;
		int side;
		
		/**
		 * @param x
		 * @param y
		 * @param side
		 */
		public PointAndSide(int x, int y, int side) {
			super();
			this.x = x;
			this.y = y;
			this.side = side;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + side;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PointAndSide))
				return false;
			PointAndSide other = (PointAndSide) obj;
			if (side != other.side)
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(PointAndSide o) {
			if(o==null)
				return 1;
			if(y!=o.y)
				return y-o.y;
			if(x!=o.x)
				return x-o.x;
			return side-o.side;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "PointAndSide [x=" + x + ", y=" + y + ", side=" + side + "]";
		}
	}
	
	/**
	 * An object which contains both an image and some associated meta-data.
	 * 
	 * @author scox
	 *
	 */
	public class ImageAndArea {
		/**
		 * A BufferedImage.
		 */
		public BufferedImage image;

		/**
		 * The x-coordinate of the image's center of rotation.
		 */
		public int rotationCenterX;

		/**
		 * The y-coordinate of the image's center of rotation.
		 */
		public int rotationCenterY;
		
		/**
		 * A value for tracking how many sprites are using this object.
		 */
		public int usageCount = 0;
		
		/**
		 * An area representing the bounds of the image for the purpose of collision detection.
		 * Centered on the costume's rotation center.
		 */
		private Area area=null;

		/**
		 * 
		 * @param image
		 * @param rotationCenterX
		 * @param rotationCenterY
		 */
		public ImageAndArea(BufferedImage image, int rotationCenterX, int rotationCenterY) {
			this.image = image;
			this.rotationCenterX = rotationCenterX;
			this.rotationCenterY = rotationCenterY;
		}
		
		/**
		 * 
		 * @param image
		 * @param rotationCenterX
		 * @param rotationCenterY
		 * @param area
		 */
		public ImageAndArea(BufferedImage image, int rotationCenterX, int rotationCenterY, Area area) {
			super();
			this.image = image;
			this.rotationCenterX = rotationCenterX;
			this.rotationCenterY = rotationCenterY;
			this.area = area;
		}

		synchronized Area getCostumeArea() {
			if(area!=null)
				return area;
			TreeSet<PointAndSide> edges = new TreeSet<>();
			int rgb;
			int x,y;
			boolean[] previous = new boolean[image.getWidth()];
			boolean[] current = new boolean[image.getWidth()];
			boolean[] tt;
			for(y=0;y<image.getHeight();y++) {
				for(x=0;x<image.getWidth();x++) {
					rgb = image.getRGB(x, y);
					rgb >>= 24;
					current[x] = (rgb!=0);
					if(current[x]) {
						if((x==0)||(!current[x-1]))
							edges.add(new PointAndSide(x,y,PointAndSide.LEFT));
						else if(x==current.length-1)
							edges.add(new PointAndSide(x,y,PointAndSide.RIGHT));
						if((y==0)||(!previous[x]))
							edges.add(new PointAndSide(x,y,PointAndSide.TOP));
						else if(y==image.getHeight()-1)
							edges.add(new PointAndSide(x,y,PointAndSide.BOTTOM));
					}
					else {
						if((x>0)&&(current[x-1]))
							edges.add(new PointAndSide(x-1,y,PointAndSide.RIGHT));
						if((y>0)&&(previous[x]))
							edges.add(new PointAndSide(x,y-1,PointAndSide.BOTTOM));
					}
				}
				tt = current;
				current = previous;
				previous = tt;
			}
			
			GeneralPath path=new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			PointAndSide testEdge = new PointAndSide(0, 0, 0);
			PointAndSide tempEdge;
			boolean foundNext = false;
			int parts = 0;
			while(edges.size()>0) {
				parts++;
				PointAndSide edge = edges.iterator().next();
				path.moveTo(edge.x, edge.y);
				while(edge!=null) {
					foundNext = false;
					edges.remove(edge);
					testEdge.x = edge.x;
					testEdge.y = edge.y;
					switch(edge.side) {
					case PointAndSide.TOP:{
						testEdge.side = PointAndSide.LEFT;
						if(edges.contains(testEdge)) {
							foundNext=true;
						}
						else if(testEdge.x>0) {
							testEdge.x--;
							testEdge.side = PointAndSide.TOP;
							if(edges.contains(testEdge)) {
								foundNext=true;
							}
							else if(testEdge.y>0) {
								testEdge.y--;
								testEdge.side = PointAndSide.RIGHT;
								if(edges.contains(testEdge)) {
									foundNext=true;
								}
							}
						}
						break;
					}
					case PointAndSide.LEFT:{
						testEdge.side = PointAndSide.BOTTOM;
						if(edges.contains(testEdge)) {
							foundNext=true;
						}
						else if(testEdge.y<image.getHeight()-1) {
							testEdge.y++;
							testEdge.side = PointAndSide.LEFT;
							if(edges.contains(testEdge)) {
								foundNext=true;
							}
							else if(testEdge.x>0) {
								testEdge.x--;
								testEdge.side = PointAndSide.TOP;
								if(edges.contains(testEdge)) {
									foundNext=true;
								}
							}
						}
						break;
					}
					case PointAndSide.BOTTOM:{
						testEdge.side = PointAndSide.RIGHT;
						if(edges.contains(testEdge)) {
							foundNext=true;
						}
						else if(testEdge.x<image.getWidth()-1) {
							testEdge.x++;
							testEdge.side = PointAndSide.BOTTOM;
							if(edges.contains(testEdge)) {
								foundNext=true;
							}
							else if(testEdge.y<image.getHeight()-1) {
								testEdge.y++;
								testEdge.side = PointAndSide.LEFT;
								if(edges.contains(testEdge)) {
									foundNext=true;
								}
							}
						}
						break;
					}
					case PointAndSide.RIGHT:{
						testEdge.side = PointAndSide.TOP;
						if(edges.contains(testEdge)) {
							foundNext=true;
						}
						else if(testEdge.y>0) {
							testEdge.y--;
							testEdge.side = PointAndSide.RIGHT;
							if(edges.contains(testEdge)) {
								foundNext=true;
							}
							else if(testEdge.x<image.getWidth()-1) {
								testEdge.x++;
								testEdge.side = PointAndSide.BOTTOM;
								if(edges.contains(testEdge)) {
									foundNext=true;
								}
							}
						}
						break;
					}
					}
					if(foundNext) {
						tempEdge = edge;
						edge = testEdge;
						testEdge = tempEdge;
						path.lineTo(edge.x, edge.y);
					}
					else {
						edge = null;
					}
				}
			}
			if(parts>0)
				path.closePath();
			area = new Area(path);
			area.transform(AffineTransform.getTranslateInstance(-rotationCenterX, -rotationCenterY));
			return area;
		}
	}
}
