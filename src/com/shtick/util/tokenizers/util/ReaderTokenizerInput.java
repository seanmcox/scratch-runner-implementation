/**
 * 
 */
package com.shtick.util.tokenizers.util;

import java.io.IOException;
import java.io.Reader;

/**
 * @author sean.cox
 *
 */
public class ReaderTokenizerInput extends TokenizerInput {
	private int peekValue;
	private boolean hasPeekValue=false;
	private Reader reader;
	private int line;
	private int linePosition;
	private int basePosition;
	private int absolutePosition;

	/**
	 * Initializes the TokenizerInput marking the current line a baseLine,
	 * with the current position on the line being basePosition. The absolute
	 * position initializes as 0.
	 * 
	 * @param reader
	 * @param baseLine 
	 * @param basePosition 
	 */
	public ReaderTokenizerInput(Reader reader, int baseLine, int basePosition) {
		this.reader = reader;
		this.line=baseLine;
		this.linePosition=basePosition;
		this.basePosition=basePosition;
		this.absolutePosition=0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.util.TokenizerInput#peek()
	 */
	@Override
	public char peek() throws IOException {
		if(!hasPeekValue){
			peekValue=reader.read();
			hasPeekValue=true;
		}
		if(peekValue<0)
			throw new IOException("EOF found.");
		return (char)peekValue;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.util.TokenizerInput#read()
	 */
	@Override
	public char read() throws IOException {
		int value;
		if(hasPeekValue){
			hasPeekValue=false;
			value=peekValue;
		}
		else{
			value=reader.read();
		}
		if(value<0){
			peekValue=value;
			hasPeekValue=true;
			throw new IOException("EOF found.");
		}
		linePosition++;
		absolutePosition++;
		return (char)value;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.util.TokenizerInput#isDone()
	 */
	@Override
	public boolean isDone() throws IOException {
		if(!hasPeekValue){
			peekValue=reader.read();
			hasPeekValue=true;
		}
		return peekValue<0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.util.TokenizerInput#newline()
	 */
	@Override
	public void newline() {
		line++;
		linePosition=basePosition;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.util.TokenizerInput#getLine()
	 */
	@Override
	public int getLine() {
		return line;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.util.TokenizerInput#getPosition()
	 */
	@Override
	public int getLinePosition() {
		return linePosition;
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.util.tokenizers.util.TokenizerInput#getPosition()
	 */
	@Override
	public int getPosition() {
		return absolutePosition;
	}

}
