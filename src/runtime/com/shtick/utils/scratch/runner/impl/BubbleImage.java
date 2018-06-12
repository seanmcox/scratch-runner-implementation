/**
 * 
 */
package com.shtick.utils.scratch.runner.impl;

import java.awt.Image;

import com.shtick.utils.scratch.runner.core.elements.Sprite;

/**
 * @author sean.cox
 *
 */
public class BubbleImage {
	private Sprite sprite;
	private Image image;
	
	/**
	 * @param sprite
	 * @param image
	 */
	public BubbleImage(Sprite sprite, Image image) {
		super();
		this.sprite = sprite;
		this.image = image;
	}

	/**
	 * @return the sprite
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}
}
