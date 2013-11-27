package com.ratemypark.client;

import java.io.Serializable;

public class LoginInfo implements Serializable {
	private String username;
	private String sessionID;
	private String firstName;
	private String lastName;
	private String email;
	private int suggestionPreference; // 0 = no pref, 1 = highest rated, 2 = most rated, 3 = random
	
	public LoginInfo() {
		
	}
	
	public LoginInfo(String username, String session) {
		this.username = username;
		this.sessionID = session;
		this.firstName = "";
		this.lastName = "";
		this.email = "";
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getSessionID() {
		return this.sessionID;
	}
	
	public String getDisplayName() {
		String displayName = this.getUsername();
		String fn = this.firstName; 
		String ln = this.lastName;
		if (ln!=null && !ln.isEmpty() && fn!=null && !fn.isEmpty()) {
			displayName = fn + " " + ln;
		} else if (fn != null && !fn.isEmpty() && (ln == null || ln.isEmpty())) {
			displayName = fn;
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
	
	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
