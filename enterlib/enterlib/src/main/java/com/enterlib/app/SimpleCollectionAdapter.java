package com.enterlib.app;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

/**
 * A {@link CollectionAdapter} that creates an {@link TextView} for displaying
 * each item in the {@link Adapter}
 * */
public class SimpleCollectionAdapter<T> extends CollectionAdapter<T> {

	public SimpleCollectionAdapter(Context context, int resource,
			List<T> objects) {
		super(context, resource, objects);
	}

	public SimpleCollectionAdapter(Context context, int resource, T[] objects) {
		super(context, resource, objects);
	}

	public SimpleCollectionAdapter(Context context, int resource) {
		super(context, resource);
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
