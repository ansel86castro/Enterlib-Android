package com.enterlib.parsing.ast;

import com.enterlib.data.sqlite.SQLQuery;

public class OrderByExpression extends VariableExpression {

	public static final String DESC = "DESC";
	
	public static final int ASCENDING = 0;
	
	public static final int DESCENDING = 1;
	
	
	public int orderType;
	
	public OrderByExpression(String name) {
		super(name);
		
	}
	
	
	public int getOrderType() {
		return orderType;
	}



	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}



	public String getOrderTypeString(){
		switch (orderType) {
		case ASCENDING:
			return "";
		case DESCENDING:
			return "DESC";
		default:
			return null;
		}
	}
	
	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset) {
		super.genSQL(context, sb, tabOffset);
		
		if(orderType == DESCENDING){
			sb.append(' ');
			sb.append(getOrderTypeString());
		}
	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
		super.genSQL(context, sb, tabOffset, query);

		if(orderType == DESCENDING){
			sb.append(' ');
			sb.append(getOrderTypeString());
		}
	}

	@Override
	public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {		
		super.genOData(context, sb, tabOffset);
		
		if(orderType == DESCENDING){
			sb.append(' ');
			sb.append(getOrderTypeString());
		}
	}

	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}

}
