/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;

/**
 * TODO Fine tune the Look and Feel
 * 
 * @author sean.cox
 *
 */
public class ListMonitor extends MonitorComponent{
	private static final Stroke PANEL_STROKE = new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final Stroke ITEM_PANEL_STROKE = new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final Color COLOR_BACKGROUND = new Color(193, 196, 199);
	private static final Color COLOR_ITEM_BACKGROUND = new Color(204, 91, 34);
	private ScriptContext context;
	private String monitorTitle;
	private int titleWidth;
	private int lineHeight;

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
		int currentY=lineHeight;
		g2.drawString(monitorTitle, (getWidth()-titleWidth)/2, currentY);
		
		FontMetrics fontMetricsMinor = getFontMetricsMinor();
		Object[] values = list.getContents();
		int itemLineHeight = lineHeight+3;
		int itemLines = Math.min(values.length, ((getHeight()-itemLineHeight - currentY))/itemLineHeight);
		int baseWidth = fontMetricsMinor.stringWidth(""+itemLines);
		Shape itemPanelShape;
		for(int i=0;i<itemLines;i++) {
			itemPanelShape = new RoundRectangle2D.Float(baseWidth+10, currentY+4, getWidth()-(baseWidth+10+20), lineHeight+3, 5, 5);
			g2.setColor(COLOR_ITEM_BACKGROUND);
			g2.fill(itemPanelShape);

			g2.setFont(FONT_MINOR);
			g2.setColor(Color.BLACK);
			g2.drawString(""+(i+1), 5+baseWidth-fontMetricsMinor.stringWidth(""+(i+1)), currentY+lineHeight+2);
			
			g2.setFont(FONT_NORMAL);
			g2.setColor(Color.WHITE);
			g2.drawString(""+values[i], baseWidth+15, currentY+lineHeight);
			Stroke oldStroke = g2.getStroke();
			g2.setStroke(ITEM_PANEL_STROKE);
			g2.draw(itemPanelShape);
			g2.setStroke(oldStroke);
			currentY+=itemLineHeight;
		}
		g2.setFont(FONT_MINOR);
		currentY+=lineHeight;
		g2.setColor(Color.BLACK);
		String text = "length: "+values.length;
		int textWidth = getFontMetricsLarge().stringWidth(text);
		g2.drawString(text, (getWidth()-textWidth)/2, getHeight()-5);
		
		g2.setColor(Color.GRAY);
		g2.setStroke(PANEL_STROKE);
		g2.draw(panelShape);
		paintChildren(g);
	}

}
