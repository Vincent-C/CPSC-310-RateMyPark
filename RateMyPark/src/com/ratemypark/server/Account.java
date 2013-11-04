package com.ratemypark.server;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Account implements Serializable {
	
	@PrimaryKey
	@Persistent
	private String username;
	@Nonnull // not sure about this call
	@Persistent
	private String passwordHash;
	@Persistent
	private String email;
	@Persistent
	private String firstName;
	@Persistent
	private String lastName;
	@Persistent
	private Date dateCreated;
	
	public Account() {
		this.dateCreated = new Date();
	}
	
	public Account(String username, String passwordHash){
		this();
		this.username = username;
		this.passwordHash = passwordHash;
	}

	public String getUsername() {
		return this.username;
	}
		
	public String getPasswordHash(){
		return this.passwordHash;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	
}
