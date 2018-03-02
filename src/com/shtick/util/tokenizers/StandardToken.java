/**
 * 
 */
package com.shtick.util.tokenizers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.shtick.util.tokenizers.util.ImmutableWrappedCollection;

/**
 * @author sean.cox
 * @param <T> 
 *
 */
public class StandardToken<T extends Token<T>> implements Token<T> {
	private int startLine;
	private int startLinePosition;
	private int endLine;
	private int endLinePosition;
	private int startPosition;
	private int endPosition;
	private LinkedList<TokenIssue> tokenIssues=new LinkedList<>();

	/**
	 * @param startLine
	 * @param startLinePosition 
	 * @param endLine
	 * @param endLinePosition 
	 * @param startPosition
	 * @param endPosition
	 */
	public StandardToken(int startLine, int startLinePosition, int endLine, int endLinePosition, int startPosition, int endPosition) {
		super();
		this.startLine = startLine;
		this.startLinePosition = startLinePosition;
		this.endLine = endLine;
		this.endLinePosition = endLinePosition;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#getStartLine()
	 */
	@Override
	public int getStartLine() {
		return startLine;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#getStartPosition()
	 */
	@Override
	public int getStartLinePosition() {
		return startLinePosition;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#getEndLine()
	 */
	@Override
	public int getEndLine() {
		return endLine;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#getEndPosition()
	 */
	@Override
	public int getEndLinePosition() {
		return endLinePosition;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#getStartPosition()
	 */
	@Override
	public int getStartPosition() {
		return startPosition;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#getEndPosition()
	 */
	@Override
	public int getEndPosition() {
		return endPosition;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#getIssues()
	 */
	@Override
	public Collection<TokenIssue> getIssues() {
		return new ImmutableWrappedCollection<>(tokenIssues);
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#addIssue(com.shtick.os.core.syntax.TokenIssue)
	 */
	@Override
	public void addIssue(TokenIssue issue) {
		tokenIssues.add(issue);
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#removeIssue(com.shtick.os.core.syntax.TokenIssue)
	 */
	@Override
	public boolean removeIssue(TokenIssue issue) {
		return tokenIssues.remove(issue);
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Token#clearIssues()
	 */
	@Override
	public void clearIssues() {
		tokenIssues.clear();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public List<Token<T>> getChildren() {
		return null;
	}

}
