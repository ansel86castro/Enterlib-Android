package com.enterlib.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.enterlib.R;

public class SearchEdit extends LinearLayout implements OnClickListener {

	EditText _edit;
	Object _value;

	@SuppressLint("NewApi")
	public SearchEdit(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	public SearchEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SearchEdit(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {

		// Get the layout inflater
		LayoutInflater lif = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// inflate the custom layout of listing 2-2
		// Use the second argument to attach the layout
		// as a child of this layout
		lif.inflate(R.layout.search_edit_layout, this);

		if (!isInEditMode()) {

			_edit = (EditText) findViewById(R.id.edit);
			// Initialize the buttons
			ImageButton b = (ImageButton) this.findViewById(R.id.searchButton);
			b.setOnClickListener(this);

			// Allow view state management
			this.setSaveEnabled(true);
		}

	}

	@Override
	public void onClick(View v) {
		Toast toas = Toast.makeText(getContext(), "Your Click me",
				Toast.LENGTH_LONG);
		toas.show();
	}

	public void setText(String value) {
		if (_edit != null) {
			_edit.setText(value);
		}
	}

	public String getText() {
		if (_edit != null) {
			return _edit.getText().toString();
		}

		return "";
	}

	public Object getValue() {
		return _value;
	}

	public void setValue(Object value) {
		this._value = value;
	}

}
