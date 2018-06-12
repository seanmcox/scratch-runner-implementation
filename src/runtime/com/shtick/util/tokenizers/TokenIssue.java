/**
 * 
 */
package com.shtick.util.tokenizers;

/**
 * @author sean.cox
 *
 */
public interface TokenIssue {
	/**
	 * 
	 * @return Should return a brief String identifying the issue.
	 *         This may be useful if one wants to suggest automated resolution methods to the user in an IDE
	 *         or word processor.
	 */
	String getID();
	
	/**
	 * 
	 * @return A description of the issue. This should be clear and reasonably concise. (Usually one sentence.)
	 */
	String getDescription();
	
	/**
	 * 
	 * @return The line number of the issue.
	 */
	public int getLine();

	/**
	 * @return the character position on the line for the issue.
	 */
	public int getCharacter();
	
	/**
	 * 
	 * @return true if the token represents a de-facto error, and false if the token represents a warning.
	 */
	boolean isError();
}
