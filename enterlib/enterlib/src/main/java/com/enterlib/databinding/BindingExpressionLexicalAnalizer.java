package com.enterlib.databinding;

import java.text.ParseException;
import java.util.LinkedList;

import com.enterlib.parsing.Token;
import com.enterlib.parsing.TokenType;

public class BindingExpressionLexicalAnalizer {
	String _string;
	int index;

	// lookahead list
	LinkedList<Token> tokens = new LinkedList<Token>();
	Token current;

	public BindingExpressionLexicalAnalizer(String value) {
		this._string = value;
	}

	public String getStringExpression(){
		return _string;
	}

	public Token getCurrent() {
		return current;
	}

	public void moveNext() throws ParseException {
		getNextToken();
	}

	public Token getNextToken() throws ParseException {
		if (tokens.size() > 0) {
			current = tokens.removeFirst();
		} else {
			current = _getNextToken();
		}

		return current;
	}

	public void consume() {
		if (tokens.size() > 0) {
			current = tokens.removeFirst();
		}
	}

	public Token peekNextToken() throws ParseException {
		Token t = _getNextToken();
		if (t != null) {
			tokens.addLast(t);
		}
		current = t;
		return t;
	}

	public Token _getNextToken() throws ParseException {
		Token token = null;
		if (_string == null || index >= _string.length()) {
			return token;
		}

		int end = index;
		char c = _string.charAt(index);

		while (end < _string.length()) {
			c = _string.charAt(end);
			if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
				if (index == end) {
					end++;
					index++;
					continue;
				} else {
					break;
				}
			}

			if (Character.isLetterOrDigit(c) || c == '_') {
				end++;
			} else if ((token = isSymbol(end)) != null) {
				break;
			} else {
				throw new ParseException(_string, end);
			}

		}

		if (end > index) {
			token = new Token(TokenType.ID, _string.substring(index, end));
			index = end;

		} else if (token != null) {
			index++;
		}

		return token;

	}

	private Token isSymbol(int index) {
		char c = _string.charAt(index);
		Token token = null;
		switch (c) {
		case '{':
			token = new Token(TokenType.LCURLY, "{");
			break;
		case '}':
			token = new Token(TokenType.RCURLY, "}");
			break;
		case '[':
			token = new Token(TokenType.LBRACK, "[");
			break;
		case ']':
			token = new Token(TokenType.RBRACK, "]");
			break;
		case ':':
			token = new Token(TokenType.COLON, ":");
			break;
		case ',':
			token = new Token(TokenType.SEMICOLON, ",");
			break;
		}
		return token;
	}
}