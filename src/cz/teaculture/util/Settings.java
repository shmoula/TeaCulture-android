package cz.teaculture.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper pro praci s nastavenim programu
 * @author vbalak
 *
 */
public class Settings {
	public static final String[] distanceStr = {"0.5 km", "1 km", "5 km", "10 km", "20 km", "50 km"};
	public static final int[] distanceVal = {500, 1000, 5000, 10000, 20000, 50000};
	
	private SharedPreferences mPrefs;

	public Settings(Context context) {
		mPrefs = context.getSharedPreferences("preferences", 0);
	}
	
	/**
     * Priznak, zda byla aplikace jiz spustena
     * @return
     */
    public boolean isFirstRun() {
    	return mPrefs.getBoolean("firstRun", true);
    }
    
    /**
     * Nastavi priznak, ze jiz bylo spusteno
     */
    public void setRunnedFlag() {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean("firstRun", false);
        edit.commit();
    }
    
    /**
     * Priznak, zda pro urcovani polohy pouzivat GPS
     * @return
     */
    public boolean isUseGps() {
    	return mPrefs.getBoolean("useGps", false);
    }
    
    /**
     * Nastavi priznak pouzivani GPSky
     * @param use
     */
    public void setUseGps(boolean use) {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean("useGps", use);
        edit.commit();
    }
    
    /**
     * Vraci okruh - jeho index, ve kterem se zobrazuji cajovny
     * @return
     */
    public int getDistanceFilter() {
    	return mPrefs.getInt("distanceFilter", 2);
    }
    
    /**
     * Vraci vzdalenost, do ktere se zobrazuji cajovny
     * @return
     */
    public int getSavedDistanceVal() {
    	return distanceVal[getDistanceFilter()];
    }
    
    /**
     * Vraci string se vzdalenosti, do ktere se zobrazuji cajovny
     * @return
     */
    public String getSavedDistanceStr() {
    	return distanceStr[getDistanceFilter()];
    }
    
    /**
     * Ulozi filtr vzdalenosti
     * @param distance
     */
    public void setDistanceFilter(int distance) {
    	SharedPreferences.Editor edit = mPrefs.edit();
        edit.putInt("distanceFilter", distance);
        edit.commit();
    }
}
