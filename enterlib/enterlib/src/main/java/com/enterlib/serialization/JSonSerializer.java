package com.enterlib.serialization;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.enterlib.annotations.DataMember;
import com.enterlib.annotations.ForeingKey;
import com.enterlib.annotations.NavigationProperty;
import com.enterlib.annotations.NotSerializable;
import com.enterlib.converters.CalendarConverter;
import com.enterlib.converters.DateConverter;
import com.enterlib.converters.IStringConverter;
import com.enterlib.data.IFactory;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.exceptions.InvalidFieldException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.exceptions.InvalidPropertyException;

/**
 * Serialize and deserialize objects to and from a JSON string
 *
 *
 */
public class JSonSerializer extends StringSerializer {

	public static final String NULL = "null";

	public JSonSerializer() {
		registerConverted(Date.class, new DateConverter("yyyy-MM-dd HH:mm:ss", true));
		registerConverted(Calendar.class, new CalendarConverter("yyyy-MM-dd HH:mm:ss"));
	}

	private JSONObject getJSon(Object item) throws JSONException,
			InvalidOperationException {
		Class<?> type = item.getClass();
		Field[] fields = type.getFields();
		JSONObject obj = new JSONObject();

		// Serialize the public ,instance and serializable fields
		for (Field f : fields) {
			int modifier = f.getModifiers();
			if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)
					|| Modifier.isPrivate(modifier)
					|| Modifier.isProtected(modifier)
					|| f.isAnnotationPresent(NotSerializable.class)) {
				continue;
			}

			Object value = null;
			Class<?> fieldType = f.getType();
			String fieldName = f.getName();
			try {
				value = f.get(item);
			} catch (IllegalAccessException e) {
				Log.e(getClass().getName(), e.getMessage());
				throw new InvalidFieldException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				Log.e(getClass().getName(), e.getMessage());
				throw new InvalidFieldException(e.getMessage(), e);
			}

			serializeValue(obj, value, fieldType, fieldName);
		}

		// serialize the DataMember properties
//		Method[] methods = type.getMethods();
//		for (int i = 0; i < methods.length; i++) {
//			Method method = methods[i];
//			String name = method.getName();
//			int modifier = method.getModifiers();
//
//			if (!method.isAnnotationPresent(DataMember.class)
//					|| (Modifier.isStatic(modifier) || Modifier
//							.isFinal(modifier))) {
//				continue;
//			}
//			if (!name.startsWith("get")) {
//				continue;
//			}
//
//			name = name.substring(3); // take off the 'get' prefix
//			Object value = null;
//			try {
//				value = method.invoke(item);
//			} catch (IllegalAccessException e) {
//				Log.e(getClass().getName(), e.getMessage());
//				throw new InvalidPropertyException(e.getMessage(), e);
//			} catch (IllegalArgumentException e) {
//				Log.e(getClass().getName(), e.getMessage());
//				throw new InvalidPropertyException(e.getMessage(), e);
//			} catch (InvocationTargetException e) {
//				Log.e(getClass().getName(), e.getMessage());
//				throw new InvalidPropertyException(e.getMessage(), e);
//			}
//			serializeValue(obj, value, method.getReturnType(), name);
//		}
		return obj;
	}

	private void serializeValue(JSONObject obj, Object value,
			Class<?> fieldType, String fieldName) throws JSONException,
			InvalidOperationException {

		if (value == null) {
			obj.put(fieldName, JSONObject.NULL);
		} else if (value instanceof String) {
			obj.put(fieldName, value);
		} else if (fieldType.isArray()) {
			obj.put(fieldName, getJsonArray(value));
		} else if (value instanceof List<?>) {
			obj.put(fieldName, getJsonArrayFromList((List<?>) value));
		} else if (fieldType.isPrimitive() || (value instanceof Number)
				|| (value instanceof Boolean)) {
			obj.put(fieldName, value);
		} else {
			if (hasConverterFor(fieldType)) {
				obj.put(fieldName, convertToString(value, fieldType));
			} else {
				obj.put(fieldName, getJSon(value));
			}
		}
	}

	private JSONArray getJsonArrayFromList(List<?> list)
			throws InvalidOperationException, JSONException {
		JSONArray jsonArray = new JSONArray();
		if (list == null) {
			return jsonArray;
		}

		int lenght = list.size();
		for (int i = 0; i < lenght; i++) {
			Object item = list.get(i);
			if (item == null) {
				jsonArray.put(JSONObject.NULL);
				continue;
			}

			Class<?> elementType = item.getClass();
			if (elementType == String.class) {
				jsonArray.put(item);
			} else if ((item instanceof Number) || item instanceof Boolean
					|| elementType.isEnum()) {
				jsonArray.put(item);
			} else {
				IStringConverter converter = getConverterFor(elementType);
				if (converter != null) {
					jsonArray.put(convertToString(item, converter));
				} else {
					jsonArray.put(getJSon(item));
				}
			}
		}
		return jsonArray;
	}

	private JSONArray getJsonArray(Object array)
			throws InvalidOperationException, JSONException {

		int lenght = Array.getLength(array);
		JSONArray jsonArray = new JSONArray();

		Class<?> elementType = array.getClass().getComponentType();
		IStringConverter converter = getConverterFor(elementType);

		for (int i = 0; i < lenght; i++) {
			try {
				if (elementType == int.class) {
					jsonArray.put(Array.getInt(array, i));
				} else if (elementType == short.class) {
					jsonArray.put(Array.getShort(array, i));
				} else if (elementType == long.class) {
					jsonArray.put(Array.getLong(array, i));
				} else if (elementType == double.class) {
					jsonArray.put(Array.getDouble(array, i));
				} else if (elementType == float.class) {
					jsonArray.put(Array.getFloat(array, i));
				} else if (elementType == byte.class) {
					jsonArray.put(Array.getByte(array, i));
				} else if (elementType == char.class) {
					jsonArray.put(Array.getChar(array, i));
				} else if (elementType == boolean.class) {
					jsonArray.put(Array.getBoolean(array, i));
				} else {
					Object value = Array.get(array, i);

					if (value == null) {
						jsonArray.put(JSONObject.NULL);
					} else if (elementType == String.class) {
						jsonArray.put(Array.get(array, i));
					} else if (elementType.isArray()) {
						jsonArray.put(getJsonArray(value));
					} else if (elementType.isEnum()) {
						jsonArray.put(value);
					} else {
						if (converter != null) {
							jsonArray.put(convertToString(value, converter));
						} else {
							jsonArray.put(getJSon(value));
						}
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e(getClass().getName(), e.getMessage());
				throw new InvalidOperationException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				Log.e(getClass().getName(), e.getMessage());
				throw new InvalidOperationException(e.getMessage(), e);
			} catch (JSONException e) {
				Log.e(getClass().getName(), e.getMessage());
				throw new InvalidOperationException(e.getMessage(), e);
			}
		}

		return jsonArray;
	}

	protected Object createInstance(Class<?>cls)throws InvalidOperationException{
		try {
			return cls.newInstance();
		} catch (InstantiationException e) {
			Log.e(getClass().getName(), e.getMessage(), e);
			throw new InvalidOperationException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			Log.e(getClass().getName(), e.getMessage(), e);
			throw new InvalidOperationException(e.getMessage(), e);
		}
	}

	private Object getObject(JSONObject json, Class<?> objectType)
			throws InvalidOperationException, JSONException {

		Field[] fields = objectType.getFields();
		Object o = createInstance(objectType);
		for (Field field : fields) {
			int modifier = field.getModifiers();

			if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)
					|| Modifier.isPrivate(modifier)
					|| Modifier.isProtected(modifier)
					|| field.isAnnotationPresent(NotSerializable.class)) {
				continue;
			}

			Class<?> fieldType = field.getType();
			String fieldName = field.getName();
			try {
				if (json.has(field.getName()) && !json.isNull(fieldName)) {
					if (fieldType == Integer.class) {
						field.set(o, json.getInt(fieldName)); 
					}else if (fieldType == int.class) {
						field.setInt(o, json.getInt(fieldName)); 
					}else if (fieldType == Boolean.class) {
						field.set(o, json.getBoolean(fieldName));
					}else if (fieldType == boolean.class) {
						field.setBoolean(o, json.getBoolean(fieldName));
					}else if (fieldType == Double.class) {						
						field.set(o, json.getDouble(fieldName));
					}else if (fieldType == double.class) {						
						field.setDouble(o, json.getDouble(fieldName));
					}else if (fieldType == Long.class) {
						field.set(o, json.getLong(fieldName));
					}else if (fieldType == long.class) {
						field.setLong(o, json.getLong(fieldName));
					}else if (fieldType == Short.class) {
						field.set(o, (short) json.getInt(fieldName));
					} else if (fieldType == short.class) {
						field.setShort(o, (short) json.getInt(fieldName));
					}else if (fieldType == Float.class) {
						field.set(o, (float) json.getDouble(fieldName));
					} else if (fieldType == float.class) {
						field.setFloat(o, (float) json.getDouble(fieldName));
					} else if (fieldType == Byte.class) {
						field.set(o, (byte) json.getInt(fieldName));
					} else if (fieldType == byte.class) {
						field.setByte(o, (byte) json.getInt(fieldName));
					} else if (fieldType == String.class) {
						field.set(o, json.getString(fieldName));
					} else if (fieldType.isArray()) {
						JSONArray jsonArray = json.getJSONArray(fieldName);
						field.set(o, getArray(jsonArray, fieldType));
					} else if (fieldType == ArrayList.class) {
						JSONArray jsonArray = json.getJSONArray(fieldName);
						DataMember annot = field.getAnnotation(DataMember.class);
						if(annot == null || annot.listType()==null)
							throw new InvalidOperationException("List type unknow. Use DataMember annotation specifying the listType");
						field.set(o, getList(jsonArray, fieldType, annot.listType()));
					} else if (hasConverterFor(fieldType)) {
						field.set(o, convertBack(json.getString(fieldName), fieldType));
					} else {
						field.set(o,getObject(json.getJSONObject(fieldName), fieldType));
					}

					ForeingKey annotation = field.getAnnotation(ForeingKey.class);
					if(annotation!=null) {
						Method method = null;
						String jsonField = null;
						NavigationProperty navAnnotation = field.getAnnotation(NavigationProperty.class);
						if(navAnnotation!=null) {
							jsonField = navAnnotation.value();
						}else{
                            jsonField = fieldName.substring(0, fieldName.length() - 2);
						}

                        method =ReflectionResolver.getDescriptor(objectType).getMethod("set"+jsonField);
						if(method != null && json.has(jsonField) && !json.isNull(jsonField)){
							try {
								method.invoke(o, getObject(json.getJSONObject(jsonField), method.getParameterTypes()[0]));
							} catch (InvocationTargetException e) {
								Log.e(getClass().getName(), e.getMessage(), e);
							}
						}

					}
				}
			} catch (IllegalAccessException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
			}

		}


//		Method[] methods = objectType.getMethods();
//		for (int i = 0; i < methods.length; i++) {
//			Method method = methods[i];
//			String name = method.getName();
//			int modifier = method.getModifiers();
//			if (!method.isAnnotationPresent(DataMember.class)
//					|| (Modifier.isStatic(modifier) || Modifier
//							.isFinal(modifier))) {
//				continue;
//			}
//			if (!name.startsWith("set")) {
//				continue;
//			}
//
//			Class<?>[] paramTypes = method.getParameterTypes();
//			if (paramTypes.length != 1) {
//				throw new RuntimeException(
//						"method " + name + "is not a property. It must begin with the preffix 'set' and do not receive any arguments");
//			}
//
//			Class<?> paramType = paramTypes[0];
//			name = name.substring(3);
//
//			try {
//
//				if (json.has(name) && !json.isNull(name)) {
//					if (paramType == Integer.class || paramType == int.class) {
//						method.invoke(o, json.getInt(name));
//					} else if (paramType == Boolean.class
//							|| paramType == boolean.class) {
//						method.invoke(o, json.getBoolean(name));
//					} else if (paramType == Double.class
//							|| paramType == double.class) {
//						method.invoke(o, json.getDouble(name));
//					} else if (paramType == Long.class
//							|| paramType == long.class) {
//						method.invoke(o, json.getLong(name));
//					} else if (paramType == Short.class
//							|| paramType == short.class) {
//						method.invoke(o, (short) json.getInt(name));
//					} else if (paramType == Float.class
//							|| paramType == float.class) {
//						method.invoke(o, (float) json.getDouble(name));
//					} else if (paramType == Byte.class
//							|| paramType == byte.class) {
//						method.invoke(o, (byte) json.getInt(name));
//					} else if (paramType == String.class) {
//						method.invoke(o, json.getString(name));
//					} else if (paramType.isArray()) {
//						JSONArray jsonArray = json.getJSONArray(name);
//						method.invoke(o, getArray(jsonArray, paramType , null));
//					} else if (hasConverterFor(paramType)) {
//						method.invoke(o,
//								convertBack(json.getString(name), paramType));
//					} else if(paramType == ArrayList.class){
//						DataMember annot = method.getAnnotation(DataMember.class);
//						if(annot == null || annot.listType()==null)
//							throw new InvalidOperationException("List type unknow. Use DataMember annotation specifying the listType");
//						method.invoke(o, getList(json.getJSONArray(name), paramType, annot.listType(), null));
//					}
//					else {
//						method.invoke(o,getObject(json.getJSONObject(name), paramType, null));
//					}
//				}
//			} catch (IllegalAccessException e) {
//				Log.e(getClass().getName(), e.getMessage(), e);
//			} catch (IllegalArgumentException e) {
//				Log.e(getClass().getName(), e.getMessage(), e);
//			} catch (InvocationTargetException e) {
//				Log.e(getClass().getName(), e.getMessage(), e);
//			}
//		}
		return o;
	}

	private Object getArray(JSONArray jsonArray, Class<?> arrayType)
			throws JSONException, InvalidOperationException {

		Class<?> componentType = arrayType.getComponentType();
		return getArrayFromComponent(jsonArray, componentType);
	}

	private Object getArrayFromComponent(JSONArray jsonArray, Class<?> componentType)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException, JSONException{
		Object array = Array.newInstance(componentType, jsonArray.length());
		IStringConverter converter = getConverterFor(componentType);

		for (int i = 0; i < jsonArray.length(); i++) {
			if (jsonArray.isNull(i)) {
				continue;
			} else if (converter != null) {
				Array.set(array, i,
						convertBack(jsonArray.getString(i), converter));
			}
			if (componentType == int.class) {
				Array.setInt(array, i, jsonArray.getInt(i));
			} else if (componentType == boolean.class) {
				Array.setBoolean(array, i, jsonArray.getBoolean(i));
			} else if (componentType == long.class) {
				Array.setLong(array, i, jsonArray.getLong(i));
			} else if (componentType == double.class) {
				Array.setDouble(array, i, jsonArray.getDouble(i));
			} else if (componentType == float.class) {
				Array.setFloat(array, i, (float) jsonArray.getDouble(i));
			} else if (componentType == short.class) {
				Array.setShort(array, i, (short) jsonArray.getInt(i));
			} else if (componentType == byte.class) {
				Array.setByte(array, i, (byte) jsonArray.getInt(i));
			} else if (componentType == String.class) {
				Array.set(array, i, jsonArray.getString(i));
			} else if (componentType.isArray()) {
				Array.set(array, i, getArray(jsonArray.getJSONArray(i), componentType));
			} else if (!componentType.isPrimitive()) {
				Array.set(array, i, getObject(jsonArray.getJSONObject(i), componentType));
			}

		}

		return array;
	}
	
	@SuppressWarnings("unchecked")
	private List<?> getList(JSONArray jsonArray, Class<?> listType, Class<?>elementType)
			throws JSONException, InvalidOperationException {
		List<Object> list;
		try {
			list = (List<Object>) listType.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException();
		} catch (IllegalAccessException e) {
			throw new RuntimeException();
		}

		for (int i = 0; i < jsonArray.length(); i++) {
			if (jsonArray.isNull(i)) {
				list.add(null);
				continue;
			}

			Object item = jsonArray.get(i);		
			if (elementType == String.class) {
				list.add(item);
				continue;
			}
			IStringConverter converter = getConverterFor(elementType);
			if (converter != null) {
				list.add(convertBack(jsonArray.getString(i), converter));
			} else if ((item instanceof Number) || (item instanceof Boolean)) {
				list.add(item);
			} else if (elementType.isArray()) {
				list.add(getArray(jsonArray.getJSONArray(i), elementType));
			} else if(elementType == ArrayList.class){
				throw new InvalidOperationException("cant deserialize inner list");
			}
			else {			
				list.add(getObject(jsonArray.getJSONObject(i), elementType));
			}
		}
		return list;
	}
	
	private ArrayList<Object> getArrayList(JSONArray jsonArray, Class<?>elementType)
			throws JSONException, InvalidOperationException {
		ArrayList<Object> list = new ArrayList<Object>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			if (jsonArray.isNull(i)) {
				list.add(null);
				continue;
			}

			Object item = jsonArray.get(i);		
			if (elementType == String.class) {
				list.add(item);
				continue;
			}
			IStringConverter converter = getConverterFor(elementType);
			if (converter != null) {
				list.add(convertBack(jsonArray.getString(i), converter));
			} else if ((item instanceof Number) || (item instanceof Boolean)) {
				list.add(item);
			} else if (elementType.isArray()) {
				list.add(getArray(jsonArray.getJSONArray(i), elementType));
			} else if(elementType == ArrayList.class){
				//throw new InvalidOperationException("cant deserialize inner list");
				list.add(getArrayList(jsonArray.getJSONArray(i), elementType));
			}
			else {			
				list.add(getObject(jsonArray.getJSONObject(i), elementType));
			}
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.toolbox.v9.serialization.IStringSerializer#serialize(java.lang.Object
	 * )
	 */
	@Override
	public String serialize(Object object) throws InvalidOperationException {
		if (object == null) {
			return null;
		}

		Class<?> type = object.getClass();
		try {

		if (hasConverterFor(type)) {
			return convertToString(object, type);
		} else if (type == String.class) {
			return object.toString();
		} else if (object instanceof Number) {
			return object.toString();
		} else if (type == Boolean.class) {
			return object.toString();
		} else if (type.isPrimitive()) {
			return object.toString();
		} else if( object instanceof ArrayList<?>){
			return getJsonArrayFromList((List<?>) object).toString();
		}
			return type.isArray() ? getJsonArray(object).toString() : getJSon(
					object).toString();
		} catch (JSONException e) {
			Log.d(getClass().getName(), e.getMessage());
			throw new RuntimeException(e);
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.toolbox.v9.serialization.IStringSerializer#deserialize(java.lang.
	 * Class, java.lang.String)
	 */
	@Override
	public Object deserialize(Class<?> type, String jsonString)
			throws InvalidOperationException {

		if (hasConverterFor(type)) {
			return convertBack(jsonString, type);
		} else if (type == String.class) {
			if (jsonString != null) {
				jsonString = jsonString.trim();
				int length = jsonString.length();
				if (length > 2 && jsonString.charAt(0) == '"'
						&& jsonString.charAt(length - 1) == '"') {
					jsonString = jsonString.substring(1, length - 1);
				}
			}
			return jsonString;
		} else if (TextUtils.isEmpty(jsonString) || jsonString.equals("null") ) {
			if (type.isPrimitive()) {
				Log.e(getClass().getName(), "Can not convert a primitive type '"+type.getSimpleName()+"' to null");
				throw new InvalidOperationException("Can not convert a primitive type '"+type.getSimpleName()+"' to null");
			}
			return null;
		}

		if (type == int.class) {
			return Integer.parseInt(jsonString);
		} else if (type == boolean.class) {
			return Boolean.parseBoolean(jsonString);
		} else if (type == long.class) {
			return Long.parseLong(jsonString);
		} else if (type == double.class) {
			return Double.parseDouble(jsonString);
		} else if (type == short.class) {
			return Short.parseShort(jsonString);
		} else if (type == float.class) {
			return Float.parseFloat(jsonString);
		} else if (type == byte.class) {
			return Byte.parseByte(jsonString);
		}

		try {
			if (type.isArray()) {
				JSONArray jsonArray = new JSONArray(jsonString);
				return getArray(jsonArray, type);
			}else if(type == ArrayList.class){
				throw new InvalidOperationException("Deserializing ArrayList is not supported. Use deserializeList instead");
			}			
			else if (!type.isPrimitive()) {
				JSONObject json = new JSONObject(jsonString);
				return getObject(json, type);
			} else {
				throw new RuntimeException("Can not deserialize the type "
						+ type.getName());
			}
		} catch (JSONException e) {
			Log.d(getClass().getName(), e.getMessage(), e);
			throw new InvalidOperationException(e.getMessage(), e);
		}
	}
	
	public void deserialize(Object o, String jsonString) throws InvalidOperationException {
		Class<?> objectType = o.getClass();
        if (TextUtils.isEmpty(jsonString) || jsonString.equals("null") ){
            return;
        }

		Field[] fields = objectType.getFields();
		JSONObject json;
		try {
			json = new JSONObject(jsonString);


			for (Field field : fields) {
				int modifier = field.getModifiers();
				if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)
						|| Modifier.isPrivate(modifier)
						|| Modifier.isProtected(modifier)
						|| field.isAnnotationPresent(NotSerializable.class)) {
					continue;
				}

				Class<?> fieldType = field.getType();
				String fieldName = field.getName();
				try {
					if(json.has(field.getName()) && json.isNull(fieldName)){
						field.set(o, null);
					}
					else if (json.has(field.getName()) && !json.isNull(fieldName)) {
						if (fieldType == Integer.class) {
							field.set(o, json.getInt(fieldName)); 
						}else if (fieldType == int.class) {
							field.setInt(o, json.getInt(fieldName)); 
						}else if (fieldType == Boolean.class) {
							field.set(o, json.getBoolean(fieldName));
						}else if (fieldType == boolean.class) {
							field.setBoolean(o, json.getBoolean(fieldName));
						}else if (fieldType == Double.class) {						
							field.set(o, json.getDouble(fieldName));
						}else if (fieldType == double.class) {						
							field.setDouble(o, json.getDouble(fieldName));
						}else if (fieldType == Long.class) {
							field.set(o, json.getLong(fieldName));
						}else if (fieldType == long.class) {
							field.setLong(o, json.getLong(fieldName));
						}else if (fieldType == Short.class) {
							field.set(o, (short) json.getInt(fieldName));
						} else if (fieldType == short.class) {
							field.setShort(o, (short) json.getInt(fieldName));
						}else if (fieldType == Float.class) {
							field.set(o, (float) json.getDouble(fieldName));
						} else if (fieldType == float.class) {
							field.setFloat(o, (float) json.getDouble(fieldName));
						} else if (fieldType == Byte.class) {
							field.set(o, (byte) json.getInt(fieldName));
						} else if (fieldType == byte.class) {
							field.setByte(o, (byte) json.getInt(fieldName));
						} else if (fieldType == String.class) {
							field.set(o, json.getString(fieldName));
						} else if (fieldType.isArray()) {
							JSONArray jsonArray = json.getJSONArray(fieldName);
							field.set(o, getArray(jsonArray, fieldType));
						} else if (fieldType == ArrayList.class) {
							JSONArray jsonArray = json.getJSONArray(fieldName);
							DataMember annot = field.getAnnotation(DataMember.class);
							if(annot == null || annot.listType()==null)
								throw new InvalidOperationException("List type unknow. Use DataMember annotation specifying the listType");
							field.set(o, getList(jsonArray, fieldType, annot.listType()));
						} else if (hasConverterFor(fieldType)) {
							field.set(o, convertBack(json.getString(fieldName),fieldType));
						} else {
							field.set( o, getObject(json.getJSONObject(fieldName), fieldType));
						}
					}
				} catch (IllegalAccessException e) {
					Log.e(getClass().getName(), e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					Log.e(getClass().getName(), e.getMessage(), e);
				}
			}

//			Method[] methods = objectType.getMethods();
//			for (int i = 0; i < methods.length; i++) {
//				Method method = methods[i];
//				String name = method.getName();
//				int modifier = method.getModifiers();
//				if (!method.isAnnotationPresent(DataMember.class)
//						|| (Modifier.isStatic(modifier) || Modifier
//								.isFinal(modifier))) {
//					continue;
//				}
//				if (!name.startsWith("set")) {
//					continue;
//				}
//
//				Class<?>[] paramTypes = method.getParameterTypes();
//				if (paramTypes.length != 1) {
//					throw new RuntimeException(
//							"method " + name+ "is not a property. It must begin with the preffix 'set' and do not receive any arguments");
//				}
//
//				Class<?> paramType = paramTypes[0];
//				name = name.substring(3);
//
//				try {
//
//					if (json.has(name) && !json.isNull(name)) {
//						if (paramType == Integer.class || paramType == int.class) {
//							method.invoke(o, json.getInt(name));
//						} else if (paramType == Boolean.class
//								|| paramType == boolean.class) {
//							method.invoke(o, json.getBoolean(name));
//						} else if (paramType == Double.class
//								|| paramType == double.class) {
//							method.invoke(o, json.getDouble(name));
//						} else if (paramType == Long.class
//								|| paramType == long.class) {
//							method.invoke(o, json.getLong(name));
//						} else if (paramType == Short.class
//								|| paramType == short.class) {
//							method.invoke(o, (short) json.getInt(name));
//						} else if (paramType == Float.class
//								|| paramType == float.class) {
//							method.invoke(o, (float) json.getDouble(name));
//						} else if (paramType == Byte.class
//								|| paramType == byte.class) {
//							method.invoke(o, (byte) json.getInt(name));
//						} else if (paramType == String.class) {
//							method.invoke(o, json.getString(name));
//						} else if (paramType.isArray()) {
//							JSONArray jsonArray = json.getJSONArray(name);
//							method.invoke(o, getArray(jsonArray, paramType, null));
//						} else if (hasConverterFor(paramType)) {
//							method.invoke(o,
//									convertBack(json.getString(name), paramType));
//						} else if(paramType == ArrayList.class){
//							DataMember annot = method.getAnnotation(DataMember.class);
//							if(annot == null || annot.listType()==null)
//								throw new InvalidOperationException("List type unknow. Use DataMember annotation specifying the listType");
//							method.invoke(o, getList(json.getJSONArray(name), paramType, annot.listType(), null));
//						}
//						else {
//							method.invoke(o, getObject(json.getJSONObject(name), paramType, null));
//						}
//					}
//				} catch (IllegalAccessException e) {
//					Log.e(getClass().getName(), e.getMessage(), e);
//				} catch (IllegalArgumentException e) {
//					Log.e(getClass().getName(), e.getMessage(), e);
//				} catch (InvocationTargetException e) {
//					Log.e(getClass().getName(), e.getMessage(), e);
//				}
//			}
		} catch (JSONException e) {
			Log.e(getClass().getName(), e.getMessage(), e);
			throw new InvalidOperationException(e.getMessage(), e);
		}
	}

	
	@SuppressWarnings("unchecked")
	public <T> T[] deserializeArray(Class<T>componentType, String jsonString){
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			return (T[]) getArrayFromComponent(jsonArray, componentType);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		} catch (JSONException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}
	}

	
	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> deserializeList(Class<T>elementType, String jsonString){
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			return (ArrayList<T>)getArrayList(jsonArray, elementType);
		} catch (JSONException e) {
			throw new InvalidOperationException(e.getMessage(), e);
		}
	}

	public static String serializeObject(Object value) {
        JSonSerializer js = new JSonSerializer();
		try {
			return js.serialize(value);
		} catch (InvalidOperationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserializeObject(Class<T> type, String jsonString) {
		IStringSerializer js = new JSonSerializer();
		try {
			return (T) js.deserialize(type, jsonString);
		} catch (InvalidOperationException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
