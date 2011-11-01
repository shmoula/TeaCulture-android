package cz.teaculture.util;

import java.util.LinkedHashMap;
import java.util.Map;

import cz.teaculture.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

/**
 * Implementace SeparableAdapteru - seznam s hlavickami
 * @author Lewis (http://androidseverywhere.info/JAAB/?p=6)
 * @author vbalak
 *
 */
public class SeparatedListAdapter extends BaseAdapter {
	// hlavicky - zahlavi jednotlivych "sekci", napr Brno, Ostrava...
	private final ArrayAdapter<String> headers;
	// sekce vcetne jednotlivych vyctu rozdelene podle "hlavicky" - jmena mesta - jako klice
	private final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();
	
	public final static int TYPE_SECTION_HEADER = 0;
	
	
	public SeparatedListAdapter(Context context) {
		headers = new ArrayAdapter<String>(context, R.layout.troom_list_header);
	}
	
	public void addSection(String section, Adapter adapter) {
		headers.add(section);
		sections.put(section, adapter);
	}

	/**
	 * Spocita vsechny polozky obsazene v tomto adapteru - vcetne hlavicek sekci
	 */
	@Override
	public int getCount() {
		int total = 0;
		for (Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}
	
	/**
	 * Suma polozek ve vsech adapterech pod destnikem +1
	 */
	public int getViewTypeCount() {
		int total = 1;
		for (Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	/**
	 * Dohleda item na zadane pozici
	 */
	@Override
	public Object getItem(int position) {
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return section;
			if (position < size)
				return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Vrati view hlavicky, do ktere patri pozadovana polozka
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return headers.getView(sectionnum, convertView, parent);
			if (position < size)
				return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	public int getItemViewType(int position) {
		int type = 1;
		for (Object section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size)
				return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}
}
