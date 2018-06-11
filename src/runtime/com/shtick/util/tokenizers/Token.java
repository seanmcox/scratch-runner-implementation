/**
 * 
 */
package com.shtick.util.tokenizers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author sean.cox
 * @param <T> 
 *
 */
public interface Token<T extends Token<T>> {
	/**
	 * 
	 * @return The line number of the start of the token.
	 *         The meaning of a line, and how they are indexed, is implementation-dependent.
	 */
	int getStartLine();
	
	/**
	 * 
	 * @return The character position of the start of the token within the start line.
	 *         The character position combined with the line number must be idempotent for a specific character,
	 *         and every character must be mappable to one and only one line number and character position
	 *         combination.
	 */
	int getStartLinePosition();
	
	/**
	 * 
	 * @return The absolute character position of the start of the token.
	 */
	int getStartPosition();

	/**
	 * 
	 * @return The line number of the end of the token.
	 *         The meaning of a line, and how they are indexed, is implementation-dependent.
	 */
	int getEndLine();
	
	/**
	 * 
	 * @return The character position of the last character of the token plus one within the end line.
	 *         If the start line and the end line are the same then
	 *         getStartPosition() - getEndPosition() is the number of characters encompassed by the token.
	 *         This also might be described as the cursor position directly after the token,
	 *         with the added clarification, that between-line cursor positions are treated
	 *         as being on the same line as the last character of the token.
	 */
	int getEndLinePosition();
	
	/**
	 * 
	 * @return The absolute character position of the last character of the token plus one.
	 */
	int getEndPosition();

	/**
	 * 
	 * @return A collection of issues associated with this token. This may be null.
	 */
	Collection<TokenIssue> getIssues();

	/**
	 * 
	 * @return A collection of issues associated with this token and any child tokens. This may not be null.
	 */
	default Collection<TokenIssue> getAllIssues(){
		Collection<TokenIssue> initialIssues = getIssues();
		LinkedList<TokenIssue> issues;
		if(initialIssues==null)
			issues = new LinkedList<>();
		else
			issues = new LinkedList<>(initialIssues);
		if(hasChildren())
			for(Token<?> token:getChildren())
				issues.addAll(token.getAllIssues());
		return issues;
	}
	
	/**
	 * 
	 * @return true if this token has subtokens, and false otherwise.
	 */
	boolean hasChildren();
	
	/**
	 * 
	 * @return A list of child tokens, or null.
	 */
	List<Token<T>> getChildren();

	/**
	 * 
	 * @param issue The issue to add. Tokens should support this function by including the added issue
	 *              as part of the return value for subsequent calls to getIssues(). Silently fails if the
	 *              function is unsupported.
	 */
	void addIssue(TokenIssue issue);
	
	/**
	 * Removes the given issue from the internal collection. Tokens must support this function by not
	 * including the issue as part of the return value for subsequent calls to getIssues().
	 * 
	 * @param issue The issue to remove.
	 * @return TODO
	 */
	boolean removeIssue(TokenIssue issue);
	
	/**
	 * Clears the internal collection of issues, if any. Tokens must support this function by returning
	 * null, or an empty collection for subsequent calls to getIssues().
	 */
	void clearIssues();
}
