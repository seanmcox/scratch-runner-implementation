package com.shtick.util.tokenizers.util;

import java.util.Iterator;

/**
 * @author sean.cox
 *
 * @param <T>
 */
public class ImmutableWrappedIterator<T> implements Iterator<T>{
	private Iterator<T> wrapped;
	
	/**
	 * @param wrapped
	 */
	public ImmutableWrappedIterator(Iterator<T> wrapped) {
		super();
		this.wrapped = wrapped;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public T next() {
		return wrapped.next();
	}
}