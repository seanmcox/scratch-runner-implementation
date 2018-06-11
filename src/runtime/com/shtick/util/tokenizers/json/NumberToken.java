/**
 * 
 */
package com.shtick.util.tokenizers.json;

import com.shtick.util.tokenizers.StandardTokenIssue;

/**
 * @author sean.cox
 *
 */
public class NumberToken extends CompositeJSONToken {
	boolean isNegative;
	DigitsToken wholePart=null;
	DigitsToken fractionalPart=null;
	DigitsToken exponentialPart=null;

	/**
	 * 
	 * @param negativeToken
	 * @param wholePart
	 * @param periodToken
	 * @param fractionalPart
	 * @param exponentToken
	 * @param signToken Can be null, a NegativeToken, or a PositiveToken.
	 * @param exponentialPart
	 */
	public NumberToken(NegativeToken negativeToken, DigitsToken wholePart, PeriodToken periodToken, DigitsToken fractionalPart, ExponentToken exponentToken, JSONToken signToken, DigitsToken exponentialPart) {
		super(negativeToken, wholePart, periodToken, fractionalPart, exponentToken, signToken, exponentialPart);
		this.isNegative = (negativeToken!=null);
		this.wholePart = wholePart;
		this.fractionalPart = fractionalPart;
		this.exponentialPart = exponentialPart;
		if((signToken!=null)&&(!((signToken instanceof NegativeToken)||(signToken instanceof PositiveToken))))
			throw new IllegalArgumentException("Invalid signToken provided.");
		if(wholePart==null)
			this.addIssue(new StandardTokenIssue("NUM01", "Whole part missing.", this.getStartLine(), this.getStartLinePosition(), true));
		if(wholePart.toString().matches("^0[0-9]+$"))
			this.addIssue(new StandardTokenIssue("NUM02", "Whole has a leading zero.", this.getStartLine(), this.getStartLinePosition(), true));
		if((periodToken!=null)&&(fractionalPart==null))
			this.addIssue(new StandardTokenIssue("NUM03", "Fractional part indicated but not provided.", this.getStartLine(), this.getStartLinePosition(), true));
		if((exponentToken!=null)&&(exponentialPart==null))
			this.addIssue(new StandardTokenIssue("NUM04", "Exponential part indicated but not provided.", this.getStartLine(), this.getStartLinePosition(), true));
		if((exponentToken==null)&&(signToken!=null))
			this.addIssue(new StandardTokenIssue("NUM05", "Exponent sign given when no exponent was indicated.", this.getStartLine(), this.getStartLinePosition(), true));
	}

	/**
	 * @return the isNegative
	 */
	public boolean isNegative() {
		return isNegative;
	}

	/**
	 * @return the wholePart
	 */
	public DigitsToken getWholePart() {
		return wholePart;
	}

	/**
	 * @return the fractionalPart
	 */
	public DigitsToken getFractionalPart() {
		return fractionalPart;
	}

	/**
	 * @return the exponentialPart
	 */
	public DigitsToken getExponentialPart() {
		return exponentialPart;
	}
}
