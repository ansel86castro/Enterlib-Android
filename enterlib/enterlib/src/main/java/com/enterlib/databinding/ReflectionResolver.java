package com.enterlib.databinding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import android.text.TextUtils;
import android.util.Log;

public final class ReflectionResolver {

	static final HashMap<Class<?>, TypeDescriptor> typeDescriptors = new HashMap<Class<?>, ReflectionResolver.TypeDescriptor>();

	public static class TypeDescriptor {
		static final String DEBUG_TAG = TypeDescriptor.class.getSimpleName();
		
		public Class<?> Type;
		HashMap<String, java.lang.reflect.Field> fields;
		HashMap<String, java.lang.reflect.Method> properties;

		public TypeDescriptor(Class<?> type) {
			this.Type = type;
			java.lang.reflect.Field[] typefields = type.getFields();

			this.fields = new HashMap<String, java.lang.reflect.Field>(typefields.length);
			
			for (int i = 0; i < typefields.length; i++) {
				java.lang.reflect.Field typeField = typefields[i];
				int modifier = typeField.getModifiers();
				if (Modifier.isStatic(modifier)) {
					continue;
				}

				fields.put(typeField.getName(), typeField);
			}

			Method[] methods = type.getMethods();

			properties = new HashMap<String, Method>(methods.length);
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				String name = method.getName();
				int modifier = method.getModifiers();
				if (Modifier.isStatic(modifier)
						|| method.getDeclaringClass() == Object.class) {
					continue;
				}

				if (name.startsWith("get") || name.startsWith("is")) {
					if (method.getParameterTypes().length != 0) {
						continue;
					}
					properties.put(name, method);
				} else if (name.startsWith("set")) {
					if (method.getParameterTypes().length != 1) {
						continue;
					}
					properties.put(name, method);
				}
			}
		}

		public void setValue(String propertyName, Object object, Object value) {
			java.lang.reflect.Field field = fields.get(propertyName);
			if (field != null) {
				try {
					field.set(object, value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			} else {

				Method method = properties.get("set" + propertyName);
				if (method == null) {
					Log.d(DEBUG_TAG, "Property '" + propertyName
							+ "' not found in " + Type.getName());
					return;
				}

				try {
					method.invoke(object, value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}

		public Object getValue(String propertyName, Object object) {
			java.lang.reflect.Field field = fields.get(propertyName);
			if (field != null) {
				try {
					return field.get(object);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			} else {

				Method method = properties.get("get" + propertyName);
				if (method == null) {
					method = properties.get("is" + propertyName);
				}

				if (method == null) {
					Log.d(DEBUG_TAG, "Property '" + propertyName
							+ "' not found in " + Type.getName());
					return null;
				}

				try {
					return method.invoke(object);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e.getMessage(), e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}

		public boolean containsProperty(String propertyName, Object object) {
			java.lang.reflect.Field field = fields.get(propertyName);
			if (field != null) {
				return true;
			} else {

				Method method = properties.get("get" + propertyName);
				if (method != null) {
					return true;
				}

				method = properties.get("is" + propertyName);
				if (method != null) {
					return true;
				}
				return false;
			}
		}
		
		public java.lang.reflect.Field getField(String field){
			return fields.get(field);
		}
		
		public java.lang.reflect.Method getMethod(String method){
			return properties.get(method);
		}
		
		public int getFieldsCount(){
			return fields.size();					
		}
	
		public void getFields(java.lang.reflect.Field[] fields){
			this.fields.values().toArray(fields);
		}
		
		public Iterable<java.lang.reflect.Field> getFields(){
			return fields.values();
		}
		
		public int getMethodCount(){
			return properties.size();					
		}
	
		public void getMethods(java.lang.reflect.Method[] methods){
			this.properties.values().toArray(methods);
		}
		
		public Iterable<java.lang.reflect.Method> getMethods(){
			return properties.values();
		}
	
	}

	public static Object getValue(String propertyName, Object object) {
		if (propertyName == null || TextUtils.isEmpty(propertyName)) {
			throw new NullPointerException("propertyName");
		}

		if (object == null) {
			return null;
		}

		Class<?> type = object.getClass();
		TypeDescriptor descriptor = typeDescriptors.get(type);
		if (descriptor == null) {
			descriptor = new TypeDescriptor(type);
			typeDescriptors.put(type, descriptor);
		}
		return descriptor.getValue(propertyName, object);
	}
	
	public static TypeDescriptor getDescriptor(Class<?>type){
		
		TypeDescriptor descriptor = typeDescriptors.get(type);
		if (descriptor == null) {
			descriptor = new TypeDescriptor(type);
			typeDescriptors.put(type, descriptor);
		}
		return descriptor;
	}

	public static boolean containsProperty(String propertyName, Object object) {
		if (propertyName == null || propertyName.isEmpty()) {
			throw new NullPointerException("propertyName");
		}

		if (object == null) {
			return false;
		}

		Class<?> type = object.getClass();
		TypeDescriptor descriptor = typeDescriptors.get(type);
		if (descriptor == null) {
			descriptor = new TypeDescriptor(type);
			typeDescriptors.put(type, descriptor);
		}
		return descriptor.containsProperty(propertyName, object);
	}

	public static void setValue(String propertyName, Object object, Object value) {
		if (propertyName == null || TextUtils.isEmpty(propertyName)) {
			throw new NullPointerException("propName");
		}

		if (object == null) {
			return;
		}

		Class<?> type = object.getClass();
		TypeDescriptor descriptor = typeDescriptors.get(type);
		if (descriptor == null) {
			descriptor = new TypeDescriptor(type);
			typeDescriptors.put(type, descriptor);
		}
		descriptor.setValue(propertyName, object, value);
	}
}
