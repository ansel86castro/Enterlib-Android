package com.enterlib.parsing;


public class Token {
	public TokenType TokenType;
	public String Value;
	public int Col;
	public int Row;
	
	public Token(TokenType tokenType, String value) {
		TokenType = tokenType;
		Value = value;
	}

	public boolean match(TokenType t) {
		return this.TokenType == t;
	}

	@Override
	public String toString() {
		return TokenType.name() + ":" + Value;
	}
}