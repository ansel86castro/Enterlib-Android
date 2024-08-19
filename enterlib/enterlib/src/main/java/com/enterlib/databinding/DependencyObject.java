package com.enterlib.databinding;

import java.util.HashMap;

import android.annotation.TargetApi;
import android.os.Build;

import com.enterlib.fields.Field;

public class DependencyObject extends NotifyPropertyChanged {
	static final HashMap<Class<?>, HashMap<String, DependencyProperty>> properties = new HashMap<Class<?>, HashMap<String, DependencyProperty>>();

	public static interface IGet<T, V> {
		V get(T host);
	}

	public static interface ISet<T, V> {
		void set(T host, V value);
	}

	public static DependencyProperty registerProperty(String name,
			Class<?> hostType, DependencyProperty property) {
		HashMap<String, DependencyProperty> hostProperties = properties
				.get(hostType);
		if (hostProperties == null) {
			hostProperties = new HashMap<String, DependencyProperty>();
			properties.put(hostType, hostProperties);
		}
		hostProperties.put(name, property);
		return property;
	}

	@SuppressWarnings("unchecked")
	public static <T> BindingProperty<T> registerProperty(Class<?> hostType,
			BindingProperty<T> property) {
		return (BindingProperty<T>) registerProperty(property.getName(),
				hostType, property);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Field> BindingProperty<T> registerProperty(Class<?> hostType, String propName) {
		return (BindingProperty<T>) registerProperty(propName, hostType,
				new BindingProperty<T>(propName) {
					@Override
					public void set(T object, ExpressionMember member,
							BindingResources dc) {
						object.addBindingHandler(BindingHandlerReflection.createHandler(member, dc, object, BindingHandlerReflection.TARGET_FIELD));
					}
				});
	}

	@SuppressWarnings("unchecked")
	public static <T extends Field> BindingProperty<T> registerCommand(Class<?> hostType, String propName) {
		return (BindingProperty<T>) registerProperty(propName, hostType,
				new BindingProperty<T>(propName) {
					@Override
					public void set(T object, ExpressionMember member,
							BindingResources dc) {
						object.addBindingHandler(new CommandHandlerClick(member.getValueString()));
					}
				});
	}

	public static <T, V> DependencyProperty registerProperty(String name,
			Class<T> hostType, Class<V> propertyType, IGet<T, V> get,
			ISet<T, V> set) {
		return registerProperty(name, hostType, new RelayProperty<T, V>(
				propertyType, name, get, set));
	}

	public <V> void setProperty(DependencyProperty property, V value) {
		property.set(this, value);
	}

	public static DependencyProperty getDeclaredProperty(Class<?> type,
			String name) {
		HashMap<String, DependencyProperty> hostProperties = properties
				.get(type);
		if (hostProperties == null) {
			return null;
		}
		return hostProperties.get(name);
	}

	public static DependencyProperty getProperty(Class<?> type, String name) {
		DependencyProperty prop = null;

		HashMap<String, DependencyProperty> hostProperties = properties
				.get(type);
		if (hostProperties != null) {
			prop = hostProperties.get(name);
		}

		if (prop == null) {
			Class<?> superType = type.getSuperclass();
			if (superType == null || superType == Object.class) {
				return null;
			}

			return getProperty(superType, name);
		}

		return prop;
	}

	public DependencyProperty getProperty(String name) {
		return getProperty(this.getClass(), name);
	}

	public void setValue(DependencyProperty p, Object value) {
		p.set(this, value);
		onPropertyChange(p.getName());
	}

	public Object getValue(DependencyObject p) {
		return p.getValue(this);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	static class RelayProperty<T, V> extends DependencyProperty {
		private final IGet<T, V> _get;
		private final ISet<T, V> _set;

		public RelayProperty(Class<V> type, String name, IGet<T, V> getHandler,
				ISet<T, V> setHanler) {
			super(type, name);
			this._get = getHandler;
			this._set = setHanler;
		}

		@Override
		public Object get(DependencyObject object) {
			return _get.get((T) object);
		}

		@Override
		public void set(DependencyObject object, Object value) {
			if (_set == null) {
				throw new UnsupportedOperationException("Property " + getName()
						+ " is read-only");
			}

			_set.set((T) object, (V) value);
			;
		}

		@Override
		public boolean isReadOnly() {
			return _set != null;
		}
	}
}
