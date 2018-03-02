/**
 * 
 */
package com.shtick.util.tokenizers.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author sean.cox
 * @param <T> 
 *
 */
public class ImmutableWrappedCollection<T> implements Collection<T> {
	private Collection<T> wrapped;

	/**
	 * @param wrapped
	 */
	public ImmutableWrappedCollection(Collection<T> wrapped) {
		super();
		this.wrapped = wrapped;
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return wrapped.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new ImmutableWrappedIterator<T>(wrapped.iterator());
	}

	@Override
	public Object[] toArray() {
		return wrapped.toArray();
	}

	@Override
	public <T2> T2[] toArray(T2[] a) {
		return wrapped.toArray(a);
	}

	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wrapped.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
