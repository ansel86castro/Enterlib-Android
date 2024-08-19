package com.enterlib.fields;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.enterlib.databinding.BindingHandler;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.DependencyObject;
import com.enterlib.databinding.ExpressionMember;

/**
 * This is a {@link Form} field that displays a {@link CompoundButton} like a
 * {@link CheckBox} or another type of two states widget in the UI
 */
public class CompoundButtonField extends TextViewField {

    protected OnFieldChangeListener onChangeListener;

    public static final BindingProperty<CompoundButtonField> OnChangeProperty = registerProperty(
    		CompoundButtonField.class, "OnChange");

	public CompoundButtonField() {
		super();
	}

	public CompoundButtonField(CompoundButton view, boolean required) {
		super(view, required);
	}

	public CompoundButtonField(CompoundButton view, String display,
			boolean required) {
		super(view, display, required);
	}

	/**
	 * @param view
	 * @param valueBinding
	 * @param display
	 * @param required
	 */
	public CompoundButtonField(CompoundButton view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);
	}

	public CompoundButtonField(CompoundButton view, String valueBinding) {
		super(view, valueBinding);
	}

	public CompoundButtonField(CompoundButton view) {
		super(view);
	}


    public void setOnChange(final OnFieldChangeListener listener){
        this.onChangeListener = listener;
        CompoundButton view = (CompoundButton) getView();
        if(listener!=null) {
            view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.onChange(CompoundButtonField.this, isChecked);
                }
            });
        }else{
            view.setOnCheckedChangeListener(null);
        }
    }

	/** returns if the control is checked */
	@Override
	protected Object getViewValue() {
		CompoundButton control = (CompoundButton) getView();
		return control.isChecked();
	}

	@Override
	protected void setViewValue(Object value) {
		CompoundButton control = (CompoundButton) getView();
		if(value == null){
			control.setChecked(false);			
		}
		else if (value instanceof Boolean) {
			Boolean b = (Boolean) value;
			control.setChecked(b);
		}
	}

}
