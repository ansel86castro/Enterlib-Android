package com.enterlib.googleservices.routes;

import com.enterlib.googleservices.Coordinate;

public class Leg {
	public LabeledValue distance;
	
	public LabeledValue duration;
	
	public String end_address;
	
	public Coordinate end_location;
	
	public String start_address;
	
	public Coordinate start_location;
	
	public LegStep[] steps;
}
