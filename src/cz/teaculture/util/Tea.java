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
			
			result = (hour >= open && hour <= close);
		}
		
		return result;
	}
	
	/**
	 * Vycuca a zformatuje informaci o otevreni/zavreni
	 * @param openingTimes
	 * @return
	 */
	public static String getOpenedStatus(List<List<Short>> openingTimes){
		String result = "";
		
		// TODO: je to koser podminka?
		if(openingTimes.size() == 7){
			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
			
			Short open = openingTimes.get(day).get(0);
			Short close = openingTimes.get(day).get(1);
			
			int hour = (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE);
			
			// pokud je otevreno, mrknem, jak dlouho jeste bude
			if(hour >= open && hour <= close){
				int howLong = close - hour;  // doba do zaviracky v minutach
				if(howLong < 60)
					result = "Otevreno jeste " + howLong + " minut.";
				else
					result = "Otevreno.";
			}else{ // pokud je zavreno, tak to stejne :-)
				int howLong = hour - open;  // doba do otviracky v minutach
				if(howLong < 60)
					result = "Zavreno jeste " + howLong + " minut.";
				else
					result = "Zavreno.";
			}
		}
		
		return result;
	}

}
