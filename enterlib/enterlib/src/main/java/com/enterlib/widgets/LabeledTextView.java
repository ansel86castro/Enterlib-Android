package com.enterlib.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LabeledTextView extends LinearLayout {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public LabeledTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public LabeledTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public LabeledTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

}
