package com.enterlib.widgets;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

import androidx.fragment.app.FragmentActivity;

import com.enterlib.StringUtils;
import com.enterlib.converters.DateConverter;
import com.enterlib.exceptions.ConversionFailException;

public class DatePickerButton extends DateTimePickerButton implements
		OnClickListener {

	public DatePickerButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DatePickerButton(Context context) {
		super(context);
		init();
	}

	public DatePickerButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {

		this.setOnClickListener(this);
		this.setConverter(new DateConverter());
	}

	private Calendar getCalendar(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		return c;
	}

	@Override
	public void onClick(View v) {
		int year;
		int month;
		int day;
		String text = getText().toString();
		DateConverter converter = getConverter();

		if (StringUtils.isNullOrWhitespace(text)) {
			final Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);
		} else {
			try {
				Date date = (Date) converter.getObject(text);
				Calendar c = Calendar.getInstance();
				c.setTime(date);

				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);

			} catch (ConversionFailException e) {
				Log.d("DatePickerButton", Objects.requireNonNull(e.getMessage()));

				final Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);
			}

		}
		Context context = getContext();
		if (context instanceof FragmentActivity) {
			SupportDatePickerFragment fragment = SupportDatePickerFragment
					.newIntance(year, month, day, this);
			fragment.show(getFragmentActivity().getSupportFragmentManager(),
					"com.SupportDatePickerFragment");
		} else if (context instanceof Activity) {
			DatePickerFragment fragment = DatePickerFragment.newIntance(year,
					month, day, this);
			fragment.show(getActivity().getFragmentManager(),
					"com.DatePickerFragment");
		}

	}

	public void setDate(int year, int monthOfYear, int dayOfMonth) {
		final Calendar c = getCalendar(year, monthOfYear, dayOfMonth);
		Date date = c.getTime();
		super.setDate(date);
	}

	public static class SupportDatePickerFragment extends
			androidx.appcompat.app.AppCompatDialogFragment implements
			DatePickerDialog.OnDateSetListener {
		int year;
		int month;
		int day;
		DatePickerButton parent;

		// Default constructor for device rotation
		public SupportDatePickerFragment() {
		}

		public static SupportDatePickerFragment newIntance(int year, int month,
				int day, DatePickerButton parent) {
			SupportDatePickerFragment frag = new SupportDatePickerFragment();
			frag.year = year;
			frag.month = month;
			frag.day = day;
			frag.parent = parent;

			Bundle args = new Bundle();
			args.putInt("year", year);
			args.putInt("month", month);
			args.putInt("day", day);
			args.putInt("parent", parent.getId());
			frag.setArguments(args);

			return frag;
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			if (parent == null) {
				int id = getArguments().getInt("parent");
				parent = (DatePickerButton) getActivity().findViewById(id);
			}
			if (parent != null) {
				Calendar c = Calendar.getInstance();
				c.set(year, monthOfYear, dayOfMonth);
				Date date = c.getTime();

				parent.setDate(date);

				OnUserSetDateListener onUsersetDate = parent
						.getOnUserSetDateListener();
				if (onUsersetDate != null) {
					onUsersetDate.onDateChange(parent, date);
				}
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			year = getArguments().getInt("year");
			month = getArguments().getInt("month");
			day = getArguments().getInt("day");

			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DatePickerFragment extends android.app.DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		int year;
		int month;
		int day;
		DatePickerButton parent;

		// Default constructor for device rotation
		public DatePickerFragment() {
		}

		@SuppressLint("NewApi")
		public static DatePickerFragment newIntance(int year, int month,
				int day, DatePickerButton parent) {
			DatePickerFragment frag = new DatePickerFragment();
			frag.year = year;
			frag.month = month;
			frag.day = day;
			frag.parent = parent;

			Bundle args = new Bundle();
			args.putInt("year", year);
			args.putInt("month", month);
			args.putInt("day", day);
			args.putInt("parent", parent.getId());
			frag.setArguments(args);

			return frag;
		}

		@SuppressLint("NewApi")
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			if (parent == null) {
				int id = getArguments().getInt("parent");
				parent = (DatePickerButton) getActivity().findViewById(id);
			}
			if (parent != null) {
				Calendar c = Calendar.getInstance();
				c.set(year, monthOfYear, dayOfMonth);
				Date date = c.getTime();

				parent.setDate(date);

				OnUserSetDateListener onUsersetDate = parent
						.getOnUserSetDateListener();
				if (onUsersetDate != null) {
					onUsersetDate.onDateChange(parent, date);
				}
			}
		}

		@SuppressLint("NewApi")
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			year = getArguments().getInt("year");
			month = getArguments().getInt("month");
			day = getArguments().getInt("day");

			return new DatePickerDialog(getActivity(), this, year, month, day);
		}
	}
}
