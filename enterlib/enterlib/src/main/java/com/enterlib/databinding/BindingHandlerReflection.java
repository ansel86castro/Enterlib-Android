package com.enterlib.databinding;

import com.enterlib.converters.IValueConverter;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.fields.Field;
import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IWorkPost;

import java.util.Objects;

public class BindingHandlerReflection extends BindingHandler implements
		IPropertyChangedListener {

    public static final int TARGET_FIELD = 0;
    public static final int TARGET_VIEW = 1;

	public final String TargetProperty;
	private boolean binded;
	private Object lastSource;
	private Field field;
	private IValueConverter converter;
    private int targetType = TARGET_FIELD;
    private boolean async;

    public BindingHandlerReflection setAsync(boolean value){
        this.async = value;
        return this;
    }

	public BindingHandlerReflection(String sourceProperty, String targetProperty, int mode){
		this(sourceProperty, targetProperty, mode, null, TARGET_FIELD);
	}

	public BindingHandlerReflection(String sourceProperty, String targetProperty, int mode, IValueConverter converter, int targetType) {
		super(sourceProperty, mode);
		this.TargetProperty = targetProperty;
		this.converter = converter;
        this.targetType = targetType;
	}

	public static BindingHandlerReflection createHandler(ExpressionMember expression, BindingResources bindingResources, Field field, int targetType) {

		String targetProperty = expression.getKey();
		String sourceProperty = null;
		int mode = Field.TwoWay;
		if (expression.value instanceof String) {
			sourceProperty = expression.getValueString();
			return new BindingHandlerReflection(sourceProperty, targetProperty, mode, null, targetType);

		} else if (expression.value instanceof BindingExpression) {

			BindingExpression exp = (BindingExpression) expression.value;
			ExpressionMember member = exp.getMember(SourceString);
			if(member == null)
				throw new RuntimeException("Invalid Binding Expression " + exp.toString() + " Missing " + SourceString);

			sourceProperty = member.getValueString();
			member = exp.getMember(ModeString);
			if(member!=null){
				String modeString = member.getValueString();
				if (modeString.equalsIgnoreCase("TwoWay")) {
					mode = Field.TwoWay;
				} else if (modeString.equalsIgnoreCase("OneTime")) {
					mode = Field.OneTime;
				} else if (modeString.equalsIgnoreCase("OneWay")) {
					mode = Field.OneWay;
				}else{
					throw new RuntimeException("Invalid Binding Expression " + exp.toString() + " Mode must be TwoWay, OneWay or OneTime");
				}
			}

			IValueConverter converter = null;
			member = exp.getMember(Converter);
			if(member!=null){
				if (bindingResources != null) {
					converter = (IValueConverter) bindingResources.get(member.getValueString());
				}

				if (converter == null) {
					Object viewModel = field.getViewModel();
					if (viewModel != null) {
						converter = (IValueConverter) ReflectionResolver.getValue(member.getValueString(), viewModel);
					}
				}
			}

            member = exp.getMember(Async);
            boolean async = false;
            if(member!=null) {
                async = member.isValueTrue();
            }

            return new BindingHandlerReflection(sourceProperty, targetProperty, mode, converter, targetType)
                    .setAsync(async);

		}else
            throw new InvalidOperationException("Invalid Binding Expreesion "+expression.toString());
	}

    private Object getTarget(Field field){
        if(targetType==TARGET_FIELD)
          return  field;
        return  field.getView();
    }

	@Override
	public void updateTarget(Field field, Object source, BindingResources res) {
		if (Mode == Field.OneTime && binded) {
			return;
		}

       final Object target =getTarget(field);

        if(SourceProperty.equals(Field.SELF)){
            Object sourceValue = source;
            if(converter!=null){
                sourceValue = converter.convert(sourceValue);
            }
            ReflectionResolver.setValue(TargetProperty, target, sourceValue);
            return;
        }

        Object viewModel = field.getViewModel();
        if (!ReflectionResolver.containsProperty(SourceProperty, source)
                && viewModel != null) {
            source = viewModel;
        }

        if(async){
            final Object finalSource =source;
            AsyncManager.postAsync(new IWorkPost() {
                Object sourceValue;

                @Override
                public boolean runWork() throws Exception {
                    sourceValue = ReflectionResolver.getValue(SourceProperty, finalSource);
                    if (converter != null) {
                        sourceValue = converter.convert(sourceValue);
                    }
                    return true;
                }

                @Override
                public void onWorkFinish(Exception workException) {
                    if(workException!=null)
                        return;

                    ReflectionResolver.setValue(TargetProperty, target, sourceValue);
                    binded = true;
                }
            });

        }else{

            Object sourceValue = ReflectionResolver.getValue(SourceProperty, source);
            if (converter != null) {
                sourceValue = converter.convert(sourceValue);
            }

            ReflectionResolver.setValue(TargetProperty, target, sourceValue);

            binded = true;
        }

		if (lastSource != source) {
			this.field = field;
            if(lastSource instanceof INotifyPropertyChanged){
                ((INotifyPropertyChanged) lastSource).removePropertyChangeListener(this);
            }
            if(source instanceof INotifyPropertyChanged) {
                INotifyPropertyChanged notify = (INotifyPropertyChanged) source;
                notify.addPropertyChangeListener(this);
            }
		}

		lastSource = source;
	}

	@Override
	public void updateSource(Field field, Object source, BindingResources res) {
		if (Mode != Field.TwoWay) {
			return;
		}

		if (!ReflectionResolver.containsProperty(SourceProperty, source)) {
			source = field.getViewModel();
		}

        final Object target =getTarget(field);
		Object targetValue = ReflectionResolver.getValue(TargetProperty, target);

        if(async && converter!=null){
            final Object finalSource = source;
            final Object finalTargetValue = targetValue;

            AsyncManager.postAsync(new IWorkPost() {
                Object asyncTargetValue;
                @Override
                public boolean runWork() throws Exception {
                    asyncTargetValue = converter.convertBack(finalTargetValue);
                    return  true;
                }

                @Override
                public void onWorkFinish(Exception workException) {
                    if(workException!=null)
                        return;
                    ReflectionResolver.setValue(SourceProperty, finalSource, asyncTargetValue);
                }
            });
        }else{

            if(converter!=null){
                targetValue = converter.convertBack(targetValue);
            }

            ReflectionResolver.setValue(SourceProperty, source, targetValue);
        }
	}

	@Override
	public void onPropertyChange(Object object, String propertyName) {
        if(SourceProperty.equals(Field.SELF)){
            Object sourceValue = object;
            if (converter != null) {
                sourceValue = converter.convert(object);
            }

            ReflectionResolver.setValue(TargetProperty, getTarget(field), sourceValue);
        }
        else if(SourceProperty.equals(propertyName)) {
            Object sourceValue = ReflectionResolver.getValue(SourceProperty, object);
            if (converter != null) {
                sourceValue = converter.convert(sourceValue);
            }

            ReflectionResolver.setValue(TargetProperty, getTarget(field), sourceValue);
        }

	}

}
