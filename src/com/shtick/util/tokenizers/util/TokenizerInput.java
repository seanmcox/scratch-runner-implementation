/**
 * 
 */
package com.shtick.util.tokenizers.util;

import java.io.IOException;

/**
 * <p>
 * In this InputStream, the line and position are tracked.<br/>
 * The starting line is 1.<br/>
 * The starting position is 1 and that value is incremented on each call to read().<br/>
 * The starting position is reset, and the starting line is incremented upon each call to newline().
 * </p>
 * 
 * @author sean.cox
 *
 */
public abstract class TokenizerInput{
	/**
	 * Reads the next character but keeps the cursor in the current position.
	 * ie. Subsequent calls will return the same thing.
	 * 
	 * @return The value that would be returned by a subsequent read.
	 * @throws IOException For any back-end IOException, or if there is no data left to peek at.
	 */
	public abstract char peek() throws IOException;
	
	/**
	 * Reads the next character and moves the cursor to the next position.
	 * 
	 * @return The next character.
	 * @throws IOException For any back-end IOException, or if there is no data left to read.
	 */
	public abstract char read() throws IOException;
	
	/**
	 * 
	 * @return true if there is no data left to read
	 *         false otherwise
	 * @throws IOException
	 */
	public abstract boolean isDone() throws IOException;

	
	/**
	 * Increments the line and resets the position.
	 */
	public abstract void newline();


	/**
	 * @return the line
	 */
	public abstract int getLine();

	/**
	 * @return the line position
	 */
	public abstract int getLinePosition();

	/**
	 * @return the absolute position
	 */
	public abstract int getPosition();
}
