/**
 * 
 */
package com.shtick.util.tokenizers.util;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * @author sean.cox
 * @param <E> 
 *
 */
public class ImmutableWrappedList<E> extends ImmutableWrappedCollection<E> implements List<E> {
	private List<E> wrapped;

	/**
	 * @param wrapped
	 */
	public ImmutableWrappedList(List<E> wrapped) {
		super(wrapped);
		this.wrapped=wrapped;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E get(int index) {
		return wrapped.get(index);
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		return wrapped.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return wrapped.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ImmutableWrappedListIterator<>(wrapped.listIterator());
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return new ImmutableWrappedListIterator<>(wrapped.listIterator(index));
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new ImmutableWrappedList<E>(wrapped.subList(fromIndex, toIndex));
	}

}
