package com.enterlib.parsing.ast;

public class ExpressionType {
	
	public final static int STD_Bool = 1;
	public final static int STD_Integer= 2;
	public final static int STD_Double= 3;
	public final static int STD_String= 4;
	public final static int STD_Null= 5;
	public final static int STD_Dynamic=6;
	public final static int STD_Object=7;
	
	public final static ExpressionType Bool = new ExpressionType("bool", STD_Bool);
	public final static ExpressionType Integer= new ExpressionType("integer", STD_Integer);
	public final static ExpressionType Double= new ExpressionType("double", STD_Double);
	public final static ExpressionType String= new ExpressionType("string", STD_String);
	public final static ExpressionType Null= new ExpressionType("null", STD_Null);
	public final static ExpressionType Dynamic= new ExpressionType("dynamic", STD_Dynamic);
    public final static ExpressionType Object= new ExpressionType("object", STD_Object);

    String name;
	private int stdType;
	
	/**
	 * @param name
	 */
	public ExpressionType(java.lang.String name, int stdType) {
		this.name = name;
		this.stdType = stdType;
	}

	public String getName() {
		return name;
	}
	
	public static ExpressionType match(ExpressionType t1, ExpressionType t2){
		if(t1.stdType == t2.stdType)
			return t1;
		else if(t1.stdType != STD_Null && t2.stdType==STD_Null){
			return t1;
		}
		else if(t2.stdType != STD_Null && t1.stdType==STD_Null){
			return t2;
		}else if(t1.stdType == STD_Double && t2.stdType == STD_Integer) {
			return t1;
		}else if(t1.stdType == STD_Integer && t2.stdType == STD_Double){
			return t2;
		}
		
		return null;
	}

	@Override
	public String toString() {
		return name;
	}
}
