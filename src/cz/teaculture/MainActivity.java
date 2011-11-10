package cz.teaculture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import cz.teaculture.domain.GeoPoint;
import cz.teaculture.domain.Tearoom;
import cz.teaculture.util.LocationComparator;
import cz.teaculture.util.Settings;
import cz.teaculture.util.Stuff;
import cz.teaculture.util.Tea;
import cz.teaculture.util.TeaDatabaseHelper;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
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
    
    private static final String URL_ALL_TEAROOMS = "http://www.teaculture.cz/api/tearooms";
    
    private static final int SHOW_TEAROOM_DETAILS_ID = 0;
    private static final int NAVIGATE_TO_ID = 1;
    
    private static final boolean DEBUGING_ENABLED = false;
    
    private ProgressDialog mProgressDialog;
    private SimpleAdapter mTearoomAdapter;
    private TeaDatabaseHelper mTeaDatabaseHelper;
    private Settings mSettings;
    
    private Location mMyLocation;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    
    private TextView mTvHeader;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.troom_list);
        
        // Nacteni preferences
        mSettings = new Settings(getApplicationContext());
        
        // Inicializace napojeni na SQLite
        mTeaDatabaseHelper = new TeaDatabaseHelper(getApplicationContext());
		
		// Pridani kontextoveho menu do seznamu
		registerForContextMenu(getListView());
		
		// Pridani hlavicky a paticky do seznamu
		//getListView().addFooterView(createFooterView());
		//getListView().addHeaderView(createHeaderView());
    }
    
    @Override
	public void onStart() {
		super.onStart();
		
		// pokusim se zafixovat pozici
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		setNewLocation(getLastKnownLocation(), false);  // nastavim posledni znamou prozatim
		mLocationListener = new MyLocationListener();
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		
		// natahnu data do view
		if(mSettings.isFirstRun()){
			new GetTeaRoomsTask().execute();
			mSettings.setRunnedFlag();
		} else {
			loadFromDatabase();
		}
	}

	@Override
	protected void onPause() {
		mLocationManager.removeUpdates(mLocationListener);
	    mTeaDatabaseHelper.close();
	      
		super.onPause();
	}

	/**
     * Vytvori view se zapatim, ktere se prida na konec seznamu
     * @return
     */
	private View createFooterView() {
		Button footer = new Button(this);
		
		footer.setText("Refresh");
		footer.setClickable(true);
		
		footer.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new GetTeaRoomsTask().execute();
			}
		});

		return footer;
	}
	
	/**
	 * Vytvori view se zahlavim seznamu (ne nadpis, ale to informacni pole)
	 * @return
	 */
	private View createHeaderView(){
		View header = getLayoutInflater().inflate(R.layout.troom_list_header, null);
		
		mTvHeader = (TextView) header.findViewById(R.id.troom_list_header_title);
		updateHeaderLine();
		header.setEnabled(false);
		
		return header;
	}
	
	/**
     * Update textu v hlavicce seznamu
     * @return
     */
	private void updateHeaderLine() {
	
		String accuracy = "Hledam polohu...";
		try{
			accuracy = "Presnost polohy " + Float.toString(mMyLocation.getAccuracy()) + " m";
			accuracy += ", filtr " + mSettings.getSavedDistanceStr();
		} catch (NullPointerException e){}
		
		mTvHeader.setText(accuracy);
	}
	
	/**
	 * Event volany pri kliknuti na ikonku v ActionBaru
	 * @param view
	 */
	public void showInfo(View view) {
		Intent intent = new Intent();
		
		intent.setClass(MainActivity.this, InfoActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Event volany pri stisknuti ikonky refresh v actionBaru
	 */
	public void doRefresh(View view) {
		new GetTeaRoomsTask().execute();
	}
    
    /**
     * Helper pro nastaveni pozice
     * @param debug - pokud se debuguje, vygeneruje lokaci
     * @return
     */
    private Location getLastKnownLocation() {
    	Location result = null;
    	
    	if(DEBUGING_ENABLED){
    		result = new Location("debug");
    		result.setLatitude(49.8);
    		result.setLongitude(18.2);
    		result.setAccuracy(500);
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
    	
    	//updateHeaderLine();
    	
    	if(disableUpdates)
    		mLocationManager.removeUpdates(mLocationListener);
    }
    
    /**
     * Ulozi do databaze z webu nacteny seznam cajoven
     * @param tearoomList
     */
    private void saveToDatabase(List<Tearoom> tearoomList) {
    	mTeaDatabaseHelper.saveTearooms(tearoomList);
    }
    
    /**
     * Nacte z SQLite databaze jiz ulozene cajovny
     */
    private void loadFromDatabase(){
    	List<Tearoom> tearoomList = mTeaDatabaseHelper.loadTearooms();
    	
    	// V pripade neuspechu se pokusim nacist cajky z webu
    	if (tearoomList == null || tearoomList.size() == 0)
    		new GetTeaRoomsTask().execute();
    	else
    		refreshTreeList(tearoomList);
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
		
		// Osetreni uzivatelova kliknuti na header nebo footer
		if(info.position == 0 || info.position > mTearoomAdapter.getCount())
			return super.onContextItemSelected(item);
		
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
		
		// Osetreni uzivatelova kliknuti na header nebo footer
		if(position == 0 || position > mTearoomAdapter.getCount())
			return;
		
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
		//position -= 1; // polozky jsou o jednu posunute diky zahlavi
		
		Object o = mTearoomAdapter.getItem(position);
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
		Intent intent = new Intent();
		
	    switch (item.getItemId()) {
	    case R.id.troom_list_menu_refresh:  // Znovunacteni seznamu z webu
	    	new GetTeaRoomsTask().execute();
	        return true;
	    case R.id.troom_list_menu_map:
	    	return true;
	    case R.id.troom_list_menu_info:  // Otevreni obrazovky s informacema
	    	intent.setClass(MainActivity.this, InfoActivity.class);
			startActivity(intent);
			
	    	return true;
	    case R.id.troom_list_menu_settings:  // Otevrei nastaveni
			intent.setClass(MainActivity.this, SettingsActivity.class);
			startActivity(intent);
			
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
    private void showLoadingProgressDialog() {
		mProgressDialog = ProgressDialog.show(this, "", getString(R.string.loading), true);
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
		
		// Setrideni seznamu podle vzdalenosti
		Collections.sort(tearoomList, new LocationComparator(mMyLocation));
		
		List<Map<String, String>> tearooms = new ArrayList<Map<String, String>>();
		
		// hodnota - vzdalenost, do ktere se budou zobrazovat cajovny
		//int distanceFilter = mSettings.getSavedDistanceVal();
		
		int tearoomCounter = 0; // pocitadlo - po dosazeni 30 se prerusi vypis
		
		// iterace skrze natazene cajovny a buildovani modelu pro view
		for (Tearoom tearoom : tearoomList) {
			Map<String, String> tearoomInfo = new HashMap<String, String>();
			
			// odhad vzdalenosti
			GeoPoint geoPoint = new GeoPoint(tearoom.getLat(), tearoom.getLng());
			//float distance = geoPoint.distanceTo(mMyLocation);
			//if(distance > distanceFilter) continue; // filtrovani vzdalenych cajoven
			
			if(++tearoomCounter > 30) break;
			
			// vlozeni jednotlivych hodnot pro adapter
			tearoomInfo.put("id", Long.toString(tearoom.getId()));
			tearoomInfo.put("name", tearoom.getName());
			tearoomInfo.put("lat", Double.toString(tearoom.getLat()));
			tearoomInfo.put("lng", Double.toString(tearoom.getLng()));
			tearoomInfo.put("city", tearoom.getCity());
			tearoomInfo.put("opened", Tea.getOpenedStatus(tearoom.getOpen_hours(), getApplicationContext()));
			
			tearooms.add(tearoomInfo);
		}

		// listAdapter pro seznam
		mTearoomAdapter = new SimpleAdapter(this, tearooms,
				R.layout.troom_list_item,
				new String[] { "name", "opened", "city" }, new int[] { R.id.name, R.id.opened, R.id.city });

		setListAdapter(mTearoomAdapter);
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

				final String url = URL_ALL_TEAROOMS;
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
			saveToDatabase(result);
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

