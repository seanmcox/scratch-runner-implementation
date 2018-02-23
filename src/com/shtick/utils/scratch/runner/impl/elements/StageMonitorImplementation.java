/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import com.shtick.utils.scratch.runner.core.elements.StageMonitor;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class StageMonitorImplementation implements StageMonitor{
	private String target;
	private String cmd;
	private String param;
	private int color;
	private String label;
	private int mode;
	private int sliderMin;
	private int sliderMax;
	private boolean isDiscrete;
	private int x;
	private int y;
	private boolean visible;
	
	/**
	 * @param target
	 * @param cmd
	 * @param param
	 * @param color
	 * @param label
	 * @param mode
	 * @param sliderMin
	 * @param sliderMax
	 * @param isDiscrete
	 * @param x
	 * @param y
	 * @param visible
	 */
	public StageMonitorImplementation(String target, String cmd, String param, int color, String label, int mode, int sliderMin,
			int sliderMax, boolean isDiscrete, int x, int y, boolean visible) {
		super();
		this.target = target;
		this.cmd = cmd;
		this.param = param;
		this.color = color;
		this.label = label;
		this.mode = mode;
		this.sliderMin = sliderMin;
		this.sliderMax = sliderMax;
		this.isDiscrete = isDiscrete;
		this.x = x;
		this.y = y;
		this.visible = visible;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public String getCmd() {
		return cmd;
	}

	@Override
	public String getParam() {
		return param;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public int getSliderMin() {
		return sliderMin;
	}

	@Override
	public int getSliderMax() {
		return sliderMax;
	}

	@Override
	public boolean isDiscrete() {
		return isDiscrete;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.StageMonitor#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if(this.visible!=visible) {
			this.visible = visible;
			ScratchRuntimeImplementation.getScratchRuntime().repaintStage();
		}
	}
}
