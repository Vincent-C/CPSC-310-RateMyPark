package com.ratemypark.client;

import java.util.List;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlexTable;
import com.ratemypark.server.LoadParksServiceImpl;
import com.ratemypark.client.Park;

public class DisplayData implements EntryPoint {

	private List<Park> parkList;

	@Override
	public void onModuleLoad() {
		final FlexTable Table = new FlexTable();
		Table.setBorderWidth(12);
		Table.setText(0, 0, "");
		Table.setText(0, 1, "Park ID");
		Table.setText(0, 2, "Park Name");
		Table.setText(0, 3, "Official");
		Table.setText(0, 4, "Street Number");
		Table.setText(0, 5, "Street Name");
		Table.setText(0, 6, "East-West Street Name");
		Table.setText(0, 7, "North-South Street Name");
		Table.setText(0, 8, "Coordinates");
		Table.setText(0, 9, "Size in Hectares");
		Table.setText(0, 10, "Neighbourhood Name");
		Table.setText(0, 11, "Neighbourhood URL");
		
		// Load park service
		parkList = LoadParksServiceImpl.getParks();
		
		for (Park p: parkList) {
			int index = 1;
			Table.insertRow(index);
			Table.setText(index, 1, String.valueOf(p.getPid()));
			Table.setText(index, 2, p.getPname());
			Table.setText(index, 3, isOfficialString(p));
			Table.setText(index, 4, String.valueOf(p.getStreetNumber()));
			Table.setText(index, 5, p.getStreetName());
			Table.setText(index, 6, p.getEwStreet());
			Table.setText(index, 7, p.getNsStreet());
			Table.setText(index, 8, getCoordinateString(p));
			Table.setText(index, 9, String.valueOf(p.getHectare()));
			Table.setText(index, 10, p.getNeighbourhoodName());
			Table.setText(index, 11, p.getNeighbourhoodURL());
			index++;
		}
	}

	private String isOfficialString(Park p) {
		if (p.isOfficial())
			return "Yes";
		else
			return "No";
	}
	
	private String getCoordinateString(Park p) {
		double latitude = p.getCoordinate().getLatitude();
		double longitude = p.getCoordinate().getLongitude();
		return String.valueOf(latitude) + ", " + String.valueOf(longitude);
	}
}
