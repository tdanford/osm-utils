package tdanford.osm;

import java.io.*;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class OSMFile {

	private File file;
	private boolean isBZip2Compressed, isGZipCompressed; 
	
	public OSMFile(File f) { 
		if(!f.exists()) { throw new IllegalArgumentException("OSM File does not exist " + f.getAbsolutePath()); }
		if(!f.canWrite()) { throw new IllegalArgumentException("Can't read OSM file " + f.getAbsolutePath()); }
		isBZip2Compressed = f.getName().toLowerCase().endsWith(".bz2");
		isGZipCompressed = f.getName().toLowerCase().endsWith(".gz");
	}
	
	public InputStream getInputStream() throws IOException { 
		InputStream is = new FileInputStream(file);
		if(isBZip2Compressed) { 
			is = new BZip2CompressorInputStream(is);
		} else if (isGZipCompressed) { 
			is = new GZIPInputStream(is);
		}
		return is;
	}
}
