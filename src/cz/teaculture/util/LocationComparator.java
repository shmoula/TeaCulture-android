package cz.teaculture.util;

import java.util.Comparator;

import cz.teaculture.domain.GeoPoint;

import android.location.Location;

/**
 * Komparator - pro razeni seznamu podle vzdalenosti
 * @author vbalak
 *
 */
public class LocationComparator implements Comparator <Location> {
	private GeoPoint geoPoint;
	
	public LocationComparator(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}

	@Override
	public final int compare(Location location1, Location location2) {
		// TODO: osetrit stavy, kdy je jedna nebo druha lokace == null
		return Float.compare(geoPoint.distanceTo(location1), geoPoint.distanceTo(location2));
	}

}
