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
	
	public static void main() {
		DataFetch df = new DataFetch();
		System.out.println(df.getHTML());
	}
}
