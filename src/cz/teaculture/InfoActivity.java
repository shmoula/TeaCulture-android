package cz.teaculture;

import android.app.Activity;
import android.os.Bundle;

/**
 * Aktivita infoobrazovky
 * TODO: Udelat jako fragment
 * @author vbalak
 *
 */
public class InfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
	}
}
