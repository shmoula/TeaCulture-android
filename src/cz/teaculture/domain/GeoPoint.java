package cz.teaculture.domain;

import android.location.Location;

/**
 * Geo pozice a pomocne rutiny pro praci s ni
 * Neplest s com.google.android.maps.GeoPoint
 * @author vbalak
 *
 */
public class GeoPoint {
	private double latitude;
	private double longitude;
	
	
	public GeoPoint(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public GeoPoint(Location location){
		this.latitude = location.getLatitude();
		this.longitude = location.getLongitude();
	}
	
	/**
	 * Vraci vzdalenost k zadanemu cili v metrech
	 * @param end
	 * @return
	 */
	public float distanceTo(GeoPoint end) {
        float[] results = new float[2];
        
        android.location.Location.distanceBetween(latitude, longitude, end.getLatitude(), end.getLongitude(), results);
        
        return results[0];
    }
	
	/**
	 * Vraci vzdalenost k zadanemu cili v metrech
	 * @param location
	 * @return
	 */
	public float distanceTo(Location location) {
		GeoPoint geoPoint = new GeoPoint(location);
		
		return distanceTo(geoPoint);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
