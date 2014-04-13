package com.clementscode.starmap;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryImpl;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class SpatialConverter {

	public static final GeometryFactory geomFactory = new GeometryFactory();

	public static final String GEOM_FIELD = "the_geom";
	private static volatile Style keepStyle; // volatile signals JVM that the
												// var might be changed in a
												// concurrent way
	private static volatile SimpleFeatureType keepFeatureType;

	// java will tell other threads when it is changed

	/**
	 * Here is how you can use a SimpleFeatureType builder to create the schema
	 * for your shapefile dynamically.
	 * <p>
	 * This method is an improvement on the code used in the main method above
	 * (where we used DataUtilities.createFeatureType) because we can set a
	 * Coordinate Reference System for the FeatureType and a a maximum field
	 * length for the 'name' field dddd
	 */
	private static SimpleFeatureType createFeatureType() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Star");
		builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference

		// add attributes in order
		// make changes to reflect StarData in future TODO hmwk
		builder.add(GEOM_FIELD, Point.class);
		builder.add(Constants.PROPER_NAME_FIELD, String.class);
		builder.add(Constants.HIP_NUM_FIELD, Integer.class);
		builder.add(Constants.MAG_FIELD, Double.class);

		// build the type
		return builder.buildFeatureType();
	}

	public static SimpleFeatureType getFeatureType() {
		// mutex has one allowed user
		if (keepFeatureType == null) { // do this for efficiency in running

			synchronized (SpatialConverter.class) { // gets reference to class
				// object for locking purpose
				if (keepFeatureType == null) {
					keepFeatureType = createFeatureType();
				}
			}
		}
		return keepFeatureType;
	}

	// called once for each row that is encapsulated in StarData
	public static SimpleFeature createFeature(StarData sd) {
		// TODO only create simple feature type once
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(
				createFeatureType());
		// TODO string lkterals for attr names should; be in a static final the
		// geom
		Point location = toLongLat(sd.decln, sd.ra);
		builder.set(GEOM_FIELD, location);
		builder.set(Constants.PROPER_NAME_FIELD, sd.properName);
		builder.set(Constants.HIP_NUM_FIELD, sd.hipNum);
		builder.set(Constants.MAG_FIELD, sd.mag);
		return builder.buildFeature(Integer.toString(sd.id));

	}

	// parameter is named decln because dec is a reserved word
	public static Point toLongLat(double decln, double ra) {
		// big TODO 0 to 24 map to -180 180 degrees
		// longitude is x
		double lat = decln;
		double lon = -((ra - 12.0) * 15.0); // 15 is 360/24
		return geomFactory.createPoint(new Coordinate(lon, lat));
	}

	public static Coordinate toEquatorial(Point pt) {
		double ra = (-pt.getX()) / 15.0 + 12.0; // convert to hours then add 12
		double rightDecl = pt.getY();
		return new Coordinate(ra, rightDecl);
	}

	public static void render(Collection<StarData> data) throws IOException {
		final MapContext map = new DefaultMapContext();
		map.setTitle("ImageLab");

		MemoryFeatureCollection coll = new MemoryFeatureCollection(
				createFeatureType());
		for (StarData sd : data) {
			coll.add(createFeature(sd));
		}
		// new april 11
		Style style = getStyle();

		// disabled april 11 Style style = getStyle();
		// GTRenderer draw = new StreamingRenderer();
		// draw.setMapContent(map);
		// BufferedImage buf = new BufferedImage(800, 600,
		// BufferedImage.TYPE_INT_RGB);
		// Graphics2D g2d = buf.createGraphics();
		// g2d.setColor(Color.black);
		// g2d.fillRect(0, 0, buf.getWidth(), buf.getHeight());
		// Rectangle paintArea = new Rectangle(buf.getWidth(), buf.getHeight());
		// draw.paint(g2d, paintArea, map.getLayerBounds());

		map.addLayer((FeatureCollection) coll, style);

		// Create a JMapFrame with a menu to choose the display style for the
		JMapFrame frame = new JMapFrame(map);
		frame.setSize(800, 600);
		frame.getMapPane().setBackground(Color.black);
		frame.enableStatusBar(true);
		// frame.enableTool(JMapFrame.Tool.ZOOM, JMapFrame.Tool.PAN,
		// JMapFrame.Tool.RESET);
		frame.enableToolBar(true);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		JMenu menu = new JMenu("Raster");
		menuBar.add(menu);

		// Finally display the map frame.
		// When it is closed the app will exit.
		frame.setVisible(true);
	}

	private static InputStream getSldStream() {
		ClassLoader loader = SpatialConverter.class.getClassLoader();
		return loader.getResourceAsStream("style.xml");
	}

	public static Style createStyle() {
		StyleFactory sf = new StyleFactoryImpl();
		InputStream in = getSldStream();
		Style rval;
		SLDParser parser;
		try {
			parser = new SLDParser(sf, in);
			Style[] styles = parser.readXML();
			// have faith
			rval = styles[0];
		} catch (Exception e) {
			e.printStackTrace();
			rval = null;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rval;
	}

	// new april 12 caching style is keepStyle
	public static Style getStyle() {
		// mutex has one allowed user
		if (keepStyle == null) { // do this for efficiency in running

			synchronized (SpatialConverter.class) { // gets reference to class
				// object for locking purpose
				if (keepStyle == null) {
					keepStyle = createStyle();
				}
			}
		}
		return keepStyle;
	}
}
