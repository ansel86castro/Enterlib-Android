package com.enterlib.databinding;

import android.content.Context;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.enterlib.fields.Form;
import com.enterlib.fields.ViewPagerField;
import com.enterlib.mvvm.Command;

public abstract class BasePageAdapter extends PagerAdapter {

	protected ViewPagerField field;
	protected BindingResources bindingRes;
	protected Context context;
	protected int layout;
	protected LayoutInflater inflater;
	protected boolean asListView;
	protected SparseArray<Float> sizes;	
	protected int pageLoadingIdx = -1;

	public BasePageAdapter(Context context, int layout, ViewPagerField field, BindingResources bindingRes, boolean asListView) {	
		
		this.context = context;
		this.layout = layout;		
		this.field = field;
		this.bindingRes = bindingRes;
		this.inflater = LayoutInflater.from(context);
		this.asListView = asListView;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {		
		return arg0 == arg1;
	}

	protected void computeSizeWeight(ViewGroup container, int position, View view) {
		if(asListView){
			view.measure(MeasureSpec.getSize(view.getMeasuredWidth()),
							 MeasureSpec.getSize(view.getMeasuredHeight()));
			
			float parentWidth = container.getWidth();	
			float percent = Math.min(1.0f, (float)view.getMeasuredWidth()/parentWidth);
			if(sizes == null)
				sizes = new SparseArray<Float>();
			sizes.put(position, percent);
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {		
		if(sizes!=null){
			sizes.remove(position);
		}
		container.removeView((View)object);
	}

	@Override
	public float getPageWidth(int position) {
		
		if(asListView && sizes!=null){
			return sizes.get(position);
		}
		
		return super.getPageWidth(position);
	}

	public Object instantiateItem(ViewGroup container, final int position, final Object item) {
		View itemView = inflater.inflate(layout, container, false);		
		BindingResources dc;
		Object viewModel = null;
		if (field != null) {
			dc = field.getBindingResources();
			viewModel = field.getViewModel();
		}else{ 
			if (this.bindingRes != null) {
				dc = this.bindingRes;			
			} else {
				dc = new BindingResources();				
			}
			viewModel = item;
		}

		Form form = Form.build(dc, itemView, viewModel);
		form.updateTargets(item);
		
		if(asListView){
			itemView.measure(MeasureSpec.getSize(itemView.getMeasuredWidth()),
							 MeasureSpec.getSize(itemView.getMeasuredHeight()));
			
			float parentWidth = container.getWidth();	
			float percent = Math.min(1.0f, (float)itemView.getMeasuredWidth()/parentWidth);
			if(sizes == null)
				sizes = new SparseArray<Float>();
			sizes.put(position, percent);
		}
				
		if(field.getSelectionCommand() != null){
			itemView.setOnClickListener(new OnClickListener() {
								
				@Override
				public void onClick(View v) {
					Command selectionCmd = field.getSelectionCommand();
					if(selectionCmd==null){
						v.setOnClickListener(null);
						return;
					}
					field.setSelectedPosition(position);
					selectionCmd.invoke(field, item);
				}
			});
		}
		
		container.addView(itemView);		
		return itemView;
	}		

}