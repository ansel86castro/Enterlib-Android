package com.enterlib.databinding;

import java.util.ArrayList;

import com.enterlib.parsing.Token;
import com.enterlib.parsing.TokenMismatchException;
import com.enterlib.parsing.TokenType;

public class BindingExpressionParser {
	BindingExpressionLexicalAnalizer lexical;

	public BindingExpressionParser(String str) {
		lexical = new BindingExpressionLexicalAnalizer(str);
	}

	public BindingExpression expression() throws Exception {
		BindingExpression e = new BindingExpression();
		Token id;
		Token t;

		// match LCURLY
		t = matchToken(TokenType.LCURLY);

		// match ID
		id = matchToken(TokenType.ID);

		t = matchToken(TokenType.COLON);

		Object _value = value();

		e.members.add(new ExpressionMember(id.Value, _value));

		while (true) {
			t = lexical.getNextToken();
			if (t == null) {
				throw new TokenMismatchException();
			}

			if (t.match(TokenType.RCURLY)) {
				break;
			}

			if (!t.match(TokenType.SEMICOLON)) {
				throw new TokenMismatchException();
			}

			id = matchToken(TokenType.ID);

			matchToken(TokenType.COLON);

			_value = value();

			e.members.add(new ExpressionMember(id.Value, _value));
		}

		e.init();
		return e;
	}

	private Object value() throws Exception {
		Token t = lexical.peekNextToken();
		if (t == null) {
			throw new TokenMismatchException();
		}

		if (t.match(TokenType.ID)) {
			lexical.consume();
			return t.Value;
		}

		if (t.match(TokenType.LCURLY)) {
			return expression();
		}

		if (t.match(TokenType.LBRACK)) {
			return expressionArray();
		}

		throw new TokenMismatchException();

	}

	private Object expressionArray() throws Exception {
		Token t = lexical.getNextToken();
		if (t == null) {
			throw new TokenMismatchException();
		}

		if (!t.match(TokenType.LBRACK)) {
			throw new TokenMismatchException();
		}

		ArrayList<Object> values = new ArrayList<Object>();
		Object v = value();
		values.add(v);

		while (true) {
			t = lexical.getNextToken();
			if (t == null) {
				throw new TokenMismatchException();
			}

			if (match(TokenType.RBRACK)) {
				break;
			}

			if (!match(TokenType.SEMICOLON)) {
				throw new TokenMismatchException();
			}

			v = value();
			values.add(v);

		}

		return values;

	}

	public Token matchToken(TokenType type) throws Exception {
		lexical.moveNext();
		if (!match(type)) {
			throw new TokenMismatchException(String.format("Expecting %s but found %s in %s", lexical.getCurrent(), type, lexical.getStringExpression()));
		}
		return lexical.getCurrent();
	}

	public Token matchOptional(TokenType type) throws Exception {
		lexical.moveNext();
		if (!match(type)) {
			throw new TokenMismatchException();
		}
		return lexical.getCurrent();
	}

	public boolean match(TokenType type) {
		Token t = lexical.getCurrent();
		return t != null && t.TokenType == type;
	}
}