/*
 * Author: tdanford
 * Date: Jul 7, 2008
 */
package tdanford.osm;

import java.util.*;
import java.awt.*;

public class OSMMap {
	
    // Approximate circumference of the earth, in meters.
	public static final double EARTH_CIRCUMFERENCE = 40075.0 * 1000.0;

	private Map<String,OSMNode> nodes;
	private Map<String,OSMWay> ways;
	private Map<OSMNode,Point> nodePoints;
	private boolean laidOut;
	
	private LinkedList<MapLayoutListener> listeners; 
	
	private Double minLong, minLat, maxLong, maxLat;
	private Double lat1, lat2, long1, long2;
	
	public OSMMap(Collection<OSMNode> ns, Collection<OSMWay> ws) { 
		nodes = new HashMap<String,OSMNode>();
		ways = new HashMap<String,OSMWay>();
		nodePoints = new HashMap<OSMNode,Point>();
		minLong = minLat = maxLong = maxLat = null;
		listeners = new LinkedList<MapLayoutListener>();
		laidOut = false;
		
		for(OSMNode n : ns) { 
			nodes.put(n.id, n);
			if(minLong == null) { 
				maxLong = minLong = n.longitude;
				minLat = maxLat = n.latitude;
			} else { 
				maxLong = Math.max(maxLong, n.longitude);
				minLong = Math.min(minLong, n.longitude);
				maxLat = Math.max(maxLat, n.latitude);
				minLat  = Math.min(minLat, n.latitude);
			}
		}
		
		for(OSMWay w : ws) { 
			ways.put(w.id, w);
			for(OSMNode n : w.nodes) { 
				if(!nodes.containsKey(n.id)) { 
					throw new IllegalArgumentException(w.id);
				}
			}
		}
		
		lat1 = minLat; lat2 = maxLat;
		long1 = minLong; long2 = maxLong;
		
		System.out.println(String.format("Latitudes: [%.5f , %.5f]", lat1, lat2));
		System.out.println(String.format("Longitudes: [%.5f , %.5f]", long1, long2));
	}
	
	public void addMapLayoutListener(MapLayoutListener mll) { 
		listeners.add(mll);
	}
	
	public void removeMapLayoutListener(MapLayoutListener mll) { 
		listeners.remove(mll);
	}
	
	public double pixelLatitudeWidth(int pix) { 
		double w = latWidth();
		double size = EARTH_CIRCUMFERENCE * (w / 360.0);
		return size / (double)pix;
	}
	
	public double pixelLongitudeWidth(int pix) { 
		double w = longWidth();
		double size = EARTH_CIRCUMFERENCE * (w / 360.0);
		return size / (double)pix;
	}
	
	public void paintMap(Graphics2D g2, int x1, int y1, int x2, int y2) {
		if(!laidOut) {
			g2.setColor(Color.black);
			g2.drawString("Repainting...", x1+5, y1+20);
			return; 
		}
		
		int w = x2 - x1, h = y2 - y1;
		
		Stroke oldStroke = g2.getStroke();
		
		double xWidth = pixelLongitudeWidth(w);
		double yWidth = pixelLatitudeWidth(h);
		double pixWidth = Math.min(xWidth, yWidth);
		//System.out.println("pixWidth: " + pixWidth);
		
		for(OSMWay way : ways.values()) { 
			float strokeWidth = way.width != null ? 
						(way.width/(float)pixWidth)/(float)2.0 : 
						(float)1.0;
			g2.setStroke(new BasicStroke(Math.max(strokeWidth, (float)0.01)));
			OSMNode prev = null;
			g2.setColor(way.isClosed() ? Color.green.darker() : Color.blue);
			for(OSMNode node : way.nodes) {
				if(prev != null) { 
					Point pp = nodePoints.get(prev);
					Point np = nodePoints.get(node);
					g2.drawLine(x1+pp.x, y1+pp.y, x1+np.x, y1+np.y);
				}
				prev = node;
			}
		}

		g2.setStroke(oldStroke);

		/*
		for(OSMNode node : nodePoints.keySet()) { 
			Point p = nodePoints.get(node);
			int x = p.x, y = p.y;
			g2.setColor(Color.white);
			g2.fillOval(x-2, y-2, 4, 4);
			g2.setColor(Color.red);
			g2.drawOval(x-2, y-2, 4, 4);
		}
		*/
	}
	
	public double latWidth() { return lat2 - lat1; }
	public double longWidth() { return long2 - long1; }
	
	public double getMinLong() { return long1; }
	public double getMaxLong() { return long2; }
	public double getMinLat() { return lat1; }
	public double getMaxLat() { return lat2; }
	
	public double findLat(double frac) { 
		return frac * latWidth() + lat1;
	}
	
	public double findLong(double frac) { 
		return frac * longWidth() + long1;
	}
	
	public void centerAt(double lt, double lg) { 
		double latWidth = latWidth()/2.0;
		double longWidth = longWidth()/2.0;
		setBounds(lt-latWidth, lt+latWidth, lg-longWidth, lg+longWidth);
	}
	
	public void zoomIn(double lt, double lg) {
		System.out.println(String.format("Zoom LAT,LONG: %.5f, %.5f", lt, lg));
		double newLatWidth = latWidth()/2.0;
		double newLongWidth = longWidth()/2.0;
		setBounds(lt-newLatWidth/2.0, lt+newLatWidth/2.0, lg-newLongWidth/2.0, lg+newLongWidth/2.0);
	}
	
	public void zoomOut() { 
		double lt = lat1 + latWidth()/2.0;
		double lg = long1 + longWidth()/2.0;
		double newLatWidth = latWidth()*2.0;
		double newLongWidth = longWidth()*2.0;
		setBounds(lt-newLatWidth/2.0, lt+newLatWidth/2.0, lg-newLongWidth/2.0, lg+newLongWidth/2.0);		
	}
	
	public void resetBounds() { 
		setBounds(minLat, maxLat, minLong, maxLong);
	}
	
	public void setBounds(double lt1, double lt2, double lg1, double lg2) {
		synchronized(nodePoints) { 
			lat1 = lt1; lat2 = lt2; 
			long1 = lg1; long2 = lg2;
			laidOut = false;
			nodePoints.clear();
			dispatchLayoutEvent(new MapLayoutEvent(this, MapLayoutEvent.LayoutChangeType.LAYOUT_CLEARED));
		}
	}
	
	public void clearLayout() { 
		synchronized(nodePoints) { 
			laidOut = false;
			nodePoints.clear();
			dispatchLayoutEvent(new MapLayoutEvent(this, MapLayoutEvent.LayoutChangeType.LAYOUT_CLEARED));
		}		
	}
	
	private void dispatchLayoutEvent(MapLayoutEvent evt) { 
		for(MapLayoutListener ll : listeners) { 
			ll.mapLayoutChanged(evt);
		}
	}
	
	private class LayoutRunnable implements Runnable { 
		
		private int pwidth, pheight;
		
		public LayoutRunnable(int pw, int ph) { 
			pwidth = pw; pheight = ph;
		}
		
		public void run() {
			layout(pwidth, pheight);
		}
	}
	
	public void threadedLayout(int pwidth, int pheight) { 
		Thread t = new Thread(new LayoutRunnable(pwidth, pheight));
		t.start();
	}

	private void layout(int pwidth, int pheight) {
		synchronized(nodePoints) {
			double latWidth = latWidth(), longWidth = longWidth();

			for(String nid : nodes.keySet()) { 
				OSMNode node = nodes.get(nid);
				double nlat = node.latitude, nlong = node.longitude;
				double latf = (nlat-lat1) / latWidth;
				double longf = (nlong-long1) / longWidth;
				
				int x = (int)Math.round(longf * (double)pwidth);
				int y = (pheight - (int)Math.round(latf * (double)pheight));
				nodePoints.put(node, new Point(x, y));
			}

			laidOut = true;
			dispatchLayoutEvent(new MapLayoutEvent(this, MapLayoutEvent.LayoutChangeType.NEW_LAYOUT));
		}
	}

	public void shiftPoints(int dx, int dy) {
		synchronized(nodePoints) { 
			for(OSMNode node : nodePoints.keySet()) { 
				Point p = nodePoints.get(node);
				nodePoints.put(node, new Point(p.x + dx, p.y + dy));
				dispatchLayoutEvent(new MapLayoutEvent(this, MapLayoutEvent.LayoutChangeType.NEW_LAYOUT));
			}
		}
	}
}
