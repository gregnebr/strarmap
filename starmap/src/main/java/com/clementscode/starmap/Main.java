package com.clementscode.starmap;

import java.util.Collection;

import org.geotools.geometry.jts.ReferencedEnvelope;

import com.clementscode.starmap.dao.StarDao;

public class Main {

	public static void main(String[] args) throws Exception {
		run();
	}

	public static void run() throws Exception {
		StarDao dao = new StarDao();

		// the following from http://opencsv.sourceforge.net/
		// StarData test = em.find(StarData.class, 1); // get item with id of 1

		try {
			if (!dao.dbExists()) {
				System.out.println("The StarData db already exists");
				// this query sets the RA range, the Declination range, and the
				// dimmest magnitude
				ReferencedEnvelope env = new ReferencedEnvelope(2.0, 10.0,
						-40.0, 40.0, null);
				Collection<StarData> data = dao.querydb(env, 6.0);
				System.out.println(data.size()
						+ " records match the query in the database");
				SpatialConverter.render(data, 800, 400);
				// for (StarData data : all) {
				// System.out.println(data);
				// }
			} else {
				dao.populateDb();
			}
		} finally {
			dao.close();
		}
	}

}
