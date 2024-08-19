package com.enterlib.googleservices.routes;

import java.util.ArrayList;
import java.util.List;

import com.enterlib.annotations.NotSerializable;
import com.enterlib.googleservices.LatLng;

public class LegPolyline {
	public String points;
	
	@NotSerializable
	List<LatLng> list;
	
	
	 
	public List<LatLng> getList() {
		return list;
	}
	public List<LatLng> parsePoints(){
		list = decodePoly(points);
		return list;
	}
	 /**
	    * Method to decode polyline points
	    * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
	    * */
	    private List<LatLng> decodePoly(String encoded) {
	 
	        List<LatLng> poly = new ArrayList<LatLng>();
	        int index = 0, len = encoded.length();
	        int lat = 0, lng = 0;
	 
	        while (index < len) {
	            int b, shift = 0, result = 0;
	            do {
	                b = encoded.charAt(index++) - 63;
	                result |= (b & 0x1f) << shift;
	                shift += 5;
	            } while (b >= 0x20);
	            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	            lat += dlat;
	 
	            shift = 0;
	            result = 0;
	            do {
	                b = encoded.charAt(index++) - 63;
	                result |= (b & 0x1f) << shift;
	                shift += 5;
	            } while (b >= 0x20);
	            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	            lng += dlng;
	 
	            LatLng p = new LatLng((((double) lat / 1E5)),
	                        (((double) lng / 1E5)));
	            poly.add(p);
	        }
	 
	        return poly;
	    }
}
