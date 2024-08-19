package com.enterlib.widgets;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import androidx.fragment.app.FragmentActivity;

import com.enterlib.R;
import com.enterlib.converters.DateConverter;
import com.enterlib.exceptions.ConversionFailException;


@SuppressLint("AppCompatCustomView")
public class DateTimePickerButton extends Button implements
		OnLongClickListener, OnMenuItemClickListener {

	public interface OnDateChangeListener {
		void onDateChange(DateTimePickerButton view, Date date);
	}

	public interface OnUserSetDateListener {
		void onDateChange(DateTimePickerButton view, Date date);
	}

	private DateConverter converter;
	private TextViewErrorController errorController;
	private OnDateChangeListener dateChangeListener;
	private OnUserSetDateListener onUserSetDateListener;

	public DateTimePickerButton(Context context) {
		super(context);

		init();
	}

	public DateTimePickerButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DateTimePickerButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setConverter(DateConverter converter) {
		this.converter = converter;
	}

	public DateConverter getConverter() {
		return this.converter;
	}

	public OnDateChangeListener getOnDateChangeListener() {
		return dateChangeListener;
	}

	public void setOnDateChangeListener(OnDateChangeListener dateChangeListener) {
		this.dateChangeListener = dateChangeListener;
	}

	public OnUserSetDateListener getOnUserSetDateListener() {
		return onUserSetDateListener;
	}

	public void setOnUserSetDateListener(
			OnUserSetDateListener onUserSetDateListener) {
		this.onUserSetDateListener = onUserSetDateListener;
	}

	private void init() {
		this.setBackgroundResource(android.R.drawable.edit_text);
		this.errorController = new TextViewErrorController(this);
		this.setOnLongClickListener(this);

	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onLongClick(View v) {
		// CurrentDateDialogFragment frag =
		// CurrentDateDialogFragment.newIntance(this);
		// frag.show(getFragmentManager(), "com.CurrentDateDialogFragment");

		PopupMenu popup = new PopupMenu(getContext(), v);
		popup.setOnMenuItemClickListener(this);

		popup.inflate(R.menu.datetimepicker_menu);
		popup.show();

		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		Date date = null;
		int id = item.getItemId();
		if (id == R.id.set_current_date) {
			Calendar c = Calendar.getInstance();
			date = c.getTime();

		}

		setDate(date);
		if (onUserSetDateListener != null) {
			onUserSetDateListener.onDateChange(this, date);
		}

		return true;
	}

	protected FragmentManager getFragmentManager() {
		Context c = getContext();
		if (c instanceof Activity) {
			return ((Activity) c).getFragmentManager();
		} else {
			throw new RuntimeException("Activity context expected instead");
		}
	}

	protected androidx.fragment.app.FragmentManager getSupportFragmentManager() {
		Context c = getContext();
		if (c instanceof FragmentActivity) {
			return ((FragmentActivity) c).getSupportFragmentManager();
		} else {
			throw new RuntimeException("Activity context expected instead");
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

	public void setDate(Date date) {
		String text = date != null 
				? converter != null ? converter.getString(date) : date.toString() 
				: null;
				
		setText(text);
		setError(null);
		refreshDrawableState();

		if (dateChangeListener != null) {
			dateChangeListener.onDateChange(this, date);
		}
	}

	public void setDateString(String value) {
		try {

			if (converter == null) {
				setDate(DateFormat.getInstance().parse(value));
			} else {
				setDate(converter.getDate(value));
			}
		} catch (ConversionFailException e) {
			Log.d("Conversion Fail", e.getMessage());
			setText("");
		} catch (ParseException e) {
			Log.d("Conversion Fail", e.getMessage());
			setText("");
		}
	}

	@Override
	public void setError(CharSequence error) {
		errorController.showError(error);
	}

	public Date getDate() throws ConversionFailException {
		return (Date) converter.getObject(getText().toString());
	}

	@Override
	protected void onDetachedFromWindow() {
		errorController.showError(null);
		
		super.onDetachedFromWindow();
	}
}
