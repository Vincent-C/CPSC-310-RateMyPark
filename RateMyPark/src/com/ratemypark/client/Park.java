package com.ratemypark.client;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gwt.user.client.rpc.IsSerializable;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Park implements IsSerializable {

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
	private Double latitude;
	@Persistent
	private Double longitude;
	@Persistent
	private Double hectare;
	@Persistent
	private String neighbourhoodName;
	@Persistent
	private String neighbourhoodURL;

	public Park() {
		// THIS IS A PARK!
	}

	public Park(Long pid) {
		this();
		this.pid = pid;
	}

	public Park(Long pid, String pname, boolean official, Integer streetNumber, String streetName, String ewStreet,
			String nsStreet, Double latitude, Double longitude, Double hectare, String neighbourhoodName, String neighbourhoodURL) {
		this();
		this.pid = pid;
		this.pname = pname;
		this.official = official;
		this.streetNumber = streetNumber;
		this.streetName = streetName;
		this.ewStreet = ewStreet;
		this.nsStreet = nsStreet;
		this.latitude = latitude;
		this.longitude = longitude;
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

	public Double getLatitude() {
		return this.latitude;
	}
	
	public Double getLongitude() {
		return this.longitude;
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

	public void printAll() {
		System.out.print("Park ID: " + getPid());
		System.out.print("Park Name: " + getPname());
		System.out.print("Park Street: " + getStreetNumber() + " " + getStreetName());
		// System.out.print("Park ID: " + getPid());
		// System.out.print("Park ID: " + getPid());

	}

}
