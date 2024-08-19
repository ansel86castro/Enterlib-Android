package com.enterlib.fields;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.enterlib.StringUtils;
import com.enterlib.annotations.NotSerializable;
import com.enterlib.converters.IValueConverter;
import com.enterlib.databinding.BindingExpression;
import com.enterlib.databinding.BindingHandler;
import com.enterlib.databinding.BindingHandlerReflection;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.DependencyObject;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.databinding.INotifyPropertyChanged;
import com.enterlib.databinding.IPropertyChangedListener;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.fields.Form.SaveStore;
import com.enterlib.serialization.IStringSerializer;
import com.enterlib.serialization.JSonSerializer;
import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IWorkPost;
import com.enterlib.validations.IValidator;
import com.enterlib.validations.IValueValidator;
import com.enterlib.validations.validators.EmailValidator;

public abstract class Field extends DependencyObject 
	implements IValidator, IPropertyChangedListener {
	
	public static final String SELF = "this";
	public static final int TwoWay = 0;
	public static final int OneWay = 1;
	public static final int OneTime = 2;

    public static final String TwoWayString = "TwoWay";

    public static final String OneTimeString = "OneTime";

    public static final String OneWayString = "OneWay";

	class ErrorRunable implements Runnable {
		@Override
		public void run() {
			onSetErrorMessage(errorMessage);
		}
	}

	public static final BindingProperty<Field> ValueProperty = registerProperty(Field.class, 
			new BindingProperty<Field>("Value") {
			@Override
			public void set(Field object, ExpressionMember value,
							BindingResources dc) {
						object.valueBinding = value.getValueString();
			}
	});
	public static final BindingProperty<Field> RequiredProperty = registerProperty(Field.class, 
			new BindingProperty<Field>("Required") {
			@Override
			public void set(Field object, ExpressionMember value,
						BindingResources dc) {
					object.required = value.isValueTrue();
		}
	});
	
	public static final BindingProperty<Field> HideEmptyProperty = registerProperty(Field.class, 
			new BindingProperty<Field>("HideEmpty") {
			@Override
			public void set(Field object, ExpressionMember value,
						BindingResources dc) {
					object.isHideEmpty = value.isValueTrue();
		}
		public Object get(DependencyObject object) {
			return ((Field)object).isHideEmpty;
		};
	});

	public static final BindingProperty<Field> RestorableProperty = registerProperty(
			Field.class, new BindingProperty<Field>("Restorable") {
				@Override
		public void set(Field object, ExpressionMember value,
						BindingResources dc) {
					object.isRestorable = value.isValueTrue();
		}
	});

	public static final BindingProperty<Field> DisplayProperty = registerProperty(
			Field.class, new BindingProperty<Field>("Display") {
				@Override
		public void set(Field object, ExpressionMember value,
						BindingResources dc) {
					object.display = (String) dc.get(value.getValueString());
		}
	});

	public static final BindingProperty<Field> DisplayResProperty = registerProperty(
			Field.class, new BindingProperty<Field>("DisplayRes") {
				@Override
		public void set(Field object, ExpressionMember value,BindingResources dc) {
					if (dc != null) {
						object.display = object.getString(dc, value.getValueString());
					}
			}
	});
	
	public static final BindingProperty<Field> LabelProperty = registerProperty(
			Field.class, new BindingProperty<Field>("Label") {
				@Override
		public void set(Field object, ExpressionMember value,BindingResources dc) {
					if (dc != null) {
						object.display = object.getString(dc, value.getValueString());
					}
			}
	});

	public static final BindingProperty<Field> ErrorMessageProperty = registerProperty(
			Field.class, new BindingProperty<Field>("ErrorMessage") {
				@Override
		public void set(Field object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.errorMessage = (String) dc.get(member
								.getValueString());
			}
				}
	});

	public static final BindingProperty<Field> ErrorMessageResProperty = registerProperty(
			Field.class, new BindingProperty<Field>("ErrorMessageRes") {
				@Override
		public void set(Field object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.errorMessage = object.getString(dc,
								member.getValueString());
			}
				}
	});

	public static final BindingProperty<Field> ConverterProperty = registerProperty(
			Field.class, new BindingProperty<Field>("Converter") {
				@Override
		public void set(Field object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.valueConverter = (IValueConverter) dc.get(member
								.getValueString());
					}

					if (object.valueConverter == null) {
				Object viewModel = object.getViewModel();
				if (viewModel != null) {
					object.valueConverter = (IValueConverter) ReflectionResolver
									.getValue(member.getValueString(), viewModel);
				}
			}
		}
	});

	public static final BindingProperty<Field> RequiredMessageProperty = registerProperty(
			Field.class, new BindingProperty<Field>("RequiredMessage") {
				@Override
		public void set(Field object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.requiredMessage = (String) dc.get(member
								.getValueString());
			}
				}
	});

	public static final BindingProperty<Field> RequiredMessageResProperty = registerProperty(
			Field.class, new BindingProperty<Field>("RequiredMessageRes") {
				@Override
		public void set(Field object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.requiredMessage = object.getString(dc,
								member.getValueString());
			}
				}
	});

	public static final BindingProperty<Field> ValidatorsProperty = registerProperty(
			Field.class, new BindingProperty<Field>("Validators") {
				@Override
		public void set(Field object, ExpressionMember member,
						BindingResources dc) {
			Object viewModel = object.getViewModel();
			List<?> list = member.getValueList();
			int count = list.size();
			if (count > 0) {
				for (int i = 0; i < count; i++) {
					Object value = list.get(i);
					if (value instanceof String) {
						String name = (String) value;
						IValueValidator validator = null;
						if (dc != null) {
							validator = (IValueValidator) dc.get(name);
						}
						if (validator == null && viewModel != null) {
							validator = (IValueValidator) ReflectionResolver
											.getValue(name, viewModel);
						}
						if (validator == null) {
							throw new RuntimeException(
											"Not Found IValueValidador with name= "
													+ name);
						}

								object.addValueValidator(validator);
					}
				}
			}
		}
			});

	public static final BindingProperty<Field> ClickCommandProperty = registerCommand(Field.class, 
			"ClickCommand");

	// public static final BindingProperty<Field> VisibilityProperty =
	// registerProperty(Field.class,"Visibility");

	public static final BindingProperty<Field> BackgroundColorProperty = registerProperty(
			Field.class, "BackgroundColor");

	public static final BindingProperty<Field> BackgroundProperty = registerProperty(
			Field.class, "Background");
	
	public static final BindingProperty<Field> VisibleProperty = registerProperty(Field.class, "Visible");

	//Validation shortcut binding properties
	public static final BindingProperty<Field> EmailProperty = registerProperty(Field.class, 
			new BindingProperty<Field>("Email") {
			@Override
			public void set(Field object, ExpressionMember value,
						BindingResources dc) {
					if(value.isValueTrue()){
						object.addValueValidator(new EmailValidator("*"));
					}
		}
	});

    public static final BindingProperty<Field> AsyncProperty = registerProperty(Field.class,
            new BindingProperty<Field>("Async") {
                @Override
                public void set(Field object, ExpressionMember value,BindingResources dc) {
                    object.setValueAsync = value.isValueTrue();
                }
            });

    public static final BindingProperty<Field> ModeProperty = registerProperty(Field.class,
            new BindingProperty<Field>("Mode") {
                @Override
                public void set(Field object, ExpressionMember value, BindingResources dc) {
                    String modeStr  = value.getValueString();
                    if(StringUtils.isNullOrWhitespace(modeStr))
                        throw  new InvalidOperationException("Mode only supports the following values (TwoWay,OneWay,OneTime)");

                    if(modeStr.equalsIgnoreCase(TwoWayString)){
                        object.mode = TwoWay;
                    }else if(modeStr.equalsIgnoreCase(OneWayString)){
                        object.mode = OneWay;
                    }else if(modeStr.equalsIgnoreCase(OneTimeString)){
                        object.mode = OneTime;
                    }else {
                        throw  new InvalidOperationException("Mode only supports the following values (TwoWay,OneWay,OneTime)");
                    }
                }
            });

	
	public Field() {
	}

	/**
	 * @param view
	 *            The UI View
	 * */
	public Field(View view) {
		this();
		this.view = view;

		if (view != null) {
			this.id = view.getId();
			requiredMessage = view.getResources().getString(
					com.enterlib.R.string.required);
		}
	}

	public Field(View view, String valueBinding, String display,
			boolean required) {
		this(view, display, required);
		this.valueBinding = valueBinding;
	}

	/**
	 * @param view
	 *            The UI View
	 * @param display
	 *            The UI name of the field. This is how the user will identify
	 *            this field in the UI
	 * @param required
	 *            Indicates if the value of the field is required
	 * */
	public Field(View view, String display, boolean required) {
		this(view);
		this.display = display;
		this.required = required;
	}

	public Field(View view, String valueBinding) {
		this(view);
		this.valueBinding = valueBinding;
	}

	/**
	 * @param view
	 *            The UI View
	 * @param required
	 *            Indicates if the value of the field is required
	 * */
	public Field(View view, boolean required) {
		this(view);
		this.required = required;
	}

	// --------- Properties------------------------------

	/**
	 * Adds a {@link IValueValidator} that validates the {@link View}'s value
	 * 
	 * @param valueValidator
	 *            The Validator
	 * */
	public Field addValueValidator(IValueValidator valueValidator) {
		if (validators == null) {
			validators = new ArrayList<IValueValidator>();
		}
		validators.add(valueValidator);
		return this;
	}

	/**
	 * Returns the number of {@link IValueValidator} instances
	 * */
	public int getValidatorsCount() {
		if (validators == null) {
			return 0;
		}
		return validators.size();
	}

	
	public boolean isVisible(){
		return view.getVisibility() == View.VISIBLE;
	}
	
	public void setVisible(boolean value){
		view.setVisibility(value ? View.VISIBLE : View.GONE);
	}
		

	public void setBackgroundColor(int color) {
		view.setBackgroundColor(color);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public int getBackgroundColor() {
		Drawable dr = getBackground();
		if (dr instanceof ColorDrawable) {
			ColorDrawable c = (ColorDrawable) dr;
			return c.getColor();
		}
		return 0;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setBackground(Drawable dr) {
		view.setBackground(dr);
	}

	public Drawable getBackground() {
		return view.getBackground();
	}

	public boolean isFocused() {
		return view.isFocused();
	}

	public boolean isShown() {
		return view.isShown();
	}

	public void setEnabled(boolean enabled) {
		view.setEnabled(enabled);
		view.setClickable(enabled);
	}

	public void setFocusable(boolean focusable) {
		view.setFocusable(focusable);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public float getAlpha() {
		return view.getAlpha();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setAlpha(float alpha) {
		view.setAlpha(alpha);
	}

	public boolean isSelected() {
		return view.isSelected();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setActivated(boolean activated) {
		view.setActivated(activated);
	}

	public Context getContext() {
		return view != null ? view.getContext() : null;
	}

	/**
	 * Returns the {@link IValueValidator} at the given index
	 * */
	public IValueValidator getValueValidator(int index) {
		if (validators == null) {
			throw new IndexOutOfBoundsException();
		}

		return validators.get(index);
	}

	/**
	 * Sets the field error message. The field will automatically display the
	 * error message in the UI
	 * */
	public final void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		onSetErrorMessage(errorMessage);
	}

	/**
	 * returns the errorMessage
	 */
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * returns the {@link View}
	 * */
	public View getView() {
		return view;
	}

	/**
	 * sets the view
	 * 
	 * @param view
	 */
	public void setView(View view) {
		if (this.view == view) {
			return;
		}

		this.view = view;
		onViewChanged();
	}

	/** Take some action after the view has changed */
	protected void onViewChanged() {
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public Object getViewModel() {
		return form != null ? form.getViewModel() : null;
	}

	public BindingResources getBindingResources() {
		return form != null ? form.getBindingResources() : null;
	}

	/** returns the name displayed in the UI */
	public String getDisplay() {
		return display;
	}

	/** sets the name displayed in the UI */
	public Field setDisplay(String display) {
		this.display = display;
		return this;
	}

	/** returns the list of {@link IValueValidator} */
	List<IValueValidator> getValidators() {
		return validators;
	}

	/**
	 * returns true if the {@link Field}'s value must be not null or not empty
	 * nor blank if the value is a {@link String}
	 */
	public boolean isRequired() {
		return required;
	}

	public Field setRequired(boolean required) {
		this.required = required;
		return this;
	}

	/**
	 * returns the message when the required validation fails
	 * */
	public String getRequiredMessage() {
		return requiredMessage;
	}

	/**
	 * sets the message for the required validation. By default it is
	 * 'Requerido'
	 */
	public Field setRequiredMessage(String requiredMessage) {
		this.requiredMessage = requiredMessage;
		return this;
	}

	public IValueConverter getValueConverter() {
		return valueConverter;
	}

	/**
	 * Sets the value converted. The Field will call the
	 * {@code Object convert(Object value)} method on the valueConverter to
	 * convert the value when {@code setValue(Object value)} is called. On the
	 * other hand the Field will call {@code Object convertBack(Object value)}
	 * on the valueConverter when {@code Object getValue()} is called
	 */
	public Field setValueConverter(IValueConverter valueConverter) {
		this.valueConverter = valueConverter;
		return this;
	}

	// ----------------------------------------------------------------

	/**
	 * Validates the value of the field and display the error in the View
	 */
	@Override
	public boolean validate() {
        if(mode != TwoWay)
            return true;

		Object value;
		try {
			value = getValue();
		} catch (ConversionFailException e) {
			e.printStackTrace();
			Log.d("Field", "validate .Invalid value"); //$NON-NLS-1$ //$NON-NLS-2$
			setErrorMessage(e.getMessage());
			return false;
		}

		if (required) {
			Object viewValue = getViewValue();
			if ((viewValue == null) || (viewValue instanceof String && StringUtils.isNullOrWhitespace((String) viewValue)) || value == null) {
				setErrorMessage(requiredMessage);
				return false;
			}
		}
		if (validators != null) {
			for (IValueValidator validator : validators) {
				boolean result = validator.validateValue(value);
				if (result == false) {
					// this validator failed
					String errorMessage = validator.getErrorMessage();
					setErrorMessage(errorMessage);
					return false;
				}
			}
		}

		// All validators passed
		setErrorMessage(null);
		return true;
	}

	/**
	 * Same as {@code validate} but this method must be called from a
	 * {@link Thread} different of the UI {@link Thread}
	 */
	@Override
	public boolean validateAsync(Handler handler) {
		Object value;
		try {
			value = getValue();
		} catch (ConversionFailException e) {
			e.printStackTrace();
			Log.d("Field", "validate .Invalid value"); //$NON-NLS-1$ //$NON-NLS-2$
			errorMessage = e.getMessage();
			if (errorPost == null) {
				errorPost = new ErrorRunable();
			}
			handler.post(errorPost);
			return false;
		}

		if (required) {
			Object viewValue = getViewValue();
			if ((viewValue == null)
					|| (viewValue instanceof String && StringUtils
							.isNullOrWhitespace((String) viewValue))) {
				errorMessage = requiredMessage;
				if (errorPost == null) {
					errorPost = new ErrorRunable();
				}
				handler.post(errorPost);
				return false;
			}
		}
		for (IValueValidator validator : validators) {
			boolean result = validator.validateValue(value);
			if (result == false) {
				errorMessage = validator.getErrorMessage();
				if (errorPost == null) {
					errorPost = new ErrorRunable();
				}
				handler.post(errorPost);
				return false;
			}
		}

		// All validators passed
		if (errorMessage != null) {
			// if there are a previus error message then clear it
			errorMessage = null;
			if (errorPost == null) {
				errorPost = new ErrorRunable();
			}
			handler.post(errorPost);
		}
		return true;
	}

	/**
	 * returns the value of the {@link Field}. This is the value of the
	 * {@link View} after conversion calling the {@link IValueConverter}
	 * {@code.convertBack} method
	 * 
	 * @throws ConversionFailException
	 *             if a {@link IValueConverter} is set and the {@link View}'s
	 *             value can not be converted
	 * */
	public Object getValue() throws ConversionFailException {
		Object value = getViewValue();
		if (valueConverter != null) {
			return valueConverter.convertBack(value);
		}
		return value;
	}

	/**
	 * Sets the value the {@link Field}. This method sets the value of the
	 * {@link View} after conversion
	 * 
	 * @throws ConversionFailException
	 *             if a {@link IValueConverter} is set and the {@link View}'s
	 *             value can not be converted
	 * */
	public void setValue(Object value) throws ConversionFailException {
		if (valueConverter != null) {
			value = valueConverter.convert(value);
		}
		if(value instanceof FieldValueProvider<?>){
			final FieldValueProvider<?> valueProvider = (FieldValueProvider<?>) value;
			if(valueProvider.isValueAvailable()){
				setViewValue(valueProvider.getValue());
			}else{
				valueProvider.getValueAsync(this);
			}
		}
		else{
			setViewValue(value);
		}
	}

	/** returns the value casted as {@link Integer} */
	public int getInt() throws ConversionFailException {
		return (Integer) getValue();
	}

	/** returns the value casted as {@link Double} */
	public double getDouble() throws ConversionFailException {
		return (Double) getValue();
	}

	/** returns the value casted as {@link String} */
	public String getString() throws ConversionFailException {
		return (String) getValue();
	}

	/** returns the value casted as {@link Boolean} */
	public boolean getBoolean() throws ConversionFailException {
		return (Boolean) getValue();
	}

	@SuppressWarnings("unchecked")
	public void initBinding(BindingExpression binding, BindingResources bindinResources) {
		int count = binding.size();
		for (int i = 0; i < count; i++) {
			ExpressionMember member = binding.get(i);
			BindingProperty<Field> bindingProperty = (BindingProperty<Field>) getProperty(member.getKey());

			if (bindingProperty == null) {
				if (view != null && ReflectionResolver.containsProperty(member.getKey(), view)) {
					addBindingHandler(BindingHandlerReflection.createHandler(member, bindinResources, this, BindingHandlerReflection.TARGET_VIEW));
				} else {
					throw new RuntimeException(
							"Not BindingProperty found with the name "
									+ member.getKey() + " in the type "
									+ getClass().getName());
				}
			} else {
				bindingProperty.set(this, member, bindinResources);
			}
		}
	}

	public String getString(BindingResources dc, String idName) {
		int resId = findResourceId(dc, idName, "R.string");
		return view.getContext().getString(resId);
	}

	public int findResourceId(BindingResources dc, String idName,
			String resClassName) {
		Class<?> resClass = (Class<?>) dc.get(resClassName);

		int resID = 0;
		if (resClass != null) {
			try {
				java.lang.reflect.Field field = resClass.getField(idName);
				resID = field.getInt(null);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("resource id not found :"
						+ resClassName + "." + idName);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			Resources res = view.getResources();
			int index= resClassName.lastIndexOf('.'); 
			String type =resClassName.substring(index+1);			
			resID = res.getIdentifier(idName, type, view.getContext().getPackageName());					
		}

		if (resID == 0) {
			throw new RuntimeException("resource id not found :" + idName);
		}
		return resID;
	}

	// ------------------ Abstracts-----------------------

	/**
	 * Descendants must implement this for display the error message in the UI
	 * */
	protected abstract void onSetErrorMessage(String errorMessage);

	/**
	 * Descendants must implement this method to return the {@link View}'s value
	 * */
	protected abstract Object getViewValue();

	/**
	 * Descendants must implement this method to set the {@link View}'s value
	 * */
	protected abstract void setViewValue(Object value);

	// ---------------------------------------------------

	public Object resolveSource(Object source) {
		Object dataContext;

		Field parent = parentField;
		while (parent != null) {
			if ((dataContext = parent.getValue()) != null) {
				return dataContext;
			}
			parent = parent.parentField;
		}

		return source;

	}

	@Override
	public void onPropertyChange(Object object, String propertyName) {
		if (valueBinding == null) {
			return;
		}
		
		if(valueBinding.equals(SELF)){
			onValueResolved(object , false);
		}
		
		else if (propertyName.equals(valueBinding)) {			
			Object value = ReflectionResolver.getValue(valueBinding, object);
			onValueResolved(value , false);
		}
		
	}

	public void updateTarget(Object sourceObject) {
		Object source = resolveSource(sourceObject);

        Object viewModel = getViewModel();
        if (valueBinding != null &&
                viewModel != null &&
                source != viewModel &&
                !ReflectionResolver.containsProperty(valueBinding, source)) {
            source = viewModel;
        }

		if (restored) {
			restored = false;
		} else if (valueBinding != null) {
            setValueFromSource(source);
		}

		if (lastSource != source) {
			if(lastSource instanceof INotifyPropertyChanged){
				((INotifyPropertyChanged)lastSource).removePropertyChangeListener(this);
			}

			if((source instanceof INotifyPropertyChanged)) {
				INotifyPropertyChanged notify = (INotifyPropertyChanged) source;
				notify.addPropertyChangeListener(this);
			}
		}

		updateHandlersTarget(source, getBindingResources());
		lastSource = source;
	}

    private void setValueFromSource(final Object source) {
        if (mode == Field.OneTime && binded) {
            return;
        }


        if(valueBinding.equals(SELF)){
            setValue(source);
            binded = true;
            return;
        }

        if(setValueAsync){
            AsyncManager.postAsync(new IWorkPost() {
                Object tempValue;
                @Override
                public boolean runWork() throws Exception {
                    tempValue = ReflectionResolver.getValue(valueBinding, source);
                    return true;
                }

                @Override
                public void onWorkFinish(Exception workException) {
                    binded = true;
                    onValueResolved(tempValue,true);
                }
            });
        }else{
            Object value = ReflectionResolver.getValue(valueBinding, source);
            binded = true;
            onValueResolved(value, false);
        }
    }

    protected void onValueResolved(Object value, boolean asyncLoaded) {
        setValue(value);
        if(isHideEmpty){
            if(value == null || (value instanceof String && StringUtils.isNullOrWhitespace((String) value))){
                setVisible(false);
            }else{
                setVisible(true);
            }
        }
    }

    public void updateSource(Object dataContext) {
		if (dataContext == null || mode != TwoWay) {
			return;
		}

		dataContext = resolveSource(dataContext);

        Object viewModel = getViewModel();
        if (valueBinding != null &&
                viewModel != null &&
                dataContext != viewModel &&
                !ReflectionResolver.containsProperty(valueBinding, dataContext)) {
            dataContext = viewModel;
        }

		if (valueBinding != null) {
			Object value = getValue();
			ReflectionResolver.setValue(valueBinding, dataContext, value);
		}

		updateHandlersSource(dataContext, getBindingResources());
		lastSource = dataContext;
	}

	@Override
	public String toString() {
		return valueBinding != null ? valueBinding : super.toString();
	}

	public void addBindingHandler(BindingHandler handler) {
		if (bindingHandlers == null) {
			bindingHandlers = new ArrayList<BindingHandler>(1);
		}
		bindingHandlers.add(handler);
	}

	public void updateHandlersTarget() {
		updateHandlersTarget(getViewModel(), getBindingResources());
	}

	void updateHandlersTarget(Object source, BindingResources resources) {
		if (bindingHandlers == null) {
			return;
		}
		int size = bindingHandlers.size();
		for (int i = 0; i < size; i++) {
			BindingHandler hanler = bindingHandlers.get(i);
			hanler.updateTarget(this, source, resources);
		}
	}

	void updateHandlersSource(Object source, BindingResources resources) {
		if (bindingHandlers == null) {
			return;
		}

		int size = bindingHandlers.size();
		for (int i = 0; i < size; i++) {
			BindingHandler hanler = bindingHandlers.get(i);
			hanler.updateSource(this, source, resources);
		}
	}

	public Field getParentField() {
		return parentField;
	}

	public void setParentField(Field parentField) {
		this.parentField = parentField;
	}

	public String getValueBinding() {
		return valueBinding;
	}

	public void setValueBinding(String valueBinding) {
		this.valueBinding = valueBinding;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setEnabled(Boolean value) {
		view.setEnabled(value);

	}

	public boolean isEnabled() {
		return view.isEnabled();
	}

	public Object getBidingSource() {
		return lastSource;
	}

	protected String getStoreId() {
		String storeId = getClass().getName()
				+ (id != View.NO_ID ? String.valueOf(this.id) : "");
		if (valueBinding != null) {
			storeId += valueBinding;
		}
		return storeId;
	}

	public void saveState(Bundle outState, IStringSerializer serializer) {
		if (!isRestorable) {
			return;
		}

		if (serializer == null) {
			serializer = new JSonSerializer();
		}

		String id = getStoreId();
		Object value = getViewValue();
		if (value == null) {
			outState.putSerializable(id, (Serializable) value);
		} else {
			Class<?> valueClass = value.getClass();
			Annotation a = valueClass.getAnnotation(NotSerializable.class);
			if (a != null) {
				setRestorable(false);
				return;
			}

			if (valueClass.isArray()) {
				try {
					SaveStore store = new SaveStore(
							serializer.serialize(value), value.getClass());
					outState.putSerializable(id, store);
				} catch (InvalidOperationException e) {
					Log.d("Field",
							"fail to serialize to Json: " + value.toString());
					throw new RuntimeException(e.getMessage(), e);
				}
			} else if (value instanceof Serializable) {
				outState.putSerializable(id, (Serializable) value);
			} else if (value != null) {
				try {
					SaveStore store = new SaveStore(
							serializer.serialize(value), value.getClass());
					outState.putSerializable(id, store);
				} catch (InvalidOperationException e) {
					Log.e("Field", "fail to serialize to Json: " + value.toString());
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	}

	public void restoreState(Bundle savedInstanceState,
			IStringSerializer serializer) {
		if (!isRestorable) {
			return;
		}

		String id = getStoreId();
		if (savedInstanceState.containsKey(id)) {
			restored = true;

			Object value = savedInstanceState.get(id);
			if (value instanceof SaveStore) {
				if (serializer == null) {
					serializer = new JSonSerializer();
				}
				SaveStore store = (SaveStore) value;
				try {
					value = serializer.deserialize(store.Type, store.Json);
					setValue(value);
				} catch (InvalidOperationException e) {
					Log.d("Form", "loadValues fail to deserialize Json: "
							+ store.Type);
					e.printStackTrace();
				}
			} else {
				restoreViewValue(value);
			}
		}
	}

	protected void restoreViewValue(Object value) {
		setViewValue(value);
	}

	public boolean isRestored() {
		return restored;
	}

	public boolean isRestorable() {
		return isRestorable;
	}

	public void setRestorable(boolean value) {
		isRestorable = value;
	}

    public boolean isLoadValueAsync(){
        return setValueAsync;
    }

    public void setLoadValueAsync(boolean value){
        setValueAsync =value;
    }

    private boolean isHideEmpty;
    protected boolean setValueAsync;
	private boolean isRestorable = true;
	protected boolean restored;
	Object lastSource;
	int id;
	View view;
	String valueBinding;
	String display;
	boolean required;
	String errorMessage;
	IValueConverter valueConverter;
	String requiredMessage = "Required";
	ErrorRunable errorPost;
	ArrayList<IValueValidator> validators;
	Form form;
	ArrayList<BindingHandler> bindingHandlers;
	Field parentField;
    int mode = TwoWay;
    boolean binded;

}
