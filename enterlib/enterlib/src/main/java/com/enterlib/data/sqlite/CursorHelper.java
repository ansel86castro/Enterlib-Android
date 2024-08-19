package com.enterlib.data.sqlite;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.enterlib.StringUtils;
import com.enterlib.converters.DateConverter;

public class CursorHelper extends CursorWrapper {

	private DateConverter dateConverter;

	public CursorHelper(Cursor cursor) {
		super(cursor);

	}
	
	public static CursorHelper create(Cursor cursor){
		return new CursorHelper(cursor);		
	}

	@Override
	public String getString(int columnIndex) {
		if (isNull(columnIndex)) {
			return null;
		}
		return super.getString(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) {
		if (isNull(columnIndex)) {
			return 0;
		}
		return super.getInt(columnIndex);
	}

	public Integer getInteger(int columnIndex) {
		if (isNull(columnIndex)) {
			return null;
		}
		return super.getInt(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex) {
		if (isNull(columnIndex)) {
			return 0;
		}
		return super.getDouble(columnIndex);
	}

	public Double getDoubleObject(int columnIndex) {
		if (isNull(columnIndex)) {
			return null;
		}
		return super.getDouble(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) {
		if (isNull(columnIndex)) {
			return 0;
		}
		return super.getFloat(columnIndex);
	}

	public Float getFloatObject(int columnIndex) {
		if (isNull(columnIndex)) {
			return null;
		}
		return super.getFloat(columnIndex);
	}

	public boolean getBool(int columnIndex) {
		return getBool(columnIndex, false);
	}

	public boolean getBool(int columnIndex, boolean defaultValue) {
		if (isNull(columnIndex)) {
			return defaultValue;
		}
		return super.getInt(columnIndex) == 1;
	}

	public Boolean getBoolean(int columnIndex) {
		if (isNull(columnIndex)) {
			return null;
		}
		return super.getInt(columnIndex) == 1;
	}

	public Date getDate(int columnIndex) {
		String dateString = getString(columnIndex);
		if (dateString == null || StringUtils.isNullOrWhitespace(dateString)) {
			return null;
		}

		if (dateConverter == null) {
			dateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");
		}
		return dateConverter.getDate(dateString);
	}

	public Calendar getCalendar(int columnIndex) {
		String dateString = getString(columnIndex);
		if (dateString == null || StringUtils.isNullOrWhitespace(dateString)) {
			return null;
		}

		if (dateConverter == null) {
			dateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");
		}

		Calendar c = Calendar.getInstance(Locale.getDefault());
		c.setTime(dateConverter.getDate(dateString));
		return c;
	}

}
