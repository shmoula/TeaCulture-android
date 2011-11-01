package cz.teaculture.util;

import java.util.Comparator;

import cz.teaculture.domain.Tearoom;

/**
 * Komparator slouzici k setrideni cajoven
 * Kriterium = nazev mesta
 * @author vbalak
 *
 */
public class CityComparator implements Comparator<Tearoom> {

	@Override
	public int compare(Tearoom o1, Tearoom o2) {
		return o1.getCity().compareTo(o2.getCity());
	}

}
