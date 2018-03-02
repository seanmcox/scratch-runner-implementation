/**
 * 
 */
package com.shtick.util.tokenizers;

import java.util.Collection;

/**
 * @author sean.cox
 *
 */
public abstract class TokenizerRegistry {
	private static TokenizerRegistry REGISTRY;
	
	/**
	 * This constructor should only be called by the bundle's activator.
	 */
	public TokenizerRegistry(){
		if(REGISTRY!=null)
			throw new RuntimeException("Can only have one TokenizerRegistry.");
		REGISTRY=this;
	}
	
	/**
	 * 
	 * @return The singleton TokenizerRegistry, if defined.
	 */
	public static TokenizerRegistry getTokenizerRegistry(){
		return REGISTRY;
	}
	
	/**
	 * 
	 * @param tokenClass
	 * @return A Collection of tokenizers generating a TokenTree with Tokens of the given type.
	 */
	public abstract Collection<Tokenizer<?>> getTokenizersForTokenType(Class<Token<?>> tokenClass);
	
	/**
	 * 
	 * @param mime 
	 * @return A Collection of tokenizers which can tokenize data with the given mime type.
	 */
	public abstract Collection<Tokenizer<?>> getTokenizersForMime(String mime);
	
	/**
	 * 
	 * @return A Collection of all tokenizers.
	 */
	public abstract Collection<Tokenizer<?>> getAllTokenizers();
}
