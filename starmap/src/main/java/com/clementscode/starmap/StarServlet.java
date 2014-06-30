/**
 * 
 */
package com.clementscode.starmap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.clementscode.starmap.dao.StarDao;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Brian ... Greg
 * 
 */
public class StarServlet extends HttpServlet {

	private final StarDao dao = new StarDao();

	private static final CoordinateReferenceSystem DEFAULT_CRS;

	private static final String NUMBER = "[-+]?[0-9]*\\.?[0-9]+";

	private static final String BBOX_PATTERN_STR = String.format(
			"^\\s*(%s),(%s),(%s),(%s)\\s*$", NUMBER, NUMBER, NUMBER, NUMBER);

	private static final Pattern bboxPattern = Pattern
			.compile(BBOX_PATTERN_STR);

	static {
		CoordinateReferenceSystem crs = null;
		try {
			crs = CRS.decode("CRS:84", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DEFAULT_CRS = crs;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -559556171407691234L;

	@Override
	public void init() throws ServletException {
		super.init();
		if (!dao.dbExists()) {
			try {
				dao.populateDb();
			} catch (Exception e) {
				throw new ServletException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			System.out.println(request.getParameterMap());
			int width = getRequestSize(request.getParameter("WIDTH"), 800);
			int height = getRequestSize(request.getParameter("HEIGHT"), 400);
			ReferencedEnvelope env = getRequestEnvelope(request
					.getParameter("BBOX"));
			System.out.println(env);
			System.out.println(width);
			System.out.println(height);
			ReferencedEnvelope convertedEnv = convert(env);
			// TODO filter stars based on envelope/size ratio (zoom level)
			// perhaps use a different style that has smaller stars when zoomed
			// out
			Collection<StarData> data = dao.querydb(convertedEnv, 6.0);
			System.out.println(data.size()
					+ " records match the query in the database");
			BufferedImage image = SpatialConverter.render(data, width, height);
			response.setContentType("image/png");
			response.setStatus(HttpServletResponse.SC_OK);
			ServletOutputStream out = response.getOutputStream();
			if (!ImageIO.write(image, "png", out)) {
				System.out.println("Unable to write image");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			PrintWriter writer = response.getWriter();
			String msg = e.getLocalizedMessage();
			if (msg == null) {
				msg = e.getClass().toString();
			}
			writer.write(msg);
			writer.flush();
			writer.close();
		}
	}

	/**
	 * takes in a lon/lat envelope and converts to right ascension, declination
	 * 
	 * @param env
	 * @return
	 */
	private ReferencedEnvelope convert(ReferencedEnvelope env) {
		Coordinate upperRight = new Coordinate(env.getMaxX(), env.getMaxY());
		Coordinate lowerLeft = new Coordinate(env.getMinX(), env.getMinY());
		Coordinate equatorialUpper = SpatialConverter.toEquatorial(upperRight);
		Coordinate equatorialLower = SpatialConverter.toEquatorial(lowerLeft);
		return new ReferencedEnvelope(equatorialLower.x, equatorialUpper.x,
				equatorialLower.y, equatorialUpper.y, null);
	}

	/**
	 * @param bboxString
	 * @return request in lat/lon coordinates
	 * @throws ParseException
	 */
	private ReferencedEnvelope getRequestEnvelope(String bboxString)
			throws ParseException {
		double minx;
		double maxx;
		double miny;
		double maxy;
		if (bboxString == null) {
			bboxString = "-180,-90,180,90";
		}
		// bboxString will be in the form 'minx,miny,maxx,maxy'
		// using regex see above static
		Matcher matcher = bboxPattern.matcher(bboxString);
		if (matcher.matches()) {
			minx = Double.parseDouble(matcher.group(1));
			miny = Double.parseDouble(matcher.group(2));
			maxx = Double.parseDouble(matcher.group(3));
			maxy = Double.parseDouble(matcher.group(4));

		} else {
			throw new ParseException(
					"bboxString did not match ...bad value is: " + bboxString,
					0);
		}
		return new ReferencedEnvelope(minx, maxx, miny, maxy, DEFAULT_CRS);
	}

	private int getRequestSize(String sizeString, int defaultSize) {
		int rval = defaultSize;
		if (sizeString != null) {
			try {
				rval = Integer.parseInt(sizeString);
			} catch (Exception e) {
				e.printStackTrace();
				// ignore and take default
			}
		}
		return rval;
	}

	public static final void main(String[] args) {
		bboxPattern.matcher("-180,-90,180,90");
		Matcher matcher = bboxPattern.matcher("-180,-90,180,90");
		if (matcher.matches()) {
			for (int i = 0; i < matcher.groupCount() + 1; i++) {
				System.out.println(i + " : " + matcher.group(i));
			}

		}
	}
}
