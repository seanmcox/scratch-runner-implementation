/**
 * 
 */
package com.shtick.util.tokenizers.util;

import java.util.ListIterator;

/**
 * @author sean.cox
 * @param <E> 
 *
 */
public class ImmutableWrappedListIterator<E> extends ImmutableWrappedIterator<E> implements ListIterator<E> {
	private ListIterator<E> wrapped;
	
	/**
	 * @param wrapped
	 */
	public ImmutableWrappedListIterator(ListIterator<E> wrapped) {
		super(wrapped);
		this.wrapped=wrapped;
	}

	@Override
	public boolean hasPrevious() {
		return wrapped.hasPrevious();
	}

	@Override
	public E previous() {
		return wrapped.previous();
	}

	@Override
	public int nextIndex() {
		return wrapped.nextIndex();
	}

	@Override
	public int previousIndex() {
		return wrapped.previousIndex();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(E e) {
		throw new UnsupportedOperationException();
	}

}
