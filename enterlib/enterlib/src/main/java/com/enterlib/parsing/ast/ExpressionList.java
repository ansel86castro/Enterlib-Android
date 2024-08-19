package com.enterlib.parsing.ast;

import com.enterlib.data.sqlite.SQLQuery;

import java.util.ArrayList;
import java.util.Iterator;

public class ExpressionList extends Expression {

	protected ArrayList<Expression>expressions = new ArrayList<Expression>();

	public ArrayList<Expression> getExpressions() {
		return expressions;
	}

	@Override
	public void checkSemantic(ASTContext context) {
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).checkSemantic(context);
		}

	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset) {
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).genSQL(context, sb, tabOffset);
		}
	}

	@Override
	public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).genSQL(context, sb, tabOffset, query);
		}
	}

	@Override
	public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {
		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).genOData(context, sb, tabOffset);
		}

	}

	public boolean add(Expression object) {
		return expressions.add(object);
	}

	public void clear() {
		expressions.clear();
	}

	public Expression get(int index) {
		return expressions.get(index);
	}

	public int size() {
		return expressions.size();
	}

	public Expression remove(int index) {
		return expressions.remove(index);
	}

	public boolean remove(Object object) {
		return expressions.remove(object);
	}

	public Expression set(int index, Expression object) {
		return expressions.set(index, object);
	}

	public Iterator<Expression> iterator() {
		return expressions.iterator();
	}

	@Override
	public Object dynamicEval(IEvaluationContext evalContext) {
		ArrayList<Object>list = new ArrayList<>(expressions.size());
		for (int i = 0; i < list.size(); i++) {
			Expression e = expressions.get(i);
			list.add(e.dynamicEval(evalContext));
		}
		return list;
	}

	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
