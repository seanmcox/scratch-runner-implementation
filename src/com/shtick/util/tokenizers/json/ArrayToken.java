/**
 * 
 */
package com.shtick.util.tokenizers.json;

import java.util.LinkedList;
import java.util.List;

import com.shtick.util.tokenizers.StandardTokenIssue;
import com.shtick.util.tokenizers.Token;

/**
 * @author sean.cox
 *
 */
public class ArrayToken extends CompositeJSONToken {
	/**
	 * @param startToken 
	 * @param contents
	 * @param endToken 
	 */
	public ArrayToken(StartOfArrayToken startToken, List<JSONToken> contents, EndOfArrayToken endToken) {
		super(joinTokens(startToken, null, contents, endToken));
		if(startToken==null)
			this.addIssue(new StandardTokenIssue("ARR01", "Array start missing.", this.getStartLine(), this.getStartLinePosition(), true));
		if(endToken==null)
			this.addIssue(new StandardTokenIssue("ARR02", "Array end missing.", this.getStartLine(), this.getStartLinePosition(), true));
	}
	
	/**
	 * 
	 * @return A list of all value tokens (ArrayToken, StringToken, NumberToken, KeywordToken, ObjectToken) contained immediately within this ArrayToken.
	 */
	public List<JSONToken> getValueTokens(){
		LinkedList<JSONToken> retval = new LinkedList<>();
		for(Token<JSONToken> token:getChildren())
			if((token instanceof ArrayToken)||(token instanceof StringToken)||(token instanceof NumberToken)||(token instanceof KeywordToken)||(token instanceof ObjectToken))
				retval.add((JSONToken)token);
		return retval;
	}
}
