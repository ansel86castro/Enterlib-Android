package com.enterlib.parsing.ast;

public class LogicalExpression extends BinaryExpression {

	
	public LogicalExpression(Expression left, Expression right,
			int operator) {
		super(left, right, operator);
		// TODO Auto-generated constructor stub
	}
	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
