package com.ratemypark.client;

import java.io.Serializable;

public class LoginInfo implements Serializable {
	private String username;
	private String sessionID;
	private String firstName;
	private String lastName;
	private int suggestionPreference; // 0 = no pref, 1 = highest rated, 2 = most rated, 3 = random
	
	public LoginInfo() {
		
	}
	
	public LoginInfo(String username, String session) {
		this.username = username;
		this.sessionID = session;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getSessionID() {
		return this.sessionID;
	}
	
	public String getDisplayName() {
		String displayName = this.getUsername();
		if (!this.firstName.isEmpty() && !this.lastName.isEmpty()) {
			displayName = this.firstName + " " + this.lastName;
		} else if (!this.firstName.isEmpty() && this.lastName.isEmpty()) {
			displayName = this.firstName;
		}
		return displayName;
	}
	
	public String getFirstName() {
		return this.firstName;
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public int getSuggestionPreference() {
		return this.suggestionPreference;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public void setSuggestionPreference(int pref) {
		this.suggestionPreference = pref;
	}
}
