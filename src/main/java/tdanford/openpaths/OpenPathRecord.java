package tdanford.openpaths;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenPathRecord {
	
	public static Pattern quoted = Pattern.compile("^\"(.*)\"$");
	
	public static String unquote(String value) { 
		Matcher m = quoted.matcher(value);
		return m.matches() ? m.group(1) : value;
	}
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public double latitude, longitude, alt;
	public Date date;
	public String device, os, version;
	
	public OpenPathRecord(
			String latitude, String longitude, String alt, String dateString, 
			String device, String os, String version) throws ParseException { 
		
		this.latitude = Double.parseDouble(latitude);
		this.longitude = Double.parseDouble(longitude);
		this.alt = Double.parseDouble(alt);
		this.date = dateFormat.parse(dateString);
		this.device = unquote(device);
		this.os = os;
		this.version = version;
	}
	
	public String toString() { 
		return String.format("%.2f,%.2f,%.2f,%s,%s,%s,%s", 
				latitude, longitude, alt, dateFormat.format(date), 
				device, os, version);
	}
}
