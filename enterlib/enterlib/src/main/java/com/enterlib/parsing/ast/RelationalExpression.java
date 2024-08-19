package com.enterlib.parsing.ast;

public class RelationalExpression extends BinaryExpression {

	
	public RelationalExpression(Expression left, Expression right, int operator) {
		super(left, right, operator);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void checkSemantic(ASTContext context) {	
		super.checkSemantic(context);
		
		if(type == ExpressionType.Null)
			throw new RecognitionException("Null type not allowed for a relational expression", Col, Row);
		
		type = ExpressionType.Bool;
	}
	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
