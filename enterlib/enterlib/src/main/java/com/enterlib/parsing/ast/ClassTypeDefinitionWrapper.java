package com.enterlib.parsing.ast;

import android.text.style.ClickableSpan;

/**
 * Created by Ansel on 3/21/2018.
 */

public class ClassTypeDefinitionWrapper implements ITypeDefinition {
    Class<?>clss;

    public ClassTypeDefinitionWrapper(Class<?>cls){
        this.clss = cls;
    }

    @Override
    public String getName() {
        return clss.getName();
    }

    @Override
    public Class<?> getEntityClass() {
        return clss;
    }

    @Override
    public IVariableDefinition getVariableDefinition(String name) {
        return null;
    }

    @Override
    public IVariableDefinition[] geVariableDefinitions() {
        return new IVariableDefinition[0];
    }
}
