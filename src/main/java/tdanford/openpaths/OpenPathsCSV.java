package tdanford.openpaths;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class OpenPathsCSV implements Iterator<OpenPathRecord> {

	private BufferedReader br;
	private OpenPathRecord nextRecord;
	
	private static InputStream openFile(File f) throws IOException { 
		InputStream is = new FileInputStream(f);
		if(f.getName().toLowerCase().endsWith(".gz")) { is = new GZIPInputStream(is); }
		return is;
	}
	
	public OpenPathsCSV(File f) throws IOException { 
		this(openFile(f));
	}

	public OpenPathsCSV(InputStream is) throws IOException { 
		br = new BufferedReader(new InputStreamReader(is));
		br.readLine(); // header.
		findNextRecord();
	}

	private void findNextRecord() { 
		nextRecord = null;
		if(br != null) { 
			try {
				String line = br.readLine();
				String[] array = line.split(",");
				if(array.length != 7) { 
					throw new IllegalStateException(String.format("Line \"%s\" doesn't have seven fields", line)); 
				}
				nextRecord = new OpenPathRecord(array[0], array[1], array[2], array[3], array[4], array[5], array[6]);
				
			} catch (IOException e) {
				try {
					close();
				} catch (IOException e1) {
					throw new IllegalStateException(e1);
				}
			} catch (ParseException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	public void close() throws IOException { 
		br.close();
		br = null;
	}

	@Override
	public boolean hasNext() {
		return nextRecord != null;
	}

	@Override
	public OpenPathRecord next() {
		OpenPathRecord r = nextRecord; 
		findNextRecord();
		return r;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() not supported");
	}
}
