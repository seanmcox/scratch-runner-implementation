/**
 * 
 */
package com.shtick.util.tokenizers.json;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.shtick.util.tokenizers.Token;
import com.shtick.util.tokenizers.util.ImmutableWrappedList;

/**
 * @author sean.cox
 *
 */
public abstract class CompositeJSONToken extends JSONToken {
	private List<JSONToken> children;

	/**
	 * @param lineContents Cannot be null or empty. 
	 */
	public CompositeJSONToken(List<JSONToken> lineContents) {
		super(
				lineContents.get(0).getStartLine(),
				lineContents.get(0).getStartLinePosition(),
				lineContents.get(lineContents.size()-1).getEndLine(),
				lineContents.get(lineContents.size()-1).getEndLinePosition(),
				lineContents.get(0).getStartPosition(),
				lineContents.get(lineContents.size()-1).getEndPosition()
				);
		this.children=lineContents;
	}

	/**
	 * @param lineContents Any null values will be culled out. Must have at least one non-null value. 
	 */
	public CompositeJSONToken(JSONToken ... lineContents) {
		this(generateCulledList(lineContents));
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.StandardToken#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.StandardToken#getChildren()
	 */
	@Override
	public List<Token<JSONToken>> getChildren() {
		return new ImmutableWrappedList<>((List)children);
	}

	private static List<JSONToken> generateCulledList(JSONToken ... lineContents){
		LinkedList<JSONToken> retval=new LinkedList<>();
		for(JSONToken token:lineContents){
			if(token!=null)
				retval.add(token);
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retval="";
		for(JSONToken token:children)
			retval+=token.toString();
		return retval;
	}

	/**
	 * <p>A utility function which creates a list consisting of the startTokens, the contents of midTokens, and then endTokens.</p>
	 * 
	 * <p>startToken1, startToken2 and individual endTokens are not added if they are null.</p>
	 * 
	 * @param startToken1
	 * @param startToken2
	 * @param midTokens
	 * @param endTokens
	 * @return The List put together as described.
	 */
	public static List<JSONToken> joinTokens(JSONToken startToken1, JSONToken startToken2, List<? extends JSONToken> midTokens, JSONToken ... endTokens){
		ArrayList<JSONToken> retval=new ArrayList<>(midTokens.size()+2);
		if(startToken1!=null)
			retval.add(startToken1);
		if(startToken2!=null)
			retval.add(startToken2);
		retval.addAll(midTokens);
		for(JSONToken endToken:endTokens)
			if(endToken!=null)
				retval.add(endToken);
		return retval;
	}

	/**
	 * @param list1
	 * @param list2
	 * @return list1 with the contents of list2 appended.
	 */
	public static List<JSONToken> appendTokens(List<JSONToken> list1, List<? extends JSONToken> list2){
		if(list2!=null)
			for(JSONToken token:list2)
				if(token!=null)
					list1.add(token);
		return list1;
	}
}
