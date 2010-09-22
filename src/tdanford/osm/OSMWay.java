/*
 * Author: tdanford
 * Date: Jul 7, 2008
 */
package tdanford.osm;

import java.util.*;

public class OSMWay {

	public String id;
	public String name;
	public Vector<OSMNode> nodes;
	public Float width;
	
	public OSMWay(XMLTreeElement xml, Map<String,OSMNode> nodeMap) { 
		id = xml.values.get("id");
		nodes = new Vector<OSMNode>();

		Map<String,String> tags = OSMXmlParser.createTagMap(xml);
		name = tags.get("name");

		if(tags.containsKey("width")) { 
			String widthString = tags.get("width"); 
			if(widthString.indexOf(";") != -1) { 
				String[] a = widthString.split(";");
				widthString = a[0].trim();
			}
			width = Float.parseFloat(widthString);
		} else { 
			width = null;
		}
		
		for(XMLTreeElement elmt : xml.children) { 
			if(elmt.name.equals("nd")) { 
				String nodeID = elmt.values.get("ref");
				OSMNode node = nodeMap.get(nodeID);
				if(node == null) { 
					throw new IllegalArgumentException(String.format("%s -> %s", 
						id, nodeID));
				}
				nodes.add(node);
			}
		}
	}
	
	public boolean isClosed() { 
		return nodes.firstElement().equals(nodes.lastElement());
	}
	
	public int hashCode() { 
		return id.hashCode();
	}
	
	public boolean equals(Object o) { 
		return (o instanceof OSMWay) && 
			((OSMWay)o).id.equals(id);
	}
}
