package com.enterlib.data;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.enterlib.IEqualityComparer;
import com.enterlib.StringUtils;
import com.enterlib.annotations.ColumnMap;
import com.enterlib.annotations.Filterable;
import com.enterlib.annotations.ForeingKey;
import com.enterlib.annotations.ModifiedBitFlag;
import com.enterlib.annotations.ExternalColumn;
import com.enterlib.annotations.TableMap;
import com.enterlib.annotations.TimeStamp;
import com.enterlib.converters.IValueConverter;
import com.enterlib.data.sqlite.EntityMap;
import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.parsing.ast.ClassTypeDefinitionWrapper;
import com.enterlib.parsing.ast.Expression;
import com.enterlib.parsing.ast.ExpressionType;
import com.enterlib.parsing.ast.ITypeDefinition;
import com.enterlib.parsing.ast.IVariableDefinition;

public class PropertyMap implements IVariableDefinition{
	
	public static final int TYPE_BLOB = 7;
	public static final int TYPE_FKEY = 6;
	public static final int TYPE_DATETIME = 5;
	public static final int TYPE_BOOL = 4;
	public static final int TYPE_STRING = 3;
	public static final int TYPE_REAL = 2;
	public static final int TYPE_INT = 1;
	public static final int TYPE_NONE = 0;

	private final String tableName;
	public String SqlColumn;		
	public java.lang.reflect.Field Field;
	public boolean IsWritable = true;
	public boolean IsForeignKey;
	public boolean IsKey;	
	public boolean IsModifiedBitFlag;
	public int StoreType;
	public boolean IsFilterable;
	public boolean IsFastSearch;
	public Object DefaultValue;
	public int Order;
	public boolean IsCurrentTimeStamp;
	
	//foreing key fields
	String fkey_to;
	String fkey_table;
	String fkey_update;
	String fkey_delete;
	Class<?>fkey_model;
	String fkey_field;
	
	//navigation fields
	public boolean IsNavigationColumn;
	public PropertyMap NavFKMap;	
	public String NavTableColumn;

	ITypeDefinition typeDefinition;

	private boolean isAggregate;

	public boolean isNonMapped() {
		return nonMapped;
	}

	public void setNonMapped(boolean nonMapped) {
		this.nonMapped = nonMapped;
		this.IsWritable = !nonMapped;
	}

	private boolean nonMapped;

	public boolean isExternalReference(){
		return isAggregate;
	}

	public void setExternalReference(boolean value){
		this.isAggregate = value;
	}

	@Override
	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	Expression expression;

	public PropertyMap(ITypeDefinition typeDefinition, java.lang.reflect.Field field) {
		this(typeDefinition, field, field.getAnnotation(ColumnMap.class));
	}
	
	public PropertyMap(ITypeDefinition typeDefinition, java.lang.reflect.Field field, ColumnMap dbMap) {
		this.Field = field;
		this.typeDefinition = typeDefinition;

		TableMap tableMap= typeDefinition.getEntityClass().getAnnotation(TableMap.class);
		if(tableMap!=null){
			tableName = tableMap.name();
		}else{
			tableName = typeDefinition.getEntityClass().getSimpleName();
		}

		if(dbMap!=null){
			//simple table column
			nonMapped = dbMap.nonMapped();

			if(!StringUtils.isNullOrWhitespace(dbMap.column())){
				SqlColumn = dbMap.column();
			}
			IsWritable = dbMap.writable();		
			IsKey = dbMap.key();			
			Order = dbMap.order();	
			
			TimeStamp timeStamp = field.getAnnotation(TimeStamp.class);
			if(timeStamp!=null){
				this.IsCurrentTimeStamp = true;
			}
			
			//the column is a foreing key
			ForeingKey Fkey = field.getAnnotation(ForeingKey.class);
			if(Fkey!=null){
				IsForeignKey = true;
				fkey_model = Fkey.model();

				TableMap modelMap = fkey_model.getAnnotation(TableMap.class);
				fkey_table = modelMap!=null? modelMap.name() : fkey_model.getSimpleName();

				fkey_field = Fkey.field();
				if(fkey_field == null || fkey_field.isEmpty()) {

					PropertyMap fieldMap = getDefaultKey(fkey_model);
					if(fieldMap == null)
						throw new InvalidOperationException(String.format("The model '%s' does not define any primary key", fkey_model.getName()));
					fkey_field = fieldMap.Field.getName();
				}

				try {
					java.lang.reflect.Field modelfield = fkey_model.getField(fkey_field);
					ColumnMap toMap = modelfield.getAnnotation(ColumnMap.class);
					fkey_to = !StringUtils.isNullOrWhitespace(toMap.column()) ? toMap.column() : modelfield.getName();
				} catch (NoSuchFieldException e) {
					throw new InvalidOperationException(e.getMessage() ,e);
				}


				fkey_update = Fkey.update();
				fkey_delete = FKey_delete();
			}
			
			ModifiedBitFlag modifiedFlag = field.getAnnotation(ModifiedBitFlag.class);
			if(modifiedFlag!=null){
				this.IsModifiedBitFlag = true;
			}
			
		}else{			
			//navigation column			
			ExternalColumn nav = field.getAnnotation(ExternalColumn.class);
			if(nav!=null){
				IsNavigationColumn = true;
				NavTableColumn = nav.column();			
			}
			
//			String fieldName = field.getName();
//			if(fieldName.equalsIgnoreCase("Id")){
//				IsKey = true;
//				IsWritable = false;
//			}
//			else{
//				Class<?>cls = field.getDeclaringClass();
//				if(fieldName.equals(cls.getSimpleName()+"Id")){
//					IsKey = true;
//					IsWritable = false;
//				}else if(fieldName.endsWith("Id")){
//					IsForeignKey = true;
//				}
//			}
		}
		
		Filterable filterable = field.getAnnotation(Filterable.class);
		if(filterable!=null){
			this.IsFilterable = true;
			this.IsFastSearch = filterable.isDefault();
			if(IsFastSearch && field.getType() != String.class){				
				throw new RuntimeException("Only strings are allowed in the fastSearch for "+Field.getName());
			}
		}
						
		StoreType = getStoreType();
	}


	public ITypeDefinition getDeclaringTypeDefinition(){
		return typeDefinition;
	}

	private int getStoreType(){
		Class<?>type = Field.getType();		
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

	//**************** SETTERS ****************************************

	public Object get(Object object) {
		try {
			return Field.get(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public boolean getBoolean(Object object){
		try {
			return Field.getBoolean(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public byte getByte(Object object) {
		try {
			return Field.getByte(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public char getChar(Object object){
		try {
			return Field.getChar(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public double getDouble(Object object){
		try {
			return Field.getDouble(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public float getFloat(Object object){
		try {
			return Field.getFloat(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public int getInt(Object object) {
		try {
			return Field.getInt(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public long getLong(Object object){
		try {
			return Field.getLong(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public short getShort(Object object) {
		try {
			return Field.getShort(object);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void set(Object object, Object value){
		try {
			Field.set(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void setBoolean(Object object, boolean value) {
		try {
			Field.setBoolean(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void setByte(Object object, byte value) {
		try {
			Field.setByte(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void setChar(Object object, char value){
		try {
			Field.setChar(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void setDouble(Object object, double value){
		try {
			Field.setDouble(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void setFloat(Object object, float value){
		try {
			Field.setFloat(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void setInt(Object object, int value){
		try {
			Field.setInt(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName(), e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName(), e);
		}
	}
	public void setLong(Object object, long value){
		try {
			Field.setLong(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}
	public void setShort(Object object, short value){
		try {
			Field.setShort(object, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to set property " + Field.getName());
		}
	}

	//****************************************************************

	@Override
	public String getTableName(){
		return  tableName;
	}

	public String getName() {
		return Field.getName();
	}

	@Override
	public Class<?> getType() {
		return Field.getType();
	}

	@Override
	public boolean isForeignKey(){
		return IsForeignKey;
	}

	public boolean IsWritable(){
		return IsWritable && !IsNavigationColumn;
	}


	@Override
	public String getSqlColumn(){
		return SqlColumn != null? SqlColumn : Field.getName();
	}
		
	public void setDefaultValue(Object object){
		if(DefaultValue!=null){		
			set(object, DefaultValue);			
		}
	}

	@Override
	public String FKey_To() {
		return fkey_to;
	}

	@Override
	public IVariableDefinition getNavigationFKey() {
		return NavFKMap;
	}

	@Override
	public String getNavigationTableColumn() {
		return NavTableColumn;
	}

	@Override
	public String FKey_table() {
		return fkey_table;
	}

	public Class<?>getFKeyModel(){
		return fkey_model;
	}

	@Override
	public String getFKeyModelField() {
		return fkey_field;
	}

	public String FKey_update() {
		return fkey_update;
	}

	public String FKey_delete() {
		return fkey_delete;
	}

	public static List<PropertyMap> getPropertyMaps (Class<?>cls){
		java.lang.reflect.Field[] typefields = cls.getFields();
		ArrayList<PropertyMap>maps = new ArrayList<PropertyMap>(typefields.length);
		
		for (int i = 0; i < typefields.length; i++) {
			java.lang.reflect.Field typeField = typefields[i];
			int modifier = typeField.getModifiers();
			if (Modifier.isStatic(modifier)) {
				continue;
			}

			PropertyMap fieldMap = new PropertyMap(null, typeField);
		    maps.add(fieldMap);
		}
		return maps;
	}
	
	public static PropertyMap getDefaultKey(Class<?>cls){
		java.lang.reflect.Field[] typefields = cls.getFields();
		ClassTypeDefinitionWrapper wrapper = new ClassTypeDefinitionWrapper(cls);
		for (int i = 0; i < typefields.length; i++) {
			java.lang.reflect.Field typeField = typefields[i];
			int modifier = typeField.getModifiers();
			if (Modifier.isStatic(modifier)) {
				continue;
			}

			PropertyMap fieldMap = new PropertyMap(wrapper, typeField);
		    if(fieldMap.IsKey && fieldMap.Order == 0)
		    	return fieldMap;
		}
		return null;
	}
	
	public static IValueConverter createValueConverter(Class<? extends Object> cls) {
		PropertyMap map = PropertyMap.getDefaultKey(cls);
		if(map == null)
			return null;
		return new PropertyMapValueConverter(map);
	}
	
	public static IEqualityComparer createValueComparer(Class<? extends Object>cls){
		PropertyMap map = PropertyMap.getDefaultKey(cls);
		if(map == null)
			return null;
		return new PropertyMapComparer(map);
	}

	static class PropertyMapValueConverter implements IValueConverter{
		PropertyMap map;
		
		public PropertyMapValueConverter(PropertyMap map) {
			super();
			this.map = map;
		}

		/**
		 * not used in SelectionFields just return the parameter, In this case ,the
		 * value that is usually an integer representing an entity id
		 * */
		@Override
		public Object convert(Object value) throws ConversionFailException {
			return value;
		}

		@Override
		public Object convertBack(Object value) throws ConversionFailException {
			if (value == null) {
				return 0;
			}
			return map.get(value);			
		}
		
	}
	
	
	static class PropertyMapComparer implements IEqualityComparer {
		PropertyMap map;				
		
		public PropertyMapComparer(PropertyMap map) {
			super();
			this.map = map;
		}



		@Override
		public boolean equals(Object item, Object value) {
			Object itemId = map.get(item);
			Object valueId;		
			if (value instanceof Integer) {
				valueId = (Integer) value;				
			}else{
				valueId = map.get(value);
			}									
			return itemId.equals(valueId);
		}
	}

	@Override
	public ExpressionType getExpressionType() {
		ExpressionType type = null;	
		switch (StoreType) {
			case PropertyMap.TYPE_BLOB:
				type = ExpressionType.Bool;
				break;
			case PropertyMap.TYPE_DATETIME:
				type = ExpressionType.String;
				break;
			case PropertyMap.TYPE_INT:
			case PropertyMap.TYPE_FKEY:
				type = ExpressionType.Integer;
				break;
			case PropertyMap.TYPE_REAL:
				type = ExpressionType.Double;
				break;
			case PropertyMap.TYPE_STRING:
				type = ExpressionType.String;
				break;
			case PropertyMap.TYPE_BOOL:
				type = ExpressionType.Bool;
				break;
		default:
				type = ExpressionType.Object;
			break;
		}
		return type;
	}

	@Override
	public Class<?> getDeclaringType() {
		return typeDefinition.getEntityClass();
	}
	
	@Override
	public String toString() {	
		return getSqlColumn();
	}
		
}