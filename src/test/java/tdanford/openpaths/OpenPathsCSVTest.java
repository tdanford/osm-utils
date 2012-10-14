package tdanford.openpaths;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class OpenPathsCSVTest {
	
	@Test
	public void parseTwoLineCSV() throws IOException { 
		OpenPathsCSV csv = new OpenPathsCSV(openFile("openpaths_test1.csv"));
		int c = 0;
		while(csv.hasNext()) { 
			OpenPathRecord rec = csv.next();
			assertEquals(rec.device, "Samsung");
			c += 1;
		}
		assertEquals(c, 2, "total count");
	}

	@Test
	public void parseTwoLineCompressedCSV() throws IOException { 
		OpenPathsCSV csv = new OpenPathsCSV(openFile("openpaths_test1.csv.gz"));
		int c = 0;
		while(csv.hasNext()) { 
			OpenPathRecord rec = csv.next();
			assertEquals(rec.device, "Samsung");
			c += 1;
		}
		assertEquals(c, 2, "total count");
	}

	public File openFile(String filename) { 
		return new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
	}
	
}
