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
public class ObjectToken extends CompositeJSONToken {
	/**
	 * @param startToken 
	 * @param contents
	 * @param endToken 
	 */
	public ObjectToken(StartOfObjectToken startToken, List<JSONToken> contents, EndOfObjectToken endToken) {
		super(joinTokens(startToken, null, contents, endToken));
		if(startToken==null)
			this.addIssue(new StandardTokenIssue("OBJ01", "Object start missing.", this.getStartLine(), this.getStartLinePosition(), true));
		if(endToken==null)
			this.addIssue(new StandardTokenIssue("OBJ02", "Object end missing.", this.getStartLine(), this.getStartLinePosition(), true));
	}
	
	/**
	 * 
	 * @return A list of all ObjectPropertyToken contained immediately within this ObjectToken.
	 */
	public List<ObjectPropertyToken> getObjectPropertyTokens(){
		LinkedList<ObjectPropertyToken> retval = new LinkedList<>();
		for(Token<JSONToken> token:getChildren())
			if(token instanceof ObjectPropertyToken)
				retval.add((ObjectPropertyToken)token);
		return retval;
	}
}
