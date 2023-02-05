/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.ui;

import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JPanel;

import com.shtick.utils.scratch.runner.core.elements.RenderableChild;

/**
 * @author sean.cox
 *
 */
public abstract class MonitorComponent extends JPanel {
	/**
	 * 
	 */
	public static final Font FONT_MINOR = new Font("sansserif", Font.PLAIN, 10);
	/**
	 * 
	 */
	public static final Font FONT_NORMAL = new Font("sansserif", Font.BOLD, 11);
	/**
	 * 
	 */
	public static final Font FONT_LARGE = new Font("sansserif", Font.BOLD, 14);
	private static FontMetrics FONT_METRICS_MINOR;
	private static FontMetrics FONT_METRICS_NORMAL;
	private static FontMetrics FONT_METRICS_LARGE;
	private RenderableChild renderableChild;
	
	
	/**
	 * @param renderableChild
	 */
	public MonitorComponent(RenderableChild renderableChild) {
		super();
		this.renderableChild = renderableChild;
	}

	/**
	 * @return the monitor
	 */
	public RenderableChild getRenderableChild() {
		return renderableChild;
	}
	
	/**
	 * @return the FontMetrics for the FONT_NORMAL
	 */
	public FontMetrics getFontMetricsMinor() {
		synchronized(FONT_MINOR) {
			if(FONT_METRICS_MINOR == null)
				FONT_METRICS_MINOR = this.getFontMetrics(FONT_MINOR);
			return FONT_METRICS_MINOR;
		}
	}
	
	/**
	 * @return the FontMetrics for the FONT_NORMAL
	 */
	public FontMetrics getFontMetricsNormal() {
		synchronized(FONT_NORMAL) {
			if(FONT_METRICS_NORMAL == null)
				FONT_METRICS_NORMAL = this.getFontMetrics(FONT_NORMAL);
			return FONT_METRICS_NORMAL;
		}
	}
	
	/**
	 * @return the FontMetrics for the FONT_LARGE
	 */
	public FontMetrics getFontMetricsLarge() {
		synchronized(FONT_LARGE) {
			if(FONT_METRICS_LARGE == null)
				FONT_METRICS_LARGE = this.getFontMetrics(FONT_LARGE);
			return FONT_METRICS_LARGE;
		}
	}

}
