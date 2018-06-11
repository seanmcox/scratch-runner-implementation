/**
 * 
 */
package com.shtick.util.tokenizers;

/**
 * @author sean.cox
 *
 */
public class StandardTokenIssue implements TokenIssue {
	private String id;
	private String description;
	private int line;
	private int character;
	private boolean isError;

	/**
	 * @param id
	 * @param description
	 * @param line 
	 * @param character 
	 * @param isError
	 */
	public StandardTokenIssue(String id, String description, int line, int character, boolean isError) {
		super();
		this.id = id;
		this.description = description;
		this.isError = isError;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.TokenIssue#getID()
	 */
	@Override
	public String getID() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.TokenIssue#getDescription()
	 */
	@Override
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the line
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @return the character
	 */
	public int getCharacter() {
		return character;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.TokenIssue#isError()
	 */
	@Override
	public boolean isError() {
		return this.isError;
	}

}
