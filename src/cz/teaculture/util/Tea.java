package cz.teaculture.util;

import java.util.Calendar;
import java.util.List;


/**
 * Trida s pomocnymi rutinami
 * Primo se tykajicimi cajovani
 * @author vbalak
 *
 */
public class Tea {
	
	/**
	 * Vraci true/false, jestli je otevreno
	 * Je to pekne prasacky stvoreny, takze
	 * TODO: trochu to zoptimalizovat :-)
	 * @param openingTimes
	 * @return
	 */
	public static boolean isOpened(List<List<Short>> openingTimes){
		boolean result = false;
		
		// TODO: je to koser podminka?
		if(openingTimes.size() == 7){
			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
			
			Short open = openingTimes.get(day).get(0);
			Short close = openingTimes.get(day).get(1);
			
			int hour = (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE);
			
			if(hour >= open && hour <= close)
				result = true;
		}
		
		return result;
	}

}
