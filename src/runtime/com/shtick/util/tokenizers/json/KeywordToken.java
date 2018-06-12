/**
 * 
 */
package com.shtick.util.tokenizers.json;

import java.util.HashSet;

import com.shtick.util.tokenizers.StandardTokenIssue;

/**
 * @author sean.cox
 *
 */
public class KeywordToken extends JSONToken {
	/**
	 * 
	 */
	public static final String KEYWORD_TRUE = "true";
	/**
	 * 
	 */
	public static final String KEYWORD_FALSE = "false";
	/**
	 * 
	 */
	public static final String KEYWORD_NULL = "null";
	
	private static final HashSet<String> KEYWORDS = new HashSet(3);
	
	private String text;
	
	static{
		KEYWORDS.add(KEYWORD_TRUE);
		KEYWORDS.add(KEYWORD_FALSE);
		KEYWORDS.add(KEYWORD_NULL);
	}

	/**
	 * @param startLine
	 * @param startLinePosition 
	 * @param startPosition
	 * @param text 
	 */
	public KeywordToken(int startLine, int startLinePosition, int startPosition, String text) {
		super(startLine, startLinePosition, startLine, startLinePosition+text.length(), startPosition, startPosition+text.length());
		this.text=text;
		if(!KEYWORDS.contains(text))
			this.addIssue(new StandardTokenIssue("KEY01", "Not a recognized keyword: "+text, this.getStartLine(), this.getStartLinePosition(), true));
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
