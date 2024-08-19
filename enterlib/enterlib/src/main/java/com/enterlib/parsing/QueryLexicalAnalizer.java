package com.enterlib.parsing;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.HashMap;

public class QueryLexicalAnalizer {	
	String _string;
	String _lower;
	int index;
	int len;	
	int simbolWidth;
	int Col;
	int Row;
	
	HashMap<String, TokenType>keywords = new HashMap<String, TokenType>();
	
	// lookahead list	
	ArrayDeque<Token>tokens =new ArrayDeque<Token>(4);		
	Token current;
	
	public QueryLexicalAnalizer() {
		
		keywords.put("and", TokenType.AND);
		keywords.put("or", TokenType.OR);
		keywords.put("null", TokenType.NULL);
		keywords.put("like", TokenType.LIKE);
		keywords.put("true", TokenType.TRUE);
		keywords.put("false", TokenType.FALSE);
		
		keywords.put("plus", TokenType.PLUS);
		keywords.put("sub", TokenType.SUB);
		keywords.put("mul", TokenType.MUL);
		keywords.put("div", TokenType.DIV);
		keywords.put("eq", TokenType.EQUAL);
		keywords.put("lt", TokenType.LESS);
		keywords.put("gt", TokenType.GREATER);
		keywords.put("ge", TokenType.GEQUAL);
		keywords.put("le", TokenType.LEQUAL);
		keywords.put("ne", TokenType.NEQUAL);
	}

	public QueryLexicalAnalizer(String value){
		this();

		setValue(value);
	}


	public void setValue(String value){
		_string = value;
		_lower = value.toLowerCase();
		len = value.length();

		tokens.clear();
		index = 0;
		simbolWidth = 0;
		Col =0;
		Row = 0;
		current = null;
	}

	public String getValue(){
		return _string;
	}

	public Token getCurrent() {
		if(current == null){
			current = _getNextToken();
			if(current!=null){
				Col = current.Col;
				Row = current.Row;				
			}
		}			
		return current;
	}
		
	public Token lookAhead() throws ParseException{		
		 Token t = _getNextToken();
		 if(t!=null){
			 Col = t.Col;
			 Row = t.Row;
			 tokens.addLast(t);
		 }
		return t;
	}

	public Token getNextToken()  {			
		if (tokens.size() > 0) {
			current = tokens.removeFirst();			
		} else {
			current= _getNextToken();
			if(current!=null){
				Col = current.Col;
				Row = current.Row;			
			}			
		}	
		return current;
	}

	public void consumeLookAhead() {
		if (tokens.size() > 0) {
			tokens.removeFirst();			
		}
	}
	
	protected Token _getNextToken() {		
		skipSpaces();		
		int simbol = peek();
		
		if(simbol == -1)
			return null;
		
		String value;
		
		TokenType tokenType = isSymbol(simbol);
		
		if(tokenType!=TokenType.UNKNOW){
			value = _string.substring(index, index+simbolWidth);
			skip(simbolWidth);
			return new Token(tokenType, value);
		}
		
		if(simbol == '\''){
			value = readString();
			return new Token(TokenType.STRING, value);
			
		}else if(Character.isDigit(simbol)){
			value= readInteger();		
			simbol = peek();
			if(simbol == -1 || isWhiteSpace(simbol))
				return new Token(TokenType.INT, value);
			
			TokenType token = isSymbol(simbol);
			if(token == TokenType.DOT){
				read();
				return new Token(TokenType.DOUBLE, value+"."+readInteger());
			}else if(token != TokenType.UNKNOW){
				return new Token(TokenType.INT, value);
			}else			
				throw new TokenMismatchException(String.format("expecting number at COL: %d ROW: %d", Col, Row));
			
		}else if (simbol =='_' || Character.isLetter(simbol)){
			value = readWord(simbol);
			TokenType keyword = getKeyword(value);
			if(keyword!=null){
				return new Token(keyword, value);
			}
			return new Token(TokenType.ID, value);			
		}
		
		return null;
			
	}

	private Token getTokenSymbol(int index) {
		char c = _string.charAt(index);
		Token token = null;		
		char nextSimbol = index < (len-1) ?_string.charAt(index+1):0;
		switch (c) {
		case '{':
			token = new Token(TokenType.LCURLY, "{");
			simbolWidth=1;
			break;
		case '}':
			token = new Token(TokenType.RCURLY, "}");
			simbolWidth=1;
			break;
		case '[':
			token = new Token(TokenType.LBRACK, "[");
			simbolWidth=1;
			break;
		case ']':
			token = new Token(TokenType.RBRACK, "]");
			simbolWidth=1;
			break;
		case ':':
			token = new Token(TokenType.COLON, ":");
			simbolWidth=1;
			break;
		case ',':
			token = new Token(TokenType.SEMICOLON, ",");
			simbolWidth=1;
			break;
		case '.':
			token = new Token(TokenType.DOT, ".");
			simbolWidth=1;
			break;
		case '<':	
			if( nextSimbol == '='){
				token = new Token(TokenType.LEQUAL, "<=");
				simbolWidth=2;
			}
			else{
				token= new Token(TokenType.LESS, "<");
				simbolWidth=1;
			}
			break;
		case '>':	
			if( nextSimbol == '='){
				token =  new Token(TokenType.GEQUAL, ">=");
				simbolWidth=2;
			}
			else{
				token = new Token(TokenType.GREATER, ">");
				simbolWidth=1;
			}
			break;		
		case '=':			
			token = new Token(TokenType.EQUAL, "=");
			simbolWidth=1;
			break;	
		case '!':			
			if(nextSimbol == '='){
				token = new Token(TokenType.NEQUAL, "!=");
				simbolWidth=2;
			}
			else
				throw new TokenMismatchException("only ! found ,expected !=");
									    
			break;	
		case '(':
			token = new Token(TokenType.LPARENT, "(");
			simbolWidth=1;
			break;
		case ')':
			token = new Token(TokenType.LPARENT, "(");
			simbolWidth=1;
			break;		
		}
		
//		if(token == null){
//			if(match(index, "and")){
//				simbolWidth+=3;
//				token = new Token(TokenType.AND, "AND");
//			}else if(match(index, "or")){
//				simbolWidth+=3;
//				token = new Token(TokenType.OR, "OR");
//			}
//			else if(match(index, "NULL")){
//				simbolWidth+=4;
//				token = new Token(TokenType.NULL, "NULL");
//			}else if(match(index, "LIKE")){
//				simbolWidth+=4;
//				token = new Token(TokenType.LIKE, "LIKE");				
//			}			
//		}
		return token;
	}
	
	public boolean match(int start, String value){
		int valueLen = value.length();
		char end = 0;
		if(start + valueLen < len){
			end = _lower.charAt(start + valueLen);		
			if(end != ' ' || end != '\t' || end!= '\r' || end!='\n')
				return false;
		}
		
		for (int i = 0; i < valueLen && (start + i) < len ; i++) {
			if(value.charAt(i)!= _lower.charAt(start + i)){
				return false;				
			}
		}			
		
		return true;
	}
	
	public TokenType isSymbol(int simbol){
		simbolWidth = 0;
		TokenType token = TokenType.UNKNOW;	
		int nextSimbol = -1;
		if((index + 1) < len)
			nextSimbol = _string.charAt(index+1);
		
		switch (simbol) {
		case '{':
			token = TokenType.LCURLY;
			simbolWidth=1;
			break;
		case '}':
			token = TokenType.RCURLY;
			simbolWidth=1;
			break;
		case '[':
			token = TokenType.LBRACK;
			simbolWidth=1;
			break;
		case ']':
			token = TokenType.RBRACK;
			simbolWidth=1;
			break;
		case ':':
			token = TokenType.COLON;;
			simbolWidth=1;
			break;
		case ',':
			token = TokenType.SEMICOLON;
			simbolWidth=1;
			break;
		case '.':
			token = TokenType.DOT;
			simbolWidth=1;
			break;
		case '<':	
			if( nextSimbol == '='){
				token =TokenType.LEQUAL;
				simbolWidth=2;
			}
			else{
				token= TokenType.LESS;
				simbolWidth=1;
			}
			break;
		case '>':	
			if( nextSimbol == '='){
				token = TokenType.GEQUAL;
				simbolWidth=2;
			}
			else{
				token = TokenType.GREATER;
				simbolWidth=1;
			}
			break;		
		case '=':			
			token = TokenType.EQUAL;
			simbolWidth=1;
			if(nextSimbol == '='){
				simbolWidth=2;
			}
			break;	
		case '!':			
			if(nextSimbol == '='){
				token =TokenType.NEQUAL;
				simbolWidth=2;
			}
			else
				throw new TokenMismatchException("only ! found ,expected !=");
									    
			break;	
		case '(':
			token = TokenType.LPARENT;
			simbolWidth=1;
			break;
		case ')':
			token = TokenType.RPARENT;
			simbolWidth=1;
			break;		
		case '-':
			token = TokenType.SUB;
			simbolWidth=1;
			break;		
		case '+':
			token = TokenType.PLUS;
			simbolWidth=1;
			break;		
		case '*':
			token = TokenType.MUL;
			simbolWidth=1;
			break;		
		case '/':
			token = TokenType.DIV;
			simbolWidth=1;
			break;
		case '&':
			if(nextSimbol == '&'){
				simbolWidth=2;
				token = TokenType.AND;
			}else
				throw new TokenMismatchException("only & found ,expected &&");
			break;
		case '|':
				if(nextSimbol == '|'){
					simbolWidth=2;
					token = TokenType.OR;
				}else
					throw new TokenMismatchException("only | found ,expected ||");
				break;
		}
		return token;
	}
	
	public int read(){
		if(index < len){
			char c = _string.charAt(index++);
			if(c == '\n'){
				Row++;
				Col = 1;
			}else{
				Col++;
			}
			return c;
		}
		return -1;
	}
	
	public void skip(int characters){
		while (characters > 0) {
			read();
			characters--;
		}
	}
	
	public int peek(){
		if(index < len)
			return _string.charAt(index);
		return -1;
	}	

	public String readString(){
		StringBuilder sb= new StringBuilder();		
		boolean scape = false;			
		int c = read();
		if(c == '\'')
			c = read();
		
		while(c > 0){
			if(scape){
				sb.append((char)c);
				scape = false;				
			}
			else if(c == '\\'){
				scape = true;				
			}
			else if(c == '\''){
				break;
			}else{
				sb.append((char)c);
			}
			c = read();
		}		
		return sb.toString();			
	}
	
	public boolean isWhiteSpace(int simbol){
		return simbol == ' ' || simbol == '\t' || simbol == '\r' || simbol == '\n';
	}
	
	public String readWord(int simbol){
		StringBuilder sb= new StringBuilder();				
		int c = peek();		
		while(c > 0){
			c = peek();			
			if(c == -1){
				break;
			} 
			
			if(isWhiteSpace(c) || isSymbol(c) != TokenType.UNKNOW || c == '\''){
				break;
			}else if(!Character.isLetterOrDigit(c) && c!='_'){
				throw new TokenMismatchException(String.format("Expecting ID '%s' at COL %d ROW %d", sb.toString(), Col, Row));
			}
			
			sb.append((char)c);
			read();
		}
		return sb.toString();
	}
	
	public String readInteger(){
		StringBuilder sb= new StringBuilder();	
		int c = peek();		
		
		while(c > 0){
			 c = peek();
			 if(c == -1)
				break;			 
			if(isWhiteSpace(c) || isSymbol(c) != TokenType.UNKNOW || c == '\''){
				break;
			}else if(!Character.isDigit(c)){
				throw new TokenMismatchException(String.format("Expecting Integer '%s' at COL:%d ROW: %d", sb.toString(), Col, Row));
			}
			
			sb.append((char)c);
			read();			
		}
		return sb.toString();
	}
	
	public String readNumber(){
		String intPart = readInteger();
		int c = peek();	
		TokenType token = isSymbol(c);
		if(token == TokenType.DOT){
			return intPart+"."+readInteger();
		}else if(token != TokenType.UNKNOW){
			return intPart;
		}else			
			throw new TokenMismatchException(String.format("expecting number at COL: %d ROW: %d", Col, Row));
	}
	
	@SuppressLint("DefaultLocale")
	public TokenType getKeyword(String word){
		return keywords.get(word.toLowerCase());
	}
	
	public void skipSpaces(){		
		while(true){
			int simbol = peek();
			if(!isWhiteSpace(simbol))
				break;
			
			read();
		}
	}
}
