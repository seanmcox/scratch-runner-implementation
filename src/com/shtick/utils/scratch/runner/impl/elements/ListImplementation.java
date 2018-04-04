/**
 * 
 */
package com.shtick.utils.scratch.runner.impl.elements;

import java.util.Iterator;
import java.util.LinkedList;

import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.impl.ScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class ListImplementation implements List{
	private String listName;
	private java.util.List<Object> contents;
	private Double x;
	private Double y;
	private Double width;
	private Double height;
	private Boolean visible;
	
	/**
	 * @param listName
	 * @param contents
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param visible
	 */
	public ListImplementation(String listName, Object[] contents, Double x, Double y, Double width, Double height, Boolean visible) {
		super();
		this.listName = listName;
		this.contents = new LinkedList<>();
		for(Object content:contents)
			this.contents.add(content);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.visible = visible;
	}
	
	private ListImplementation(ListImplementation listImplementation) {
		synchronized(listImplementation) {
			this.listName = listImplementation.listName;
			this.contents = new LinkedList<>();
			synchronized(listImplementation.contents) {
				for(Object content:listImplementation.contents)
					this.contents.add(content);
			}
			this.x = listImplementation.x;
			this.y = listImplementation.y;
			this.width = listImplementation.width;
			this.height = listImplementation.height;
			this.visible = listImplementation.visible;
		}
	}

	@Override
	public String getListName() {
		return listName;
	}

	@Override
	public Object[] getContents() {
		synchronized(contents) {
			return contents.toArray();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Object> iterator() {
		return new LinkedList<Object>(contents).iterator();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#getItem(int)
	 */
	@Override
	public Object getItem(int index) {
		return contents.get(index-1);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#deleteItem(int)
	 */
	@Override
	public void deleteItem(int index) {
		synchronized(contents) {
			contents.remove(index-1);
			if(visible)
				ScratchRuntimeImplementation.getScratchRuntime().repaintStage();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#setItem(java.lang.Object, int)
	 */
	@Override
	public void setItem(Object item, int index) {
		synchronized(contents) {
			contents.set(index-1, item);
			if(visible)
				ScratchRuntimeImplementation.getScratchRuntime().repaintStage();
		}		
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#addItem(java.lang.Object, int)
	 */
	@Override
	public void addItem(Object item, int index) {
		synchronized(contents) {
			contents.add(index-1, item);
			if(visible)
				ScratchRuntimeImplementation.getScratchRuntime().repaintStage();
		}
	}

	@Override
	public synchronized void addItem(Object item) {
		synchronized(contents) {
			contents.add(item);
			if(visible)
				ScratchRuntimeImplementation.getScratchRuntime().repaintStage();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#getItemCount()
	 */
	@Override
	public int getItemCount() {
		return contents.size();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object item) {
		return contents.contains(item);
	}

	@Override
	public Double getX() {
		return x;
	}

	@Override
	public Double getY() {
		return y;
	}

	@Override
	public Double getWidth() {
		return width;
	}

	@Override
	public Double getHeight() {
		return height;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.RenderableChild#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new ListImplementation(this);
	}
}
