/**
 *
 */
package com.enterlib.fields;

import com.enterlib.converters.Converters;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.DependencyObject;
import com.enterlib.databinding.ExpressionMember;

import android.content.res.ColorStateList;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

/**
 * This is a {@link Form} field that displays a {@link TextView} in the UI
 */
public class TextViewField extends Field {

	private boolean underline;

	public static final BindingProperty<TextViewField> DateProperty = registerProperty(TextViewField.class, 
			new BindingProperty<TextViewField>("Date") {
			@Override
			public void set(TextViewField object, ExpressionMember value,
							BindingResources dc) {
					if(value.isValueTrue()){
						object.valueConverter = Converters.DateToStringConverter;
					}
			}
	});

	public static final BindingProperty<TextViewField> TextColorProperty = registerProperty(TextViewField.class, "TextColor");

	public static final BindingProperty<TextViewField> UnderlineProperty = registerProperty(TextViewField.class,
			new BindingProperty<TextViewField>("Underline") {
				@Override
				public void set(TextViewField object, ExpressionMember value, BindingResources dc) {
					object.underline = value.isValueTrue();
				}

				@Override
				public Object get(DependencyObject object) {
					return ((TextViewField)object).getUnderline();
				}
			});
	
	public TextViewField() {
	}

	/**
	 * @param view
	 * @param valueBinding
	 * @param display
	 * @param required
	 */
	public TextViewField(TextView view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);

	}

	public TextViewField(TextView view, String display, boolean required) {
		super(view, display, required);



	}

	public TextViewField(TextView view, String valueBinding) {
		super(view, valueBinding);

	}

	public TextViewField(TextView view) {
		super(view);
	}

	public TextViewField(TextView view, boolean inRequired) {
		super(view, inRequired);
	}


	public void setUnderline(boolean value){
		this.underline = value;
	}

	public boolean getUnderline(){
		return underline;
	}

	public int getTextColor(){
		TextView view = (TextView) getView();
		return  view.getCurrentTextColor();
	}

	public void setTextColor(int color){
		TextView view = (TextView) getView();
		view.setTextColor(color);
	}

	/**
	 * Use the android {@link TextView}.{@code setError} to show the errors.
	 * */
	@Override
	protected void onSetErrorMessage(String message) {
		getTextView().setError(message);
	}

	public TextView getTextView() {
		return (TextView) getView();
	}

	/** returns {@link TextView}.{@code getText().toSql()} */
	@Override
	protected Object getViewValue() {
		return getTextView().getText().toString();
	}
	
	/**
	 * calls {@link TextView}.{@code setText()} passing the value. The value is
	 * already converted to String
	 * */
	@Override
	protected void setViewValue(Object value) {
		TextView tv = getTextView();
		if(value == null){
			tv.setText(null);
			return;
		}

		CharSequence sequence;

		if (value instanceof CharSequence) {
			sequence = (CharSequence) value;
		} else {
			sequence = value.toString();
		}

		if(underline){
			SpannableString ss = new SpannableString(sequence);
			ss.setSpan(new UnderlineSpan(),0,ss.length(),0);
			sequence = ss;
		}

		tv.setText(sequence);
	}

	public void setValue(int res) {
		getTextView().setText(res);
	}
		
}
