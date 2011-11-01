package cz.teaculture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.teaculture.domain.Tearoom;
import cz.teaculture.util.Stuff;
import cz.teaculture.util.TearoomOpenHelper;
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
	private TearoomOpenHelper mOpenHelper;
	private Tearoom mTearoom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tearoom_details);
		
		// Inicializace napojeni na SQLite
        mOpenHelper = new TearoomOpenHelper(getApplicationContext());
		
		// Vytazeni identifikatoru z bundlu a dohledani odpovidajiciho teaRoomu
		Bundle bundle = this.getIntent().getExtras();
		String tearoomId = bundle.getString("tearoomId");
		mTearoom = getTearoomDetails(Long.parseLong(tearoomId));
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
	
	/**
	 * doplni texty pro jednotlive views
	 */
	private void fillViewsWithInfo(){
		TextView tvName = (TextView) findViewById(R.id.tearoom_name);
		tvName.setText(mTearoom.getName());
		
		TextView tvOpeningTimes = (TextView) findViewById(R.id.tearoom_opening_times);
		tvOpeningTimes.setText(getFormattedOpeningTimes());
		
		Button bNavigate = (Button) findViewById(R.id.tearoom_navigate);
		bNavigate.setOnClickListener(new NavigateButtonListener());
	}
	
	/**
	 * Vraci naformatovany string s oteviraci dobou aktualni cajovny
	 * @return
	 */
	private String getFormattedOpeningTimes(){
		String result = "";
		List<List<Short>> openingTimes = mTearoom.getOpen_hours();
		String days[] = { "Pondeli", "Utery", "Streda", "Ctvrtek", "Patek", "Sobota", "Nedele" };
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
		
		result += (morning/60) + ":" + String.format("%02d", morning%60) + " - " + (evening/60) + ":" + String.format("%02d", evening%60);
		
		return result;
	}
		
	/**
	 * Natahne z databaze pozadovany teaRoom
	 * TODO: tearoomList by mohl existovat v singletonu ve formatu Map<id, Tearoom>
	 * @param tearoomId
	 * @return
	 */
	private Tearoom getTearoomDetails(long tearoomId) {
		byte[] bArray = mOpenHelper.getSavedList();
		Tearoom result = null;
		
		try {
			@SuppressWarnings("unchecked")
			List<Tearoom> tearoomList = Stuff.setObjectFromByteArray(bArray, ArrayList.class);
			
			for(Tearoom tearoom : tearoomList){
				if(tearoomId == tearoom.getId()){
					result = tearoom;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
