package com.enterlib.databinding;

import java.util.ArrayList;

import com.enterlib.fields.ViewPagerField;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class AdapterPageForm extends BasePageAdapter {

	private ViewPagerField field;
	private BindingResources bindingRes;
	private Context context;
	private int layout;
	private ArrayList<Object> objects;
	private LayoutInflater inflater;		
	private boolean asListView;	
	private SparseArray<Float>sizes;	
	
	public AdapterPageForm(Context context, int layout, ArrayList<Object> objects, ViewPagerField field, BindingResources bindingRes, 
			boolean asListView) {
		
		super(context ,layout ,field, bindingRes, asListView);		
		this.objects = objects;		
	}

	@Override
	public int getCount() {		
		return objects.size();
	}

	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {		
		Object item = objects.get(position);
		return instantiateItem(container, position, item);				
	}		
}
