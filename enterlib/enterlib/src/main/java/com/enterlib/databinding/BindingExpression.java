package com.enterlib.databinding;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Binding expression sample {required:true, binding:PasswordCheck,
 * validators:[passwordMatch]} A Binding expression consist in a key-word value
 * pair dictionary keys: required: [true|false] binding: Name validators:
 * NamedArray comparer:Name converter:Name
 * */
public class BindingExpression {
	ArrayList<ExpressionMember> members = new ArrayList<ExpressionMember>();
	HashMap<String, ExpressionMember> loookup = new HashMap<String, ExpressionMember>();

	public BindingExpression() {
	}

	public ExpressionMember get(int index) {
		return members.get(index);
	}

	public ExpressionMember getMember(String key) {
		return loookup.get(key);
	}

	public boolean containsMember(String key) {
		return loookup.containsKey(key);
	}

	public int size() {
		return members.size();
	}

	public static BindingExpression parse(String value) throws Exception {
		BindingExpressionParser parser = new BindingExpressionParser(value);
		BindingExpression binding = parser.expression();
		return binding;
	}

	public void init() {
		int count = members.size();
		for (int i = 0; i < count; i++) {
			ExpressionMember member = members.get(i);
			loookup.put(member.key, member);
		}
	}

	/*
	 * Grammar expression: LCURLY ID COLON value (SEMICOLON ID COLON value)* RCURLY;
	 * value: ID |expression |expressionArray
	 * 
	 * expressionArray: LBRACK value (SEMICONLON value)* RBRACK
	 */

}
