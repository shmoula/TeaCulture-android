package cz.teaculture.util;

import java.util.Calendar;
import java.util.List;

import cz.teaculture.R;

import android.content.Context;


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
	public static String getOpenedStatus(List<List<Short>> openingTimes, Context context){
		String result = "";
		
		// TODO: je to koser podminka?
		if(openingTimes.size() == 7){
			Calendar cal = Calendar.getInstance();
			int day = cal.get(Calendar.DAY_OF_WEEK) - 1;  // SUNDAY = 1, SATURDAY = 7
			
			Short open = openingTimes.get(day).get(0);
			Short close = openingTimes.get(day).get(1);
			
			// obcas je vraceno null - nenacetly se korektne casy z webu? nebo chybi?
			if(open == null || close == null)
				return context.getString(R.string.unknown);
			
			int currentMinute = (cal.get(Calendar.HOUR_OF_DAY) * 60) + cal.get(Calendar.MINUTE);
			
			// pokud je otevreno, mrknem, jak dlouho jeste bude
			if(currentMinute >= open && currentMinute <= close){
				int howLong = close - currentMinute;  // doba do zaviracky v minutach
				if(howLong < 120 && howLong > 0)
					result = context.getString(R.string.still_opened) + " " + howLong + " minut";
				else
					result = context.getString(R.string.opened);
			}else{ // pokud je zavreno, tak to stejne :-)
				int howLong = open - currentMinute;  // doba do otviracky v minutach
				if(howLong < 120 && howLong > 0)
					result = context.getString(R.string.still_closed) + " " + howLong + " minut.";
				else
					result = context.getString(R.string.closed);
			}
		}
		
		return result;
	}

}
