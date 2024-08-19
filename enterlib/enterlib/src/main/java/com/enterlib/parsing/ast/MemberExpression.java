package com.enterlib.parsing.ast;

import com.enterlib.StringUtils;
import com.enterlib.data.sqlite.SQLQuery;
import com.enterlib.exceptions.InvalidOperationException;

/**
 * Created by ansel on 10/10/2016.
 */
public class MemberExpression extends Expression {
    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    Expression left;

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    String member;

    public IVariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    public void setVariableDefinition(IVariableDefinition variableDefinition) {
        this.variableDefinition = variableDefinition;
    }

    IVariableDefinition variableDefinition;


    public MemberExpression(Expression left, String member) {
        this.left = left;
        this.member = member;
    }

    @Override
    public void checkSemantic(ASTContext context) {
        IVariableDefinition leftVar = null;

        if(left instanceof  VariableExpression){
            ((VariableExpression)left).checkSemanticForMemberExp(context);
             leftVar = ((VariableExpression)left).getVariableDefinition();
        }else if(left instanceof MemberExpression){
            left.checkSemantic(context);
            leftVar = ((MemberExpression)left).variableDefinition;
        }

        if(leftVar == null)
            throw new RecognitionException(String.format("Variable Definition"), Col, Row);


        if(!leftVar.isForeignKey())
            throw new RecognitionException(String.format("The reference '%s' is not a foreign key", leftVar.getName()), Col, Row);

        ITypeDefinition tableDefinition = context.getTypeDefinition(leftVar.getFKeyModel().getName());
        if(tableDefinition == null){
            throw new RecognitionException(String.format("The type '%s' is not defined", leftVar.FKey_table()), Col, Row);
        }

        variableDefinition = tableDefinition.getVariableDefinition(member);
        if(variableDefinition == null){
            variableDefinition = tableDefinition.getVariableDefinition(member+"Id");
            if(variableDefinition == null)
                throw new RecognitionException(String.format("Member '%s' not found in %s",member, tableDefinition.getName()),Col, Row);
        }

        type = variableDefinition.getExpressionType();
        if(type == null)
            throw new RecognitionException(String.format("Missing Property Type %s name", member), Col, Row);

    }

    @Override
    public void genSQL(ASTContext context, StringBuilder sb, int tabOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
        String tableRef = appendJoint(query);

        StringUtils.append(sb, '\t', tabOffset);
        sb.append(tableRef).append('.').append(variableDefinition.getSqlColumn());

    }

    @Override
    public void genOData(ASTContext context, StringBuilder sb, int tabOffset) {
        StringUtils.append(sb, '\t', tabOffset);

        if(left instanceof  VariableExpression){
            VariableExpression var =((VariableExpression) left);
            sb.append(getNavigationProperty(var.getName())).append('.').append(variableDefinition.getName());
        } else if(left instanceof MemberExpression) {
            ((MemberExpression) left).append(sb);
            sb.append('.').append(variableDefinition.getName());
        }
    }

    private void append(StringBuilder sb){
        if(left instanceof  VariableExpression){
            VariableExpression var =((VariableExpression) left);
            sb.append(getNavigationProperty(var.getName()))
                    .append('.')
                    .append(getNavigationProperty(variableDefinition.getName()));
        } else if(left instanceof  MemberExpression) {
            MemberExpression memberExp =((MemberExpression) left);
            memberExp.append(sb);
            sb.append('.').append(getNavigationProperty(variableDefinition.getName()));
        }
    }

    public static String getNavigationProperty(String foreignKey){
        String prop = foreignKey;
        if(foreignKey.toLowerCase().endsWith("id")){
            prop = foreignKey.substring(0, foreignKey.length() - 2);
        }else if(foreignKey.toLowerCase().endsWith("_id")){
            prop = foreignKey.substring(0, foreignKey.length() - 3);
        }
        return prop;
    }

    public String getPath() {
        if (left instanceof VariableExpression) {
            return ((VariableExpression) left).getVariableDefinition().getName()+ "." + variableDefinition.getName();
        } else if (left instanceof MemberExpression) {
            return ((MemberExpression) left).getPath() + "." + variableDefinition.getName();
        }
        return null;
    }

    public String appendJoint(SQLQuery query){
        IVariableDefinition leftVariableDef;
        String tableRef;

        if(left instanceof  VariableExpression){
            leftVariableDef = ((VariableExpression) left).getVariableDefinition();
            tableRef = query.getTableRef(leftVariableDef.getName());
            if(tableRef == null){
                tableRef  = query.createTableRef(leftVariableDef.getName());
                query.addJoint(getJointType(leftVariableDef.getType()) + "\""+leftVariableDef.FKey_table()+"\" "+tableRef,
                        tableRef+"."+leftVariableDef.FKey_To(),
                        query.getTableRef(leftVariableDef.getTableName())+"."+leftVariableDef.getSqlColumn());
            }
        }else {
            MemberExpression leftMember = ((MemberExpression) left);
            String path = leftMember.getPath();
            tableRef = query.getTableRef(path);
            if(tableRef == null) {
                String leftRef = leftMember.appendJoint(query);

                tableRef = query.createTableRef(path);
                leftVariableDef =leftMember.variableDefinition;

                query.addJoint(getJointType(leftVariableDef.getType()) + "\"" + leftVariableDef.FKey_table() + "\" " + tableRef,
                        tableRef + "." + leftVariableDef.FKey_To(),
                        leftRef + "." + leftVariableDef.getSqlColumn());
            }
        }

        return tableRef;

    }

    public static String getJointType(Class<?>fieldType){
        String jointType;
        if(fieldType ==Integer.class){
            jointType= "LEFT OUTER JOIN ";
        }else{
            jointType ="INNER JOIN ";
        }
        return jointType;
    }

    @Override
    public Object dynamicEval(IEvaluationContext evalContext) {
        if(member == null)
            throw  new InvalidOperationException("member missing");

        Object leftValue = left.dynamicEval(evalContext);
        if(leftValue == null)
            return null;
        return evalContext.getMemberValue(leftValue, member);
    }

    @Override
    public Object visit(ASTNodeVisitor visitor, Object arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public String toString() {
        return left.toString()+"."+member;
    }
}

