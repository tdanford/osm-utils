/*
 * Author: tdanford
 * Date: Jul 7, 2008
 */
package tdanford.osm;

import java.awt.Event;

public class MapLayoutEvent {
	
	public static enum LayoutChangeType { NEW_LAYOUT, LAYOUT_CLEARED };
	
	private OSMMap map;
	private LayoutChangeType type;

	public MapLayoutEvent(OSMMap m, LayoutChangeType t) {
		map = m; 
		type = t;
	}
	
	public OSMMap getMap() { return map; }
	public LayoutChangeType getType() { return type; }
}
