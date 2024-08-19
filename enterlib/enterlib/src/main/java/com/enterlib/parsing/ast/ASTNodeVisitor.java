package com.enterlib.parsing.ast;

import java.util.ArrayList;

/**
 * Created by Ansel on 3/20/2018.
 */

public class ASTNodeVisitor {
    public Object visit(LiteralExpression expression, Object context){
        return null;
    }

    public Object visit(BinaryExpression expression, Object context){
        expression.getLeft().visit(this, context);
        expression.getRight().visit(this, context);
        return null;
    }
    public Object visit(Expression expression, Object context){
        return expression;
    }

    public Object visit(ExpressionList expression, Object context){
        ArrayList<Expression> list = expression.getExpressions();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).visit(this,  context);
        }
        return expression;
    }

    public Object visit(FunctionCall expression, Object context){
        Expression[] args = expression.getParameters();
        for (int i = 0; i < args.length; i++) {
            args[i].visit(this, context);
        }
        return expression;
    }

    public Object visit(LogicalExpression expression, Object context){
        return visit((BinaryExpression) expression, context);
    }
    public Object visit(MemberExpression expression, Object context){
        expression.getLeft().visit(this, context);
        return null;
    }
    public Object visit(OrderByExpression expression, Object context){
        return null;
    }
    public Object visit(OrderByListExpression expression, Object context){
        ArrayList<Expression> list = expression.getExpressions();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).visit(this,context);
        }
        return null;
    }
    public Object visit(RelationalExpression expression, Object context){
        visit((BinaryExpression) expression, context);
        return null;
    }
    public Object visit(UnaryExpression expression, Object context){
        expression.getExpression().visit(this, context);
        return null;
    }

    public Object visit(VariableExpression expression, Object context){
        return null;
    }
}
