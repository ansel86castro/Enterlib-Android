package com.enterlib.parsing.ast;


import com.enterlib.data.sqlite.SQLQuery;

public abstract class ASTNode {
	public int Col;
	public int Row;

	private Object userData;

	public Object getUserData(){
		return userData;
	}

	public void setUserData(Object userData){
		this.userData = userData;
	}

	public abstract void checkSemantic(ASTContext context);
	
	public abstract void genSQL(ASTContext context, StringBuilder sb, int tabOffset);

	public abstract void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query);

	public abstract void genOData(ASTContext context, StringBuilder sb, int tabOffset);
}
