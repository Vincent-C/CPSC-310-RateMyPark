package com.ratemypark.server;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ratemypark.client.Coordinate;
import com.ratemypark.client.Park;

public class DomXMLParser {
	
	private ArrayList<Park> parkList;

	public DomXMLParser() {
		// Create an ArrayList to hold the Park objects we create after parsing the XML data
		parkList = new ArrayList<Park>();
	}

	public ArrayList<Park> parse() {
		try {

			// Fetch input XML file as string, and convert into something we can iterate through
			DataFetch fetch = new DataFetch();
			String xmlString = fetch.getHTML();
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));

			doc.getDocumentElement().normalize();

			// Search for all "Park" tags in XML...
			NodeList nodeList = doc.getElementsByTagName("Park");

			// ...and iterate through each "Park",
			// parsing each attribute to create useful objects from the given data
			for (int i = 0; i < nodeList.getLength(); i++) {

				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					// Uncomment if you want to see the raw XML parsed data, e.g. for testing
					//System.out.println("id : " + element.getAttribute("ID"));
					//System.out.println("official : " + element.getElementsByTagName("Official").item(0).getTextContent());
					//System.out.println("street # : " + element.getElementsByTagName("StreetNumber").item(0).getTextContent());

					Long pid = Long.parseLong(element.getAttribute("ID"));

					String pname = element.getElementsByTagName("Name").item(0).getTextContent();

					boolean official = false;
					if (element.getElementsByTagName("Official").item(0).getTextContent().equals("1")) {
						official = true;
					}

					Integer streetNumber = Integer.valueOf(element.getElementsByTagName("StreetNumber").item(0).getTextContent());

					String streetName = element.getElementsByTagName("StreetName").item(0).getTextContent();

					String ewStreet = element.getElementsByTagName("EWStreet").item(0).getTextContent();

					String nsStreet = element.getElementsByTagName("NSStreet").item(0).getTextContent();

					String coords = element.getElementsByTagName("GoogleMapDest").item(0).getTextContent();
					String[] coordsArray = coords.split(",");
					Coordinate coordinate = null;// new Coordinate(Double.parseDouble(coordsArray[0]), Double.parseDouble(coordsArray[1]));

					Double hectare = Double.parseDouble(element.getElementsByTagName("Hectare").item(0).getTextContent());

					String neighbourhoodName = element.getElementsByTagName("NeighbourhoodName").item(0).getTextContent();

					String neighbourhoodURL = element.getElementsByTagName("NeighbourhoodURL").item(0).getTextContent();


					// For testing purposes; uncomment to see data after being parsed,
					// in the form of objects we can use to create Park objects
					/*
					System.out.println(pid);
					System.out.println(pname);
					System.out.println(official);
					System.out.println(streetNumber);
					System.out.println(streetName);
					System.out.println(ewStreet);
					System.out.println(nsStreet);
					System.out.println(coordinate);
					System.out.println(hectare);
					System.out.println(neighbourhoodName);
					System.out.println(neighbourhoodURL);
					 */

					// And finally, create Park object, and append to list of Parks					
					Park park = new Park(pid, pname, official, streetNumber, streetName, ewStreet, nsStreet, coordinate, hectare, neighbourhoodName, neighbourhoodURL);
					parkList.add(park);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Return ArrayList of Park objects once parser has iterated through the entire XML file
		return parkList;
	}

	public static void main(String[] args) {

		// Example of how to call parser...
		// For testing purposes: uncomment to iterate through constructed list
		// and print out to console the names of each Park in the list
		DomXMLParser parser = new DomXMLParser();
		ArrayList<Park> myParkList = parser.parse();
		System.out.println(myParkList.size() + " parks in total...");
		for (Park park : myParkList) {
			System.out.println("park: " + park.getPname());
		}
	}

}
