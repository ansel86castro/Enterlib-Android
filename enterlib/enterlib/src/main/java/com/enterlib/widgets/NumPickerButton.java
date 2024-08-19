package com.enterlib.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import androidx.fragment.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.enterlib.R;

@SuppressLint("AppCompatCustomView")
public class NumPickerButton extends Button implements OnClickListener,
		OnValueChangeListener {
	public int minValue = Integer.MIN_VALUE;
	public int maxValue = Integer.MAX_VALUE;

	public Integer oldSelectedValue;
	public Integer value;

	public interface OnNumberSelectedListener {
		void onValueSelected(NumPickerButton view, Integer oldnumber,
				Integer newNumber);
	}

	public interface OnNumberChangeListener {
		void onValueChange(NumPickerButton view, int oldnumber, int newNumber);
	}

	private TextViewErrorController errorController;
	private OnNumberChangeListener onNumberChangeListener;
	private OnNumberSelectedListener onNumberSelectedListener;

	public NumPickerButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// Use the array constant to read the bag once
		TypedArray t = context.obtainStyledAttributes(attrs,
				R.styleable.NumPickerButton, defStyle, // if any values are in
														// the theme
				0); // Do you have your own style group
		// Use the offset in the bag to get your value
		minValue = t.getInt(R.styleable.NumPickerButton_minValue, minValue);
		maxValue = t.getInt(R.styleable.NumPickerButton_maxValue, maxValue);

		attrs.getAttributeValue("enterlib", "comparer");

		// Recycle the typed array
		t.recycle();

		init();
	}

	public NumPickerButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NumPickerButton(Context context) {
		super(context);
		init();
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getValue() {
		CharSequence secuence = getText();
		if (secuence != null && secuence.length() > 0) {
			try {
				value = Integer.decode(secuence.toString());
			} catch (NumberFormatException e) {
				Log.d("IntegerConverter", "formato no valido :" + value);
			}
		}
		return value;
	}

	public void setValue(int value) {
		this.value = value;
		this.oldSelectedValue = value;
		setText(String.valueOf(value));
	}

	public int getOldSelectedValue() {
		return oldSelectedValue;
	}

	public void setOnNumberChangeListener(
			OnNumberChangeListener onNumberChangeListener) {
		this.onNumberChangeListener = onNumberChangeListener;
	}

	public void setOnNumberSelectedListener(
			OnNumberSelectedListener onNumberSelectedListener) {
		this.onNumberSelectedListener = onNumberSelectedListener;
	}

	private void init() {
		// this.setBackgroundResource(android.R.drawable.edit_text);
		this.errorController = new TextViewErrorController(this);
		this.setOnClickListener(this);

		CharSequence secuence = getText();
		if (secuence != null && secuence.length() > 0) {
			try {
				value = Integer.decode(secuence.toString());
			} catch (NumberFormatException e) {
				Log.d("IntegerConverter", "formato no valido :" + value);
			}
		}
	}

	protected Activity getActivity() {
		Context c = getContext();
		if (c instanceof Activity) {
			return ((Activity) c);
		} else {
			throw new RuntimeException("Activity context expected instead");
		}
	}

	protected FragmentActivity getFragmentActivity() {
		Context c = getContext();
		if (c instanceof FragmentActivity) {
			return ((FragmentActivity) c);
		} else {
			throw new RuntimeException("Activity context expected instead");
		}
	}

	@Override
	public void setError(CharSequence error) {
		errorController.showError(error);
	}

	@Override
	public void onClick(View v) {
		NumberPicker numberPicker = new NumberPicker(getContext());
		numberPicker.setMinValue(minValue);
		numberPicker.setMaxValue(maxValue);
		numberPicker.setOnValueChangedListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setView(numberPicker);
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.multiple_filter_dialog_accept,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (onNumberSelectedListener != null) {
							onNumberSelectedListener.onValueSelected(
									NumPickerButton.this, oldSelectedValue,
									value);

						}
						oldSelectedValue = value;
					}
				});
		builder.setNegativeButton(R.string.multiple_filter_dialog_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		value = newVal;
		if (onNumberChangeListener != null) {
			onNumberChangeListener.onValueChange(this, oldVal, value);
		}
	}

}
