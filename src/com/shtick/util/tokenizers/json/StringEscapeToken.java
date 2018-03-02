/**
 * 
 */
package com.shtick.util.tokenizers.json;

import com.shtick.util.tokenizers.StandardTokenIssue;

/**
 * @author sean.cox
 *
 */
public class StringEscapeToken extends JSONToken {
	/**
	 * The set of characters that are valid string escape characters.
	 */
	public static final String STRING_ESCAPE_CHARS="\\\"/bfnr'u";
	private String escapeSequence;
	private String representedChar = "";
	
	/**
	 * @param startLine
	 * @param startLinePosition 
	 * @param startPosition
	 * @param escapeSequence Must not be null. If no valid escape character was given, then this should be an empty string. Otherwise, this should either be a valid escape sequence, or one beginning with 'u'.
	 */
	public StringEscapeToken(int startLine, int startLinePosition, int startPosition, String escapeSequence) {
		super(startLine, startLinePosition, startLine, startLinePosition+escapeSequence.length()+1, startPosition, startPosition+escapeSequence.length()+1);
		this.escapeSequence=escapeSequence;
		if(escapeSequence.length()==0){
			this.addIssue(new StandardTokenIssue("ESC01", "No valid escape character given.", this.getStartLine(), this.getStartLinePosition(), true));
			return;
		}
		char leadChar = escapeSequence.charAt(0);
		if(leadChar=='u'){
			if(!escapeSequence.matches("^u[0-9a-fA-F]{4}$"))
				this.addIssue(new StandardTokenIssue("ESC02", "No valid escape character given.", this.getStartLine(), this.getStartLinePosition(), true));
			else
				representedChar = ""+(char)Integer.parseInt(escapeSequence.substring(1), 16);
		}
		else{
			switch(leadChar){
			case '\\':
			case '"':
			case '/':
				representedChar = ""+leadChar;
				break;
			case 'b':
				representedChar = "\b";
				break;
			case 'f':
				representedChar = "\f";
				break;
			case 'n':
				representedChar = "\n";
				break;
			case 'r':
				representedChar = "\r";
				break;
			case 't':
				representedChar = "\t";
				break;
			default:
				throw new IllegalArgumentException("Invalid escape character provided.");
			}
		}
	}
	
	/**
	 * 
	 * @return The escape sequence of this token.
	 */
	public String getEscapeSequence(){
		return escapeSequence;
	}
	
	/**
	 * 
	 * @return The value represented by this token.
	 */
	public String getRepresentedChar(){
		return representedChar;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(\\|"+escapeSequence+")";
	}
}
