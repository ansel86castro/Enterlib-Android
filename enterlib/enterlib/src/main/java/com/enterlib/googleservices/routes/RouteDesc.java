package com.enterlib.googleservices.routes;

import com.enterlib.googleservices.Bound;

import java.io.Serializable;

public class RouteDesc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Bound bounds;
	
	public String copyrights;
	
	public Leg[] legs;
	
	public String summary;
	
	public String warnings;
}
