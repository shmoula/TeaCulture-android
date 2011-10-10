package cz.teaculture.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Bean s informacemi o cajovne - rest template jej nacpe do pole
 * @author vbalak
 *
 */
public class Tearoom {
	private String address;
    private Date changed_at;
    private String city;
    private Long id;
    private Double lat;
    private Double lng;
    private String name;
    private List<List<Short>> open_hours;
	
    
    public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Date getChanged_at() {
		return changed_at;
	}
	public void setChanged_at(Date changed_at) {
		this.changed_at = changed_at;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public Double getLng() {
		return lng;
	}
	public void setLng(Double lng) {
		this.lng = lng;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Oteviraci doba podniku v minutach
	 * @return output/60 = hodina otevreni/zavreni
	 */
	public List<List<Short>> getOpen_hours() {
		return open_hours;
	}
	public void setOpen_hours(List<List<Short>> open_hours) {
		this.open_hours = open_hours;
	}
	
	/**
	 * Profrci oteviracku a vrati pole s oteviracimi casy
	 * Priznak morning znamena rano (true)
	 * @return
	 */
	public List<Short> getOpeningTimes(boolean morning){
		List<Short> openingTimes = new ArrayList<Short>();
		
		for(Iterator<List<Short>> i = open_hours.iterator() ; i.hasNext() ; ){
			List<Short> oneDay = i.next();
			openingTimes.add(oneDay.get(morning ? 0 : 1));
			
		}
		
		return openingTimes;
	}
}
