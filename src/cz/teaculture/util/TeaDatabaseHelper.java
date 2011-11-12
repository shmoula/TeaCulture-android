package cz.teaculture.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.teaculture.domain.Tearoom;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper pro ukladani cajoven do databaze
 * @author vbalak
 *
 */
public class TeaDatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
	
	public static final String FIELD_LIST = "tearoomList";
    
    private static final String DATABASE_NAME = "TearoomsDatabase";
    private static final String TABLE_NAME = "tearoomList";
    
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_CHANGED = "changed";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LNG = "lng";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_WEBSITE = "website";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_WIFI = "wifi";
    private static final String COLUMN_OPEN_HOURS = "open";
    
    
    
    private static final String TABLE_CREATE_QUERY = "CREATE TABLE " + TABLE_NAME + " (" 
    													+ COLUMN_ID + " INTEGER PRIMARY KEY,"
    													+ COLUMN_CITY + " TEXT,"
    													+ COLUMN_ADDRESS + " TEXT,"
    													+ COLUMN_CHANGED + " INTEGER,"
    													+ COLUMN_LAT + " REAL,"
    													+ COLUMN_LNG + " REAL,"
    													+ COLUMN_NAME + " TEXT,"
    													+ COLUMN_WEBSITE + " TEXT,"
    													+ COLUMN_PHONE + " TEXT,"
    													+ COLUMN_EMAIL + " TEXT,"
    													+ COLUMN_WIFI + " INTEGER,"
    													+ COLUMN_OPEN_HOURS + " DATA"
    													+ ");";
    

	public TeaDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * Nacte seznam cajoven z databaze
	 * @return
	 */
	public List<Tearoom> loadTearooms() {
		List<Tearoom> result = null;
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_NAME, 
				new String[] {COLUMN_ID, COLUMN_CITY, COLUMN_ADDRESS, COLUMN_CHANGED, COLUMN_LAT, COLUMN_LNG, COLUMN_NAME, COLUMN_WEBSITE, COLUMN_PHONE, COLUMN_EMAIL, COLUMN_WIFI, COLUMN_OPEN_HOURS},
				null, null, null, null, null
		);
		
		if (cursor != null) {
			result = new ArrayList<Tearoom>(cursor.getCount());
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				Tearoom tearoom = extractTearoom(cursor);
				result.add(tearoom);
			}
    	
    		cursor.close();
    	}
		
		return result;
	}
	
	/**
	 * Vrati cajovnu se specifikovanym id
	 * @param id
	 * @return
	 */
	public Tearoom loadTearoom(long id) {
		Tearoom tearoom = null;
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_NAME, 
				new String[] {COLUMN_ID, COLUMN_CITY, COLUMN_ADDRESS, COLUMN_CHANGED, COLUMN_LAT, COLUMN_LNG, COLUMN_NAME, COLUMN_WEBSITE, COLUMN_PHONE, COLUMN_EMAIL, COLUMN_WIFI, COLUMN_OPEN_HOURS},
				COLUMN_ID + "=" + id, null, null, null, null
		);
		
		if (cursor != null) {
			cursor.moveToFirst();
			tearoom = extractTearoom(cursor);
			cursor.close();
		}
		
		return tearoom;
	}
	
	/**
	 * Vyextrahuje z dane pozice kurzoru cajovnu
	 * @param cursor
	 * @return
	 */
	private Tearoom extractTearoom(Cursor cursor) {
		Tearoom tearoom = new Tearoom();
		
		try {
			byte[] bArray = cursor.getBlob(cursor.getColumnIndex(COLUMN_OPEN_HOURS));
			List<List<Short>> openHours = Stuff.setObjectFromByteArray(bArray, ArrayList.class);
			tearoom.setOpen_hours(openHours);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		tearoom.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
		tearoom.setCity(cursor.getString(cursor.getColumnIndex(COLUMN_CITY)));
		tearoom.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
		tearoom.setChanged_at(new Date(cursor.getInt(cursor.getColumnIndex(COLUMN_CHANGED))));
		tearoom.setLat(cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT)));
		tearoom.setLng(cursor.getDouble(cursor.getColumnIndex(COLUMN_LNG)));
		tearoom.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
		tearoom.setWebsite(cursor.getString(cursor.getColumnIndex(COLUMN_WEBSITE)));
		tearoom.setPhone(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
		tearoom.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
		tearoom.setWifi(cursor.getInt(cursor.getColumnIndex(COLUMN_WIFI)) != 0);
		
		return tearoom;
	}
	
	/**
	 * Ulozi seznam s cajovnama
	 * @param teaList
	 */
	public void saveTearooms(List<Tearoom> teaList) {
		SQLiteDatabase db = getWritableDatabase();
		
		if (teaList != null) {
			db.beginTransaction();
			try {
				recreateTable(db);

				for (Tearoom tea : teaList) {
					ContentValues dataToInsert = prepareRow(tea);
			    	
					if(dataToInsert != null)
			    		db.insert(TABLE_NAME, null, dataToInsert);
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
	
	/**
	 * Vytvori radek na vlozeni do db
	 * @param tea
	 * @return
	 */
	private ContentValues prepareRow(Tearoom tea) {
		ContentValues dataToInsert = new ContentValues();                          
    	
		// Zacneme hodinama, protoze ty maji sanci padnout na hubu - potom se nevlozi nic ;-)
    	try {
			dataToInsert.put(COLUMN_OPEN_HOURS, Stuff.getObjectAsByteArray(tea.getOpen_hours()));
			
			dataToInsert.put(COLUMN_ID, tea.getId());
	    	dataToInsert.put(COLUMN_CITY, tea.getCity());
	    	dataToInsert.put(COLUMN_ADDRESS, tea.getAddress());
	    	dataToInsert.put(COLUMN_CHANGED, tea.getChanged_at().getTime());
	    	dataToInsert.put(COLUMN_LAT, tea.getLat());
	    	dataToInsert.put(COLUMN_LNG, tea.getLng());
	    	dataToInsert.put(COLUMN_NAME, tea.getName());
	    	dataToInsert.put(COLUMN_WEBSITE, tea.getWebsite());
	    	dataToInsert.put(COLUMN_PHONE, tea.getPhone());
	    	dataToInsert.put(COLUMN_EMAIL, tea.getEmail());
	    	dataToInsert.put(COLUMN_WIFI, tea.isWifi());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return dataToInsert;
	}
	
	/**
	 * Dropne databazi
	 */
	private void recreateTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE_QUERY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		recreateTable(db);
	}

}
