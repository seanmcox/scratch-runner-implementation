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
public class ObjectPropertyToken extends CompositeJSONToken {
	/**
	 * 
	 * @param label
	 * @param colon
	 * @param value
	 */
	public ObjectPropertyToken(StringToken label, ColonToken colon, JSONToken value) {
		super(label, colon, value);
		if(label==null)
			this.addIssue(new StandardTokenIssue("OBJP01", "Label missing.", this.getStartLine(), this.getStartLinePosition(), true));
		if(colon==null)
			this.addIssue(new StandardTokenIssue("OBJP02", "Colon missing.", this.getStartLine(), this.getStartLinePosition(), true));
		if(value==null)
			this.addIssue(new StandardTokenIssue("OBJP03", "Value missing.", this.getStartLine(), this.getStartLinePosition(), true));
	}
	
	/**
	 * 
	 * @return The label for this ObjectPropertyToken, or null if none was specified. (A syntax error.)
	 */
	public StringToken getLabel(){
		List<Token<JSONToken>> children = this.getChildren();
		if(children.size()==0)
			return null;
		if(children.get(0) instanceof StringToken)
			return (StringToken)children.get(0);
		return null;
	}
	
	/**
	 * 
	 * @return The value for this ObjectPropertyToken, or null if none was specified. (A syntax error.)
	 */
	public JSONToken getValue(){
		List<Token<JSONToken>> children = this.getChildren();
		if(children.size()==0)
			return null;
		if((children.size()==1)&&(children.get(0) instanceof StringToken))
			return null; // If there is only one token and it is a StringToken, then take this to be the label.
		JSONToken candidate=(JSONToken)children.get(children.size()-1);
		if(candidate instanceof ColonToken)
			return null;
		return candidate;
	}
}
