package com.ratemypark.server;

// Mostly taken from CPSC 210 Android application project

/**
 * Encapsulates a latitude/longitude pair.
 */
public class Coordinate {
	
	// This class is fully implemented for you.
	
	// Lat and long values
	private double latitude;
	private double longitude;
	
	/**
	 * Constructor
	 * 
	 * <br><br>
	 * <b>Requires:</b> latitude is between -90 and 90 and longitude is between -180 and 180
	 * <b>Modifies:</b>: (similar to all constructors, creates objects and modifies this)<br><br>
	 * <b>Effects:</b> latitude and longitude are correctly set in the object<br><br>
	 * 
	 * @param latitude The latitude 
	 * @param longitude The longitude
	 */
	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Getter for latitude
	 * 
	 * <br><br>
	 * <b>Effects:</b><br>Returns the latitude value between -90 and 90 <br><br>
	 * 
	 * @return Lat value between -90 and 90
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * Getter for longitude
	 * 
	 * <br><br>
	 * <b>Effects:</b><br>Returns the longitude value between -180 and 180 <br><br>
	 * 
	 * @return Lat value between -180 and 180
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * Find distance between two lat long points
	 * 
	 * <br><br>
	 * <b>Requires:</b> A valid lat long to compare to<br><br>
	 * <b>Effects:</b>The distance between the two points
	 * 
	 * @param other A valid lat long object
	 * @return Distance between the two lat long points
	 */
    public double distanceTo(Coordinate other) {
        double lat1 = Math.toRadians(latitude);
        double lon1 = Math.toRadians(longitude);
        double lat2 = Math.toRadians(other.latitude);
        double lon2 = Math.toRadians(other.longitude);

        double r = 6371;  // Earth's radius.
        return r * Math.acos(
            (Math.sin(lat1) * Math.sin(lat2)) +
            (Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1))
        );
    }

    /**
	 * We override hashCode() and equals(), so that RouteEndpoints (which contains two Coordinate values) can
	 * be used as a key in a HashMap.
	 * 
	 * Code for this method was generated using Eclipse: Source > Generate hashCode() and equals().
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * We override hashCode() and equals(), so that RouteEndpoints (which contains two Coordinate values) can
	 * be used as a key in a HashMap.
	 * 
	 * Code for this method was generated using Eclipse: Source > Generate hashCode() and equals().
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (Double.doubleToLongBits(latitude) != Double
				.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double
				.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

	
}