package com.ratemypark.server;

import java.io.*;
import java.net.*;

public class DataFetch {
	
	public DataFetch() {
		
	}
	
	public String getHTML() {
		URL url;
		HttpURLConnection connection;
		BufferedReader rd;
		String line;
		String result = "";
		
		try {
			// Retrieved data from ftp://webftp.vancouver.ca/opendata/xml/parks_facilities.xml and placed onto
			// Vincent's private webserver in order to use HTTP Get
			// Refer to https://piazza.com/class/hkubwxvsg8u5?cid=98 for clarification
			url = new URL("http://www.vcheng.org/parks_facilities.xml");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			rd = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
}
