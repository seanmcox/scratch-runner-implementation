/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JLabel;

import com.shtick.utils.scratch.runner.impl.elements.StageMonitorImplementation;

/**
 * @author sean.cox
 *
 */
public class MonitorNormal extends MonitorComponent{
	private static final Stroke PANEL_STROKE = new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final Color COLOR_BACKGROUND = new Color(160, 160, 160);
	private static final int VERTICAL_PAD = 2;
	private static final int HORIZONTAL_PAD = 5;
	private int labelWidth;
	private int fontHeight;

	/**
	 * @param monitor 
	 * 
	 */
	public MonitorNormal(StageMonitorImplementation monitor) {
		super(monitor);
		setLayout(new FlowLayout(FlowLayout.LEFT,HORIZONTAL_PAD,VERTICAL_PAD));
		labelWidth = getFontMetricsNormal().stringWidth(monitor.getLabel());
		fontHeight = getFontMetricsNormal().getHeight();
		JLabel label = new JLabel(monitor.getLabel());
		label.setFont(FONT_NORMAL);
		label.setForeground(Color.BLACK);
		add(label);
		add(new MonitorValueComponent(monitor.getCmd(), monitor.getParam(), monitor.getTarget(), new Color(monitor.getColor()), FONT_NORMAL, getFontMetricsNormal()));

		Dimension preferredSize = getPreferredSize();
		setBounds(monitor.getX(),monitor.getY(), preferredSize.width, preferredSize.height);
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		if(!getRenderableChild().isVisible())
			return;
		Graphics2D g2 = (Graphics2D)g;
		Shape panelShape = new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		g2.setColor(COLOR_BACKGROUND);
		g2.fill(panelShape);
		g2.setColor(Color.GRAY);
		g2.setStroke(PANEL_STROKE);
		g2.draw(panelShape);
		paintChildren(g);
	}

}
