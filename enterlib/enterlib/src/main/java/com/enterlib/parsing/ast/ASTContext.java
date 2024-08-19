package com.enterlib.parsing.ast;

public interface ASTContext {

	IVariableDefinition getVariableDefinition(String name);

	ITypeDefinition getTypeDefinition(String name);

}
