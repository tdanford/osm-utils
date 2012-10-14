package tdanford.openpaths;

import java.text.ParseException;
import java.util.Calendar;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class OpenPathRecordTest {

	@Test
	public void testParseLatitude() throws ParseException { 
		OpenPathRecord rec = new OpenPathRecord("10.0", "-70.0", "0", "2010-10-05 15:35:02", "\"Test Device\"", "Version", "OS");
		assertEquals(rec.latitude, Double.parseDouble("10.0"));
	}

	@Test
	public void testParseLongitude() throws ParseException { 
		OpenPathRecord rec = new OpenPathRecord("10.0", "-70.0", "0", "2010-10-05 15:35:02", "\"Test Device\"", "Version", "OS");
		assertEquals(rec.longitude, Double.parseDouble("-70.0"));
	}

	@Test
	public void testParseAlt() throws ParseException { 
		OpenPathRecord rec = new OpenPathRecord("10.0", "-70.0", "0", "2010-10-05 15:35:02", "\"Test Device\"", "Version", "OS");
		assertEquals(rec.alt, Double.parseDouble("0"));
	}
	
	@Test
	public void testUnquoteDevice() throws ParseException { 
		OpenPathRecord rec = new OpenPathRecord("10.0", "-70.0", "0", "2010-10-05 15:35:02", "\"Test Device\"", "Version", "OS");
		assertEquals(rec.device, "Test Device");		
	}
	
	@Test
	public void testParseDate() throws ParseException { 
		OpenPathRecord rec = new OpenPathRecord("10.0", "-70.0", "0", "2010-10-05 15:35:02", "\"Test Device\"", "Version", "OS");
		Calendar cal = Calendar.getInstance();
		cal.setTime(rec.date);
		
		assertEquals(cal.get(Calendar.YEAR), 2010, "year");
		assertEquals(cal.get(Calendar.MONTH), 10-1, "month");  // month indices start at 0.
		assertEquals(cal.get(Calendar.DAY_OF_MONTH), 5, "day");
		
		assertEquals(cal.get(Calendar.HOUR_OF_DAY), 15, "hour");
		assertEquals(cal.get(Calendar.MINUTE), 35, "minute");
		assertEquals(cal.get(Calendar.SECOND), 2, "second");
	}

	@Test(expectedExceptions = ParseException.class)
	public void testParseIllegalDate() throws ParseException { 
		OpenPathRecord rec = new OpenPathRecord("10.0", "-70.0", "0", "2010-10-05 15:35", "\"Test Device\"", "Version", "OS");
	}
	
}
