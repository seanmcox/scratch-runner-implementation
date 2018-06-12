/**
 * 
 */
package com.shtick.util.tokenizers.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

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
public class BasicInputStreamTokenizerInput extends TokenizerInput{
	private InputStream in;
	private CharsetDecoder decoder;
	private byte[] b=new byte[512];
	private int bLength=0;

	private ByteBuffer inputBuffer; // An inputBuffer to wrap around b.
	private CharBuffer outputBuffer=CharBuffer.allocate(512); // A buffer to which decoded input should be written.
	
	private int basePosition;
	private int line;
	private int linePosition;
	private int absolutePosition;
	
	/**
	 * @param in
	 * @param baseLine 
	 * @param basePosition 
	 * @param cs
	 * @throws IOException 
	 */
	public BasicInputStreamTokenizerInput(InputStream in, int baseLine, int basePosition, Charset cs) throws IOException{
		this.in=in;
		this.basePosition = basePosition;
		this.line=baseLine;
		this.linePosition=basePosition;
		this.decoder=cs.newDecoder();
		inputBuffer=ByteBuffer.wrap(b, 0, 0);
		outputBuffer.flip();
		inputBuffer.compact();
	}

	/**
	 * @param in
	 * @param baseLine 
	 * @param basePosition 
	 * @param dec
	 * @throws IOException 
	 */
	public BasicInputStreamTokenizerInput(InputStream in, int baseLine, int basePosition, CharsetDecoder dec) throws IOException {
		this.in=in;
		this.basePosition = basePosition;
		this.line=baseLine;
		this.linePosition=basePosition;
		this.decoder=dec;
		inputBuffer=ByteBuffer.wrap(b, 0, 0);
		outputBuffer.flip();
		inputBuffer.compact();
	}

	/**
	 * @param in
	 * @param baseLine 
	 * @param basePosition 
	 * @param charsetName
	 * @throws UnsupportedEncodingException
	 * @throws IOException 
	 */
	public BasicInputStreamTokenizerInput(InputStream in, int baseLine, int basePosition, String charsetName) throws UnsupportedEncodingException, IOException {
		this.in=in;
		this.basePosition = basePosition;
		this.line=baseLine;
		this.linePosition=basePosition;
		Charset cs=Charset.forName(charsetName);
		this.decoder=cs.newDecoder();
		inputBuffer=ByteBuffer.wrap(b, 0, 0);
		outputBuffer.flip();
		inputBuffer.compact();
	}

	/**
	 * @param in
	 * @param baseLine 
	 * @param basePosition 
	 * @param encodingIdentifier 
	 * @throws IOException 
	 * @throws InvalidCharsetIdentificationException If the InputStream contains internally conflicting character encoding information.
	 *                                               This might occur if a byte order mark fatally disagrees with a determination made by the encodingIdentifier.
	 */
	public BasicInputStreamTokenizerInput(InputStream in, int baseLine, int basePosition, ContentBasedEncodingIdentifier encodingIdentifier) throws IOException, InvalidCharsetIdentificationException{
		this.in=in;
		this.basePosition = basePosition;
		this.line=baseLine;
		this.linePosition=basePosition;
		// Try to identify character encoding by byte order mark.
		String internalCharsetID=null;
		if(encodingIdentifier!=null){
			internalCharsetID=encodingIdentifier.identifyEncoding(new BufferingInputStream(),null);
			inputBuffer=ByteBuffer.wrap(b, 0, bLength);
		}
		else{
			inputBuffer=ByteBuffer.wrap(b, 0, 0);
		}
		outputBuffer.flip();
		inputBuffer.compact();
		Charset cs=null;
		if(internalCharsetID==null)
			cs=Charset.forName("UTF-8");
		else
			cs=Charset.forName(internalCharsetID);
		this.decoder=cs.newDecoder();
	}
	
	/**
	 * Reads the next character but keeps the cursor in the current position.
	 * ie. Subsequent calls will return the same thing.
	 * 
	 * @return The value that would be returned by a subsequent read.
	 * @throws IOException For any back-end IOException, or if there is no data left to peek at.
	 */
	@Override
	public char peek() throws IOException{
		if(!updateBuffer())
			throw new IOException("No data left to peek at.");
		return outputBuffer.charAt(0);
	}
	
	/**
	 * Reads the next character and moves the cursor to the next position.
	 * 
	 * @return The next character.
	 * @throws IOException For any back-end IOException, or if there is no data left to read.
	 */
	@Override
	public char read() throws IOException{
		if(!updateBuffer())
			throw new IOException("No data left to read.");
		linePosition++;
		absolutePosition++;
		return outputBuffer.get();
	}
	
	/**
	 * 
	 * @return true if there is no data left to read
	 *         false otherwise
	 * @throws IOException
	 */
	@Override
	public boolean isDone() throws IOException{
		return !updateBuffer();
	}
	
	/**
	 * 
	 * @return true if there is data in the buffer and
	 *         false if there was no data left to buffer.
	 * 
	 */
	private boolean updateBuffer() throws IOException{
		if(outputBuffer.hasRemaining())
			return true;
		
		// Fill the buffer, if possible.
		outputBuffer.compact(); // Prepare for writing.
		int l=0;
		while((l>=0)&&(inputBuffer.position()<(inputBuffer.limit()/2))){
			l=in.read(b, inputBuffer.position(), b.length-inputBuffer.position());
			if(l>0)
				inputBuffer.position(inputBuffer.position()+l);
		}
		inputBuffer.flip(); // Prepare for reading
		if(inputBuffer.hasRemaining())
			decoder.decode(inputBuffer, outputBuffer, false); // Decode from inputBuffer to outputBuffer
		outputBuffer.flip(); // Prepare for reading.
		inputBuffer.compact(); // Prepare for writing.
		return outputBuffer.hasRemaining();
	}
	
	/**
	 * Increments the line and resets the position.
	 */
	@Override
	public void newline(){
		line++;
		linePosition=basePosition;
	}

	/**
	 * @return the in
	 */
	public InputStream getIn() {
		return in;
	}

	/**
	 * @return the line
	 */
	@Override
	public int getLine() {
		return line;
	}

	/**
	 * @return the position
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
	
	/**
	 * This InputStream will begin reading in the buffer and then will copy to the buffer anything
	 * read from in. It will cut off access to any data that would overflow the buffer.
	 * 
	 * @author sean.cox
	 *
	 */
	private class BufferingInputStream extends InputStream{
		private int pos=0;

		/* (non-Javadoc)
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException {
			int retval;
			if(pos>=b.length)
				return -1;
			if(pos<bLength){
				retval=b[pos];
				pos++;
			}
			else{
				retval=in.read();
				if(retval>=0){
					b[pos]=(byte)retval;
					bLength++;
					pos++;
				}
			}
			return retval;
		}
		
	}
}
