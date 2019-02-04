/**
 * 
 */
package com.shtick.utils.scratch.runner.impl2.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.shtick.utils.scratch.runner.core.elements.List;
import com.shtick.utils.scratch.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch.runner.core.elements.Sprite;
import com.shtick.utils.scratch.runner.impl2.ScratchRuntimeImplementation;
import com.shtick.utils.scratch.runner.impl2.ui.ListMonitor;

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
	private boolean visible;
	private ScratchRuntimeImplementation runtime;
	private ListMonitor monitor;
	private boolean monitorCalculated = false;
	
	/**
	 * @param listName
	 * @param contents
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param visible
	 * @param runtime 
	 */
	public ListImplementation(String listName, Object[] contents, Double x, Double y, Double width, Double height, boolean visible, ScratchRuntimeImplementation runtime) {
		super();
		this.listName = listName;
		this.contents = new ArrayList<>(contents.length);
		for(Object content:contents)
			this.contents.add(content);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.visible = visible;
		this.runtime = runtime;		
	}
	
	private ListImplementation(ListImplementation listImplementation) {
		synchronized(listImplementation) {
			this.listName = listImplementation.listName;
			this.contents = new ArrayList<>();
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
		return Collections.unmodifiableList(contents).iterator();
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
				runtime.repaintStage();
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.List#deleteAll()
	 */
	@Override
	public void deleteAll() {
		synchronized(contents) {
			contents.clear();
			if(visible)
				runtime.repaintStage();
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
				runtime.repaintStage();
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
				runtime.repaintStage();
		}
	}

	@Override
	public synchronized void addItem(Object item) {
		synchronized(contents) {
			contents.add(item);
			if(visible)
				runtime.repaintStage();
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
		if(this.visible==visible)
			return;
		this.visible = visible;
		runtime.repaintStage();
		// This is done here to ensure it gets done, so that the listeners on the component only capture the input when expected.
		if(monitorCalculated&&(monitor!=null))
			monitor.setVisible(visible);
	}
	
	/**
	 * 
	 * @return The ListMonitor (instance of Component) for this list.
	 */
	public ListMonitor getMonitor() {
		if(!monitorCalculated) {
			ScriptContext context = null;
			if(runtime.getCurrentStage().getContextListByName(listName)!=null) {
				context = runtime.getCurrentStage();
			}
			else {
				for(RenderableChild child:runtime.getAllRenderableChildren()) {
					if(child instanceof Sprite) {
						Sprite sprite = (Sprite)child;
						if(sprite.getContextListByName(listName)!=null) {
							context = sprite;
							break;
						}
					}
				}
			}
	
			if(context!=null)
				monitor = new ListMonitor(this, context);
			monitorCalculated = true;
			monitor.setVisible(visible);
		}
		return monitor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new ListImplementation(this);
	}
}
