package com.enterlib.parsing.ast;

import com.enterlib.data.sqlite.SQLQuery;

public class OrderByListExpression extends ExpressionList {

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset) {
			
		for (int i = 0; i < expressions.size(); i++) {
			if( i > 0)
				sb.append(',');			
			expressions.get(i).genSQL(context, sb, tabOffset);
		}
	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
		for (int i = 0; i < expressions.size(); i++) {
			if( i > 0)
				sb.append(',');
			expressions.get(i).genSQL(context, sb, tabOffset, query);
		}
	}

	@Override
	public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {
		for (int i = 0; i < expressions.size(); i++) {
			if( i > 0)
				sb.append(',');			
			expressions.get(i).genOData(context, sb, tabOffset);
		}
	}

	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
