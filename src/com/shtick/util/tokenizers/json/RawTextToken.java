/**
 * 
 */
package com.shtick.util.tokenizers.json;

/**
 * @author sean.cox
 *
 */
public class RawTextToken extends JSONToken {
	private String text;

	/**
	 * @param startLine
	 * @param startLinePosition 
	 * @param endLine
	 * @param endLinePosition 
	 * @param startPosition
	 * @param endPosition
	 * @param text 
	 */
	public RawTextToken(int startLine, int startLinePosition, int endLine, int endLinePosition, int startPosition, int endPosition, String text) {
		super(startLine, startLinePosition, endLine, endLinePosition, startPosition, endPosition);
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
