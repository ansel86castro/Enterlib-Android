package com.enterlib.parsing.ast;

import android.database.DatabaseUtils;

import com.enterlib.StringUtils;
import com.enterlib.data.sqlite.SQLQuery;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.TokenType;

public class LiteralExpression extends Expression {

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	String value;

	public TokenType getTokenType() {
		return tokenType;
	}

	public void setTokenType(TokenType tokenType) {
		this.tokenType = tokenType;
	}

	private TokenType tokenType;
	
	
	public LiteralExpression(String value ,ExpressionType type) {
		super();
		this.value = value;
		this.type = type;		
	}
	
	public LiteralExpression(String value ,ExpressionType type, TokenType tokenType){
		this(value, type);
		this.tokenType = tokenType;
	}

	@Override
	public void checkSemantic(ASTContext context) {
		if(type == null)
			throw new RecognitionException(String.format("missing type for %s", value),Col, Row);

	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset) {
		StringUtils.append(sb, '\t', tabOffset);		
		if(type == ExpressionType.String){
			SQLQuery.appendEscapedSQLString(sb, value);
		}else if (type == ExpressionType.Null){
			sb.append("NULL");
		}else if(type == ExpressionType.Bool){			
			sb.append(tokenType == TokenType.TRUE?1:0);
		}
		else
			sb.append(value);
	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
		genSQL(context, sb, tabOffset);
	}

	@Override
	public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {
		if(type == ExpressionType.String){
			SQLQuery.appendEscapedSQLString(sb, value);
		}else if (type == ExpressionType.Null)
			sb.append("null");
		else
			sb.append(value);
		
	}

	@Override
	public String toString() {
		if(type == ExpressionType.String){
			return "'"+value+"'";
		}
		return value;
	}

	@Override
	public Object dynamicEval(IEvaluationContext evalContext) {
		if(type == ExpressionType.Double)
			return Double.valueOf(value);
		else if(type == ExpressionType.Integer)
			return Integer.valueOf(value);
		else if(type == ExpressionType.Bool)
			return Boolean.valueOf(value);
		else if(type == ExpressionType.String)
			return value;
		else if(type == ExpressionType.Null)
			return null;

		throw new InvalidOperationException("Literal type not found for "+value);
	}

	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
