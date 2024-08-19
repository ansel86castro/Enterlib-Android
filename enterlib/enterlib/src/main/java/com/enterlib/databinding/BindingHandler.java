package com.enterlib.databinding;

import com.enterlib.fields.Field;

public abstract class BindingHandler {

	public static final String SourceString = "Source";
	public static final String ModeString = "Mode";
	public static final String TwoWay = "TwoWay";
	public static final String OneTime = "OneTime";
	public static final String OneWay = "OneWay";
	public static final String Converter = "Converter";
	public static final String Async = "Async";

	public final String SourceProperty;
	public int Mode;

	public BindingHandler(String sourceProperty) {
		SourceProperty = sourceProperty;
	}

	public BindingHandler(String sourceProperty, int mode) {
		SourceProperty = sourceProperty;
		this.Mode = mode;
		if (SourceProperty == null) {
			throw new RuntimeException("Missing Source Property");
		}
	}

	public abstract void updateTarget(Field field, Object source,BindingResources res);

	public void updateSource(Field field, Object source, BindingResources res) {
	}

    public Object resolveSourceProperty(Field field, Object source){
        Object viewModel = field.getViewModel();
        if (!ReflectionResolver.containsProperty(SourceProperty, source) && viewModel != null) {
            source = viewModel;
        }

        return ReflectionResolver.getValue(SourceProperty, source);
    }
}