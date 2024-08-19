package com.enterlib.parsing.ast;

import com.enterlib.StringUtils;
import com.enterlib.data.sqlite.SQLQuery;
import com.enterlib.exceptions.InvalidOperationException;

public  class BinaryExpression extends Expression {
	
		public static final int OP_ADD = 1;
		public static  final int OP_SUB = 2;
		public static  final int OP_DIV = 3;
		public static  final int OP_MUL = 4;
		public static  final int OP_AND = 5;
		public static  final int OP_OR = 6;
		public static  final int OP_EQUAL = 7;
		public static  final int OP_DISTINT = 8;
		public static  final int OP_LIKE = 9;
		public static  final int OP_LESS = 10;
		public static  final int OP_GREATHER = 11;
		public static  final int OP_GREATHER_EQ = 12;
		public static  final int OP_LESS_EQ = 13;
		
	
	
	Expression left;
	Expression right;
	int operator;
	
	public Expression getLeft() {
		return left;
	}
	public void setLeft(Expression left) {
		this.left = left;
	}
	public Expression getRight() {
		return right;
	}
	public void setRight(Expression right) {
		this.right = right;
	}
	public int getOperator() {
		return operator;
	}
	public void setOperator(int operator) {
		this.operator = operator;
	}
	public BinaryExpression() {
		// TODO Auto-generated constructor stub
	}
	public BinaryExpression(Expression left, Expression right,
			int operator) {
		super();
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	
	@Override
	public void checkSemantic(ASTContext context) {
		left.checkSemantic(context);
		
		right.checkSemantic(context);
		
		type = ExpressionType.match(left.type, right.type);
		if(type==null){
			throw new RecognitionException("Type mistmatch "+toString(),Col, Row);
		}				
	}
	
	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset) {
		StringUtils.append(sb, '\t', tabOffset);
		
		if(left instanceof BinaryExpression){
			sb.append('(');
			left.genSQL(context, sb, 0);
			sb.append(')');
		}else{
			left.genSQL(context, sb, 0);
		}
		
		sb.append(' ');

		if((right instanceof LiteralExpression && ((LiteralExpression)right).type == ExpressionType.Null) ||
				(left instanceof LiteralExpression && ((LiteralExpression)left).type == ExpressionType.Null) ){
			sb.append(" IS ");
		}
		else {
			sb.append(getSQLOperator(operator));
		}
		
		sb.append(' ');
		
		if(right instanceof BinaryExpression){
			sb.append('(');
			right.genSQL(context, sb, 0);
			sb.append(')');
		}else{
			right.genSQL(context, sb, 0);
		}
	
	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
		StringUtils.append(sb, '\t', tabOffset);

		if(left instanceof BinaryExpression){
			sb.append('(');
			left.genSQL(context, sb, 0, query);
			sb.append(')');
		}else{
			left.genSQL(context, sb, 0, query);
		}

		sb.append(' ');

		if((right instanceof LiteralExpression && ((LiteralExpression)right).type == ExpressionType.Null) ||
				(left instanceof LiteralExpression && ((LiteralExpression)left).type == ExpressionType.Null) ){
			sb.append(" IS ");
		}
		else {
			sb.append(getSQLOperator(operator));
		}

		sb.append(' ');

		if(right instanceof BinaryExpression){
			sb.append('(');
			right.genSQL(context, sb, 0 ,query);
			sb.append(')');
		}else{
			right.genSQL(context, sb, 0, query);
		}

	}

	@Override
	public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {
		if(left instanceof BinaryExpression){
			sb.append('(');
			left.genOData(context, sb, 0);
			sb.append(')');
		}else{
			left.genOData(context, sb, 0);
		}
		
		sb.append(' ');
		
		sb.append(getODataOperator(operator));
		
		sb.append(' ');
		
		if(right instanceof BinaryExpression){
			sb.append('(');
			right.genOData(context, sb, 0);
			sb.append(')');
		}else{
			right.genOData(context, sb, 0);
		}
	
	
	}

	@Override
	public String toString() {
		return left.toString()+" " +getSQLOperator(operator)+" "+right.toString();
	}

	public static String getSQLOperator(int op){
		switch (op) {
		case OP_ADD: return "+";
		case OP_SUB: return "-";
		case OP_MUL: return "*";
		case OP_DIV: return "/";
		case OP_EQUAL: return "=";
		case OP_LESS: return "<";
		case OP_GREATHER: return ">";
		case OP_GREATHER_EQ: return ">=";
		case OP_LESS_EQ: return "<=";
		case OP_AND: return "AND";
		case OP_OR: return "OR";
		case OP_LIKE: return "LIKE";
		case OP_DISTINT: return "!=";
		default: return null;
		}
	}
	
	public static String getODataOperator(int op){
		switch (op) {
		case OP_ADD: return "plus";
		case OP_SUB: return "sub";
		case OP_MUL: return "mul";
		case OP_DIV: return "div";
		case OP_EQUAL: return "eq";
		case OP_LESS: return "lt";
		case OP_GREATHER: return "gt";
		case OP_GREATHER_EQ: return "ge";
		case OP_LESS_EQ: return "le";
		case OP_AND: return "and";
		case OP_OR: return "or";
		case OP_LIKE: return "like";
		case OP_DISTINT: return "ne";
		default: return null;
		}
	}

	@Override
	public Object dynamicEval(IEvaluationContext evalContext) {
		Object valueLeft = left.dynamicEval(evalContext);
		Object valueRight = right.dynamicEval(evalContext);

		switch (operator) {
			case OP_ADD: return Add(valueLeft, valueRight);
			case OP_SUB: return Substract(valueLeft, valueRight);
			case OP_MUL: return Mul(valueLeft, valueRight);
			case OP_DIV: return Div(valueLeft, valueRight);
			case OP_EQUAL: return valueLeft!=null ?valueLeft.equals(valueRight):
								  valueRight!=null ? valueRight.equals(valueLeft):
									null;
			case OP_LESS: return valueLeft == null || valueRight==null ? null: ((Comparable)valueLeft).compareTo(valueRight) < 0;
			case OP_GREATHER: return valueLeft == null || valueRight==null ? null:((Comparable)valueLeft).compareTo(valueRight) > 0;
			case OP_GREATHER_EQ: return valueLeft == null || valueRight==null ? null: ((Comparable)valueLeft).compareTo(valueRight) >= 0;
			case OP_LESS_EQ: return valueLeft == null || valueRight==null ? null: ((Comparable)valueLeft).compareTo(valueRight) <= 0;
			case OP_AND: return valueLeft == null || valueRight==null ? null: (Boolean)valueLeft && (Boolean)valueRight;
			case OP_OR: return valueLeft == null || valueRight==null ? null: (Boolean)valueLeft || (Boolean)valueRight;
			case OP_LIKE: return like((String)valueLeft, (String)valueRight);
			case OP_DISTINT: return valueLeft == null || valueRight==null ? null: !valueLeft.equals(valueRight);
			default: throw  new InvalidOperationException("Operator not supported "+getSQLOperator(operator));
		}
	}

	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
