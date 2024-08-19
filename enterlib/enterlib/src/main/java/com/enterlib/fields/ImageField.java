package com.enterlib.fields;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.enterlib.databinding.BindingProperty;
import com.enterlib.serialization.IStringSerializer;
import com.enterlib.widgets.ViewErrorController;

public class ImageField extends Field {

	public static final BindingProperty<ImageField> DrawableProperty = registerProperty(
			ImageField.class, "Drawable");

	static final String DEBUG_TAG = ImageField.class.getSimpleName();
	
	private Class<?> valueCls; 
	
	private ViewErrorController errorController;
	
	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			errorController.showError(null);
		}
	};

	public ImageField() {		
	}	

	/**
	 * @param view
	 * @param valueBinding
	 * @param display
	 * @param required
	 */
	public ImageField(ImageView view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);
		init();

	}

	public ImageField(ImageView view, boolean required) {
		super(view, required);
		init();
	}

	public ImageField(ImageView view, String display, boolean required) {
		super(view, display, required);
		init();
	}

	public ImageField(ImageView view, String valueBinding) {
		super(view, valueBinding);
		init();
	}

	public ImageField(ImageView view) {
		super(view);
		init();
	}

	private void init() {
		errorController = new ViewErrorController(getView());
		errorController.setOnClickListener(onClickListener);
		setRestorable(false);				
	}

	@Override
	protected void onViewChanged() {
		super.onViewChanged();

		init();
	}

	@Override
	protected void onSetErrorMessage(String errorMessage) {
		errorController.showError(errorMessage);
	}

	@Override
	protected Object getViewValue() {
		ImageView view = (ImageView) getView();					
		if(valueCls == Bitmap.class){
			BitmapDrawable image = (BitmapDrawable) view.getDrawable();
			return image.getBitmap();
		}else if(valueCls == Integer.class){
			ColorDrawable c = (ColorDrawable) view.getDrawable();
			return c.getColor();
		}else 
			return view.getDrawable();
	}

	@Override
	protected void setViewValue(Object value) {
		ImageView view = (ImageView) getView();
		if(value == null){
			view.setImageDrawable(null);
			valueCls = null;
		}
		else if (value instanceof Bitmap) {
			Bitmap bitmap = (Bitmap) value;
			view.setImageBitmap(bitmap);
			valueCls = Bitmap.class;
		} else if (value instanceof Drawable) {
			Drawable dr = (Drawable)value;
			view.setImageDrawable(dr);
			valueCls = Drawable.class;
		} else if(value instanceof Integer){
			view.setImageDrawable(new ColorDrawable((Integer)value));
			valueCls = Integer.class;
		}
	}
	
	public void setDrawable(Drawable dr) {
		ImageView view = (ImageView) getView();
		view.setImageDrawable(dr);
		valueCls =Drawable.class;
	}
	
	@Override
	public void saveState(Bundle outState, IStringSerializer serializer) {					
		if (!isRestorable()) {
			return;
		}
		
		String id = getStoreId();
		String valueType = id+"#ImageType";
		Object value = getViewValue();		
		if(value instanceof Bitmap){			
			outState.putParcelable(id, (Bitmap) value);
			outState.putString(valueType, valueCls.getName());
		}else if(value instanceof Integer){
			outState.putInt(id, (Integer) value);
			outState.putString(valueType, valueCls.getName());
		}else if(value instanceof BitmapDrawable){
			BitmapDrawable dr = (BitmapDrawable) value;
			outState.putParcelable(id, dr.getBitmap());
			valueCls = BitmapDrawable.class;
			outState.putString(valueType, valueCls.getName());
		}
	}
	
	@Override
	public void restoreState(Bundle savedInstanceState,
			IStringSerializer serializer) {

		super.restoreState(savedInstanceState, serializer);
		if (!isRestorable()) {
			return;
		}
		
		String id = getStoreId();
		String valueType = id+"#ImageType";
		
		if (savedInstanceState.containsKey(id)) {
			restored = true;
			
			String className = savedInstanceState.getString(valueType);
			if(className == null)
				return;
			
			try {
				valueCls = Class.forName(className);
				if(valueCls == Integer.class){
					int color = savedInstanceState.getInt(id);
					setValue(color);
				}else if(valueCls == Bitmap.class){
					Bitmap bmp = savedInstanceState.getParcelable(id);
					setValue(bmp);
				}else if(valueCls == BitmapDrawable.class){
					Bitmap bmp = savedInstanceState.getParcelable(id);
					BitmapDrawable dr = new BitmapDrawable(getContext().getResources(), bmp);
					setValue(dr);
				}
				
			} catch (ClassNotFoundException e) {
				Log.d(DEBUG_TAG, e.getMessage(), e);
			}
			
		}		
	}

}
