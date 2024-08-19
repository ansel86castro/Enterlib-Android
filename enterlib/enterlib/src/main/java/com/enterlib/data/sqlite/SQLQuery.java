package com.enterlib.data.sqlite;

import com.enterlib.StringUtils;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.IQuerable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by ansel on 09/10/2016.
 */
public class SQLQuery<T> implements IQuerable<T> {

    public static final int GEN_NONE = 0;
    public static final int GEN_COLUMNS = 1<<0;
    public static final int GEN_ORDERBY = 1<<1;
    public static final int GEN_INCLUDE = 1<<2;

    public static final int GEN_ALL = GEN_COLUMNS|GEN_ORDERBY|GEN_INCLUDE;

    private StringBuilder select;
    private String from;
    private StringBuilder joints;
    private StringBuilder where;
    private StringBuilder orderBy;
    private StringBuilder groupBy;
    private StringBuilder having;

    private int columnCount;
    private HashMap<String, String> jointMap;
    private HashSet<String>aggregates;

    HashSet<String> columns = new HashSet<>();
    private int jointCount;

    EntityMap<T> entityMap;

    OnQueryListener listener;
    boolean compiled;

    private int skip = -1;
    private int take = -1;
    private ArrayList<String> includes;

    ArrayList<String> whereExpression = new ArrayList<>();
    StringBuilder orderByExpression = new StringBuilder();

    public SQLQuery(EntityMap<T> entityMap) {
        this.entityMap = entityMap;
    }

    public SQLQuery(String where, String orderBy, int skip, int take, String[] includes, EntityMap<T> entityMap){
        this(entityMap);

        where(where);
        orderBy(orderBy);
        skip(skip);
        take(take);
        include(includes);
    }

    public void setListener(OnQueryListener listener) {
        this.listener = listener;
    }


    public int getJointCount(){
        return jointCount;
    }

    public SQLQuery addColumn(String table, String columnDefinition, String name){
        if(select == null)
            select = new StringBuilder();

        if(columnCount > 0)
            select.append(',');

        select.append(table);
        select.append('.');
        select.append(columnDefinition);
        select.append(" as \"");
        select.append(name);
        select.append("\"");

        select.append('\n');
        columnCount++;
        columns.add(table+"."+columnDefinition);

        return this;
    }

    public SQLQuery addColumn(String columnDefinition){
        if(select == null)
            select = new StringBuilder();

        if(columnCount > 0)
            select.append(',');

        select.append(columnDefinition);
        select.append('\n');
        columnCount++;
        return this;
    }


    public void setTableRef(String navitationProp, String table){
        if(jointMap == null){
            jointMap = new HashMap<String,String>();
        }

        jointMap.put(navitationProp, table);
    }

    public String createTableRef(String path){
        if(jointMap == null){
            jointMap = new HashMap<String,String>();
        }

        int size = jointMap.size();
        String table = "t"+String.valueOf(size);
        setTableRef(path, table);

        return  table;
    }

    public String getTableRef(String navigationProp){
        if(jointMap == null)
            return null;
        return jointMap.get(navigationProp);
    }

    public SQLQuery setFrom(String table, String ref){
        return setFrom(table + " " + ref);
    }

    public SQLQuery setFrom(String table){
        from = table;
        return this;
    }

    public String getFrom(){
        return from;
    }

    public SQLQuery pushJoint(String joint){
        if(joints == null) {
            joints = new StringBuilder();
            joints.append(joint);

        }else{
            joints.insert(0, joint);
        }
        jointCount++;
        return this;
    }

    public SQLQuery pushJoint(String joint, String tableColumn, String jointColumn ){
        if(joints == null){
            joints = new StringBuilder();
            return addJoint(joint, tableColumn, jointColumn);
        }

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(joint);
        sb.append(" on ");
        sb.append(tableColumn);
        sb.append(" = ");
        sb.append(jointColumn);
        sb.append(' ');

        joints.insert(0, sb.toString());
        jointCount++;
        return this;
    }


    public SQLQuery addJoint(String joint, String tableColumn, String jointColumn ){
        if(joints == null){
            joints = new StringBuilder();
        }

        joints.append('\n');
        joints.append(joint);
        joints.append(" on ");
        joints.append(tableColumn);
        joints.append(" = ");
        joints.append(jointColumn);
        joints.append(' ');

        jointCount++;
        return this;

    }

    public SQLQuery addWhere(String condition){
        return addWhere(condition, "AND");
    }

    public SQLQuery addWhere(String condition, String operator){
        if(StringUtils.isNullOrWhitespace(condition))
            return this;

        if(where == null){
            where = new StringBuilder();
        }

        if(where.length() > 0){
            where.append(' ');
            where.append(operator);
            where.append(' ');
        }
        where.append(condition);
        return this;
    }

    public SQLQuery addHaving(String condition){
        return addHaving(condition, "AND");
    }

    public SQLQuery addHaving(String condition, String operator){
        if(StringUtils.isNullOrWhitespace(condition))
            return this;

        if(having == null){
            having = new StringBuilder();
        }

        if(having.length() > 0){
            having.append(' ');
            having.append(operator);
            having.append(' ');
        }
        having.append(condition);
        return this;
    }

    public SQLQuery addOrderBy(String value){
        if(StringUtils.isNullOrWhitespace(value))
            return this;

        if(orderBy == null){
            orderBy = new StringBuilder();
        }

        if(orderBy.length() > 0){
            orderBy.append(',');
        }

        orderBy.append(value);
        return this;
    }

    public ArrayList<String> getIncludes(){
        return includes;
    }

    public void setIncludes(ArrayList<String> includes) {
        this.includes = includes;
    }

    public void setIncludes(String[] includes) {
        if(includes == null)
            return;

        if(this.includes == null){
            this.includes = new ArrayList<String>(includes.length);
        }

        Collections.addAll(this.includes, includes);
    }

    public int getTake() {
        return take;
    }

    public void setTake(int take) {
        this.take = take;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public String getWhere(){
        if(where!=null){
            return where.toString();
        }
        return null;
    }

    public String getFromClause(){
        String result = from;
        if(joints!=null){
            if(result == null)
                result = joints.toString();
            else
                result+="\n"+joints.toString();
        }
        return result;
    }

    public String getTable(){
        return from;
    }

    public String getOrderBy(){
        if(orderBy!=null){
            return orderBy.toString();
        }
        return null;
    }

    public String getSelect(){
        String sql = "SELECT ";
        if(select!=null){
            sql+=select.toString();
        }

        if(from!=null) {
            sql +=" FROM "+ from+"\n";
            if(joints!=null)
                sql+=joints.toString();
        }
        return sql;
    }

    public SQLQuery addInclude(String path){
        if(path == null || path.length() == 0)
            return  this;

        if(includes == null){
            includes = new ArrayList<String>();
        }
        includes.add(path);
        return this;
    }

    private StringBuilder getGroupBy(){
        StringBuilder  sb = new StringBuilder();
        int index = 0;
        for(String c : columns){
            if(!aggregates.contains(c)){
                if(index > 0)
                    sb.append(',');
                sb.append(c);
                index++;
            }
        }
        return  sb;
    }

    public String getCountBody() {
        StringBuilder sql =new StringBuilder();
        if(from!=null) {
            sql.append("FROM ").append(from);
            if(joints!=null) {
                sql.append(joints);
            }
        }
        if(where!=null){
            sql.append("\nWHERE ").append(where);
        }
        if(aggregates!=null){
            sql.append("\nGROUP BY ").append(getGroupBy());
        }
        if(having!=null){
            sql.append("\nHAVING ").append(having);
        }
        if(take > 0){
            sql.append("\nLIMIT ").append(take);
        }

        if(skip > 0){
            if(take == 0)
                sql.append("\nLIMIT ").append(Integer.MAX_VALUE);
            sql.append("\nOFFSET ").append(skip);
        }

        compiled = false;
        return sql.toString();
    }

    public SQLQuery<T> clone(){
        SQLQuery<T>c = new SQLQuery<T>(entityMap);
        c.skip = skip;
        c.take = take;
        if(includes!=null){
            c.includes = new ArrayList<>(includes);
        }

        if(whereExpression!=null){
            c.whereExpression = new ArrayList<>(whereExpression);
        }

        if(orderByExpression!=null){
            c.orderByExpression = new StringBuilder(orderByExpression);
        }
        return  c;
    }

    @Override
    public IQuerable<T> where(String expression) {
        if(expression == null || expression.length() == 0)
            return this;

        whereExpression.add(expression);
        compiled = false;
        return this;
    }

    @Override
    public IQuerable<T> where(String expression, Object... params){
        return where(String.format(expression, params));
    }

    @Override
    public IQuerable<T> orderBy(String expression) {
        if(expression == null || expression.length() == 0)
            return this;

        int length = orderByExpression.length();
        if(length > 0)
            orderByExpression.append(",");
        orderByExpression.append(expression);

        compiled = false;

        return this;
    }

    @Override
    public IQuerable<T> include(String expression) {
        if(expression == null || expression.length() ==0)
            return this;

        addInclude(expression);
        compiled = false;

        return this;
    }
    @Override
    public IQuerable<T> include(String[] expressions) {
        if(expressions == null || expressions.length ==0)
            return this;

        for (int i = 0; i < expressions.length; i++) {
            addInclude(expressions[i]);
        }
        compiled = false;
        return this;
    }


    @Override
    public IQuerable<T> skip(int value) {
        skip = value;
        return  this;
    }

    @Override
    public IQuerable<T> take(int value) {
        take = value;
        return  this;
    }

    @Override
    public ArrayList<T> toList() {
        return entityMap.queryToList(toSql());
    }

    @Override
    public IEntityCursor<T> toCursor() {
        return entityMap.queryToCursor(this);
    }

    @Override
    public T first() {
        return entityMap.queryToFirst(toSql());
    }

    @Override
    public long count() {
        long count = entityMap.queryCount(compile(0));
        compiled = false;
        return count;
    }

    public SQLQuery<T> compile(int genOptions){
        reset();
        entityMap.generateSQL(this, genOptions);
        SQLQuery<T>result = this;
        if(listener!=null){
           result = listener.onQuery(this);
        }
       compiled = true;
        return result;
    }

    private void reset() {
        if(select!=null)
            select.delete(0, select.length());
        from = null;

        if(joints!=null)
            joints.delete(0, joints.length());

        if(where!=null)
            where.delete(0, where.length());

        if(orderBy!=null)
            orderBy.delete(0, orderBy.length());

        if(groupBy!=null)
            groupBy.delete(0, groupBy.length());

        if(having!=null)
            having.delete(0, having.length());

        if(jointMap!=null)
            jointMap.clear();
        if(aggregates!=null)
            aggregates.clear();
        columnCount = 0;
        if(columns!=null)
            columns.clear();
        jointCount = 0;
        compiled =false;
    }


    public void addAggregate(String tableRef, String sqlColumn) {
        if(aggregates ==null){
            aggregates = new HashSet<>();
            groupBy = new StringBuilder();
        }

        aggregates.add(tableRef+"."+sqlColumn);
    }

    @Override
    public String toString() {
        return clone().toSql();
    }

    public String toSql() {
        if(!compiled){
            compile(GEN_ALL);
        }

        StringBuilder sql =new StringBuilder();
        if(select!=null){
            sql.append("SELECT ");
            sql.append(select);
        }

        if(from!=null) {
            sql.append("FROM ").append(from);
            if(joints!=null) {
                sql.append(joints);
            }
        }

        if(where!=null){
            sql.append("\nWHERE ").append(where);
        }
        if(aggregates!=null){
            sql.append("\nGROUP BY ").append(getGroupBy());
        }
        if(having!=null){
            sql.append("\nHAVING ").append(having);
        }

        if(orderBy!=null){
            sql.append("\nORDER BY ").append(orderBy);
        }

        if(take > 0){
            sql.append("\nLIMIT ").append(take);
        }

        if(skip > 0){
            if(take == 0)
                sql.append("\nLIMIT ").append(Integer.MAX_VALUE);
            sql.append("\nOFFSET ").append(skip);
        }


        return sql.toString();
    }

    public static void appendEscapedSQLString(StringBuilder sb, String sqlString) {
        sb.append('\'');
        if (sqlString.indexOf('\'') != -1) {
            int length = sqlString.length();
            for (int i = 0; i < length; i++) {
                char c = sqlString.charAt(i);
                if (c == '\'') {
                    sb.append('\'');
                }
                sb.append(c);
            }
        } else
            sb.append(sqlString);
        sb.append('\'');
    }

    /**
     * SQL-escape a string.
     */
    public static String sqlEscapeString(String value) {
        StringBuilder escaper = new StringBuilder();

        appendEscapedSQLString(escaper, value);

        return escaper.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new SqlQueryIterator<>(toCursor());
    }
}
