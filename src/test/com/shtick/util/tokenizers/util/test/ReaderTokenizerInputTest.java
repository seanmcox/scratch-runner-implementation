/**
 * 
 */
package com.shtick.util.tokenizers.util.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.shtick.util.tokenizers.util.ReaderTokenizerInput;

/**
 * 
 * @author sean.cox
 *
 */
public class ReaderTokenizerInputTest {

	/**
	 * 
	 */
	@Test
	public void testConstructor() {
		@SuppressWarnings("unused")
		ReaderTokenizerInput input;

		input = new ReaderTokenizerInput(new StringReader(""), 0, 0);
		assertEquals(0,input.getLine());
		assertEquals(0,input.getLinePosition());
		assertEquals(0,input.getPosition());
		try{
			assertTrue(input.isDone());
		}
		catch(IOException t){
			t.printStackTrace();
			fail(t.getMessage());
		}

		input = new ReaderTokenizerInput(new StringReader("123"), 1, 2);
		assertEquals(1,input.getLine());
		assertEquals(2,input.getLinePosition());
		assertEquals(0,input.getPosition());
		try{
			assertFalse(input.isDone());
		}
		catch(IOException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	/**
	 * 
	 */
	@Test
	public void testIsDone() {
		ReaderTokenizerInput input;

		input = new ReaderTokenizerInput(new StringReader(""), 0, 0);
		try{
			assertTrue(input.isDone());
		}
		catch(IOException t){
			t.printStackTrace();
			fail(t.getMessage());
		}

		input = new ReaderTokenizerInput(new StringReader("1"), 1, 2);
		try{
			assertFalse(input.isDone());
			input.read();
			assertTrue(input.isDone());
		}
		catch(IOException t){
			t.printStackTrace();
			fail(t.getMessage());
		}

		input = new ReaderTokenizerInput(new StringReader("123456789"), 1, 2);
		try{
			assertFalse(input.isDone());
			for(int i=0;i<9;i++)
				input.read();
			assertTrue(input.isDone());
		}
		catch(IOException t){
			t.printStackTrace();
			fail(t.getMessage());
		}
	}
}
