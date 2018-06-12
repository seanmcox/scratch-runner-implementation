/**
 * 
 */
package com.shtick.util.tokenizers.json.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.shtick.util.tokenizers.Token;
import com.shtick.util.tokenizers.TokenTree;
import com.shtick.util.tokenizers.json.ColonToken;
import com.shtick.util.tokenizers.json.DigitsToken;
import com.shtick.util.tokenizers.json.DoubleQuoteToken;
import com.shtick.util.tokenizers.json.EndOfObjectToken;
import com.shtick.util.tokenizers.json.ExponentToken;
import com.shtick.util.tokenizers.json.JSONToken;
import com.shtick.util.tokenizers.json.JSONTokenizer;
import com.shtick.util.tokenizers.json.KeywordToken;
import com.shtick.util.tokenizers.json.NegativeToken;
import com.shtick.util.tokenizers.json.NumberToken;
import com.shtick.util.tokenizers.json.ObjectPropertyToken;
import com.shtick.util.tokenizers.json.ObjectToken;
import com.shtick.util.tokenizers.json.PeriodToken;
import com.shtick.util.tokenizers.json.PositiveToken;
import com.shtick.util.tokenizers.json.RawTextToken;
import com.shtick.util.tokenizers.json.StartOfObjectToken;
import com.shtick.util.tokenizers.json.StringEscapeToken;
import com.shtick.util.tokenizers.json.StringToken;
import com.shtick.util.tokenizers.json.UnrecognizedToken;

/**
 * @author sean.cox
 *
 */
public class JSONTokenizerTest {
	/**
	 * 
	 */
	@Test
	public void testConstructor() {
		@SuppressWarnings("unused")
		JSONTokenizer tokenizer=new JSONTokenizer();
	}

	/**
	 * 
	 */
	@Test
	public void testEmptyObjectParsing() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"{}","{}\n","{\n}","{}\r\n","{\r\n}"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof ObjectToken);
				assertFalse(iter.hasNext());
				
				ObjectToken objectToken = (ObjectToken)token;
				List<Token<JSONToken>> children = objectToken.getChildren();
				assertEquals(2, children.size());
				assertTrue(children.get(0) instanceof StartOfObjectToken);
				assertTrue(children.get(1) instanceof EndOfObjectToken);
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
		
	}

	/**
	 * 
	 */
	@Test
	public void testTokenPropertyParsing() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"{\"prop\":true}","{\"prop\":false}\n","{\"prop\":null}"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof ObjectToken);
				assertFalse(iter.hasNext());
				
				ObjectToken objectToken = (ObjectToken)token;
				List<Token<JSONToken>> children = objectToken.getChildren();
				assertEquals(3, children.size());
				assertTrue(children.get(0) instanceof StartOfObjectToken);
				assertTrue(children.get(1) instanceof ObjectPropertyToken);
				assertTrue(children.get(2) instanceof EndOfObjectToken);
				
				ObjectPropertyToken objectPropertyToken = (ObjectPropertyToken)children.get(1);
				children = objectPropertyToken.getChildren();
				assertEquals(3, children.size());
				assertTrue(children.get(0) instanceof StringToken);
				assertTrue(children.get(1) instanceof ColonToken);
				assertTrue(children.get(2) instanceof KeywordToken);
				
				assertEquals("prop",((StringToken)children.get(0)).getRepresentedString());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
		
	}

	/**
	 * 
	 */
	@Test
	public void testSimpleStringParsing() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"\"Hello World\""}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof StringToken);
				assertFalse(iter.hasNext());
				
				StringToken stringToken = (StringToken)token;
				assertEquals("Hello World",stringToken.getRepresentedString());
				List<Token<JSONToken>> children = stringToken.getChildren();
				assertEquals(3,children.size());
				assertTrue(children.get(0) instanceof DoubleQuoteToken);
				assertTrue(children.get(1) instanceof RawTextToken);
				assertTrue(children.get(2) instanceof DoubleQuoteToken);
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testStringWithEscapeCodeParsing() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"\"Hello\\\"\\\\ World\""}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof StringToken);
				assertFalse(iter.hasNext());
				
				StringToken stringToken = (StringToken)token;
				assertEquals("Hello\"\\ World",stringToken.getRepresentedString());
				List<Token<JSONToken>> children = stringToken.getChildren();
				assertEquals(6,children.size());
				assertTrue(children.get(0) instanceof DoubleQuoteToken);
				assertTrue(children.get(1) instanceof RawTextToken);
				assertTrue(children.get(2) instanceof StringEscapeToken);
				assertTrue(children.get(3) instanceof StringEscapeToken);
				assertTrue(children.get(4) instanceof RawTextToken);
				assertTrue(children.get(5) instanceof DoubleQuoteToken);
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testStringWithoutCloseParsing() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"\"Hello World"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(1, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof StringToken);
				assertFalse(iter.hasNext());
				
				StringToken stringToken = (StringToken)token;
				assertEquals("Hello World",stringToken.getRepresentedString());
				List<Token<JSONToken>> children = stringToken.getChildren();
				assertEquals(2,children.size());
				assertTrue(children.get(0) instanceof DoubleQuoteToken);
				assertTrue(children.get(1) instanceof RawTextToken);
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingInt() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"123"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(1,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertFalse(	numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertNull(numberToken.getExponentialPart());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingNegativeInt() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"-123"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(2,children.size());
				assertTrue(children.get(0) instanceof NegativeToken);
				assertTrue(children.get(1) instanceof DigitsToken);
				assertTrue(numberToken.isNegative());
				assertEquals(children.get(1),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertNull(numberToken.getExponentialPart());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingBadPositive() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"+123"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(1, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof UnrecognizedToken);
				assertFalse(iter.hasNext());
				
				UnrecognizedToken unrecognizedToken = (UnrecognizedToken)token;
				assertEquals("+123",unrecognizedToken.getText());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingZero() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"0"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(1,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertFalse(numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("0",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertNull(numberToken.getExponentialPart());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingInvalidZero() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"0123"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(1, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(1,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertFalse(numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("0123",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertNull(numberToken.getExponentialPart());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingIntDec() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"123."}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(1, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(2,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertTrue(children.get(1) instanceof PeriodToken);
				assertFalse(	numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertNull(numberToken.getExponentialPart());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingFloat() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"123.456"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(3,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertTrue(children.get(1) instanceof PeriodToken);
				assertTrue(children.get(2) instanceof DigitsToken);
				assertFalse(	numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertEquals(children.get(2),numberToken.getFractionalPart());
				assertEquals("456",numberToken.getFractionalPart().getText());
				assertNull(numberToken.getExponentialPart());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingExp1() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"123e789"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(3,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertTrue(children.get(1) instanceof ExponentToken);
				assertTrue(children.get(2) instanceof DigitsToken);
				assertFalse(	numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertEquals('e',((ExponentToken)children.get(1)).getSymbol());
				assertEquals(children.get(2),numberToken.getExponentialPart());
				assertEquals("789",numberToken.getExponentialPart().getText());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingExp2() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"123E+897"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(4,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertTrue(children.get(1) instanceof ExponentToken);
				assertTrue(children.get(2) instanceof PositiveToken);
				assertTrue(children.get(3) instanceof DigitsToken);
				assertFalse(	numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertEquals('E',((ExponentToken)children.get(1)).getSymbol());
				assertEquals(children.get(3),numberToken.getExponentialPart());
				assertEquals("897",numberToken.getExponentialPart().getText());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingExp3() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"123e-978"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(4,children.size());
				assertTrue(children.get(0) instanceof DigitsToken);
				assertTrue(children.get(1) instanceof ExponentToken);
				assertTrue(children.get(2) instanceof NegativeToken);
				assertTrue(children.get(3) instanceof DigitsToken);
				assertFalse(	numberToken.isNegative());
				assertEquals(children.get(0),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertNull(numberToken.getFractionalPart());
				assertEquals('e',((ExponentToken)children.get(1)).getSymbol());
				assertEquals(children.get(3),numberToken.getExponentialPart());
				assertEquals("978",numberToken.getExponentialPart().getText());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void testNumberParsingAll() {
		JSONTokenizer tokenizer=new JSONTokenizer();
		for(String json:new String[]{"-123.456e-789"}){
			try{
				TokenTree<JSONToken> tokenTree=tokenizer.tokenize(new ByteArrayInputStream(json.getBytes("UTF-8")));
				assertEquals(0, tokenTree.getAllIssues().size());
				Iterator<JSONToken> iter = tokenTree.iterator();
				assertTrue(iter.hasNext());
				JSONToken token = iter.next(); 
				assertTrue(token instanceof NumberToken);
				assertFalse(iter.hasNext());
				
				NumberToken numberToken = (NumberToken)token;
				List<Token<JSONToken>> children = numberToken.getChildren();
				assertEquals(7,children.size());
				assertTrue(children.get(0) instanceof NegativeToken);
				assertTrue(children.get(1) instanceof DigitsToken);
				assertTrue(children.get(2) instanceof PeriodToken);
				assertTrue(children.get(3) instanceof DigitsToken);
				assertTrue(children.get(4) instanceof ExponentToken);
				assertTrue(children.get(5) instanceof NegativeToken);
				assertTrue(children.get(6) instanceof DigitsToken);
				assertTrue(	numberToken.isNegative());
				assertEquals(children.get(1),numberToken.getWholePart());
				assertEquals("123",numberToken.getWholePart().getText());
				assertEquals(children.get(3),numberToken.getFractionalPart());
				assertEquals("456",numberToken.getFractionalPart().getText());
				assertEquals('e',((ExponentToken)children.get(4)).getSymbol());
				assertEquals(children.get(6),numberToken.getExponentialPart());
				assertEquals("789",numberToken.getExponentialPart().getText());
			}
			catch(IOException t){
				throw new RuntimeException(t);
			}
		}
	}
}
