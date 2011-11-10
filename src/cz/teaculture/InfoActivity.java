package cz.teaculture;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Aktivita infoobrazovky
 * @author vbalak
 *
 */
public class InfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		
		TextView actionBarTitle = (TextView)findViewById(R.id.actionbar_title);
		actionBarTitle.setText(R.string.menu_info);
	}
	
	/**
	 * Akce v actionbaru - jit domu :-)
	 * @param view
	 */
	public void goHome(View view) {
		finish();
	}
}
