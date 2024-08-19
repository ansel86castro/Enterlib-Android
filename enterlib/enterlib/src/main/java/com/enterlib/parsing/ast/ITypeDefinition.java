package com.enterlib.parsing.ast;

/**
 * Created by ansel on 10/10/2016.
 */
public interface ITypeDefinition {

    String getName();

    Class<?> getEntityClass();

    IVariableDefinition getVariableDefinition(String name);

    IVariableDefinition[] geVariableDefinitions();
}
