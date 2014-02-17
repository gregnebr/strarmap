package com.clementscode.starmap;

import java.io.FileReader;

import au.com.bytecode.opencsv.CSVReader;

public class Main {

	public static void main(String[] args) throws Exception {
		run();
	}

	private static class Stars {
		String Id, HipNum, stringRA, stringDec, stringMag, ProperName;
	}

	public static void run() throws Exception {
		// EntityManagerFactory emf = Persistence
		// .createEntityManagerFactory("test");
		// EntityManager em = emf.createEntityManager();
		// EntityTransaction tx = em.getTransaction();

		// the following from http://opencsv.sourceforge.net/

		CSVReader reader = new CSVReader(new FileReader("stars.csv"), '\t');
		String[] nextLine;
		Stars stringStarData[];
		stringStarData = new Stars[600];

		int numStars = 0;

		reader.readNext();

		while ((nextLine = reader.readNext()) != null) {
			// nextLine[] is an array of values from the line
			// System.out.println(nextLine[0] + nextLine[1] + "etc...");

			Stars current = new Stars();

			// how do I set up array???
			current.Id = nextLine[0]; // id number in file int
			current.HipNum = nextLine[1]; // Hipparchos number
											// int
			current.ProperName = nextLine[2]; // ProperName
			current.stringRA = nextLine[3]; // Right Ascension
											// double
			current.stringDec = nextLine[4]; // Declination
												// double
			current.stringMag = nextLine[5]; // Magnitude
												// smaller is
												// brighter
												// double
			stringStarData[numStars] = current;
			numStars++;
		}

		// show the first line of the database
		System.out.println("1st record " + stringStarData[0].Id + " "
				+ stringStarData[0].stringRA);
		System.out.println("2nd record " + stringStarData[1].Id + " "
				+ stringStarData[1].stringRA);
		// em.close();
		// emf.close();
	}
}

/*
 * the following were in the orginal main.java file Test test =
 * em.find(Test.class, 1); // get item with id of 1 if (test == null) {
 * System.out.println("None found need to create"); test = new Test(); test.id =
 * 1; test.data = "a";
 * 
 * tx.begin(); em.persist(test); // store
 * 
 * tx.commit(); } else { System.out.println("Test found in database"); }
 * 
 * System.out.format("Test{id=%s, data=%s}\n", test.id, test.data);
 * 
 * em.close(); emf.close(); }
 * 
 * }
 */
