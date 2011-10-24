package cz.teaculture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import cz.teaculture.domain.GeoPoint;
import cz.teaculture.domain.Tearoom;
import cz.teaculture.util.Stuff;
import cz.teaculture.util.Tea;
import cz.teaculture.util.TearoomOpenHelper;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Seznam vsech cajoven
 * @author vbalak
 *
 */
public class MainActivity extends ListActivity {
    private static String TAG = "teacultureMain";
    
    private static final int SHOW_TEAROOM_DETAILS_ID = 0;
    private static final int NAVIGATE_TO_ID = 1;
    
    private ProgressDialog mProgressDialog;
    private SimpleAdapter mTreeAdapter;
    private SharedPreferences mPrefs;
    private TearoomOpenHelper mOpenHelper;
    
    private Location mMyLocation;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.troom_list);
        
        // Nacteni preferences
        Context mContext = this.getApplicationContext();
        mPrefs = mContext.getSharedPreferences("preferences", 0);
        
        // Inicializace napojeni na SQLite
        mOpenHelper = new TearoomOpenHelper(mContext);
        
        TextView tv = (TextView) findViewById(R.id.infobar);
		tv.setText("Teaculture");
		
		// Pridani kontextoveho menu do seznamu
		registerForContextMenu(getListView());
    }
    
    @Override
	public void onStart() {
		super.onStart();
		
		// pokusim se zafixovat pozici
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		setNewLocation(getLastKnownLocation(false), false);  // nastavim posledni znamou prozatim
		mLocationListener = new MyLocationListener();
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		
		// natahnu data do view
		if(isFirstRun()){
			new GetTeaRoomsTask().execute();
			setRunnedFlag();
		} else {
			loadFromDatabase();
		}
	}
    
    /**
     * Helper pro nastaveni pozice
     * @param debug - pokud se debuguje, vygeneruje lokaci
     * @return
     */
    private Location getLastKnownLocation(boolean debug) {
    	Location result = null;
    	
    	if(debug){
    		result = new Location("debug");
    		result.setLatitude(49);
    		result.setLongitude(16);
    	} else {
    		result = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	}

    	return result;
    }
    
    /**
     * Nastavi novou lokaci a volitelne odstrani listener a prestane updatovat polohu
     * @param disableUpdates
     * @param location
     */
    private void setNewLocation(Location location, boolean disableUpdates) {
    	mMyLocation = location;
    	
    	if(disableUpdates)
    		mLocationManager.removeUpdates(mLocationListener);
    }
    
    /**
     * Priznak, zda byla aplikace jiz spustena
     * @return
     */
    private boolean isFirstRun(){
    	return mPrefs.getBoolean("firstRun", true);
    }
    
    /**
     * Nastavi priznak, ze jiz bylo spusteno
     */
    private void setRunnedFlag() {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean("firstRun", false);
        edit.commit();
    }
    
    /**
     * Ulozi do databaze z webu nacteny seznam cajoven
     * @param tearoomList
     * @throws IOException
     */
    private void saveToDatabase(List<Tearoom> tearoomList) throws IOException{
    	byte[] tearoomByteArray = Stuff.getObjectAsByteArray(tearoomList);
    	mOpenHelper.saveNewList(tearoomByteArray);
    }
    
    /**
     * Nacte z SQLite databaze jiz ulozene cajovny
     */
    private void loadFromDatabase(){
    	byte[] bArray = mOpenHelper.getSavedList();
    	
    	try {
			List<Tearoom> tearoomList = Stuff.setObjectFromByteArray(bArray, ArrayList.class);
			refreshTreeList(tearoomList);
		} catch (IOException e) {
			e.printStackTrace();
			
			// V pripade neuspechu se pokusim nacist cajky z webu
			new GetTeaRoomsTask().execute();
		}
    }
    
    
    @Override
    /**
     * Kontextove menu polozky v seznamu
     */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, SHOW_TEAROOM_DETAILS_ID, 0, R.string.context_details);
		menu.add(0, NAVIGATE_TO_ID, 1, R.string.context_navigate_to);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Vytazeni id cajovny, nad kterou bylo vyvolano kontextove menu
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		String tearoomId = getTearoomParameter(info.position, "id");
		
		switch(item.getItemId()){
		case SHOW_TEAROOM_DETAILS_ID:
			openTearoomDetails(tearoomId);
			return true;
			
		case NAVIGATE_TO_ID:
			String mLatitude = getTearoomParameter(info.position, "lat");
			String mLongitude = getTearoomParameter(info.position, "lng");
			
			openNavigation(tearoomId, mLatitude, mLongitude);
			return true;
		}
		
		return super.onContextItemSelected(item);
	}
    
    /**
	 * Klepnuti na item v seznamu
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		String tearoomId = getTearoomParameter(position, "id");
		openTearoomDetails(tearoomId);
	}
	
	/**
	 * Otevre detaily cajovny
	 * @param tearoomId
	 */
	private void openTearoomDetails(String tearoomId){
		Bundle bundle = new Bundle();
		bundle.putString("tearoomId", tearoomId);
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, TearoomActivity.class);
	    intent.putExtras(bundle);
		startActivity(intent);
	}
	
	/**
	 * Otevre dialog s vyberem navigace
	 * TODO: zprovoznit vyber - nejaky intent navigateTo
	 * TODO: vytvorit vlastni helper - volano z TearoomActivity
	 * @param tearoomId
	 */
	private void openNavigation(String tearoomId, String mLatitude, String mLongitude) {
		startActivity(
				new Intent(
						Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + mLatitude + "," + mLongitude)
		));
	}
	
	/**
	 * Vrati dany parametr cajovny
	 * @param position
	 * @return
	 */
	private String getTearoomParameter(int position, String key){
		Object o = mTreeAdapter.getItem(position);
		if(o instanceof Map){
			Map<String, String> tearoomInfo = (Map<String, String>) o;
			return tearoomInfo.get(key);
		}
		return null;
	}
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
	    
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.troom_list_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.troom_list_menu_refresh:
	    	new GetTeaRoomsTask().execute();
	        return true;
	    case R.id.troom_list_menu_map:
	    	return true;
	    case R.id.troom_list_menu_info:  // Otevreni obrazovky s informacema
	    	Intent intent = new Intent();
			intent.setClass(MainActivity.this, InfoActivity.class);
			startActivity(intent);
			
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
    private void showLoadingProgressDialog() {
		mProgressDialog = ProgressDialog.show(this, "", "Nacitam, strpeni prosim...", true);
	}

	private void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
	
	/**
	 * Touto metodou se preda listu seznam cajoven (z db ci z netu)
	 * @param tearoomList
	 */
	private void refreshTreeList(List<Tearoom> tearoomList) {
		if (tearoomList == null || tearoomList.size() == 0){
			return;
		}
		
		List<Map<String, String>> tearooms = new ArrayList<Map<String, String>>();
		
		// iterace skrze natazene cajovny a buildovani modelu pro view
		for (Tearoom tearoom : tearoomList) {
			Map<String, String> tearoomInfo = new HashMap<String, String>();
			tearoomInfo.put("id", Long.toString(tearoom.getId()));
			tearoomInfo.put("name", tearoom.getName());
			tearoomInfo.put("lat", Double.toString(tearoom.getLat()));
			tearoomInfo.put("lng", Double.toString(tearoom.getLng()));
			tearoomInfo.put("city", tearoom.getCity());
			tearoomInfo.put("opened", Tea.getOpenedStatus(tearoom.getOpen_hours()));
			
			// odhad vzdalenosti
			GeoPoint geoPoint = new GeoPoint(tearoom.getLat(), tearoom.getLng());
			float distance = geoPoint.distanceTo(mMyLocation);
			tearoomInfo.put("distance", Float.toString(distance) + "m");
			
			tearooms.add(tearoomInfo);
		}

		// listAdapter pro seznam
		mTreeAdapter = new SimpleAdapter(this, tearooms,
				R.layout.troom_list_item,
				new String[] { "name", "opened", "city", "distance" }, new int[] { R.id.name, R.id.opened, R.id.city , R.id.distance});
		
		setListAdapter(mTreeAdapter);
	}
    
    private class GetTeaRoomsTask extends AsyncTask<Void, Void, List<Tearoom>> {

		public GetTeaRoomsTask() {
		}

		@Override
		protected void onPreExecute() {
			showLoadingProgressDialog();
		}

		@Override
		protected List<Tearoom> doInBackground(Void... params) {
			try {
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

				final String url = "http://www.teaculture.cz/api/tearooms";
				Tearoom[] result = restTemplate.getForObject(url, Tearoom[].class);

				return Arrays.asList(result);
			} catch (Exception e) {
				Stuff.logException(e, TAG);
			}

			return null;
		}

		@Override
		protected void onPostExecute(List<Tearoom> result) {
			dismissProgressDialog();
			
			if(result == null || result.isEmpty())
				return;
			
			refreshTreeList(result);
			
			// Pokus o zazalohovani nacteneho seznamu cajoven, aby se priste nemuselo nacitat
			try {
				saveToDatabase(result);
			} catch (IOException e) {
				Stuff.logException(e, TAG);
			}
		}
	}
    
    /**
     * Implementace LocationListeneru - pokusi se zjistit pozici dle BTS a po uspechu sam zdechne
     * @author vbalak
     *
     */
    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
        	setNewLocation(location, true);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
      };
}

