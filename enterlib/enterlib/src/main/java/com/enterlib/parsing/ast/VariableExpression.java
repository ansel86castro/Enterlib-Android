package com.enterlib.parsing.ast;

import com.enterlib.StringUtils;
import com.enterlib.data.sqlite.SQLQuery;

public class VariableExpression extends Expression {
	String name;
	IVariableDefinition variableDef;	
	
	public VariableExpression(String name) {
		super();
		this.name = name;
	}

	public IVariableDefinition getVariableDefinition(){
		return variableDef;
	}

	public String getName() {
		return name;
	}

	public void setName(String varName) {
		this.name = varName;
	}

	@Override
	public void checkSemantic(ASTContext context) {
		variableDef = context.getVariableDefinition(name);	
		if(variableDef == null)
			throw new RecognitionException(String.format("Property %s not defined in %s", name, context.toString()), Col, Row);
		
		type = variableDef.getExpressionType();				
		if(type == null)
			throw new RecognitionException(String.format("Missing Property Type %s name", name), Col, Row);
		
	}

	public void checkSemanticForMemberExp(ASTContext context) {
		variableDef = context.getVariableDefinition(name);
		if(variableDef == null) {
            variableDef = context.getVariableDefinition(name+"Id");
            if(variableDef == null)
			    throw new RecognitionException(String.format("Property %sId not defined ", name), Col, Row);
		}

        if(variableDef.getNavigationFKey()!=null){
            throw new RecognitionException(String.format("Invalid navigation property %s ", name), Col, Row);
        }

		type = variableDef.getExpressionType();
		if(type == null)
			throw new RecognitionException(String.format("Missing Property Type %s name", name), Col, Row);
	}

	@Override
	public void genSQL(ASTContext context,StringBuilder sb, int tabOffset) {
		throw new UnsupportedOperationException();
	}

	@Override
	 public void genSQL(ASTContext context, StringBuilder sb, int tabOffset, SQLQuery query) {
		StringUtils.append(sb, '\t', tabOffset);

		String tableRef = query.getTableRef(variableDef.getTableName());
		if(tableRef == null){
			tableRef = query.createTableRef(variableDef.getTableName());
		}

		if(variableDef.getNavigationFKey()!=null){
			IVariableDefinition fkey = variableDef.getNavigationFKey();
			String fKeyTableRef = query.getTableRef(fkey.getName());
			if(fKeyTableRef == null){
				fKeyTableRef = query.createTableRef(fkey.getName());
				String jointType = MemberExpression.getJointType(fkey.getType());
				query.addJoint(jointType + "\"" + fkey.FKey_table() + "\" " + fKeyTableRef,
						fKeyTableRef + "." + fkey.FKey_To(),
						tableRef + "." + fkey.getSqlColumn());
			}

			sb.append(fKeyTableRef);
			sb.append(".");
			sb.append(variableDef.getNavigationTableColumn());
		}else {
			sb.append(tableRef);
			sb.append(".");
			sb.append(variableDef.getSqlColumn());
		}
	}

	@Override
	public void genOData(ASTContext context,StringBuilder sb, int tabOffset) {
		if(variableDef.getNavigationFKey()!=null){
			IVariableDefinition fkey = variableDef.getNavigationFKey();
			sb.append(MemberExpression.getNavigationProperty(fkey.getName()));
			sb.append('.');
			sb.append(variableDef.getNavigationTableColumn());
			return;
		}

		sb.append(variableDef.getName());
	}

    public void genODataForMember(ASTContext context,StringBuilder sb, int tabOffset) {
        sb.append(name);
    }

	@Override
	public Object dynamicEval(IEvaluationContext evalContext) {
		return evalContext.getValue(name);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object visit(ASTNodeVisitor visitor, Object arg) {
		return visitor.visit(this, arg);
	}
}
