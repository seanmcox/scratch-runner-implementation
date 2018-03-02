/**
 * 
 */
package com.shtick.util.tokenizers.json;

/**
 * @author sean.cox
 *
 */
public class ExponentToken extends JSONToken {
	private char exponentSymbol;

	/**
	 * @param startLine
	 * @param startLinePosition 
	 * @param startPosition
	 * @param exponentSymbol This should be a string of numeric characters.
	 */
	public ExponentToken(int startLine, int startLinePosition, int startPosition, char exponentSymbol) {
		super(startLine, startLinePosition, startLine, startLinePosition+1, startPosition, startPosition+1);
		if((exponentSymbol!='e')&&(exponentSymbol!='E'))
			throw new IllegalArgumentException("Invalid exponent symbol, "+exponentSymbol+", provided to ExponentToken constructor.");
		this.exponentSymbol=exponentSymbol;
	}
	
	/**
	 * 
	 * @return The value represented by this token.
	 */
	public char getSymbol(){
		return exponentSymbol;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ""+exponentSymbol;
	}
}
