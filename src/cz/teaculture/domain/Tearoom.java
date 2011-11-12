package cz.teaculture.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.location.Location;

/**
 * Bean s informacemi o cajovne - rest template jej nacpe do pole
 * @author vbalak
 *
 */
public class Tearoom implements Serializable {
	private static final long serialVersionUID = 8042671639369119413L;
	
	private String address;
    private Date changed_at;
    private String city;
    private Long id;
    private Double lat;
    private Double lng;
    private String name;
    private String website;
    private String phone;
    private String email;
    private String facebook;
    private String twitter_handle;
    private boolean wifi;
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
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFacebook() {
		return facebook;
	}
	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}
	public String getTwitter_handle() {
		return twitter_handle;
	}
	public void setTwitter_handle(String twitter_handle) {
		this.twitter_handle = twitter_handle;
	}
	public boolean isWifi() {
		return wifi;
	}
	public void setWifi(boolean wifi) {
		this.wifi = wifi;
	}
	
	public Location getLocation() {
		if(lat == null || lng == null)
			return null;
		
		Location result = new Location("tearoom_exact");
   		result.setLatitude(lat);
   		result.setLongitude(lng);
   		result.setAccuracy(0);
   		
   		return result;
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
