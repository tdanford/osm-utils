/*
 * Author: tdanford
 * Date: Jul 7, 2008
 */
package tdanford.osm;

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

public class OSMXmlParser extends XMLTreeHandler {
	
	public static void main(String[] args) { 
		File f = args.length > 0 ? new File(args[0]) : 
			new File("C:\\Users\\tdanford\\Desktop\\mit_harvard_map.osm");
		OSMXmlParser parser = new OSMXmlParser(f);
		try {
			parser.parse();

			XMLTreeElement root = parser.getTree();
			XMLTreeElement osm = root.children.firstElement();
			System.out.println(String.format("OSM Children: %d", osm.children.size()));
			
			System.out.println(String.format("# Nodes: %d", parser.nodes().size()));
			System.out.println(String.format("# Ways: %d", parser.ways().size()));
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private File osmMapFile;
	private Map<String,OSMNode> nodeMap;
	private LinkedList<OSMWay> ways;
	
	public OSMXmlParser(File f) {
		osmMapFile = f;
	
		addTag("osm");
		addTag("node");
		addTag("nd");
		addTag("way");
		addTag("tag");
		
		nodeMap= new HashMap<String,OSMNode>();
		ways = new LinkedList<OSMWay>();
	}
	
	public Collection<OSMNode> nodes() { return nodeMap.values(); }
	public Collection<OSMWay> ways() { return ways; }
	
	public static Map<String,String> createTagMap(XMLTreeElement elmt) { 
		Map<String,String> tags = new HashMap<String,String>();
		for(XMLTreeElement celmt : elmt.children) { 
			if(celmt.name.equals("tag")) { 
				String key = celmt.values.get("k");
				String value = celmt.values.get("v");
				if(key != null && value != null) { 
					tags.put(key, value);
				}
			}
		}
		return tags;
	}

	public void parse()  
		throws SAXException, IOException {
		
		FileReader reader = new FileReader(osmMapFile);
		
        XMLReader oParser = XMLReaderFactory.createXMLReader(); 
        
        oParser.setContentHandler(this);
        oParser.parse(new InputSource(reader));
        
        XMLTreeElement root = getTree();
        XMLTreeElement osm = root.children.firstElement();
        
        for(XMLTreeElement elmt : osm.children) { 
        	if(elmt.name.equals("node")) { 
        		OSMNode node = new OSMNode(elmt);
        		nodeMap.put(node.id, node);
        	} else if (elmt.name.equals("way")) { 
        		OSMWay way = new OSMWay(elmt, nodeMap);
        		ways.add(way);
        	}
        }
	}
}
