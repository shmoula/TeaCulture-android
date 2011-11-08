package cz.teaculture;

import cz.teaculture.util.Settings;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Dialog s nastavenim programu
 * @author vbalak
 *
 */
public class SettingsActivity extends Activity implements OnItemSelectedListener {
	private Settings mSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		mSettings = new Settings(getApplicationContext());
		
		// bindovani komponent
		Spinner distanceSpinner = (Spinner) findViewById(R.id.distance_filter);
		setupDistanceSpinner(distanceSpinner);
		
		CheckBox useGps = (CheckBox) findViewById(R.id.use_gps);
		setupUseGpsCheckBox(useGps);
	}
	
	/**
	 * Nastaveni vlastnosti spinneru
	 * @param spinner
	 */
	private void setupDistanceSpinner(Spinner spin) {
		spin.setOnItemSelectedListener(this);
		
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Settings.distanceStr);

		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(aa);	
		
		spin.setSelection(mSettings.getDistanceFilter());
	}
	
	/**
	 * Nastavi vlastnosti checkboxu
	 * @param box
	 */
	private void setupUseGpsCheckBox(CheckBox box) {
		box.setChecked(mSettings.isUseGps());
		
		box.setOnClickListener(new OnClickListener() {
			// prepnuti stavu pouzivani GPS
		    public void onClick(View v) {
		    	if(v instanceof CheckBox) { // zajimal by me pripad, kdy tohle bude false :-)
		    		boolean useGps = !mSettings.isUseGps();
		    		((CheckBox)v).setChecked(useGps);
		    		mSettings.setUseGps(useGps);
		    	}
		    }
		});
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mSettings.setDistanceFilter(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// nic :-p
	}


}
