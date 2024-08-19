package com.enterlib.data.sqlite;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.Nullable;
import android.util.Log;

import com.enterlib.StringUtils;
import com.enterlib.annotations.ColumnMap;
import com.enterlib.annotations.ExpressionColumn;
import com.enterlib.annotations.ExternalColumn;
import com.enterlib.annotations.TableMap;
import com.enterlib.converters.DateConverter;
import com.enterlib.converters.IStringConverter;
import com.enterlib.data.IEntityContext;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.IQuerable;
import com.enterlib.data.IRepository;
import com.enterlib.data.ModifiedValues;
import com.enterlib.data.PropertyMap;
import com.enterlib.data.IFactory;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.ExpressionParser;
import com.enterlib.parsing.OrderByParser;
import com.enterlib.parsing.ast.ASTContext;
import com.enterlib.parsing.ast.ASTNodeVisitor;
import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.ITypeDefinition;
import com.enterlib.parsing.ast.IVariableDefinition;
import com.enterlib.parsing.ast.MemberExpression;
import com.enterlib.parsing.ast.VariableExpression;

public class EntityMap<T> implements ASTContext ,IFactory<T>, ITypeDefinition , IRepository<T> ,ISQLQueryProvider<T>{
	
	static final String LOG_TAG = EntityMap.class.getSimpleName();

	@Override
	public void close() {

	}

	public interface IValueSetListener<T>{		
		void onSetted(T entity, String property, Object value);		
	}
	
	public interface OnCreatedListener<T>{
		void onCreated(T entity);
	}
		
	private Class<T> cls;
	
	/**map sql column to PropertyMap*/	
	//private HashMap<String, PropertyMap> mappings;
	/**map field name to PropertyMap*/
	private HashMap<String, PropertyMap> inverseMap;
	private ArrayList<PropertyMap> properties;

	private PropertyMap[] keys;
	private PropertyMap modifiedFlag;	
	private HashMap<String, IValueSetListener<T>> propertyListeners;
	private HashMap<Class<?>, IStringConverter> converters;	
	private IValueSetListener<T> globalListener;
	private DateConverter dateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");		
	private IFactory<T>entityFactory;
	private EntityMapContext mapContext;
	private OnCreatedListener<T> onCreateListener;
	private Constructor<T> constructor;
	private String tableName;
	private ComputedColumnGenerator<T> computedColumnGenerator;

    private HashSet<String> checkSemantics;

	public static <T> EntityMap<T> from (Class<T>type, EntityMapContext mapContext){
		EntityMap<T> map = new EntityMap<T>(type, mapContext);
		return map;
	}
	
		
	public EntityMap(Class<T>cls, EntityMapContext mapContext){
		this.cls = cls;
		this.mapContext = mapContext;

		TableMap tableMap= cls.getAnnotation(TableMap.class);
		if(tableMap!=null){
			tableName = tableMap.name();
		}else{
			tableName = cls.getSimpleName();
		}
		
		java.lang.reflect.Field[] typefields = cls.getFields();
		properties = new ArrayList<>(typefields.length);
		inverseMap  = new HashMap<String, PropertyMap>();
		
		ArrayList<PropertyMap>keyList = new ArrayList<PropertyMap>();

		ExpressionParser parser = new ExpressionParser();
		for (int i = 0; i < typefields.length; i++) {
			java.lang.reflect.Field typeField = typefields[i];
			int modifier = typeField.getModifiers();
			if (Modifier.isStatic(modifier)) {
				continue;
			}

			ExpressionColumn expressionColumn = typeField.getAnnotation(ExpressionColumn.class);
			if(expressionColumn != null) {
				Expression expression = parser.parse(expressionColumn.expr());
				PropertyMap fieldMap = new PropertyMap(this, typeField);
				fieldMap.IsWritable = false;
				fieldMap.setExpression(expression);
				inverseMap.put(typeField.getName(), fieldMap);
				properties.add(fieldMap);
			}else {

				ColumnMap dbMap = typeField.getAnnotation(ColumnMap.class);
				if (dbMap == null) {
					continue;
				}

				PropertyMap fieldMap = new PropertyMap(this, typeField);

				properties.add(fieldMap);
				inverseMap.put(typeField.getName(), fieldMap);

				if (fieldMap.IsKey) {
					keyList.add(fieldMap);
				}

				if (fieldMap.IsModifiedBitFlag)
					modifiedFlag = fieldMap;
			}
		}
		
		if(keyList.size() > 0){
			Collections.sort(keyList,new Comparator<PropertyMap>() {

				@Override
				public int compare(PropertyMap lhs, PropertyMap rhs) {
					return  (lhs.Order < rhs.Order)? -1 :
							(lhs.Order > rhs.Order)? 1 : 0;
				}
			});
			
			keys = new PropertyMap[keyList.size()];
			keyList.toArray(keys);
		}

		
		try {
			constructor = cls.getDeclaredConstructor(IEntityContext.class);
			constructor.setAccessible(true);			
		} catch (NoSuchMethodException e) {
			//Log.w(LOG_TAG, "A constructor with  one IEntityContext as  parameter was not found so Lazy evaluation will be disabled");
		}


		for (final PropertyMap pmap : properties){
			Expression expression = pmap.getExpression();
			if(expression!=null){
				expression.visit(new ASTNodeVisitor(){
					@Override
					public Object visit(VariableExpression expression, Object context) {
                        if(getMapByFieldName(expression.getName()) == null) {
                            inverseMap.put(expression.getName(), pmap);
                            pmap.setExternalReference(true);
                        }
						return null;
					}
				}, null);
			}
		}

	}


	HashMap<String, PropertyMap> getInverseMap() {
		return inverseMap;
	}

	PropertyMap getModifiedFlag() {
		return modifiedFlag;
	}

	public DateConverter getDateConverter() {
		return dateConverter;
	}

	public void setDateConverter(DateConverter dateConverter) {
		this.dateConverter = dateConverter;
	}

	public IFactory<T> getEntityFactory() {
		return entityFactory;
	}

	public void setEntityFactory(IFactory<T> entityFactory) {
		this.entityFactory = entityFactory;
	}
	

	public IValueSetListener<T> getGlobalListener() {
		return globalListener;
	}

	public OnCreatedListener<T> getOnCreateListener() {
		return onCreateListener;
	}

	public SQLiteDatabase getReadableDatabase(){
		return mapContext.getDatabase(false);
	}
	
	public SQLiteDatabase getWritableDatabase(){
		return mapContext.getDatabase(true);
	}
		
	public PropertyMap getModificationFlag(){
		return modifiedFlag;
	}
	
	
	
	/**Any values declared in {@link ModifiedValues} */
	public boolean setModifiedFlag(T item, int modified){
		if(modifiedFlag == null)
			return false;
		modifiedFlag.setInt(item, modified);
		return true;
	}
	
	public int getModifiedValue(T item){
		if(modifiedFlag == null)
			return -1;
		return modifiedFlag.getInt(item);
	}
	
	
	public void setOnCreateListener(OnCreatedListener<T> onCreateListener) {
		this.onCreateListener = onCreateListener;
	}
	
	private PropertyMap createMap(String fieldName){
		try {
			Field field = cls.getField(fieldName);
			return new PropertyMap(this, field);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e.getMessage(), e);
		}						
	}
	
	public PropertyMap getMapByFieldName(String fieldName){
        return inverseMap.get(fieldName);
	}

	
	public PropertyMap[] getKeys() {
		return keys;
	}

	public  IVariableDefinition[] geVariableDefinitions(){
		IVariableDefinition[]vars = new IVariableDefinition[properties.size()];
		properties.toArray(vars);
		return vars;

	}

	public EntityMap<T> setDefault(String modelProperty, Object value){
		getMapByFieldName(modelProperty).DefaultValue = value;		
		return this;
	}
		
	
	public IFactory<T> getFactory() {
		return entityFactory;
	}

	public EntityMap<T> setFactory(IFactory<T> factory) {
		this.entityFactory = factory;
		return this;
	}

	public EntityMap<T> setGlobalListener(IValueSetListener<T> globalListener) {
		this.globalListener = globalListener;
		return this;
	}
	
	public EntityMap<T> setPropertyListener(String property, IValueSetListener<T> listener) {
		if(propertyListeners == null){
			propertyListeners = new HashMap<String, EntityMap.IValueSetListener<T>>(1);
		}
		propertyListeners.put(property, listener);
		return this;
	}
	
	public EntityMap<T> setConverter(Class<?>type , IStringConverter converter){
		if(converters == null){
			converters = new HashMap<Class<?>, IStringConverter>(1);			
		}
		converters.put(type, converter);		
		return this;
	}
	
	@Override
	public T getInstance() {
		T entity = null;
		try {
			if(entityFactory == null){
				if(constructor!=null){
					try {
						entity = constructor.newInstance(mapContext);
					} catch (Exception e) {
						Log.e(LOG_TAG, e.getMessage());
						throw new InvalidOperationException(e.getMessage(), e);
					} 
				}
				else{
					entity = cls.newInstance();
				}				
			} else {
				entity = entityFactory.getInstance();
			}
			
		} catch (InstantiationException e) {
			Log.e(LOG_TAG, e.getMessage() , e);
			throw new RuntimeException("Unable to instantiate entity");
		} catch (IllegalAccessException e) {
			Log.e(LOG_TAG, e.getMessage() , e);
			throw new RuntimeException("Unable to instantiate entity");
		}

		
		return entity;
	}

	public T getFromCursor(Cursor cursor){
		T entity = getInstance();
		
		getFromCursor(cursor, entity);
		
		return entity;
	}
	
	public void getFromCursor(Cursor cursor, T entity){

		int columns = cursor.getColumnCount();
		ObjectCache target = new ObjectCache(entity);

		for (int i = 0; i < columns; i++) {
			String columnName = cursor.getColumnName(i);

			if(columnName.charAt(0)=='.'){
				String[] path = columnName.substring(1).split("\\.");
				setNavigationProperties(path, 0, target, cursor, i);
				continue;
			}

			PropertyMap map = inverseMap.get(columnName);
			if(map == null)
				continue;
			
			Object value  = getValue(map.getType(), cursor, i);
			map.set(entity, value);
			onPropertySetted(entity, map.getName(), value);
		}
		
		if(entity != null && onCreateListener != null) {
			onCreateListener.onCreated(entity);
		}
	}

	public T getNextFromCursor(Cursor cursor){
		if(cursor.moveToNext()){
			return getFromCursor(cursor);
		}
		return null;
	}

	public void getNextFromCursor(Cursor cursor, T item){
		if(cursor.moveToNext()){
			getFromCursor(cursor, item);
		}
	}

	private void setNavigationProperties(String[] path, int index, ObjectCache target, Cursor cursor, int column){
		if(target == null)
			return;

		if(index == path.length-1){
			EntityMap<Object> entityMap  = (EntityMap<Object>) mapContext.getMap(target.Value.getClass());
		 	PropertyMap map = entityMap.getMapByFieldName(path[index]);
			Object value  = getValue(map.getType(), cursor, column);
			map.set(target.Value, value);
			entityMap.onPropertySetted(target.Value, map.getName(), value);
		}else{
			try {
				Class<?>targetCls = target.Value.getClass();
				ObjectCache object = target.getValue(path[index]);
				if(object == null){
					ReflectionResolver.TypeDescriptor td = ReflectionResolver.getDescriptor(targetCls);
					Method setterProperty = td.getMethod("set" + path[index]);
					if(setterProperty == null)
						throw new InvalidOperationException("Missing setter method for "+path[index]);

					EntityMap<Object> entityMap  = (EntityMap<Object>) mapContext.getMap(setterProperty.getParameterTypes()[0]);
					Object propValue = entityMap.getInstance();
					setterProperty.invoke(target.Value, propValue);
					object = new ObjectCache(propValue);
					target.setValue(path[index], object);
				}
				setNavigationProperties(path, index + 1, object, cursor, column);

			} catch (InvocationTargetException e) {
				throw new InvalidOperationException(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new InvalidOperationException(e.getMessage(), e);
			}
		}
	}

	protected void onPropertySetted(T entity, String modelPropName, Object value) {
		if(globalListener!=null){
			globalListener.onSetted(entity, modelPropName, value);
		}
		if(propertyListeners!=null){
			IValueSetListener<T>listener = propertyListeners.get(modelPropName);
			if(listener!=null){
				listener.onSetted(entity, modelPropName, value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public T[] getArrayFromCursor(Cursor cursor){
		T[] array = (T[]) Array.newInstance(cls, cursor.getCount());
		int i = 0;
		while(cursor.moveToNext()){
			array[i++]=getFromCursor(cursor);
		}	
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> getListFromCursor(Cursor cursor){		
		ArrayList<T>items = new ArrayList<T>(cursor.getCount());
		int i = 0;
		while(cursor.moveToNext()){
			items.add(getFromCursor(cursor));
		}	
		return items;
	}
	
	private ContentValues getValues(T entity){
		ContentValues values = new ContentValues();

		for (int i = 0; i < properties.size(); i++) {
			PropertyMap map = properties.get(i);
			boolean writable = map.IsWritable();
			if(!writable){
				continue;
			}

			String sqlColumn = map.getSqlColumn();
			Class<?>type = map.getType();

			Object value = map.get(entity);
			if(value == null){
				if(map.IsCurrentTimeStamp){
					Date date = new Date();
					String str = dateConverter.getString(date);
					values.put(sqlColumn, str);
				}else if(map.DefaultValue !=null){
					setContentValue(values, map, sqlColumn, map.DefaultValue, type);
				}
				else
					values.putNull(sqlColumn);
			}else{
				setContentValue(values, map, sqlColumn, value, type);
			}
		}
		
		return values;
	}
	
	private ContentValues getDbValues(ContentValues props){
		ContentValues values = new ContentValues();
		Set<Entry<String, Object>> set = props.valueSet();
		
		boolean change = false;
		for (Map.Entry<String, Object> entry : set){
			String key = entry.getKey();
			PropertyMap map = inverseMap.get(key);			
			if(map == null)
				throw new RuntimeException("Property "+key+" not defined in "+cls.getSimpleName());
			
			if(!map.getSqlColumn().equals(key)){
					change =true;
					break;
			}
		}
			
		if(!change)
			return props;
		
		for (Map.Entry<String, Object> entry : set) {
			PropertyMap map = inverseMap.get(entry.getKey());			
			if(map == null)
				throw new RuntimeException("Property "+entry.getKey()+" not defined in "+cls.getSimpleName());
			
			String sqlColumn = map.getSqlColumn();
			Object value = entry.getValue();
			if(value == null){
				values.putNull(sqlColumn);
			}else{
				setContentValue(values, map, sqlColumn, value, map.getType());
			}
		}
		return values;
	}

	private void setContentValue(ContentValues values, PropertyMap map, String sqlColumn, Object value, Class<?> type) {
		if(type == Integer.class || type == int.class){
			Integer integer = (Integer)value;
			if(map.IsForeignKey && integer <= 0){
				values.putNull(sqlColumn);
			}else{
				values.put(sqlColumn, integer);
			}
		}else if(type == Double.class || type == double.class){
			values.put(sqlColumn, (Double)value);
		}else if(type == Short.class || type == short.class){
			values.put(sqlColumn, (Short)value);
		}else if(type == Float.class || type == float.class){
			values.put(sqlColumn, (Float)value);
		}else if(type == Boolean.class || type == boolean.class){
			values.put(sqlColumn, (Boolean)value);
		}else if(type == Long.class || type == long.class){
			values.put(sqlColumn, (Long)value);
		}else if(type == String.class){
			values.put(sqlColumn, (String)value);
		}else if(type == Date.class){
			String str = dateConverter.getString(value);
			values.put(sqlColumn, str);
		}else if(type == Calendar.class){
			Date date = ((Calendar)value).getTime();
			String str = dateConverter.getString(date);
			values.put(sqlColumn, str);						
		}else if(converters !=null){
			IStringConverter converter = converters.get(type);
			if(converter!=null){
				String str = converter.getString(value);
				values.put(sqlColumn, str);
			}
		}
	}

	private Object getValue(Class<?> type, Cursor cursor, int columnIndex) {
		if(type == String.class){	
			if (cursor.isNull(columnIndex)) {
				return null;
			}
			return cursor.getString(columnIndex);
		}else if(type == int.class){
			if (cursor.isNull(columnIndex))
				return 0;			
			return cursor.getInt(columnIndex);			
		}else if(type == Integer.class){
			if (cursor.isNull(columnIndex))
				return null;		
			return  Integer.valueOf((cursor.getInt(columnIndex))); 
		}else if(type == double.class){
			if (cursor.isNull(columnIndex))
				return Double.valueOf(0);		
			return Double.valueOf((cursor.getDouble(columnIndex))); 
		}else if(type == Double.class){
			if (cursor.isNull(columnIndex))
				return null;		
			return cursor.getDouble(columnIndex);
		}else if(type == float.class){
			if (cursor.isNull(columnIndex))
				return Float.valueOf(0);		
			return Float.valueOf(cursor.getFloat(columnIndex)); 
		}else if(type == Float.class){
			if (cursor.isNull(columnIndex))
				return null;		
			return Float.valueOf(cursor.getFloat(columnIndex)); 
		}else if(type == short.class){
			if (cursor.isNull(columnIndex))
				return Short.valueOf((short) 0);		
			return Short.valueOf(cursor.getShort(columnIndex)); 
		}else if(type == Short.class){
			if (cursor.isNull(columnIndex))
				return null;		
			return Short.valueOf(cursor.getShort(columnIndex)); 
		}else if(type == boolean.class){
			if (cursor.isNull(columnIndex))
				return Boolean.valueOf(false);		
			return Boolean.valueOf(cursor.getInt(columnIndex) == 1); 
		}else if(type == Boolean.class){
			if (cursor.isNull(columnIndex))
				return null;		
			return Boolean.valueOf(cursor.getInt(columnIndex) == 1); 
		}else if(type == Long.class){
			if (cursor.isNull(columnIndex))
				return null;		
			return Long.valueOf(cursor.getLong(columnIndex)); 
		}else if(type == long.class){
			if (cursor.isNull(columnIndex))
				return 0;		
			return Long.valueOf(cursor.getLong(columnIndex)); 
		}else if(type == Date.class){
			if(cursor.isNull(columnIndex))
				return null;
			return dateConverter.getDate(cursor.getString(columnIndex));
		}else if(type == Calendar.class){
			if(cursor.isNull(columnIndex))
				return null;
			Date date = dateConverter.getDate(cursor.getString(columnIndex));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar;
		}else if(type == byte[].class){
			if (cursor.isNull(columnIndex))
				return null;		
			return cursor.getBlob(columnIndex);
		}else if(type == byte.class){
			if (cursor.isNull(columnIndex))
				return 0;		
			return Byte.valueOf((byte)cursor.getInt(columnIndex));
		}else if(type == Byte.class){
			if (cursor.isNull(columnIndex))
				return null;		
			return Byte.valueOf((byte)cursor.getInt(columnIndex));
		}else if(converters!=null && converters.containsKey(type)){
			String value = !cursor.isNull(columnIndex) ? cursor.getString(columnIndex):null;
			return converters.get(type).getObject(value);
		}
		
		throw new RuntimeException("Unsoported type "+type.getName());
		
		
	}

    private void addCondition(SQLQuery query, String condition, ExpressionParser parser, WhereGenerator<T>generator){
        if(condition == null || condition.length() == 0)
            return;

        Expression exp = parser.parse(condition);
        exp.checkSemantic(this);

		generator.generateCode(exp ,query);
    }

	private void addOrderBy(SQLQuery query, String orderByExp){
        if(orderByExp == null || orderByExp.length() == 0)
            return;

        OrderByParser parser = new OrderByParser();
        Expression exp = parser.parse(orderByExp);

        exp.checkSemantic(this);
        StringBuilder sb = new StringBuilder();
        exp.genSQL(this, sb, 0, query);
        query.addOrderBy(sb.toString());
    }


	@Override
	public SQLQuery<T> getQuery() {
		SQLQuery<T> query = new SQLQuery(this);
        generateSQL(query, SQLQuery.GEN_ALL);
		return query;
	}


    protected void generateSQL(SQLQuery<T> query, int genOptions){
		String t0 =  query.getTableRef(tableName);
        if(t0 == null)
            t0  = query.createTableRef(tableName);

		query.setFrom("\"" + tableName + "\"", t0);

		if((genOptions & SQLQuery.GEN_COLUMNS) > 0) {
			generateColumns(query, t0, null, null);
		}

		if((genOptions & SQLQuery.GEN_INCLUDE) > 0)
			IncludeNode.generateSQL(query, this);

		ExpressionParser parser = new ExpressionParser();
		WhereGenerator<T>generator = new WhereGenerator<>(this);
		for (int i = 0; i < query.whereExpression.size(); i++) {
			String where = query.whereExpression.get(i);
			addCondition(query, where, parser, generator);
		}

		if((genOptions & SQLQuery.GEN_ORDERBY) > 0) {
			String orderBy = query.orderByExpression.toString();
			addOrderBy(query, orderBy);
		}
    }

	void generateColumns(SQLQuery<?> query, String t0 , String path , String ref) {

		for (PropertyMap map : properties) {
			String columnName = path != null ? "." + path + "." + map.getName() : map.getName();

            if(map.IsNavigationColumn){
                PropertyMap fkey = map.NavFKMap;
                if(fkey == null)
                    continue;
				String name = ref != null ? ref + "." + fkey.getName() :  fkey.getName();
                String tableRef =query.getTableRef(name);
                if(tableRef == null){
                    tableRef = query.createTableRef(name);
                    String jointType = MemberExpression.getJointType(fkey.getType());
                    query.addJoint(jointType + "\"" + fkey.FKey_table() + "\" " + tableRef,
							tableRef + "." + fkey.FKey_To(),
							t0 + "." + fkey.getSqlColumn());
                }


				query.addColumn(tableRef, map.NavTableColumn, columnName);

            }else if(map.getExpression()!=null){
				if(computedColumnGenerator == null){
					computedColumnGenerator = new ComputedColumnGenerator(this);
				}

				if(checkSemantics == null)
				    checkSemantics = new HashSet<>();

                if(!checkSemantics.contains(map.getName())) {
                    Expression expression = map.getExpression();
                    expression.checkSemantic(this);
                    checkSemantics.add(map.getName());
                }

				if(!map.isNonMapped())
					computedColumnGenerator.generateCode(map, query ,ref, path);
			}

            else if(!map.isNonMapped()){
                query.addColumn(t0, map.getSqlColumn(), columnName);
            }
        }
	}

//	private Class<?> getTypeFromName(String name){
//		String packageName = cls.getPackage().getName();
//		try {
//			Class<?> type = Class.forName(packageName + "." + name);
//			return  type;
//		} catch (ClassNotFoundException e) {
//			throw new InvalidOperationException(e.getLocalizedMessage(), e);
//		}
//	}


	private PropertyMap getIncludeMaps(String[]path, int index, PropertyMap[] includeMap){
		if(includeMap[index]!=null)
			return includeMap[index];

		if(index == 0){
			PropertyMap pMap = getMapByFieldName(path[index]);
            if(pMap == null){
                pMap = getMapByFieldName(path[index]+"Id");
                if(pMap == null)
                    throw new InvalidOperationException("Invalid Field "+ path[index]);
            }
			includeMap[index]= pMap;
			return pMap;
		}else {
            PropertyMap leftMap = getIncludeMaps(path, index - 1, includeMap);
			ITypeDefinition tableDefinition = getTypeDefinition(leftMap.getFKeyModel());
			if(tableDefinition == null){
				throw new InvalidOperationException(String.format("The type '%s' is not defined", leftMap.FKey_table()));
			}

			PropertyMap pMap = (PropertyMap) tableDefinition.getVariableDefinition(path[index]);
            if(pMap == null){
                pMap =(PropertyMap) tableDefinition.getVariableDefinition(path[index]+"Id");
                if(pMap == null)
                    throw new InvalidOperationException("Invalid Field "+ path[index]);
            }
			includeMap[index] = pMap;
			return pMap;
		}
	}

	private String appendIncludeJoints(String include, int index, SQLQuery<?> query,  PropertyMap[] includeMap){
	 	String tableRef = query.getTableRef(include);

		if(tableRef == null){
			tableRef = query.createTableRef(include);

			String jointRef;
			if(index == 0){
				jointRef = query.getTableRef(tableName);
			}else{
				jointRef = appendIncludeJoints(StringUtils.aggregate(includeMap,".",0, index),  index-1, query, includeMap);
			}

			PropertyMap leftMap= includeMap[index-1];
			PropertyMap map = includeMap[index];

			query.addJoint(MemberExpression.getJointType(map.getType()) + "\"" + map.FKey_table() + "\" " + tableRef,
					tableRef + "." + map.FKey_To(),
					jointRef + "." + leftMap.getSqlColumn());
		}
		return  tableRef;
	}


	public String getTableName(){
		return tableName;
	}

	@Override
	public T get(int id){
		return get(id, null);
	}

	@Override
	public T get(int id, String[] includes){
		if(keys == null || keys.length == 0)
			throw new RuntimeException("Key not found");

		SQLQuery q = new SQLQuery(this);
		q.include(includes);

		PropertyMap map = keys[0];
		q.where(String.format("%s=%d ",  map.getName(), id));

		return queryToFirst(q.toSql());
	}

	@Override
	public T get(int[] ids){
		return get(ids, null);
	}

	@Override
	public T get(int[] ids, String[]includes){
		if(ids == null || ids.length == 0)
			throw new InvalidOperationException("ids array must not be null or empty");

		if(keys == null || keys.length == 0)
			throw new InvalidOperationException("Keys not found for "+cls.getName());

		SQLQuery q = new SQLQuery(this);
		q.include(includes);

		String table = q.getTableRef(tableName);

		for (int i = 0; i < keys.length; i++) {
			PropertyMap map = keys[i];
			q.where(String.format("%s=%d", map.getName(), ids[i]));
		}

		return queryToFirst(q.toSql());
	}


	private int getId(String where){
		SQLiteDatabase db = mapContext.getDatabase(false);
		if(keys == null || keys.length ==0)
			throw new InvalidOperationException("Keys not found for "+cls.getName());
		
		String query =String.format("SELECT %s FROM %s", keys[0].getSqlColumn(), getTableName());
	
		if(where!=null){
			query+=" WHERE "+where;
		}
		Log.d(LOG_TAG, query);
		
		Cursor cursor = db.rawQuery(query, null);
		int id =0;
		if(cursor.moveToNext()){
			id = cursor.getInt(0);
		}
		cursor.close();		
		
		return id;
	}
	
	private int[] getIds(String where){
		SQLiteDatabase db = mapContext.getDatabase(false);
		if(keys == null || keys.length ==0)
			throw new InvalidOperationException("Keys not found for "+cls.getName());
		
		String query ="SELECT ";
		for (int i = 0; i < keys.length; i++) {
			if(i>0){
				query+=",";
			}				
			query+=keys[i].getSqlColumn();
		}
	
		query+=" FROM "+getTableName();
		if(where!=null){
			query+=" WHERE "+where;
		}
		Log.d(LOG_TAG, query);
		
		Cursor cursor = db.rawQuery(query, null);
		int[] ids = null;
		if(cursor.moveToNext()){
			ids = new int[keys.length];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = cursor.getInt(i);
			}
		}
		cursor.close();		
		
		return ids;
	}
	
	public Cursor execute(String sql){
		SQLiteDatabase db = mapContext.getDatabase(false);
		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}


    @Override
	public boolean create(T entity){
		ContentValues values = getValues(entity);
		SQLiteDatabase db =this.mapContext.getDatabase(true);
		
		int id = (int) db.insertOrThrow("\"" + getTableName() + "\"", null, values);
		if(id > 0){
			if(keys!=null && keys.length == 1){			
				 keys[0].setInt(entity, id);
			}
		}

		return id > 0;
	}

    @Override
	public boolean update(T entity){
		ContentValues values = getValues(entity);
		SQLiteDatabase db =this.mapContext.getDatabase(true);
		if(keys == null)
			throw new RuntimeException("Keys not found for "+cls.getName());
		
		String where = createWhere(entity);		
		int rows = db.update("\"" + getTableName() + "\"", values, where, null);
		return rows > 0;
	}

	public boolean update(int id, ContentValues values){
		if(keys == null || keys.length == 0)
			throw new RuntimeException("Key not found for "+cls.getName());
		
		PropertyMap map = keys[0];
		String where = String.format("%s=%d", map.getSqlColumn(), id);
		values = getDbValues(values);
		
		SQLiteDatabase db =this.mapContext.getDatabase(true);
		int rows = (int) db.update("\""+ getTableName()+"\"", values, where, null);
		return rows > 0;
	}

	private String createWhere(T entity) {		
		
		StringBuilder where = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			PropertyMap map = keys[i];
			int id =map.getInt(entity);
			
			if(i > 0)
				where.append(" AND ");
			
			where.append(String.format("%s=%d ", map.getSqlColumn(), id));
		}
		return where.toString();
	}

    @Override
	public boolean delete(T entity){
		if(entity == null)
			throw new InvalidOperationException("entity can not be null");
		
		SQLiteDatabase db =this.mapContext.getDatabase(true);
		if(keys==null)
			throw new InvalidOperationException("Key not Found for " +cls.getName());
		
		String where = createWhere(entity);
		int rows = db.delete("\""+getTableName()+"\"", where, null);
		return rows > 0;
	}

	public boolean delete(int id){
		SQLiteDatabase db =this.mapContext.getDatabase(true);
		if(keys==null || keys.length == 0)
			throw new RuntimeException("Key not Found for "+cls.getName());
								
		String where = String.format("%s=%d", keys[0].getSqlColumn(), id);
		int rows = db.delete("\"" + getTableName() + "\"", where, null);
		return rows > 0;
	}

    @Override
	public int delete(@Nullable String expression){
        SQLQuery query = new SQLQuery(this);
	    query.where(expression);
		query.compile(0);
		String tableRef = query.getTableRef(tableName);

        SQLiteDatabase db =this.mapContext.getDatabase(true);

		int rows;
		if(query.getJointCount() == 0) {
			String where = query.getWhere();
			if(where!=null)
				where = where.replace(tableRef+".", "");
			rows = db.delete("\"" + tableName + "\"", where, null);
		}else {
			if(keys == null || keys.length == 0)
				throw new InvalidOperationException("Key not Found for "+cls.getName());

			PropertyMap key = keys[0];
			String sql = query.toSql();
			String innerQuery = key.getSqlColumn()+" in (SELECT "+tableRef+"."+ key.getSqlColumn()+" "+sql+ ")";
			rows = db.delete("\"" + tableName + "\"", innerQuery, null);
		}

		return rows;
	}


	@Override
	public IQuerable<T> query(@Nullable String where, @Nullable String orderBy, int skip, int take, @Nullable String[] includes) {
		return new SQLQuery<T>(where, orderBy, skip, take, includes, this);
	}

	@Override
	public IQuerable<T> query(String where, String orderBy, int skip, int take) {
		return query(where, orderBy, skip, take, null);
	}

	@Override
	public IQuerable<T> query(String where, String orderBy) {
		return query(where, orderBy, -1, -1, null);
	}

	@Override
	public IQuerable<T> query(String where) {
		return query(where, null, -1, -1, null);
	}

	@Override
	public IQuerable<T> query() {
		return query(null, null, -1, -1, null);
	}

	@Override
	public int count(@Nullable String expression) {
		IQuerable<T> query = query(expression);
		return (int) query.count();
	}


	public ArrayList<T> queryToList(String sql){
        Log.d(LOG_TAG, sql);

        SQLiteDatabase db = mapContext.getDatabase(false);
        Cursor cursor = db.rawQuery(sql, null);
        ArrayList<T> items = getListFromCursor(cursor);
        cursor.close();

        return items;
    }

    public T queryToFirst(String sql){
        Log.d(LOG_TAG, sql);

        SQLiteDatabase db = mapContext.getDatabase(false);
        Cursor cursor = db.rawQuery(sql, null);
        T item = getNextFromCursor(cursor);
        cursor.close();
        return item;
    }

	public void queryToFirst(SQLQuery sqlQuery, T item){
		String sql = sqlQuery.toSql();
		Log.d(LOG_TAG, sql);

		SQLiteDatabase db = mapContext.getDatabase(false);
		Cursor cursor = db.rawQuery(sql, null);
		getNextFromCursor(cursor, item);
		cursor.close();
	}

	public int queryCount(SQLQuery sqlQuery){
		if(keys==null || keys.length == 0)
			throw new InvalidOperationException();

		String sql = sqlQuery.toSql();
		Log.d(LOG_TAG, sql);

		PropertyMap p = keys[0];
		sql =String.format("SELECT COUNT(%s.%s) %s",
				sqlQuery.getTableRef(tableName),
				p.getSqlColumn(),
				sql);

		SQLiteDatabase db = mapContext.getDatabase(false);
		Cursor cursor = db.rawQuery(sql, null);
		int count = -1;
		if(cursor.moveToNext()){
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

    public IEntityCursor<T> queryToCursor(SQLQuery sqlQuery){
		String sql = sqlQuery.toSql();
		Cursor cursor = getCursor(sql);
		return new SQLiteEntityCursor<T>(cursor, this ,sqlQuery);
    }

	public Cursor getCursor(String sql){
		Log.d(LOG_TAG, sql);

		SQLiteDatabase db = mapContext.getDatabase(false);
		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}
	

	@Override
	public String getName() {
		return getTableName();
	}

	@Override
	public Class<?> getEntityClass() {
		return  cls;
	}

	@Override
	public IVariableDefinition getVariableDefinition(String name) {
		return getMapByFieldName(name);
	}

	@Override
	public ITypeDefinition getTypeDefinition(String name) {
		if(!name.contains(".")) {
			String packageName = cls.getPackage().getName();
			name =packageName+"."+name;
		}

		EntityMap<?> typeMap = mapContext.getMap(name);
		return typeMap;
	}


	public ITypeDefinition getTypeDefinition(Class<?> cls) {
		EntityMap<?> typeMap = mapContext.getMap(cls);
		return typeMap;
	}

	@Override
	public String toString() {	
		return getTableName();
	}

	static class ObjectCache{
		public Object Value;

		private HashMap<String, ObjectCache> properties;


		public ObjectCache(Object value){
			this.Value = value;
		}

		public ObjectCache(){}

		public ObjectCache getValue(String name){
			if(properties == null)
				return null;
			return properties.get(name);
		}

		public void setValue(String name ,ObjectCache value){
			if(properties == null)
				properties = new HashMap<>();
			properties.put(name, value);
		}
	}

}
