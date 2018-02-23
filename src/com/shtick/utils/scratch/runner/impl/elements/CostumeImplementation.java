/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
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
	private int rotationCenterX;
	private int rotationCenterY;
	private BufferedImage image;
	private Area costumeArea = null;
	
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
		this.rotationCenterX = rotationCenterX;
		this.rotationCenterY = rotationCenterY;
		this.image = image;
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
		return rotationCenterX;
	}

	@Override
	public int getRotationCenterY() {
		return rotationCenterY;
	}

	@Override
	public BufferedImage getImage() {
		return image;
	}
	
	Area getCostumeArea() {
		synchronized(image) {
			if(costumeArea!=null)
				return costumeArea;
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
			costumeArea = new Area(path);
			costumeArea.transform(AffineTransform.getTranslateInstance(-rotationCenterX, -rotationCenterY));
			return costumeArea;
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
}
