/**
 * 
 */
package com.shtick.util.tokenizers.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import com.shtick.util.tokenizers.StandardTokenIssue;
import com.shtick.util.tokenizers.StandardTokenTree;
import com.shtick.util.tokenizers.TokenTree;
import com.shtick.util.tokenizers.Tokenizer;
import com.shtick.util.tokenizers.util.BasicInputStreamTokenizerInput;
import com.shtick.util.tokenizers.util.ReaderTokenizerInput;
import com.shtick.util.tokenizers.util.TokenizerInput;

/**
 * See: http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf
 * 
 * @author sean.cox
 *
 */
public class JSONTokenizer implements Tokenizer<JSONToken> {
	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Tokenizer#tokenize(java.io.InputStream)
	 */
	@Override
	public TokenTree<JSONToken> tokenize(InputStream in)throws IOException{
		return tokenize(new BasicInputStreamTokenizerInput(in, 0, 1,"UTF8"));
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Tokenizer#tokenize(java.io.Reader)
	 */
	@Override
	public TokenTree<JSONToken> tokenize(Reader in) throws IOException {
		return tokenize(new ReaderTokenizerInput(in, 0, 1));
	}

	/* (non-Javadoc)
	 * @see com.shtick.os.core.syntax.Tokenizer#getValidMimeTypes()
	 */
	@Override
	public String[] getValidMimeTypes() {
		return new String[]{"application/json"};
	}

	private static TokenTree<JSONToken> tokenize(TokenizerInput in) throws IOException{
		JSONToken token=parseValueToken(in);

		// Only one value token should exist. Handle end of buffer.
		skipWhitespace(in);
		String text="";
		int line=in.getLine();
		int linePosition=in.getLinePosition();
		int position=in.getPosition();
		while(!in.isDone())
			text+=in.read();
			
		LinkedList<JSONToken> retlist=new LinkedList<>();
		if(token!=null)
			retlist.add(token);
		if(text.length()>0)
			retlist.add(new UnrecognizedToken(line, linePosition, in.getLine(), in.getLinePosition(), position, in.getPosition(), text));
		return new StandardTokenTree<JSONToken>(retlist);
	}
	
	/**
	 * Parses out a single value token.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static JSONToken parseValueToken(TokenizerInput in) throws IOException{
		skipWhitespace(in);
		if(in.isDone())
			throw new IOException("JSONToken expected to be parsed but not found.");
		char c=in.peek();
		switch(c){
		case '"': // Quoted string
			return parseStringToken(in);
		case '[': // Array
			return parseArrayToken(in);
		case '{': // Object
			return parseObjectToken(in);
		default: // Number, keyword, or invalid here.
			if(isAlphabeticChar(c))
				return parseKeywordToken(in);
			if(isNumericChar(c)||(c=='-'))
				return parseNumberToken(in);
		}
		return null;
	}
	
	/**
	 * 
	 * @param in
	 * @return A parsed ArrayToken, or null if in is done, or next character is not numeric/-.
	 * @throws IOException
	 */
	private static NumberToken parseNumberToken(TokenizerInput in) throws IOException{
		char c=in.peek();
		if(!(isNumericChar(c)||(c=='-')))
			return null;
		NegativeToken negativeToken=null;
		DigitsToken wholePart=null;
		PeriodToken periodToken=null;
		DigitsToken fractionalPart=null;
		ExponentToken exponentToken=null;
		JSONToken signToken=null;
		DigitsToken exponentialPart=null;
		if(c=='-'){
			negativeToken = new NegativeToken(in.getLine(), in.getLinePosition(), in.getPosition());
			in.read();
		}
		if((!in.isDone())&&(isNumericChar(in.peek()))){
			wholePart=parseDigitsToken(in);
		}
		if((!in.isDone())&&(in.peek()=='.')){
			periodToken = new PeriodToken(in.getLine(), in.getLinePosition(), in.getPosition());
			in.read();
			if((!in.isDone())&&(isNumericChar(in.peek())))
				fractionalPart=parseDigitsToken(in);
		}
		if((!in.isDone())&&((in.peek()=='e')||(in.peek()=='E'))){
			exponentToken = new ExponentToken(in.getLine(), in.getLinePosition(), in.getPosition(),in.read());
			if(!in.isDone()){
				if(in.peek()=='-'){
					signToken = new NegativeToken(in.getLine(), in.getLinePosition(), in.getPosition());
					in.read();
				}
				else if(in.peek()=='+'){
					signToken = new PositiveToken(in.getLine(), in.getLinePosition(), in.getPosition());
					in.read();
				}
			}
			if((!in.isDone())&&(isNumericChar(in.peek())))
				exponentialPart=parseDigitsToken(in);
		}
		return new NumberToken(negativeToken, wholePart, periodToken, fractionalPart, exponentToken, signToken, exponentialPart);
	}
	
	/**
	 * 
	 * @param in
	 * @return A parsed DigitsToken, or null if in is done, or next character is not numeric.
	 * @throws IOException
	 */
	private static DigitsToken parseDigitsToken(TokenizerInput in) throws IOException{
		if(!isNumericChar(in.peek()))
			return null;
		int line=in.getLine();
		int linePosition=in.getLinePosition();
		int position=in.getPosition();
		String digits="";
		while((!in.isDone())&&isNumericChar(in.peek()))
			digits+=in.read();
		return new DigitsToken(line, linePosition, position, digits);
	}
	
	/**
	 * 
	 * @param in
	 * @return A parsed ArrayToken, or null if in is done, or next character is not a beginning array bracket.
	 * @throws IOException
	 */
	private static ArrayToken parseArrayToken(TokenizerInput in) throws IOException{
		if((in.isDone())||(in.peek()!='['))
			return null;
		StartOfArrayToken startToken=new StartOfArrayToken(in.getLine(), in.getLinePosition(), in.getPosition());
		EndOfArrayToken endToken=null;
		in.read();
		skipWhitespace(in);
		LinkedList<JSONToken> contents=new LinkedList<>();
		JSONToken token = null;
		String unrecognizedText="";
		int line=0;
		int linePosition=0;
		int position=0;
		while(!(in.isDone()||(in.peek()==']'))){
			if(in.peek()==','){
				token = new CommaToken(in.getLine(), in.getLinePosition(), in.getPosition());
				if(contents.isEmpty()||(contents.getLast() instanceof CommaToken))
					token.addIssue(new StandardTokenIssue("ARR03", "Value expected in array.", token.getStartLine(), token.getStartLinePosition(), true));
				in.read();
				skipWhitespace(in);
			}
			else{
				token = parseValueToken(in);
				if((token!=null)&&(!contents.isEmpty())&&(!(contents.getLast() instanceof CommaToken)))
					token.addIssue(new StandardTokenIssue("ARR04", "Value in array should be preceeded with a comma.", token.getStartLine(), token.getStartLinePosition(), true));
				if(token!=null)
					skipWhitespace(in);
			}
			if(token!=null){
				if(unrecognizedText.length()>0){
					contents.add(new UnrecognizedToken(line, linePosition, token.getStartLine(), token.getStartLinePosition(), position, token.getStartPosition(), unrecognizedText));
					unrecognizedText="";
				}
				contents.add(token);
			}
			else{
				if(unrecognizedText.length()==0){
					line=in.getLine();
					linePosition=in.getLinePosition();
					position=in.getPosition();
				}
				unrecognizedText+=in.read();
			}
		}
		if(unrecognizedText.length()>0)
			contents.add(new UnrecognizedToken(line, linePosition, in.getLine(), in.getLinePosition(), position, in.getPosition(), unrecognizedText));
		if(!in.isDone()){
			endToken = new EndOfArrayToken(in.getLine(), in.getLinePosition(), in.getPosition());
			in.read();
		}
		return new ArrayToken(startToken, contents, endToken);
	}
	
	/**
	 * 
	 * @param in
	 * @return A parsed ObjectToken, or null if in is done, or next character is not a beginning object bracket.
	 * @throws IOException
	 */
	private static ObjectToken parseObjectToken(TokenizerInput in) throws IOException{
		if((in.isDone())||(in.peek()!='{'))
			return null;
		StartOfObjectToken startToken=new StartOfObjectToken(in.getLine(), in.getLinePosition(), in.getPosition());
		EndOfObjectToken endToken=null;
		in.read();
		skipWhitespace(in);
		LinkedList<JSONToken> contents=new LinkedList<>();
		JSONToken token = null;
		String unrecognizedText="";
		int line=0;
		int linePosition=0;
		int position=0;
		while(!(in.isDone()||(in.peek()=='}'))){
			if(in.peek()==','){
				token = new CommaToken(in.getLine(), in.getLinePosition(), in.getPosition());
				in.read();
				skipWhitespace(in);
			}
			else if(in.peek()==':'){
				token = new ColonToken(in.getLine(), in.getLinePosition(), in.getPosition());
				in.read();
				skipWhitespace(in);
			}
			else{
				token = parseValueToken(in);
				if(token!=null)
					skipWhitespace(in);
			}
			if(token!=null){
				if(unrecognizedText.length()>0){
					contents.add(new UnrecognizedToken(line, linePosition, token.getStartLine(), token.getStartLinePosition(), position, token.getStartPosition(), unrecognizedText));
					unrecognizedText="";
				}
				contents.add(token);
			}
			else{
				if(unrecognizedText.length()==0){
					line=in.getLine();
					linePosition=in.getLinePosition();
					position=in.getPosition();
				}
				unrecognizedText+=in.read();
			}
		}
		if(unrecognizedText.length()>0)
			contents.add(new UnrecognizedToken(line, linePosition, in.getLine(), in.getLinePosition(), position, in.getPosition(), unrecognizedText));
		if(!in.isDone()){
			endToken = new EndOfObjectToken(in.getLine(), in.getLinePosition(), in.getPosition());
			in.read();
		}
		blockObjectProperties(contents);
		return new ObjectToken(startToken, contents, endToken);
	}
	
	/**
	 * Blocks tokens into ObjectPropertyTokens
	 * @param tokens
	 */
	private static void blockObjectProperties(LinkedList<JSONToken> tokens){
		ListIterator<JSONToken> iter = tokens.listIterator();
		JSONToken lastToken=null;
		JSONToken token;
		StringToken label;
		ColonToken colon;
		JSONToken value;
		while(iter.hasNext()){
			token = iter.next();
			if(token instanceof UnrecognizedToken){
				lastToken = token;
				continue;
			}
			if(token instanceof CommaToken){
				if((lastToken==null)||(lastToken instanceof CommaToken))
					token.addIssue(new StandardTokenIssue("OBJ04", "Object parameter expected.", token.getStartLine(), token.getStartLinePosition(), true));
				lastToken = token;
				continue;
			}
			label=null;
			colon=null;
			value=null;
			if(token instanceof StringToken){
				label = (StringToken) token;
				iter.remove();
				if(iter.hasNext())
					token = iter.next();
				else
					token = null;
			}
			if((token!=null)&&(token instanceof ColonToken)){
				colon = (ColonToken) token;
				iter.remove();
				if(iter.hasNext())
					token = iter.next();
				else
					token = null;
			}
			if(token!=null){
				if(!((token instanceof ColonToken)||(token instanceof CommaToken))){
					value = token;
					iter.remove();
				}
				else{
					iter.previous();
				}
			}
			token = new ObjectPropertyToken(label, colon, value);
			if((lastToken!=null)&&(lastToken instanceof ObjectPropertyToken))
				token.addIssue(new StandardTokenIssue("OBJ05", "Comma expected.", token.getStartLine(), token.getStartLinePosition(), true));
			iter.add(token);
			lastToken = token;
		}
	}
	
	/**
	 * 
	 * @param in
	 * @return A parsed StringToken, or null if in is done, or next character is not a double quote.
	 * @throws IOException
	 */
	private static StringToken parseStringToken(TokenizerInput in) throws IOException{
		if((in.isDone())||(in.peek()!='"'))
			return null;
		DoubleQuoteToken startToken=new DoubleQuoteToken(in.getLine(), in.getLinePosition(), in.getPosition());
		char quote=in.read();
		String text="";
		LinkedList<JSONToken> contents=new LinkedList<>();
		JSONToken token=null;
		int line=0;
		int linePosition=0;
		int position=0;
		while((!in.isDone())&&(in.peek()!=quote)){
			if(in.peek()=='\\'){
				token=parseStringEscapeToken(in);
			}
			else{
				if(text.length()==0){
					line=in.getLine();
					linePosition=in.getLinePosition();
					position=in.getPosition();
				}
				text+=in.read();
			}
			if(token!=null){
				if(text.length()>0){
					contents.add(new RawTextToken(line, linePosition,token.getStartLine(),token.getStartLinePosition(), position, token.getStartPosition(), text));
					text="";
				}
				contents.add(token);
				token=null;
			}
		}
		if(text.length()>0)
			contents.add(new RawTextToken(line, linePosition, in.getLine(), in.getLinePosition(), position, in.getPosition(), text));
		DoubleQuoteToken endToken=null;
		if((!in.isDone())&&(in.peek()==quote)){
			endToken=new DoubleQuoteToken(in.getLine(), in.getLinePosition(), in.getPosition());
			in.read();
		}
		return new StringToken(startToken,contents,endToken);
	}
	
	/**
	 * 
	 * @param in
	 * @return A parsed KeywordToken, or null if in is done, or next character is not alphabetic.
	 * @throws IOException
	 */
	private static KeywordToken parseKeywordToken(TokenizerInput in) throws IOException{
		if(in.isDone())
			return null;
		char c=in.peek();
		if(!isAlphabeticChar(c))
			return null;
		String text="";
		int line=in.getLine();
		int linePosition=in.getLinePosition();
		int position=in.getPosition();
		while((!in.isDone())&&(isAlphabeticChar(c)||isNumericChar(c))) {
			text+=in.read();
			c=in.peek();
		}
		return new KeywordToken(line, linePosition, position, text);
	}
	
	/**
	 * 
	 * @param in
	 * @return A parsed CharRefToken or EntityRefToken, or null if none can be parsed.
	 * @throws IOException
	 */
	private static JSONToken parseStringEscapeToken(TokenizerInput in) throws IOException{
		if(in.isDone())
			return null;
		if(in.peek()!='\\')
			return null;
		int line=in.getLine();
		int linePosition=in.getLinePosition();
		int position=in.getPosition();
		in.read();
		String escapeSequence="";
		if(in.isDone()||(!StringEscapeToken.STRING_ESCAPE_CHARS.contains(""+in.peek()))){
			return new UnrecognizedToken(line, linePosition, in.getLine(), in.getLinePosition(), position, in.getPosition(), "\\");
		}
		escapeSequence+=in.read();
		if(escapeSequence.equals("u"))
			for(int i=0;(i<4)&&(!in.isDone())&&(isHexChar(in.peek()));i++)
				escapeSequence+=in.read();
		return new StringEscapeToken(line, linePosition, position,escapeSequence);
	}
	
	private static void skipWhitespace(TokenizerInput in) throws IOException{
		while((!in.isDone())&&(isJSONWhitespace(in.peek()))){
			if(in.peek()=='\n'){
				in.read();
				in.newline();
			}
			else if(in.peek()=='\r'){
				in.read();
				char c = in.peek();
				if(c=='\n')
					in.read();
				in.newline();
			}
			else{
				in.read();
			}
		}
	}

	/**
	 * See: http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf (section 4)
	 * 
	 * @param c The char to check
	 * @return true if the given character is JSON whitespace, and false otherwise.
	 */
	private static boolean isJSONWhitespace(char c){
		return (c==0x09)||(c==0x0A)||(c==0x0D)||(c==0x20);
	}

	/**
	 * @param c The char to check
	 * @return true if the given character is an alphabetic character.
	 */
	private static boolean isAlphabeticChar(char c){
		return ((c<='Z')&&(c>='A'))||((c<='z')&&(c>='a'));
	}

	/**
	 * @param c The char to check
	 * @return true if the given character is an alphabetic character.
	 */
	private static boolean isHexChar(char c){
		return isNumericChar(c)||((c<='F')&&(c>='A'))||((c<='f')&&(c>='a'));
	}

	/**
	 * @param c The char to check
	 * @return true if the given character is a numeric character.
	 */
	private static boolean isNumericChar(char c){
		return ((c<='9')&&(c>='0'));
	}
}
