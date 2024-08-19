package com.enterlib.data.sqlite;

import com.enterlib.data.PropertyMap;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.TokenType;
import com.enterlib.parsing.ast.ASTNodeVisitor;
import com.enterlib.parsing.ast.BinaryExpression;
import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.ExpressionType;
import com.enterlib.parsing.ast.FunctionCall;
import com.enterlib.parsing.ast.IVariableDefinition;
import com.enterlib.parsing.ast.LiteralExpression;
import com.enterlib.parsing.ast.MemberExpression;
import com.enterlib.parsing.ast.UnaryExpression;
import com.enterlib.parsing.ast.VariableExpression;

import static com.enterlib.parsing.ast.BinaryExpression.OP_DISTINT;
import static com.enterlib.parsing.ast.BinaryExpression.OP_EQUAL;

/**
 * Created by Ansel on 3/24/2018.
 */

public class WhereGenerator<T> extends ASTNodeVisitor {

    SQLQuery<?>query;
    StringBuilder sb = new StringBuilder();
    EntityMap<T>map;
    private boolean useHaving;

    public WhereGenerator(EntityMap<T>entityMap){
        this.map = entityMap;
    }

    public void generateCode(Expression expression, SQLQuery<?>query){
        this.query  =query;
        sb.delete(0, sb.length());
        useHaving = false;

        expression.visit(this, null);

        if(!useHaving)
            query.addWhere(sb.toString());
        else
            query.addHaving(sb.toString());
    }

    @Override
    public Object visit(LiteralExpression expression, Object args) {
        ExpressionType type = expression.getType();
        if(type == ExpressionType.String){
            SQLQuery.appendEscapedSQLString(sb, expression.getValue());
        }else if (type == ExpressionType.Null){
            sb.append("NULL");
        }else if(type == ExpressionType.Bool){
            sb.append(expression.getTokenType() == TokenType.TRUE?1:0);
        }
        else
            sb.append(expression.getValue());

        return null;
    }

    @Override
    public Object visit(BinaryExpression expression, Object args) {
        Expression left = expression.getLeft();
        Expression right = expression.getRight();

        if(left instanceof BinaryExpression){
            sb.append('(');
            left.visit(this, expression);
            sb.append(')');
        }else{
            left.visit(this, expression);
        }

        sb.append(' ');

        if((right instanceof LiteralExpression && ((LiteralExpression)right).getType() == ExpressionType.Null) || (left instanceof LiteralExpression && ((LiteralExpression)left).getType() == ExpressionType.Null) ){
            switch (expression.getOperator()){
                case OP_EQUAL:
                    sb.append(" IS ");
                    break;
                case OP_DISTINT:
                    sb.append(" IS NOT ");
                    break;
                default:
                    throw new InvalidOperationException("Operation not supported at "+expression.toString());
            }
        }
        else {
            sb.append(BinaryExpression.getSQLOperator(expression.getOperator()));
        }

        sb.append(' ');

        if(right instanceof BinaryExpression){
            sb.append('(');
            right.visit(this, expression);
            sb.append(')');
        }else{
            right.visit(this, expression);
        }

        return  null;
    }

    @Override
    public Object visit(UnaryExpression expression, Object args) {
        String opString = BinaryExpression.getSQLOperator(expression.getOperator());
        if(opString == null)
            throw new InvalidOperationException("Operator not supported "+expression.toString());

        sb.append(opString);
        if(expression.getExpression() instanceof BinaryExpression){
            sb.append("(");
            expression.getExpression().visit(this, expression);
            sb.append(")");
        }else {
            expression.getExpression().visit(this, expression);
        }

        return null;
    }

    @Override
    public Object visit(VariableExpression expression, Object args) {
        IVariableDefinition variableDef = expression.getVariableDefinition();
        String tableRef = null;
        Expression alias = variableDef.getExpression();
        boolean inMemberExpression = args instanceof MemberExpression;

        if(variableDef == null){
           throw new InvalidOperationException(String.format("Member %s not defined in %s",expression.getName(), map.getName()));
        }

        if(!variableDef.isForeignKey() && alias == null){
            if(inMemberExpression) {
                throw new InvalidOperationException(String.format("Member %s.%s is not a foreign key", variableDef.getTableName(), expression.getName()));
            }
            tableRef = query.getTableRef(variableDef.getTableName());
        }else if(variableDef.isForeignKey()){
            if(inMemberExpression){
                tableRef = query.getTableRef(variableDef.getName());
                if (tableRef == null) {
                    tableRef = query.createTableRef(variableDef.getName());
                    String jointType = variableDef.isExternalReference() ?
                            "LEFT OUTER JOIN ":
                            MemberExpression.getJointType(variableDef.getType());

                    query.addJoint(jointType + "\"" + variableDef.FKey_table() + "\" " + tableRef,
                            tableRef + "." + variableDef.FKey_To(),
                            query.getTableRef(variableDef.getTableName()) + "." + variableDef.getSqlColumn());
                }
            }else if(alias == null){
                tableRef = query.getTableRef(variableDef.getTableName());
            }
        }

        if(!inMemberExpression){
            if(tableRef == null) {
                if(alias == null)
                    throw new InvalidOperationException("ExpressionColumn annotation not found in "+expression.getName());
                tableRef = (String) alias.visit(this, args);
            }else {
                sb.append(tableRef + "." + variableDef.getSqlColumn());
                if (useHaving) {
                    query.addAggregate(tableRef, variableDef.getSqlColumn());
                }
            }
        }

        if(tableRef == null){
            throw  new InvalidOperationException("Unable to find table reference for "+expression.getName());
        }

        return  tableRef;
    }

    @Override
    public Object visit(MemberExpression expression, Object context) {
        IVariableDefinition leftVariableDef;
        String tableRef;

        if (expression.getLeft() instanceof VariableExpression) {
            tableRef = (String)expression.getLeft().visit(this, expression);
        } else {
            MemberExpression leftMember = ((MemberExpression) expression.getLeft());
            String path = leftMember.getPath();
            tableRef = query.getTableRef(path);
            if (tableRef == null) {
                String leftRef = (String) leftMember.visit(this, expression);

                tableRef = query.createTableRef(path);
                leftVariableDef = leftMember.getVariableDefinition();

                query.addJoint(MemberExpression.getJointType(leftVariableDef.getType()) + "\"" + leftVariableDef.FKey_table() + "\" " + tableRef,
                        tableRef + "." + leftVariableDef.FKey_To(),
                        leftRef + "." + leftVariableDef.getSqlColumn());
            }
        }

        if(context instanceof MemberExpression == false){
            sb.append(tableRef).append('.').append(expression.getVariableDefinition().getSqlColumn());
            if(useHaving){
                query.addAggregate(tableRef, expression.getVariableDefinition().getSqlColumn());
            }
        }

        return tableRef;
    }

    @Override
    public Object visit(FunctionCall expression, Object args) {
        String name = expression.getName().toLowerCase();
        if(!name.matches("sum|avg|count|max|min|concat|total|ifnull|contains|exclude")){
            throw new InvalidOperationException("function "+name+" not supported");
        }

        if(name.equals("contains") || name.equals("exclude")){
            generateContains(expression,  name);
            return null;
        }

        if(name.equals("sum")){
            name = "total";
        }
        if(name.equals("concat")){
            name = "group_concat";
        }

        useHaving = isAggregate(name);

        sb.append(name).append('(');
        Expression[] params = expression.getParameters();
        for (int i = 0; i < params.length; i++) {
            if(i > 0)
                sb.append(',');
            Expression arg = params[i];
            arg.visit(this, expression);
        }
        sb.append(")");

        return null;
    }


    private static boolean isAggregate(String function){
        return !function.equalsIgnoreCase("ifnull") && !function.equalsIgnoreCase("contains");
    }

    private void generateContains(FunctionCall functionCall, String name){
        Expression[]p = functionCall.getParameters();
        Expression condition = p[0];

        ContainsVisitor visitor = new ContainsVisitor();
        condition.visit(visitor, null);
        if(visitor.variableExpression == null)
            throw new InvalidOperationException("Format not supported "+condition.toString());
        if(visitor.entityMap == null)
            throw new InvalidOperationException("EntityMap not found "+visitor.variableExpression.toString());

        IVariableDefinition variableDefinition = visitor.variableExpression.getVariableDefinition();
        sb.append(query.getTableRef(variableDefinition.getTableName())).append(".").append(variableDefinition.getSqlColumn());

        if(name.equals("exclude"))
            sb.append(" NOT");

        sb.append(" IN (");

        String exp = condition.toString().replace(visitor.variableExpression+".","");
        SQLQuery<?> q = new SQLQuery<>(visitor.entityMap);
        q.where(exp);
        q.compile(SQLQuery.GEN_NONE);

        PropertyMap[]keys = visitor.entityMap.getKeys();
        PropertyMap key = null;
        for (int i = 0; i < keys.length; i++) {
            if(keys[i].isForeignKey() && keys[i].getFKeyModel() == variableDefinition.getDeclaringType()){
                key = keys[i];
                break;
            }
        }
        if(key == null)
            throw new InvalidOperationException("Valid foreign key not found in "+visitor.entityMap.getEntityClass().getName());

        q.addColumn(q.getTableRef(visitor.entityMap.getTableName()),  key.getSqlColumn(),  key.getName());
        sb.append(q.toSql());
        sb.append(")");
    }

    class ContainsVisitor extends ASTNodeVisitor{
        EntityMap<?>entityMap;
        VariableExpression variableExpression;

        @Override
        public Object visit(VariableExpression expression, Object context) {
            entityMap = (EntityMap<?>) map.getTypeDefinition(expression.getVariableDefinition().getFKeyModel());
            variableExpression = expression;
            return null;
        }
    }
}
