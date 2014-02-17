package com.clementscode.starmap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "StarData")
public class StarData {

	@Id
	// for stars.csv file column headers
	public int Id; // number in file

	@Column
	public int HipNum; // Hipparchos star catalog number
	@Column
	public String ProperName; // name of star, may be blank
	@Column
	public double RA; // Right Ascension 0 to 24
	@Column
	public double Dec; // Declination +90 to -90 degrees
	@Column
	public double Mag; // brightness more negative is brighter

}
