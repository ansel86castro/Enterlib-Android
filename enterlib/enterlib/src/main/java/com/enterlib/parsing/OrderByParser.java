package com.enterlib.parsing;

import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.OrderByExpression;
import com.enterlib.parsing.ast.OrderByListExpression;
import com.enterlib.parsing.ast.RecognitionException;

public class OrderByParser {
	/*
	 * GRAMMAR
	 *  orderByExp: ID(. ID)* ID? (, ID(. ID)* ID?)*
	 *  		  ;
	 * */
	QueryLexicalAnalizer lexicalAnalizer;
	
	public Expression parse(String query){
		lexicalAnalizer = new QueryLexicalAnalizer(query);
		Expression exp = orderByListExpression();
		if(lexicalAnalizer.getCurrent() != null){
			throw new RecognitionException("Expecting EOF");
		}
		return exp;
	}

	private Expression orderByListExpression() {
		OrderByListExpression list=new OrderByListExpression();								
		list.add(orderbyExpression());
		
		while(true){
			Token t  = lexicalAnalizer.getCurrent();			
			if (t == null) {
				break;
			}
			
			if(!t.match(TokenType.SEMICOLON)){
				throw new RecognitionException("Expecting ',' Found "+t.TokenType.name(), t.Row, t.Col);
			}						
			lexicalAnalizer.getNextToken();						
			list.add(orderbyExpression());											
		}
		
		return list;
	}

	private OrderByExpression orderbyExpression() {
		Token t = lexicalAnalizer.getCurrent();		
		if(t == null){
			throw new RecognitionException("Expecting 'variable' at", t.Row, t.Col);
		}
		
		if(!t.match(TokenType.ID)){		
			throw new RecognitionException("Expecting 'variable' instead found "+t.TokenType.name(), t.Row, t.Col);
		}
				
		OrderByExpression exp = new OrderByExpression(t.Value);		
		
		t= lexicalAnalizer.getNextToken();
		if(t != null && t.match(TokenType.ID)){
			if(!t.Value.equalsIgnoreCase(OrderByExpression.DESC)){
				throw new RecognitionException("Expecting 'DESC' at", t.Row, t.Col);
			}
			exp.setOrderType(OrderByExpression.DESCENDING);
			lexicalAnalizer.getNextToken();
		}
		return exp;
	}
}
