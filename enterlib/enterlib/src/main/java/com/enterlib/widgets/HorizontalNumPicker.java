package com.enterlib.widgets;

import com.enterlib.R;
import com.enterlib.StringUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class HorizontalNumPicker extends FrameLayout implements OnClickListener, TextWatcher {
	
	private View rootView;
	private EditText editText;
	private ImageButton dec;
	private ImageButton inc;
	private Integer maxValue;
    private Integer minValue;


	public HorizontalNumPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HorizontalNumPicker(Context context) {
		this(context, null);
	}

	public HorizontalNumPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = LayoutInflater.from(context);
		rootView = inflater.inflate(com.enterlib.R.layout.view_num_picker, this, false);
		
		inc = (ImageButton) rootView.findViewById(com.enterlib.R.id.btnInc);
		dec = (ImageButton) rootView.findViewById(com.enterlib.R.id.btnDec);
		editText = (EditText) rootView.findViewById(com.enterlib.R.id.editText);
        editText.addTextChangedListener(this);

		if (!isInEditMode()){
			inc.setOnClickListener(this);
			dec.setOnClickListener(this);
			dec.setEnabled(false);
		}

        // Use the array constant to read the bag once
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.HorizontalNumPicker, defStyle, // if any values are in
                // the theme
                0); // Do you have your own style group
        // Use the offset in the bag to get your value
        minValue = t.getInt(R.styleable.HorizontalNumPicker_picker_minValue, Integer.MIN_VALUE);
        maxValue = t.getInt(R.styleable.HorizontalNumPicker_picker_maxValue, Integer.MAX_VALUE);
        int value =  t.getInt(R.styleable.HorizontalNumPicker_picker_value, Math.max(minValue, 0));

        setValue(value);

        // Recycle the typed array
        t.recycle();
		
		addView(rootView);		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		Integer value = getValue();
        if(value == null)
            value = 0;

		if(id == com.enterlib.R.id.btnInc){
            setValue(value+1);
		}else if(id == com.enterlib.R.id.btnDec){
			setValue(value - 1);
		}		
	}
	
	public Integer getValue(){
		String str = editText.getText().toString();
		if(StringUtils.isNullOrWhitespace(str))
			return null;
		
		try{
			return Integer.valueOf(str);
		}catch(NumberFormatException e){
			return null;
		}		
	}
	
	public void setValue(Integer value){
		if(value == null){
			editText.setText("");
            dec.setEnabled(false);
            inc.setEnabled(true);
            return;
		}

        inc.setEnabled(true);
        dec.setEnabled(true);

        if(minValue !=null && value <= minValue){
            value = minValue;
            dec.setEnabled(false);
        }

        if(maxValue != null && value >= maxValue){
            value = maxValue;
            inc.setEnabled(false);
        }

        editText.setText(String.valueOf(value));
			
	}
	
	public void setError(CharSequence error){
		editText.setError(error);
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer value){
		this.maxValue = value;
	}

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Integer value = getValue();
        if(value == null){
            dec.setEnabled(false);
            inc.setEnabled(true);
            return;
        }

        if(minValue!=null && value == minValue){
            dec.setEnabled(false);
            return;
        }else if(maxValue!=null && value == maxValue ){
            inc.setEnabled(false);
            return;
        }

        if(minValue !=null && value < minValue){
            setValue(minValue);
            return;
        }

        if(maxValue != null && value > maxValue){
            setValue(maxValue);
            return;
        }

        dec.setEnabled(true);
        inc.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
