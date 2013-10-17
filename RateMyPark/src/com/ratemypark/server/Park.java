package com.ratemypark.server;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.api.services.coordinate.Coordinate;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Park {

	@PrimaryKey
	@Persistent
	private Long pid;
	@Persistent
	private String pname;
	@Persistent
	private boolean official;
	@Persistent
	private Integer streetNumber;
	@Persistent
	private String streetName;
	@Persistent
	private String ewStreet;
	@Persistent
	private String nsStreet;
	@Persistent
	private Coordinate coordinate;
	@Persistent
	private Double hectare;
	@Persistent
	private String neighbourhoodName;
	@Persistent
	private String neighbourhoodURL;
	
	public Park(Long pid){
		this.pid = pid;
	}
	
	
	public Park(Long pid, String pname, boolean official, Integer streetNumber,
			String streetName, String ewStreet, String nsStreet,
			Coordinate coordinate, Double hectare, String neighbourhoodName,
			String neighbourhoodURL) {
		super();
		this.pid = pid;
		this.pname = pname;
		this.official = official;
		this.streetNumber = streetNumber;
		this.streetName = streetName;
		this.ewStreet = ewStreet;
		this.nsStreet = nsStreet;
		this.coordinate = coordinate;
		this.hectare = hectare;
		this.neighbourhoodName = neighbourhoodName;
		this.neighbourhoodURL = neighbourhoodURL;
	}


	public Long getPid() {
		return this.pid;
	}
	public String getPname() {
		return this.pname;
	}
	public boolean isOfficial() {
		return this.official;
	}
	public Integer getStreetNumber() {
		return this.streetNumber;
	}
	public String getStreetName() {
		return this.streetName;
	}
	public String getEwStreet() {
		return this.ewStreet;
	}
	public String getNsStreet() {
		return this.nsStreet;
	}
	public Coordinate getGoogleMapDest() {
		return this.coordinate;
	}
	public Double getHectare() {
		return this.hectare;
	}
	public String getNeighbourhoodName() {
		return this.neighbourhoodName;
	}
	public String getNeighbourhoodURL() {
		return this.neighbourhoodURL;
	}
	
		
}
