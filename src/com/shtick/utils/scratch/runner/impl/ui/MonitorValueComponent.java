/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import com.shtick.utils.scratch.runner.core.ScratchRuntime;
import com.shtick.utils.scratch.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch.runner.core.StageMonitorCommand;
import com.shtick.utils.scratch.runner.core.ValueListener;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl.bundle.Activator;

/**
 * @author sean.cox
 *
 */
public class MonitorValueComponent extends JComponent{
	private static final Stroke PANEL_STROKE = new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final int VERTICAL_PAD = 1;
	private static final int HORIZONTAL_PAD = 1;
	private int fontHeight;
	private String command;
	private String param;
	private Color backgroundColor;
	private Font font;
	private FontMetrics fontMetrics;
	private ScriptContext context;

	/**
	 * @param command 
	 * @param param 
	 * @param target 
	 * @param backgroundColor 
	 * @param font 
	 * @param fontMetrics 
	 * 
	 */
	public MonitorValueComponent(String command, String param, String target, Color backgroundColor, Font font, FontMetrics fontMetrics) {
		this.command = command;
		this.param = param;
		this.backgroundColor = backgroundColor;
		this.font = font;
		this.fontMetrics = fontMetrics;
		fontHeight = fontMetrics.getHeight();
		context = ScratchRuntimeImplementation.getScratchRuntime().getScriptContextByName(target);
		setDoubleBuffered(false);

		setPreferredSize(new Dimension(50,fontHeight+VERTICAL_PAD*2));
		StageMonitorCommand monitorCommand = Activator.STAGE_MONITOR_COMMAND_TRACKER.getCommand(command);
		if(monitorCommand==null) {
			System.err.println("Error: StageMonitorCommand not found: "+command);
			return;
		}
		monitorCommand.addValueListener(new ValueListener() {
			String[] arguments = new String[] {param};
			@Override
			public void valueUpdated(Object oldValue, Object newValue) {
				repaint();
			}
			
			@Override
			public ScriptTupleRunner getScriptRunner() {
				return null;
			}
			
			@Override
			public ScriptContext getScriptContext() {
				return context;
			}
			
			@Override
			public ScratchRuntime getRuntime() {
				return ScratchRuntimeImplementation.getScratchRuntime();
			}
			
			@Override
			public Object[] getArguments() {
				return arguments;
			}
		});
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		Shape panelShape = new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 5, 5);
		g2.setColor(backgroundColor);
		g2.fill(panelShape);
		g2.setColor(Color.WHITE);
		g2.setStroke(PANEL_STROKE);
		g2.draw(panelShape);
		
		g2.setFont(font);
		StageMonitorCommand commandImpl = Activator.STAGE_MONITOR_COMMAND_TRACKER.getCommand(command);
		String text;
		if(commandImpl==null)
			text = "Err";
		else
			text = commandImpl.execute(ScratchRuntimeImplementation.getScratchRuntime(), context, param);
		int labelWidth = fontMetrics.stringWidth(text);
		g2.drawString(text, (getWidth()-labelWidth)/2, VERTICAL_PAD+fontMetrics.getAscent());
		
		paintChildren(g);
	}

}
