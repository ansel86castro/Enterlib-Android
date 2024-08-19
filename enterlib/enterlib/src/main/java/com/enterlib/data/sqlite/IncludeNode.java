package com.enterlib.data.sqlite;

import com.enterlib.StringUtils;
import com.enterlib.data.PropertyMap;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.ast.MemberExpression;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hp on 11/10/2016.
 */
public class IncludeNode {
    String propertyName;
    PropertyMap propertyMap;
    String nodeInclude;
    HashMap<String, IncludeNode> nodes = new HashMap<>();
    EntityMap<?>propertyMapContext;
    IncludeNode parent;

    public IncludeNode(String[]path, int index,  EntityMap<?> rootContext, IncludeNode parent){
        this.parent = parent;
        propertyName=path[index];
        nodeInclude = StringUtils.aggregate(path, ".", 0, index + 1);
        EntityMap<?>context;
        if(index == 0) {
            context = rootContext;
        }else{
            context = (EntityMap<?>)rootContext.getTypeDefinition(parent.propertyMap.getFKeyModel());
        }

        propertyMap = context.getMapByFieldName(propertyName);
        if(propertyMap == null)
            propertyMap = context.getMapByFieldName(propertyName+"Id");

        if(propertyMap == null)
            propertyMap = context.getMapByFieldName(propertyName+"id");

        if(propertyMap == null)
            propertyMap = context.getMapByFieldName(propertyName+"_id");

        if(propertyMap == null)
            throw new InvalidOperationException("Property "+propertyName+"Id not found in "+context.getName());

        if(!propertyMap.isForeignKey())
            throw new InvalidOperationException("Property "+propertyMap.getName()+" is not a foreign key");

        propertyMapContext =  (EntityMap<?>)context.getTypeDefinition(propertyMap.getFKeyModel());
        if(propertyMapContext == null)
            throw new InvalidOperationException("EntityMap not defined");

        if(index + 1 < path.length)
            nodes.put(path[index +1], new IncludeNode(path, index +1, rootContext, this));
    }

    public String getQueryReference(){
        if(parent!=null){
            return parent.getQueryReference()+"."+propertyMap.getName();
        }
        return propertyMap.getName();
    }

    public boolean add(String[] path, int index, EntityMap<?> rootContext){
        if(!propertyName.equals(path[index])){
            return false;
        }

        if(++index < path.length){
            IncludeNode node = nodes.get(path[index]);
            if(node == null){
                nodes.put(path[index], new IncludeNode(path, index, rootContext, this));
                return true;
            }else{
                return node.add(path, index, rootContext);
            }
        }

        return true;
    }

    public static void generateSQL(SQLQuery<?> query, EntityMap<?> rootContext){
        ArrayList<String> includes = query.getIncludes();
        if(includes != null && includes.size() > 0) {
            HashMap<String,IncludeNode>nodes = new HashMap<>();
            for (int i = 0; i < includes.size(); i++) {
                String include = includes.get(i);
                String[] path = include.split("\\.");

                IncludeNode node = nodes.get(path[0]);
                if(node!=null){
                    node.add(path, 0, rootContext);
                }else{
                    nodes.put(path[0], new IncludeNode(path, 0, rootContext, null));
                }
            }

            String jointRef = query.getTableRef(rootContext.getTableName());
            if(jointRef == null)
                jointRef = query.createTableRef(rootContext.getTableName());

            for (IncludeNode node: nodes.values()) {
                node.generateIncludes(query, jointRef);
            }
        }
    }

    public void generateIncludes(SQLQuery<?> query, String jointRef){
        String includeRef = getQueryReference();
        String tableRef = query.getTableRef(includeRef);
        if(tableRef == null) {
            tableRef = query.createTableRef(includeRef);
            query.addJoint(MemberExpression.getJointType(propertyMap.getType()) + "\"" + propertyMap.FKey_table() + "\" " + tableRef,
                    tableRef + "." + propertyMap.FKey_To(),
                    jointRef + "." + propertyMap.getSqlColumn());
        }

        propertyMapContext.generateColumns(query, tableRef, nodeInclude, includeRef);

        for (IncludeNode node: nodes.values()) {
            node.generateIncludes(query, tableRef);
        }
    }
}
