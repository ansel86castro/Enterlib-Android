package com.enterlib.parsing.ast;

import java.util.Objects;

public interface IVariableDefinition {
	
	String getName();
	
	Class<?> getDeclaringType();

	String getTableName();

	Class<?> getType();

	String getSqlColumn();

	ExpressionType getExpressionType();

	boolean isForeignKey();

	String FKey_table();

	Class<?> getFKeyModel();

	String getFKeyModelField();

	String FKey_To();

	IVariableDefinition getNavigationFKey();

	String getNavigationTableColumn();

	Object get(Object target);
	
	void set(Object target, Object value);

    Expression getExpression();

    ITypeDefinition getDeclaringTypeDefinition();

	boolean isExternalReference();

}
