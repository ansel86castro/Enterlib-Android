package com.enterlib;

/** A Class that holds a Hour, Minutes and Seconds component */
public class TimeValue {
	public int Hours;
	public int Minutes;
	public int Seconds;

	public TimeValue() {
		super();
	}

	public TimeValue(int hours, int minutes, int seconds) {
		Hours = hours;
		Minutes = minutes;
		Seconds = seconds;
	}

}