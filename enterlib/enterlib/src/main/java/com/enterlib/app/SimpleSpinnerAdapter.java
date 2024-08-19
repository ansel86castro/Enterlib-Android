package com.enterlib.app;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * A {@link ArrayAdapter} that creates an {@link TextView} for displaying each
 * item in the {@link Adapter}
 * 
 * @author Ansel
 *
 */
public class SimpleSpinnerAdapter extends ArrayAdapter<Object> {

	public SimpleSpinnerAdapter(Context context, int resource, Object[] objects) {
		super(context, resource, objects);
	}

	public SimpleSpinnerAdapter(Context context, int resource,
			int textViewResourceId, List<Object> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public SimpleSpinnerAdapter(Context context, int resource,
			List<Object> objects) {
		super(context, resource, objects);
	}

	public SimpleSpinnerAdapter(Context context, List<Object> objects) {
		super(context, 0, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv;
		if (convertView != null) {
			tv = (TextView) convertView;
		} else {
			tv = new TextView(getContext());
		}

		Object item = getItem(position);
		tv.setText(item.toString());
		// tv.setBackgroundResource(R.color.gray);
		return tv;
	}

}