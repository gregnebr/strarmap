package com.clementscode.starmap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@org.hibernate.annotations.Table(appliesTo = "StarData", indexes = { @Index(name = "COORDIDX", columnNames = {
		"RA", "Decln" }) })
@Table(name = "StarData")
public class StarData {

	// TODO make vars lowercase start
	@Id
	// for stars.csv file column headers
	public int id; // number in file

	@Column
	public int hipNum; // Hipparchos star catalog number
	@Column
	public String properName; // name of star, may be blank
	@Column
	public double ra; // Right Ascension 0 to 24
	@Column
	public double decln; // Declination +90 to -90 degrees
	@Column
	public double mag; // brightness more negative is brighter

	@Override
	public String toString() {
		return "StarData [Id=" + id + ", HipNum=" + hipNum + ", ProperName="
				+ properName + ", RA=" + ra + ", Decln=" + decln + ", Mag="
				+ mag + "]";
	}

}
