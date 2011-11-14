package cz.teaculture;

import java.util.List;

import cz.teaculture.domain.Tearoom;
import cz.teaculture.util.TeaDatabaseHelper;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Zobrazeni cajovny a jejich detailu
 * TODO: Udelat jako fragment
 * @author vbalak
 *
 */
public class TearoomActivity extends Activity {
	private TeaDatabaseHelper mTeaDatabaseHelper;
	private Tearoom mTearoom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tearoom_details);
		
		// Inicializace napojeni na SQLite
        mTeaDatabaseHelper = new TeaDatabaseHelper(getApplicationContext());
		
		// Vytazeni identifikatoru z bundlu a dohledani odpovidajiciho teaRoomu
		Bundle bundle = this.getIntent().getExtras();
		String tearoomId = bundle.getString("tearoomId");
		mTearoom = mTeaDatabaseHelper.loadTearoom(Long.parseLong(tearoomId));
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if(mTearoom != null)
			fillViewsWithInfo();
		else{
			Toast.makeText(getApplicationContext(), "Nastal problem pri nacitani detailu cajovny.", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	@Override
	protected void onPause() {
	    mTeaDatabaseHelper.close();
	      
		super.onPause();
	}
	
	/**
	 * Akce v actionbaru - jit domu :-)
	 * @param view
	 */
	public void goHome(View view) {
		finish();
	}
	
	/**
	 * doplni texty pro jednotlive views
	 */
	private void fillViewsWithInfo(){
		TextView tvName = (TextView) findViewById(R.id.tearoom_name);
		tvName.setText(mTearoom.getName());
		
		TextView tvOpeningTimes = (TextView) findViewById(R.id.tearoom_opening_times);
		tvOpeningTimes.setText(getFormattedOpeningTimes());
		
		TextView tvContacts = (TextView) findViewById(R.id.tearoom_contacts);
		tvContacts.setText(getContacts());
		
		TextView tvAddress = (TextView) findViewById(R.id.tearoom_address);
		tvAddress.setText(getString(R.string.address) + mTearoom.getAddress());
		
		if(mTearoom.isWifi()) {
			TextView tvWifi = (TextView) findViewById(R.id.tearoom_wifi);
			tvWifi.setText(getString(R.string.we_have_wifi));
		}
		
		Button bNavigate = (Button) findViewById(R.id.tearoom_navigate);
		bNavigate.setOnClickListener(new NavigateButtonListener());
	}
	
	/**
	 * Sesklada dohromady kontakty
	 * @return
	 */
	private String getContacts(){
		String website = mTearoom.getWebsite();
		String phone = mTearoom.getPhone();
		String email = mTearoom.getEmail();
		String result = "";
		
		if(website != null & website != "")
			result += getString(R.string.web) + " " + website + "\n";
		
		if(phone != null & phone != "")
			result += getString(R.string.phone) + " " + phone + "\n";
		
		if(email != null & email != "")
			result += getString(R.string.email) + " " + email + "\n";
		
		return result;
	}

	
	/**
	 * Vraci naformatovany string s oteviraci dobou aktualni cajovny
	 * @return
	 */
	private String getFormattedOpeningTimes(){
		String result = "";
		List<List<Short>> openingTimes = mTearoom.getOpen_hours(false);
		String days[] = { 
				getString(R.string.monday), 
				getString(R.string.tuesday),
				getString(R.string.wednesday),
				getString(R.string.thursday),
				getString(R.string.friday),
				getString(R.string.saturday),
				getString(R.string.sunday)
		};
		short dayIndex = 0;
		
		for(List<Short> oneDay : openingTimes){
			result += days[dayIndex++] + ": " + formatOneDayTimes(oneDay) + "\n";
		}
		return result;
	}
	
	/**
	 * Naformatuje casy v jeden den na vhodny string
	 * @param dayTime
	 * @return
	 */
	private String formatOneDayTimes(List<Short> dayTime){
		String result = "";
		
		Short morning = dayTime.get(0);
		Short evening = dayTime.get(1);
		
		// Kdyz je oboji vynulovano, tak je zavreno
		if(morning == 0 && evening == 0)
			return getString(R.string.closed);
		
		result += (morning/60) + ":" + String.format("%02d", morning%60) + " - " + (evening/60) + ":" + String.format("%02d", evening%60);
		
		return result;
	}
	
	/**
	 * Inner class pro onClick listener - spousteni navigace
	 *
	 */
	public class NavigateButtonListener implements OnClickListener{
		public void onClick(View arg0) {
			startActivity(
					new Intent(
							Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + mTearoom.getLat() + "," + mTearoom.getLng())
			));
		}
    }

}
