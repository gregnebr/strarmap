package com.clementscode.starmap.dao;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.geotools.geometry.jts.ReferencedEnvelope;

import au.com.bytecode.opencsv.CSVReader;

import com.clementscode.starmap.Constants;
import com.clementscode.starmap.StarData;

public class StarDao {

	private EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("stars");
	private EntityManager em = emf.createEntityManager();

	public boolean dbExists() {
		Query query = em.createQuery("SELECT a FROM StarData a");
		return !query.setMaxResults(1).getResultList().isEmpty();
	}

	private static final Object DB_MUTEX = new Object();

	/**
	 * @param env
	 *            request envelope in right ascension, declination
	 * @param dimmest
	 * @return
	 */
	@SuppressWarnings("unchecked")
	// parameters RA range, Declination Range, dimmest star magnitude
	public Collection<StarData> querydb(ReferencedEnvelope env, double dimmest) {
		// just curious on how long the query takes
		long startTime = System.nanoTime();
		// use createNativeQuery for SQL style query

		Query query = em.createQuery(buildQuery(env, dimmest));
		Collection<StarData> all;
		synchronized (DB_MUTEX) {
			all = (Collection<StarData>) query.getResultList();
		}

		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("Elapsed time is " + estimatedTime / 1000000
				+ " milliseconds for query");
		return all;

	}

	public static String buildQuery(ReferencedEnvelope env, double dimmest) {
		double raLow = env.getMinX();
		double raHigh = env.getMaxX();
		double declnLow = env.getMinY();
		double declnHigh = env.getMaxY();
		// make sure the coordinates are in range
		if (declnLow < -90.0) {
			declnLow = -90.0;
		}
		if (declnHigh > 90.) {
			declnHigh = 90.0;
		}
		if (raLow < 0.0) {
			raLow = 0.0;
		}
		if (raHigh > 24.) {
			raHigh = 24.;
		}
		// object oriented query a is row
		String q = "SELECT a FROM StarData a WHERE a." + Constants.RA_FIELD
				+ " BETWEEN " + raLow + " AND " + raHigh + " AND a."
				+ Constants.DECLN_FIELD + " BETWEEN " + declnLow + " AND "
				+ declnHigh + "AND a." + Constants.MAG_FIELD + " <= " + dimmest;
		return q;

	}

	public void populateDb() throws Exception {
		EntityTransaction tx = em.getTransaction();
		CSVReader reader = null;
		try {
			ClassLoader loader = this.getClass().getClassLoader();
			InputStream in = loader.getResourceAsStream("stars.csv");
			reader = new CSVReader(new InputStreamReader(in), '\t');
			String[] nextLine;

			reader.readNext();
			tx.begin(); // store
			while ((nextLine = reader.readNext()) != null) {
				StarData data = getRow(nextLine);
				em.persist(data);
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public void close() {
		em.close();
		emf.close();
	}

	public static StarData getRow(String[] csvLine) {
		StarData current = new StarData();

		current.id = parseInt(csvLine[0]); // id number in file
											// int

		current.hipNum = parseInt(csvLine[1]); // Hipparchos
												// number

		current.properName = csvLine[2]; // ProperName
		current.ra = parseDouble(csvLine[3]); // Right Ascension
		// double
		current.decln = parseDouble(csvLine[4]); // Declination
		// double
		current.mag = parseDouble(csvLine[5]); // Magnitude
		// smaller is brighter double
		return current;
	}

	public static int parseInt(String str) {
		if (str == null || str.trim().isEmpty()) {
			return -1;
		}
		return Integer.parseInt(str);
	}

	public static double parseDouble(String str) {
		if (str == null || str.trim().isEmpty()) {
			return Double.NaN;
		}
		return Double.parseDouble(str);

	}
}
