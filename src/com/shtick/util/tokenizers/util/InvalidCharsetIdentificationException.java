/**
 * 
 */
package com.shtick.util.tokenizers.util;

/**
 * @author sean.cox
 *
 */
public class InvalidCharsetIdentificationException extends Exception {

	/**
	 * 
	 */
	public InvalidCharsetIdentificationException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public InvalidCharsetIdentificationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidCharsetIdentificationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public InvalidCharsetIdentificationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidCharsetIdentificationException(Throwable cause) {
		super(cause);
	}

}
