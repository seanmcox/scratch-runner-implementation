/**
 * 
 */
package com.shtick.util.tokenizers.json;

/**
 * @author sean.cox
 *
 */
public class DigitsToken extends JSONToken {
	private String text;

	/**
	 * @param startLine
	 * @param startLinePosition 
	 * @param startPosition
	 * @param text This should be a string of numeric characters.
	 */
	public DigitsToken(int startLine, int startLinePosition, int startPosition, String text) {
		super(startLine, startLinePosition, startLine, startLinePosition+text.length(), startPosition, startPosition+text.length());
		if(!text.matches("^[0-9]+$"))
			throw new IllegalArgumentException("Non-numeric text provided to DigitsToken constructor.");
		this.text=text;
	}
	
	/**
	 * 
	 * @return The value represented by this token.
	 */
	public String getText(){
		return text;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return text;
	}
}
