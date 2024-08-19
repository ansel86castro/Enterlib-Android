package com.enterlib.data.sqlite;

import com.enterlib.StringUtils;
import com.enterlib.annotations.ColumnMap;
import com.enterlib.data.PropertyMap;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.ExpressionType;
import com.enterlib.parsing.ast.ITypeDefinition;
import com.enterlib.parsing.ast.IVariableDefinition;

import java.util.Date;

/**
 * Created by Ansel on 3/25/2018.
 */

public class BaseVariableDefinition implements IVariableDefinition {
    String name;
    private Class<?> declaringType;
    String tableName;
    String sqlColumn;
    Class<?>type;
    ExpressionType expressionType;
    boolean IsForeignKey;
    private String fKeyTable;
    private Class<?> fKeyModel;
    private String fKeyModelField;
    private String fkey_to;
    private Expression expression;
    private ITypeDefinition typeDefinition;
    private boolean externalReference;

    public BaseVariableDefinition(String name, ITypeDefinition typeDefinition, String sqlColumn,
                                  Class<?>fKeyModel, String fKeyModelField, Expression expression,
                                  Class<?>type){
        this.name = name;
        this.typeDefinition = typeDefinition;
        this.type = type;
        this.expressionType = getExpressionType();
        this.sqlColumn = sqlColumn;
        this.IsForeignKey = true;
        this.fKeyModel = fKeyModel;
        this.expression = expression;
        this.fKeyModelField = fKeyModelField;

        try {
            java.lang.reflect.Field modelfield = typeDefinition.getEntityClass().getField(fKeyModelField);
            ColumnMap toMap = modelfield.getAnnotation(ColumnMap.class);
            fkey_to = !StringUtils.isNullOrWhitespace(toMap.column()) ? toMap.column() : modelfield.getName();
        } catch (NoSuchFieldException e) {
            throw new InvalidOperationException(e.getMessage() ,e);
        }
    }

    private int getStoreType(){
        if(type == String.class)
            return PropertyMap.TYPE_STRING;
        else if(type == int.class || type == Integer.class)
            return IsForeignKey?  PropertyMap.TYPE_FKEY : PropertyMap.TYPE_INT;
        else if(type == boolean.class || type == Boolean.class)
            return PropertyMap.TYPE_BOOL;
        else if(type == double.class || type == Double.class)
            return PropertyMap.TYPE_REAL;
        else if(type == Date.class)
            return PropertyMap.TYPE_DATETIME;
        else if(type  == byte[].class)
            return PropertyMap.TYPE_BLOB;
        else
            return 0;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getDeclaringType() {
        return declaringType;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getSqlColumn() {
        return sqlColumn;
    }

    @Override
    public ExpressionType getExpressionType() {
        return expressionType;
    }

    @Override
    public boolean isForeignKey() {
        return IsForeignKey;
    }

    @Override
    public String FKey_table() {
        return fKeyTable;
    }

    @Override
    public Class<?> getFKeyModel() {
        return fKeyModel;
    }

    @Override
    public String getFKeyModelField() {
        return fKeyModelField;
    }

    @Override
    public String FKey_To() {
        return fkey_to;
    }

    @Override
    public IVariableDefinition getNavigationFKey() {
        return null;
    }

    @Override
    public String getNavigationTableColumn() {
        return null;
    }

    @Override
    public Object get(Object target) {
        return null;
    }

    @Override
    public void set(Object target, Object value) {

    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    @Override
    public ITypeDefinition getDeclaringTypeDefinition() {
        return typeDefinition;
    }

    @Override
    public boolean isExternalReference() {
        return externalReference;
    }
}
