/**
 * 
 */
package com.shtick.util.tokenizers.json;

import java.util.List;

import com.shtick.util.tokenizers.StandardTokenIssue;
import com.shtick.util.tokenizers.Token;

/**
 * @author sean.cox
 *
 */
public class StringToken extends CompositeJSONToken {
	/**
	 * @param startToken 
	 * @param contents
	 * @param endToken 
	 */
	public StringToken(DoubleQuoteToken startToken, List<JSONToken> contents, DoubleQuoteToken endToken) {
		super(joinTokens(startToken, null, contents, endToken));
		if(startToken==null)
			this.addIssue(new StandardTokenIssue("STR01", "String start quote missing.", this.getStartLine(), this.getStartLinePosition(), true));
		if(endToken==null)
			this.addIssue(new StandardTokenIssue("STR02", "String end quote missing.", this.getStartLine(), this.getStartLinePosition(), true));
	}

	/**
	 * 
	 * @return The String encoded by the tokens. (RawText tokens concatenated together with the characters represented by the StringEscapeTokens.)
	 */
	public String getRepresentedString(){
		String retval="";
		for(Token<JSONToken> child:getChildren()){
			if(child instanceof RawTextToken)
				retval+=child.toString();
			else if(child instanceof StringEscapeToken)
				retval+=((StringEscapeToken)child).getRepresentedChar();
		}
		return retval;
	}
}
