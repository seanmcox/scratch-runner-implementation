/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.ui;

import java.awt.BasicStroke;
import java.awt.Color;
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
	private static final Color COLOR_BACKGROUND = new Color(160, 160, 160);
	private ScriptContext context;
	private int nameWidth;
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
		nameWidth = getFontMetricsLarge().stringWidth(list.getListName());
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
		int currentY=lineHeight+3;
		g2.drawString(name, (getWidth()-nameWidth)/2, currentY);
		
		g2.setFont(FONT_NORMAL);
		Object[] values = list.getContents();
		for(int i=0;i<values.length;i++) {
			g2.setColor(Color.BLACK);
			g2.drawString(""+(i+1), 10, currentY+lineHeight);
			
			g2.setColor(Color.WHITE);
			g2.drawString(""+(i+1), 30, currentY+lineHeight);
			currentY+=lineHeight;
		}
		currentY+=lineHeight;
		g2.setColor(Color.BLACK);
		String text = "length: "+values.length;
		int textWidth = getFontMetricsLarge().stringWidth(text);
		g2.drawString(text, (getWidth()-textWidth)/2, currentY);
		
		g2.setColor(Color.GRAY);
		g2.setStroke(PANEL_STROKE);
		g2.draw(panelShape);
		paintChildren(g);
	}

}
