package com.enterlib.data.sqlite;

import android.database.DatabaseUtils;

import com.enterlib.data.PropertyMap;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.TokenType;
import com.enterlib.parsing.ast.ASTNodeVisitor;
import com.enterlib.parsing.ast.BinaryExpression;
import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.ExpressionType;
import com.enterlib.parsing.ast.FunctionCall;
import com.enterlib.parsing.ast.IVariableDefinition;
import com.enterlib.parsing.ast.LiteralExpression;
import com.enterlib.parsing.ast.LogicalExpression;
import com.enterlib.parsing.ast.MemberExpression;
import com.enterlib.parsing.ast.RelationalExpression;
import com.enterlib.parsing.ast.UnaryExpression;
import com.enterlib.parsing.ast.VariableExpression;

import static com.enterlib.parsing.ast.BinaryExpression.OP_DISTINT;
import static com.enterlib.parsing.ast.BinaryExpression.OP_EQUAL;

/**
 * Created by Ansel on 3/20/2018.
 */

public class ComputedColumnGenerator<T> extends ASTNodeVisitor {

    SQLQuery<?>query;
    StringBuilder sb = new StringBuilder();
    private String ref;
    private String path;

    public PropertyMap getProp() {
        return prop;
    }

    public void setProp(PropertyMap prop) {
        this.prop = prop;
    }

    PropertyMap prop;

    public ComputedColumnGenerator(EntityMap<T>entityMap){
        this.map = entityMap;
    }

    public EntityMap<T> getMap() {
        return map;
    }

    public void setMap(EntityMap<T> map) {
        this.map = map;
    }

    EntityMap<T>map;

    public SQLQuery<?> getQuery() {
        return query;
    }

    public void setQuery(SQLQuery<?> query) {
        this.query = query;
    }

    public void generateCode(PropertyMap prop, SQLQuery<?> query, String ref, String path){
        this.query  =query;
        this.prop = prop;
        this.ref = ref;
        this.path = path;
        sb.delete(0, sb.length());
        prop.getExpression().visit(this, null);
    }

    private String getPropertyName(){
        String columnName = path != null ? "." + path + "." + prop.getName() : prop.getName();
        return columnName;
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

        if(args == null && !prop.isNonMapped()){
            query.addColumn(sb.toString()+" as \"" +getPropertyName()+"\"");
        }


        return  null;
    }

    @Override
    public Object visit(RelationalExpression expression, Object args) {
        return visit((BinaryExpression)expression, args);
    }

    @Override
    public Object visit(LogicalExpression expression, Object args) {
        return visit((BinaryExpression)expression, args);
    }

    @Override
    public Object visit(UnaryExpression expression, Object args) {
        sb.append(BinaryExpression.getSQLOperator(expression.getOperator()));
        if(expression.getExpression() instanceof BinaryExpression){
            sb.append("(");
            expression.getExpression().visit(this, expression);
             sb.append(")");
        }else {
            expression.getExpression().visit(this, expression);
        }

        if(args == null && !prop.isNonMapped()){
            query.addColumn(sb.toString()+" as \"" + getPropertyName()+"\"");
        }

        return null;
    }

    @Override
    public Object visit(VariableExpression expression, Object args) {
        IVariableDefinition variableDef = expression.getVariableDefinition();
        String tableRef;

        if(variableDef == null){
            tableRef = query.getTableRef(map.getTableName());
            sb.append(tableRef+"."+expression.getName());
            if(args instanceof FunctionCall){
                FunctionCall call = (FunctionCall) args;
                if(isAggregate(call.getName())){
                    query.addAggregate(tableRef, expression.getName());
                }
            }
            return tableRef;
        }

        String reference;

        if(!variableDef.isForeignKey()){
            reference = ref!=null?ref:variableDef.getTableName();
            tableRef = query.getTableRef(reference);
            sb.append(tableRef+"."+variableDef.getSqlColumn());
            if(args instanceof FunctionCall){
                FunctionCall call = (FunctionCall) args;
                if(isAggregate(call.getName())){
                    query.addAggregate(tableRef, expression.getVariableDefinition().getSqlColumn());
                }
            }
        }
        else {
            reference = ref!=null ? ref +"."+variableDef.getName():variableDef.getName();
            tableRef = query.getTableRef(reference);
            if (tableRef == null) {
                tableRef = query.createTableRef(reference);

                String jointType = variableDef.isExternalReference() ?
                        "LEFT OUTER JOIN ":
                        MemberExpression.getJointType(variableDef.getType());

                String jointRef =query.getTableRef(ref!=null?ref:variableDef.getTableName());
                query.addJoint(jointType + "\"" + variableDef.FKey_table() + "\" " + tableRef,
                        tableRef + "." + variableDef.FKey_To(),
                        jointRef + "." + variableDef.getSqlColumn());
            }
        }

        if(args == null && !prop.isNonMapped()){
            query.addColumn(tableRef,expression.getVariableDefinition().getSqlColumn(), getPropertyName());
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

        if(context == null && !prop.isNonMapped()){
            query.addColumn(tableRef,expression.getVariableDefinition().getSqlColumn(), getPropertyName());
        }
        else if(context instanceof MemberExpression == false){
            sb.append(tableRef).append('.').append(expression.getVariableDefinition().getSqlColumn());
        }

        if(context instanceof FunctionCall){
            FunctionCall call = (FunctionCall) context;
            if(isAggregate(call.getName())){
                query.addAggregate(tableRef, expression.getVariableDefinition().getSqlColumn());
            }
        }
        return tableRef;
    }

    @Override
    public Object visit(FunctionCall expression, Object args) {
        String name = expression.getName().toLowerCase();
        if(!name.matches("sum|avg|count|max|min|group_concat|total|ifnull")){
            throw new InvalidOperationException("function "+name+" not supported");
        }
        if(name.equals("sum")){
            name = "total";
        }

        sb.append(name).append('(');
        Expression[] params = expression.getParameters();
        for (int i = 0; i < params.length; i++) {
            if(i > 0)
                sb.append(',');
            Expression arg = params[i];
            arg.visit(this, expression);
        }
        sb.append(")");

        if(args == null && !prop.isNonMapped()) {
            query.addColumn(sb.toString()+" as \""+getPropertyName()+"\"");
        }

        return null;
    }

    private boolean isAggregate(String function){
        return  !function.equalsIgnoreCase("ifnull");
    }
}
