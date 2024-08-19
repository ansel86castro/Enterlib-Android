package com.enterlib;

import com.enterlib.converters.CalendarConverter;
import com.enterlib.converters.DateConverter;

import java.util.Calendar;
import java.util.Date;

/**
 * This class handles common dates operations like conversions between dates and
 * calendars, get the current date without the time amoung others
 * Functionalities
 * 
 * */
public class DateUtils {

	private static DateConverter dateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");

	public static Calendar getCalendar(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public static Calendar getCalendar(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		return c;
	}

	public static Calendar getCalendar(int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(0, 0, 0, hourOfDay, minute);
		return c;
	}

	public static Date getDate(int year, int month, int day) {
		final Calendar c = getCalendar(year, month, day);
		Date date = c.getTime();
		return date;
	}

	public static Date getTime(int hourOfDay, int minute) {
		final Calendar c = getCalendar(hourOfDay, minute);
		Date date = c.getTime();
		return date;
	}

	public static Date getDateTime(int year, int month, int day, int hourOfDay,
			int minute) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hourOfDay, minute);
		return c.getTime();
	}

	/** Creates a date time from a date and a time */
	public static Date getDateTime(Date date, Date time) {
		Calendar cdate = Calendar.getInstance();
		cdate.setTime(date);

		Calendar ctime = Calendar.getInstance();
		ctime.setTime(time);

		cdate.set(Calendar.HOUR_OF_DAY, ctime.get(Calendar.HOUR_OF_DAY));
		cdate.set(Calendar.MINUTE, ctime.get(Calendar.MINUTE));
		cdate.set(Calendar.SECOND, ctime.get(Calendar.SECOND));
		return cdate.getTime();
	}

	/** returns the current date without the time specification */
	public static Date getCurrentDate() {
		return getCurrentCalendar() .getTime();
	}

	public static Calendar getCurrentCalendar(){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return  c;
	}

	public static TimeValue getTimeValue(Date date) {
		Calendar cdate = Calendar.getInstance();
		cdate.setTime(date);
		return new TimeValue(cdate.get(Calendar.HOUR_OF_DAY),
				cdate.get(Calendar.MINUTE), cdate.get(Calendar.SECOND));
	}

	public static String getDbDate(Date date){
		return dateConverter.getString(date);
	}

	public static String getDbDate(Calendar date){
		return dateConverter.getString(date.getTime());
	}
}
