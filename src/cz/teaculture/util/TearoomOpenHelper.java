package cz.teaculture.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Pomocne rutiny pro praci s SQLite databazi
 * TODO: udelat singleton, kde bude stale natazena DB, aby bylo mozne si odpustit queries
 * TODO: vypada to, ze to tu neni prechodne, takze zrusit blob a udelat normalni databazi
 * @author vbalak
 *
 */
public class TearoomOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
	
	public static final String FIELD_LIST = "tearoomList";
    
    private static final String DATABASE_NAME = "TearoomsDatabase";
    private static final String TABLE_NAME = "tearooms";
    
    
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + FIELD_LIST + " DATA);";

    public TearoomOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }
    
    /**
     * Vlozi do databaze byte array
     * @param bArray
     */
    public void saveNewList(byte[] bArray) {
    	SQLiteDatabase db = getWritableDatabase();
    	
    	ContentValues dataToInsert = new ContentValues();                          
    	dataToInsert.put(FIELD_LIST, bArray);
    	db.insert(TABLE_NAME, null, dataToInsert);
    	
    	db.close();
    }
    
    /**
     * Nacte z databaze blob a vrati jej jako byte array
     * @return
     */
    public byte[] getSavedList() {
    	SQLiteDatabase db = getReadableDatabase();
    	byte[] bArray = null;
    	
    	Cursor cursor = db.query(TABLE_NAME, new String[] {FIELD_LIST}, null, null, null, null, null);
    	if (cursor != null) {
			cursor.moveToFirst();
    	
			bArray = cursor.getBlob(0);
    	
    		cursor.close();
    	}
    	
    	db.close();
    	
    	return bArray;
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}