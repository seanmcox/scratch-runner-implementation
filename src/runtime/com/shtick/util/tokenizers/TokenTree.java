/**
 * 
 */
package com.shtick.util.tokenizers;

import java.util.Collection;
import java.util.LinkedList;

/**
 * <p>The TokenTree represents an branching tree structure of Tokens.</p>
 * 
 * <p>Tokens can also have child tokens, however, the basic iteration capability of the token tree will only
 * iterate over the root tokens. Child tokens are not included in iteration. Instead tokens with child tokens
 * must be explicitly iterated over separately.</p>
 * 
 * <p>The tree can be queried in a more ordered fashion using XPath, if supported.</p>
 * 
 * @author sean.cox
 * @param <T> The type of Token in the TokenTree.
 *
 */
public interface TokenTree<T extends Token<T>> extends Iterable<T>{

	/**
	 * <p>Queries the token tree using XPath. Support for the specification may vary from
	 * implementation to implementation.</p> 
	 * 
	 * @param xpath
	 * @return An Object representing the result of the query.
	 * @throws UnsupportedOperationException If XPath querying is not supported at all.
	 * @throws IllegalArgumentException If the xpath parameter uses unsupported features, or is not understood at all.
	 */
	Object executeXPathQuery(String xpath) throws UnsupportedOperationException, IllegalArgumentException;
	
	/**
	 * 
	 * @return All of the issues in any of the Tokens in the TokenTree.
	 */
	default Collection<TokenIssue> getAllIssues(){
		LinkedList<TokenIssue> retval = new LinkedList<>();
		for(Token<?> token:this)
			retval.addAll(token.getAllIssues());
		return retval;
	}
}
