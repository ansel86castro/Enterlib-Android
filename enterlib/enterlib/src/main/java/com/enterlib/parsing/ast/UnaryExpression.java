package com.enterlib.parsing.ast;

import com.enterlib.StringUtils;
import com.enterlib.data.sqlite.SQLQuery;
import com.enterlib.exceptions.InvalidOperationException;

public class UnaryExpression extends Expression {

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	Expression expression;

	public int getOperator() {
		return operator;
	}

	public void setOperator(int operator) {
		this.operator = operator;
	}

	int operator;
				
	public UnaryExpression(Expression expression, int operator) {
		super();
		this.expression = expression;
		this.operator = operator;
	}

	@Override
	public void checkSemantic(ASTContext context) {
		expression.checkSemantic(context);
		type = expression.type;

	}

	@Override
	public void genSQL(ASTContext context,StringBuilder sb, int tabOffset) {
		StringUtils.append(sb, '\t', tabOffset);
		
		sb.append(BinaryExpression.getSQLOperator(operator));
		if(expression instanceof  BinaryExpression) {
			sb.append('(');
			expression.genSQL(context, sb, 0);
			sb.append(')');
		}else{
			expression.genSQL(context, sb, 0);
		}
	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
		StringUtils.append(sb, '\t', tabOffset);

		sb.append(BinaryExpression.getSQLOperator(operator));
		expression.genSQL(context, sb, 0, query);
	}

	@Override
	public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {				
		sb.append(BinaryExpression.getODataOperator(operator));
		expression.genOData(context, sb, 0);		
		
	}

	@Override
	public String toString() {
		return BinaryExpression.getSQLOperator(operator)+expression.toString();
	}

	@Override
	public Object dynamicEval(IEvaluationContext evalContext) {
		Object value = expression.dynamicEval(evalContext);
		if (value == null)
			return null;

		switch (operator) {
			case BinaryExpression.OP_ADD:
				return value;
			case BinaryExpression.OP_SUB:
				return Mul(value, -1);
			default:
				throw new InvalidOperationException("Unary Operator not supported " + BinaryExpression.getSQLOperator(operator));
		}
	}

	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
