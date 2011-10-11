package cz.teaculture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import cz.teaculture.domain.Tearoom;
import cz.teaculture.util.Stuff;
import cz.teaculture.util.Tea;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Seznam vsech cajoven
 * @author vbalak
 *
 */
public class MainActivity extends ListActivity {
    private static String TAG = "teacultureMain";
    
    private ProgressDialog mProgressDialog;
    private SimpleAdapter mTreeAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.troom_list);
        
        TextView tv = (TextView) findViewById(R.id.infobar);
		tv.setText("Teaculture (proof of concept)");
    }
    
    @Override
	public void onStart() {
		super.onStart();
		
		// TODO: pridat onResume
		if(mTreeAdapter == null)
			new GetTeaRoomsTask().execute();
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
	 * Touto metodou se preda listu seznam natazenych cajoven
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
			tearoomInfo.put("name", tearoom.getName());			
			tearoomInfo.put("opened", Tea.getOpenedStatus(tearoom.getOpen_hours()));
			
			tearooms.add(tearoomInfo);
		}

		mTreeAdapter = new SimpleAdapter(this, tearooms,
				R.layout.troom_list_item,
				new String[] { "name", "opened" }, new int[] { R.id.name, R.id.opened });

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
			refreshTreeList(result);
		}
	}
}

