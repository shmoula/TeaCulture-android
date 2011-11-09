package cz.teaculture.util;

import java.util.Comparator;

import cz.teaculture.domain.GeoPoint;
import cz.teaculture.domain.Tearoom;

import android.location.Location;

/**
 * Komparator - pro razeni seznamu podle vzdalenosti
 * Srovnava objekty vzhledem k aktualni poloze srovnavatele (=uzivatele_
 * @author vbalak
 *
 */
public class LocationComparator implements Comparator <Tearoom> {
	private GeoPoint myLocation;
	
	public LocationComparator(Location location) {
		this.myLocation = new GeoPoint(location);
	}

	@Override
	public int compare(Tearoom o1, Tearoom o2) {
		Location l1 = o1.getLocation();
		Location l2 = o2.getLocation();
		
		if(l1 == null)
			return 1;
		if(l2 == null)
			return -1;
		if(l2 == null & l1 == null)
			return 0;
		
		return Float.compare(myLocation.distanceTo(l1), myLocation.distanceTo(l2));
	}

}

