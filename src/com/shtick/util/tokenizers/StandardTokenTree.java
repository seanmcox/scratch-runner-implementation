/**
 * 
 */
package com.shtick.util.tokenizers;

import java.util.Iterator;
import java.util.List;

import com.shtick.util.tokenizers.util.ImmutableWrappedIterator;

/**
 * @author sean.cox
 * @param <T> 
 *
 */
public class StandardTokenTree<T extends Token<T>> implements TokenTree<T> {
	private List<T> rootTokens;

	/**
	 * @param rootTokens
	 */
	public StandardTokenTree(List<T> rootTokens) {
		super();
		this.rootTokens = rootTokens;
	}

	@Override
	public Object executeXPathQuery(String xpath) throws UnsupportedOperationException, IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<T> iterator() {
		return new ImmutableWrappedIterator<>(rootTokens.iterator());
	}

}
