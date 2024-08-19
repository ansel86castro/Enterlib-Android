package com.enterlib.parsing.ast;

import com.enterlib.data.sqlite.SQLQuery;
import com.enterlib.exceptions.InvalidOperationException;

/**
 * Created by Ansel on 1/30/2018.
 */

public class FunctionCall extends Expression {

    private  String name;
    private  Expression[] parameters;

    public FunctionCall(String name, Expression[]parameters){
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public void checkSemantic(ASTContext context) {
        for (int i = 0; i < parameters.length; i++) {
            parameters[i].checkSemantic(context);
        }

        String lower = name.toLowerCase();

        if (lower.matches("sum|avg|count|max|min|group_concat|total|ifnull")) {
            type =parameters[0].getType();
        }

    }

    @Override
    public void genSQL(ASTContext context, StringBuilder sb, int tabOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object dynamicEval(IEvaluationContext evalContext) {
        Object[]args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++) {
            args[i]=parameters[i].dynamicEval(evalContext);
        }
        return evalContext.invokeFunction(name, args);
    }

    @Override
    public Object visit(ASTNodeVisitor visitor, Object arg) {
        return visitor.visit(this ,arg);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Expression[] getParameters() {
        return parameters;
    }

    public void setParameters(Expression[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append('(');
        for (int i = 0; i < parameters.length; i++) {
            if(i> 0)
                sb.append(", ");
            sb.append(parameters[i].toString());
        }
        sb.append(')');
        return sb.toString();
    }
}
