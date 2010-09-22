/*
 * Author: tdanford
 * Date: Jul 7, 2008
 */
package tdanford.osm;

public class OSMNode {

	public String id;
	public Double latitude, longitude;
	
	public OSMNode(XMLTreeElement xml) {
		id = xml.values.get("id");
		
		if(id == null) { 
			System.err.println(xml.values.toString());
			System.err.println("Node ? is missing an \"id\" field.");
		}

		try { 
			latitude = Double.parseDouble(xml.values.get("lat"));
			longitude = Double.parseDouble(xml.values.get("lon"));
		} catch(NullPointerException npe) { 
			latitude = longitude = null;
			if(id != null) { System.err.println(xml.values.toString()); }
			System.err.println(String.format("Node %s is missing lat/lon", id));
		}
	}
	
	public boolean equals(Object o) { 
		if(!(o instanceof OSMNode)) { return false; }
		OSMNode node = (OSMNode)o;
		return node.id.equals(id);
	}
	
	public int hashCode() { 
		return id.hashCode(); 
	}
}
