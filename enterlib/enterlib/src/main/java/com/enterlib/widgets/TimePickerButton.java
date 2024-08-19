package com.enterlib.widgets;

import java.util.Calendar;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TimePicker;

import com.enterlib.StringUtils;
import com.enterlib.converters.DateConverter;
import com.enterlib.exceptions.ConversionFailException;

public class TimePickerButton extends DateTimePickerButton implements
		OnClickListener {

	public TimePickerButton(Context context) {
		super(context);
		init();
	}

	public TimePickerButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TimePickerButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setOnClickListener(this);
		setConverter(new DateConverter("HH:mm"));
	}

	@Override
	public void onClick(View v) {
		int hour;
		int minutes;
		String text = getText().toString();
		DateConverter converter = getConverter();

		if (StringUtils.isNullOrWhitespace(text)) {
			final Calendar c = Calendar.getInstance();
			hour = c.get(Calendar.HOUR_OF_DAY);
			minutes = c.get(Calendar.MINUTE);
		} else {
			try {
				Date date = (Date) converter.getObject(text);
				Calendar c = Calendar.getInstance();
				c.setTime(date);

				hour = c.get(Calendar.HOUR_OF_DAY);
				minutes = c.get(Calendar.MINUTE);

			} catch (ConversionFailException e) {
				Log.d("DatePickerButton", e.getMessage());

				final Calendar c = Calendar.getInstance();
				hour = c.get(Calendar.HOUR_OF_DAY);
				minutes = c.get(Calendar.MINUTE);
			}

		}

		Context context = getContext();
		if (context instanceof FragmentActivity) {
			SupportTimePickerFragment fragment = SupportTimePickerFragment
					.newIntance(hour, minutes, this);
			fragment.show(getFragmentActivity().getSupportFragmentManager(),
					"com.SupportTimePickerFragment");
		} else if (context instanceof Activity) {
			TimePickerFragment fragment = TimePickerFragment.newIntance(hour,
					minutes, this);
			fragment.show(getActivity().getFragmentManager(),
					"com.TimePickerFragment");
		}
	}

	public Calendar getCalendar(int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(0, 0, 0, hourOfDay, minute);
		return c;
	}

	public static class SupportTimePickerFragment extends
			androidx.appcompat.app.AppCompatDialogFragment implements
			TimePickerDialog.OnTimeSetListener {
		int hourOfDay;
		int minutes;
		TimePickerButton parent;

		// Default constructor for device rotation
		public SupportTimePickerFragment() {
		}

		public static SupportTimePickerFragment newIntance(int hourOfDay,
				int minutes, TimePickerButton parent) {
			SupportTimePickerFragment frag = new SupportTimePickerFragment();
			frag.hourOfDay = hourOfDay;
			frag.minutes = minutes;
			frag.parent = parent;

			Bundle args = new Bundle();
			args.putInt("hourOfDay", hourOfDay);
			args.putInt("minutes", minutes);
			args.putInt("parent", parent.getId());
			frag.setArguments(args);

			return frag;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (parent == null) {
				int id = getArguments().getInt("parent");
				parent = (TimePickerButton) getActivity().findViewById(id);
			}
			if (parent != null) {
				final Calendar c = parent.getCalendar(hourOfDay, minute);
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
			hourOfDay = getArguments().getInt("hourOfDay");
			minutes = getArguments().getInt("minutes");

			return new TimePickerDialog(getActivity(), this, hourOfDay,
					minutes, true);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class TimePickerFragment extends android.app.DialogFragment
			implements TimePickerDialog.OnTimeSetListener {

		int hourOfDay;
		int minutes;
		TimePickerButton parent;

		// Default constructor for device rotation
		public TimePickerFragment() {
		}

		public static TimePickerFragment newIntance(int hourOfDay, int minutes,
				TimePickerButton parent) {
			TimePickerFragment frag = new TimePickerFragment();
			frag.hourOfDay = hourOfDay;
			frag.minutes = minutes;
			frag.parent = parent;

			Bundle args = new Bundle();
			args.putInt("hourOfDay", hourOfDay);
			args.putInt("minutes", minutes);
			args.putInt("parent", parent.getId());
			frag.setArguments(args);

			return frag;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (parent == null) {
				int id = getArguments().getInt("parent");
				parent = (TimePickerButton) getActivity().findViewById(id);
			}
			if (parent != null) {
				final Calendar c = parent.getCalendar(hourOfDay, minute);
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
			hourOfDay = getArguments().getInt("hourOfDay");
			minutes = getArguments().getInt("minutes");

			return new TimePickerDialog(getActivity(), this, hourOfDay,
					minutes, true);
		}
	}

}
