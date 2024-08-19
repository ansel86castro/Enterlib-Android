package com.enterlib.parsing.ast;

import com.enterlib.exceptions.InvalidOperationException;

public abstract class Expression extends ASTNode {

	protected  ExpressionType type;

	public ExpressionType getType() {
		return type;
	}

	public void setType(ExpressionType type) {
		this.type = type;
	}

	public abstract Object dynamicEval(IEvaluationContext evalContext);

	public static Object IntegerAdd(Object a, Object b){
		Integer ai= (Integer) a;
		if(b instanceof  Integer)
			return Integer.valueOf(ai + (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai+ (Double)b);
		else if(b instanceof String)
			return ai + (String)b;
		throw new InvalidOperationException("operator + not supported");
	}

	public static Object DoubleAdd(Object a, Object b){
		Double ai= (Double) a;
		if(b instanceof  Integer)
			return Double.valueOf(ai + (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai+ (Double)b);
		else if(b instanceof String)
			return ai + (String)b;
		throw new InvalidOperationException("operator + not supported");
	}

	public static Object Add(Object a, Object b){
		if(a == null)
			return b;
		if(b == null)
			return a;

		if(a instanceof Integer)
			return IntegerAdd(a, b);
		else if(a instanceof Double)
			return DoubleAdd(a, b);
		else if(a instanceof String){
			return a.toString()+b.toString();
		}
		throw new InvalidOperationException("Operator + not supported");
	}

	public static Object Substract(Object a, Object b){
		if(a == null)
			return b;
		if(b == null)
			return a;

		if(a instanceof Integer)
			return IntegerSub(a, b);
		else if(a instanceof Double)
			return DoubleSub(a, b);
		throw new InvalidOperationException("Operator + not supported");
	}

	public static Object Mul(Object a, Object b){
		if(a == null)
			return b;
		if(b == null)
			return a;

		if(a instanceof Integer)
			return IntegerMul(a, b);
		else if(a instanceof Double)
			return DoubleMul(a, b);
		throw new InvalidOperationException("Operator + not supported");
	}

	public static Object Div(Object a, Object b){
		if(a == null)
			return b;
		if(b == null)
			return a;

		if(a instanceof Integer)
			return IntegerDiv(a, b);
		else if(a instanceof Double)
			return DoubleDiv(a, b);
		throw new InvalidOperationException("Operator + not supported");
	}


	public static Object IntegerSub(Object a, Object b){
		Integer ai= (Integer) a;
		if(b instanceof  Integer)
			return Integer.valueOf(ai - (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai - (Double)b);
		throw new InvalidOperationException("operator - not supported");
	}

	public static Object DoubleSub(Object a, Object b){
		Double ai= (Double) a;
		if(b instanceof  Integer)
			return Double.valueOf(ai - (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai - (Double)b);
		throw new InvalidOperationException("operator - not supported");
	}

	public static Object IntegerMul(Object a, Object b){
		Integer ai= (Integer) a;
		if(b instanceof  Integer)
			return Integer.valueOf(ai * (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai * (Double)b);
		throw new InvalidOperationException("operator * not supported");
	}

	public static Object DoubleMul(Object a, Object b){
		Double ai= (Double) a;
		if(b instanceof  Integer)
			return Double.valueOf(ai * (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai * (Double)b);
		throw new InvalidOperationException("operator * not supported");
	}

	public static Object IntegerDiv(Object a, Object b){
		Integer ai= (Integer) a;
		if(b instanceof  Integer)
			return Integer.valueOf(ai / (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai / (Double)b);
		throw new InvalidOperationException("operator / not supported");
	}

	public static Object DoubleDiv(Object a, Object b){
		Double ai= (Double) a;
		if(b instanceof  Integer)
			return Double.valueOf(ai / (Integer)b);
		else if(b instanceof Double)
			return Double.valueOf(ai / (Double)b);
		throw new InvalidOperationException("operator / not supported");
	}

	public boolean like(String a, String pattern){
		if(a == null || a.isEmpty())
			return pattern == null || pattern.isEmpty();

		if(pattern == null || pattern.length() == 0)
			return a==null || a.isEmpty();

		if(pattern.charAt(0)=='%' && pattern.charAt(pattern.length()-1)!= '%')
			return a.startsWith(pattern.substring(1));
		else if(pattern.charAt(pattern.length()-1)=='%' && pattern.charAt(0)!= '%'){
			return a.endsWith(pattern.substring(0, pattern.length()-1));
		}else if(pattern.charAt(0)=='%' && pattern.charAt(pattern.length()-1)== '%'){
			return a.contains(pattern);
		}else{
			return a.equalsIgnoreCase(pattern);
		}
	}

	public abstract Object visit(ASTNodeVisitor visitor, Object arg);
}
