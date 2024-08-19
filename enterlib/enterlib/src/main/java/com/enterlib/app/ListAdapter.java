package com.enterlib.app;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

public abstract class ListAdapter<T> extends CollectionAdapter<T> {
	private LayoutInflater _inflater;
	private int _layout;
	private int _dropDownLayout;

	public ListAdapter(Context context, int layout, ArrayList<T> objects) {
		super(context, layout, objects);
		_inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_layout = layout;
		_dropDownLayout = layout;
	}

	public ListAdapter(Context context, int layout, int dropDownLayout, ArrayList<T> objects){
		this(context, layout, objects);
		this._dropDownLayout = dropDownLayout;
	}

	public ListAdapter(Context context, int layout, T[] objects) {
		super(context, layout, objects);
		_inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;

		if (convertView != null) {
			view = convertView;
		} else {
			view = _inflater.inflate(_layout, parent, false);
		}

		if(view instanceof CheckedTextView){
			((CheckedTextView) view).setChecked(false);
		}

		T item = getItem(position);
		updateView(view, item, position);
		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = null;

		if (convertView != null) {
			view = convertView;
		} else {
			view = _inflater.inflate(_dropDownLayout, parent, false);
		}

		T item = getItem(position);
		updateView(view, item, position);
		return view;
	}

	protected abstract void updateView(View view, T item, int position);
}
