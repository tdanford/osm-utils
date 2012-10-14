/*
 * Author: tdanford
 * Date: Jul 7, 2008
 */
package tdanford.osm;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

import org.xml.sax.SAXException;

public class OSMViewPanel extends JPanel implements MapLayoutListener {
	
	public static void main(String[] args) { 
		File f = args.length > 0 ? new File(args[0]) : 
			new File("C:\\Documents and Settings\\tdanford\\Desktop\\OSM\\maps\\mit_harvard_map.osm");
		OSMXmlParser parser = new OSMXmlParser(f);
		try {
			parser.parse();

			XMLTreeElement root = parser.getTree();
			XMLTreeElement osm = root.children.firstElement();
			System.out.println(String.format("OSM Children: %d", osm.children.size()));
			
			System.out.println(String.format("# Nodes: %d", parser.nodes().size()));
			System.out.println(String.format("# Ways: %d", parser.ways().size()));

			OSMMap map = new OSMMap(parser.nodes(), parser.ways());
			OSMViewPanel.Frame frame = new OSMViewPanel.Frame(map);
			
			frame.run();
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static class Frame extends JFrame implements Runnable {
		
		private OSMViewPanel panel;

		public Frame(OSMMap m) { 
			super("OSM Map");
			panel = new OSMViewPanel(m);
			Container c = (Container)getContentPane();
			c.setLayout(new BorderLayout());
			
			c.add(panel, BorderLayout.CENTER);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			setJMenuBar(createJMenuBar());
		}
		
		public void run() { 
			setVisible(true);
			pack();
		}
		
		public JMenuBar createJMenuBar() { 
			JMenuBar bar = new JMenuBar();
			
			JMenu menu = null;
			JMenuItem item = null;

			bar.add(menu = new JMenu("File"));
			menu.add(item = new JMenuItem("Save Image..."));
			item.addActionListener(new ActionListener() { 
	            public void actionPerformed(ActionEvent e) { 
	                String pwdName = System.getProperty("user.dir");
	                JFileChooser chooser;
	                if(pwdName != null) { 
	                    chooser = new JFileChooser(new File(pwdName));
	                } else {
	                    chooser = new JFileChooser();
	                }
	                chooser.setApproveButtonText("Save");
	                
	                int v = 
	                    chooser.showOpenDialog(null);
	                if(v == JFileChooser.APPROVE_OPTION) { 
	                    File f = chooser.getSelectedFile();
	                    try {
	                        panel.saveImage(f);
	                    } catch(IOException ie) {
	                        ie.printStackTrace(System.err);
	                    }
	                }
	            }
			});
			
			bar.add(menu = new JMenu("View"));
			menu.add(item = new JMenuItem("Original Bounds"));
			item.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) { 
					panel.map.resetBounds();
				}
			});
			
			return bar;
		}
	}

	private OSMMap map;
	
	private int baseX, baseY;
	private int dragX, dragY;
	
	public OSMViewPanel(OSMMap m) { 
		map = m;
		map.addMapLayoutListener(this);
		baseX = baseY = dragX = dragY = -1;
		
		double ratio = map.latWidth() / map.longWidth();
		int width = 500;
		int height = (int)(width * ratio);
		
		setPreferredSize(new Dimension(width, height));
		
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				map.clearLayout();
				int w = getWidth(), h = getHeight();
				double latLongRatio = (double)h / (double)w;
				
				double latw = map.latWidth();
				double longw = map.longWidth();
				double clat = latw/2.0 + map.getMinLat();
				double clong = longw/2.0 + map.getMinLong();
				
				longw = latw / latLongRatio;
				map.setBounds(clat-latw/2.0, clat+latw/2.0, clong-longw/2.0, clong+longw/2.0);
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() { 
			public void mouseDragged(MouseEvent e) {
				dragX = e.getX(); dragY = e.getY();
				repaint();
			}
		});
		
		addMouseListener(new MouseAdapter() { 
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) { 
					Point p = e.getPoint();
					int w = getWidth(), h = getHeight();
					double yf = (double)(h - p.y) / (double)h;
					double xf = (double)p.x / (double)w;
					
					System.out.println(String.format("XF, YF: %.3f, %.3f", xf, yf));
					
					double lt = map.findLat(yf);
					double lg = map.findLong(xf);
					map.zoomIn(lt, lg);
				} else { 
					map.zoomOut();
				}
			}
			
			public void mousePressed(MouseEvent e) { 
				if(e.getButton() == MouseEvent.BUTTON1) { 
					baseX = dragX = e.getX();
					baseY = dragY = e.getY();
				}
			}
			
			public void mouseReleased(MouseEvent e) { 
				if(e.getButton() == MouseEvent.BUTTON1) {
					int dx = dragX - baseX, dy = dragY - baseY;
					double longf = (double)dx / (double)getWidth();
					double latf = (double)dy / (double)getHeight();
					
					double longw = map.longWidth(), latw = map.latWidth();
					double longShift = - (longw * longf); 
					double latShift = latw * latf;
					
					double minLat = map.getMinLat(), minLong = map.getMinLong();
					
					baseX = dragX = baseY = dragY = -1;				
					map.setBounds(minLat + latShift, minLat + latw + latShift, 
							minLong + longShift, minLong + longw + longShift);
					
					//map.shiftPoints(dragX - baseX, dragY - baseY);
				}
			}
		});
		
	}
	
	public void saveImage(File f) throws IOException {
		int w = getWidth(), h = getHeight();
        BufferedImage im = 
            new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = im.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
        		RenderingHints.VALUE_ANTIALIAS_ON));
        paintComponent(g2);
        ImageIO.write(im, "png", f);
	}
	
	protected void paintComponent(Graphics g) { 
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
        		RenderingHints.VALUE_ANTIALIAS_ON));
		g2.setColor(Color.white);
		int w = getWidth(), h = getHeight();
		g2.fillRect(0, 0, w, h);
		
		int dx = dragX - baseX, dy = dragY - baseY;
		map.paintMap(g2, dx, dy, dx+w, dy+h);
	}

	public void mapLayoutChanged(MapLayoutEvent e) {
		repaint();
		
		if(e.getType().equals(MapLayoutEvent.LayoutChangeType.LAYOUT_CLEARED)) {
			int w = Math.max(1, getWidth());
			int h = Math.max(1, getHeight());
			map.threadedLayout(w, h);
		}
	}
}
