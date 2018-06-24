/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;

/**
 * 
 * @author sean.cox
 *
 */
public class ListMonitor extends MonitorComponent{
	private static final Stroke PANEL_STROKE = new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final Stroke ITEM_PANEL_STROKE = new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final Color COLOR_BACKGROUND = new Color(193, 196, 199);
	private static final Color COLOR_ITEM_BACKGROUND = new Color(204, 91, 34);
	private static final Color COLOR_SELECTED_ITEM_BACKGROUND = new Color(244, 188, 159);
	private static final Color COLOR_HIGHLIGHT_BACKGROUND = new Color(181, 213, 255);
	private static final Color COLOR_SCROLLBAR = new Color(106, 108, 111);
	private static final Color COLOR_SCROLLBAR_BACKGROUND = new Color(199, 202, 204);
	private static final Pattern LTRIM = Pattern.compile("^\\s+");
	private static final Pattern RTRIM = Pattern.compile("\\s+$");
	private static final Pattern WORDWRAP_ADJUST = Pattern.compile("^(.*[\\s-]+)[^\\s-]+$");
	private ScriptContext context;
	private String monitorTitle;
	private int titleWidth;
	private int lineHeight;
	private Rectangle scrollHandleRectangle = new Rectangle(0, 0, 0, 0);
	private Rectangle scrollBarRectangle = new Rectangle(0, 0, 0, 0);
	private int scrollRectangleBaseY=-1;
	private int rectangleReferenceY=0;
	private int mouseReferenceY=0;
	private int scrollRectForItemCount = -1;
	
	private ItemDetails selectedItem = null;
	private int selectedTextI = -1;
	private int selectedTextLength = 0;
	private ArrayList<ItemDetails> visibleItemDetails = new ArrayList<>();
	
	/**
	 * @param list 
	 * @param context 
	 * 
	 */
	public ListMonitor(List list, ScriptContext context) {
		super(list);
		this.context = context;
		setLayout(null);
		setBounds(list.getX().intValue(),list.getY().intValue(), list.getWidth().intValue(), list.getHeight().intValue());
		
		// Some rendering precalculation.
		monitorTitle = context.getObjName()+": "+list.getListName();
		titleWidth = getFontMetricsLarge().stringWidth(monitorTitle);
		lineHeight = getFontMetricsLarge().getHeight();
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {
				Point dataPoint = e.getPoint();
				dataPoint.y-=lineHeight;
				System.out.println(dataPoint);
				if(scrollBarRectangle.contains(dataPoint)) {
					setScrollHandlePositionByDataPoint(dataPoint);
					mouseReferenceY = dataPoint.y;
				}
				else {
					mouseReferenceY = -1;
				}
				int oldIndex = (selectedItem!=null)?selectedItem.index:-1;
				identifySelectedItem(dataPoint);
				if(((selectedItem!=null)?selectedItem.index:-1)!=oldIndex)
					repaint();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					synchronized(visibleItemDetails) {
						if(selectedItem!=null) {
							selectedTextI = 0;
							selectedTextLength = selectedItem.fulltext.length();
						}
					}
				}
			}
		});

		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(mouseReferenceY>0) {
					Point dataPoint = e.getPoint();
					dataPoint.y-=lineHeight;
					setScrollHandlePositionByDataPoint(dataPoint);
				}
			}
		});
	}
	
	/**
	 * 
	 * @param dataPoint The point with respect to the data display area of the monitor. (Clipped vertically at the top and bottom of the scrollbar.)
	 */
	private void setScrollHandlePositionByDataPoint(Point dataPoint) {
		scrollHandleRectangle.y = Math.min(Math.max(dataPoint.y-5,scrollRectangleBaseY),scrollBarRectangle.height-scrollHandleRectangle.height);
		repaint();
	}

	private void identifySelectedItem(Point p) {
		synchronized(visibleItemDetails) {
			for(ItemDetails itemDetails:visibleItemDetails) {
				if(itemDetails.shape.contains(p.x, p.y)) {
					selectedItem = itemDetails;
					return;
				}
			}
			selectedItem = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		String name = ((List)getRenderableChild()).getListName();
		List list = context.getContextListByName(name);
		if(!list.isVisible())
			return;
		
		Shape panelShape = new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10);
	
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(COLOR_BACKGROUND);
		g2.fill(panelShape);

		g2.setFont(FONT_LARGE);
		g2.setColor(Color.BLACK);
		g2.drawString(monitorTitle, (getWidth()-titleWidth)/2, lineHeight);
		
		paintDataArea((Graphics2D)g2.create(0, lineHeight, getWidth(), getHeight()-lineHeight*2), list);
		
		g2.setFont(FONT_MINOR);
		g2.setColor(Color.BLACK);
		String text = "length: "+list.getItemCount();
		int textWidth = getFontMetricsLarge().stringWidth(text);
		g2.drawString(text, (getWidth()-textWidth)/2, getHeight()-5);
		
		g2.setColor(Color.GRAY);
		g2.setStroke(PANEL_STROKE);
		g2.draw(panelShape);
				
		paintChildren(g);
	}
	
	private void paintDataArea(Graphics2D g2, List list) {
		Object[] values = list.getContents();
		int currentY = 0;
		int startY = 0;
		FontMetrics fontMetricsMinor = getFontMetricsMinor();
		int simpleItemLineHeight = lineHeight+3;
		int baseWidth = fontMetricsMinor.stringWidth(""+values.length);
		Shape itemPanelShape;
		int clipHeight = g2.getClipBounds().height;
		int simpleLinesPerScreen = clipHeight/simpleItemLineHeight;
		if(simpleLinesPerScreen == 0)
			return;
		int dataWidth = getWidth()-(baseWidth+10+20);

		synchronized(visibleItemDetails) {
			// Define starting scrollbar.
			if(scrollRectangleBaseY<0) {
				scrollRectangleBaseY = 0;
				scrollHandleRectangle.x = getWidth()-13;
				scrollHandleRectangle.y = 0;
				scrollHandleRectangle.width = 10;
				scrollHandleRectangle.height = Math.max(1, clipHeight*simpleLinesPerScreen/values.length);
				scrollBarRectangle.x = getWidth()-13;
				scrollBarRectangle.y = 0;
				scrollBarRectangle.width = 10;
				scrollBarRectangle.height = clipHeight;
				scrollRectForItemCount = values.length;
			}
			int i0 = values.length*scrollHandleRectangle.y/(clipHeight-scrollHandleRectangle.height);
			if(i0 >= values.length)
				i0 = values.length - 1;

			// Paint data lines
			String text;
			ItemDetails itemDetails;
			visibleItemDetails.clear();
			for(int i=i0;(currentY<clipHeight)&&(i<values.length);i++) {
				text = ""+values[i];
				
				itemPanelShape = new RoundRectangle2D.Float(baseWidth+10, currentY+4, dataWidth, lineHeight+3, 5, 5);
				itemDetails = new ItemDetails(i, itemPanelShape, text, new int[] {0}, new String[] {text});
				createTextLines(itemDetails, dataWidth-10, getFontMetricsNormal());
				((RoundRectangle2D.Float)itemPanelShape).height = lineHeight*itemDetails.lines.length+3;
				visibleItemDetails.add(itemDetails);
				if((selectedItem!=null)&&(selectedItem.index == i))
					g2.setColor(COLOR_SELECTED_ITEM_BACKGROUND);
				else
					g2.setColor(COLOR_ITEM_BACKGROUND);
				g2.fill(itemPanelShape);
	
				startY = currentY;
				
				g2.setFont(FONT_NORMAL);
				g2.setColor(Color.WHITE);
				for(String line:itemDetails.lines) {
					currentY += lineHeight;
					g2.drawString(line, baseWidth+15, currentY);
				}
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(ITEM_PANEL_STROKE);
				g2.draw(itemPanelShape);
				g2.setStroke(oldStroke);
				
				g2.setFont(FONT_MINOR);
				g2.setColor(Color.BLACK);
				g2.drawString(""+(i+1), 5+baseWidth-fontMetricsMinor.stringWidth(""+(i+1)), (startY+currentY+lineHeight)/2);

				currentY+=3;
			}

			// Paint scrollbar
			if(visibleItemDetails.size()<values.length) {
				Shape scrollAreaShape = new RoundRectangle2D.Float(scrollBarRectangle.x, scrollBarRectangle.y, scrollBarRectangle.width, scrollBarRectangle.height, 10, 10);
				g2.setColor(COLOR_SCROLLBAR_BACKGROUND);
				g2.fill(scrollAreaShape);
				Shape scrollHandleShape = new RoundRectangle2D.Float(scrollHandleRectangle.x, scrollHandleRectangle.y, scrollHandleRectangle.width, scrollHandleRectangle.height, 10, 10);
				g2.setColor(COLOR_SCROLLBAR);
				g2.fill(scrollHandleShape);
			}
		}
	}
	
	private class ItemDetails{
		public int index;
		public Shape shape;
		public String fulltext;
		public String[] lines;
		public int[] lineIndexes;
		
		/**
		 * 
		 * @param index
		 * @param shape
		 * @param fullText
		 * @param lineIndexes 
		 * @param lines
		 */
		public ItemDetails(int index, Shape shape, String fullText, int[] lineIndexes, String[] lines) {
			this.index = index;
			this.shape = shape;
			this.fulltext = fullText;
			this.lineIndexes = lineIndexes;
			this.lines = lines;
		}
	}
	/**
	 * 
	 * @param itemDetails 
	 * @param width 
	 * @param fontMetrics 
	 */
	private static void createTextLines(ItemDetails itemDetails, int width, FontMetrics fontMetrics) {
		String text = itemDetails.fulltext;
		if((text==null)||(text.length()==0)) {
			itemDetails.lines = new String[] {""};
			itemDetails.lineIndexes = new int[] {0};
			return;
		}
		Stack<String> lines = new Stack<>();
		Stack<Integer> lineIndexes = new Stack<>();
		text = RTRIM.matcher(text).replaceAll("");
		lines.push(text);
		lineIndexes.push(0);
		while(processWrapRemainder(lines, lineIndexes, width, fontMetrics));
		itemDetails.lines = lines.toArray(new String[lines.size()]);
		itemDetails.lineIndexes = new int[lines.size()];
		int i=0;
		for(Integer lineIndex:lineIndexes.toArray(new Integer[lineIndexes.size()])) {
			itemDetails.lineIndexes[i] = lineIndex;
			i++;
		}
	}
	/**
	 * 
	 * @param lines
	 * @param lineIndexes
	 * @param lineWidth 
	 * @param fontMetrics 
	 * @return An image containing the text layed out on a transparent background.
	 */
	private static boolean processWrapRemainder(Stack<String> lines, Stack<Integer> lineIndexes, int lineWidth, FontMetrics fontMetrics) {
		String line = lines.pop();
		int remainderWidth = fontMetrics.stringWidth(line);
		if(remainderWidth<=lineWidth) {
			lines.push(line);
			return false;
		}
		
		int l = line.length()*lineWidth/remainderWidth;
		String subline = line.substring(0, l);
		int lWidth = fontMetrics.stringWidth(subline);
		if(lWidth>lineWidth) {
			while(lWidth>lineWidth) {
				l--;
				subline = line.substring(0, l);
				lWidth = fontMetrics.stringWidth(subline);
			}
		}
		else if(lWidth<lineWidth){
			while(lWidth<lineWidth) {
				l++;
				subline = line.substring(0, l);
				lWidth = fontMetrics.stringWidth(subline);
			}
			if(lWidth>lineWidth)
				l--;
		}
		if(l==0)
			l++;
		subline = line.substring(0, l);
		char c = line.charAt(l);
		Matcher matcher = WORDWRAP_ADJUST.matcher(subline);
		String remainder;
		if(matcher.matches()&&(!Character.isWhitespace(c))) {
			// No breaking characters
			subline = matcher.group(1);
		}
		remainder = line.substring(subline.length());
		remainder = LTRIM.matcher(remainder).replaceAll("");
		lines.push(subline);
		if(remainder.length()==0)
			return false;
		lines.push(remainder);
		lineIndexes.push(lineIndexes.peek()+line.length()-remainder.length());
		return true;
	}
}
