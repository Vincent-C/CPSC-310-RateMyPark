package com.ratemypark.server;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomXMLParser {

	private ArrayList<Park> parkList;

	public DomXMLParser() {
		// empty constructor
	}

	public ArrayList<Park> parse() {
		// Fetch input XML file as string, and convert into something we can iterate through
		DataFetch fetch = new DataFetch();
		String xmlString = fetch.getHTML();
		Document doc = null;
		try {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (doc.hasChildNodes()) {
			createParks(doc.getChildNodes());


		}

		return parkList;

	}

	private void createParks(NodeList nodeList) {

		// skip the first "node", which is the entire COVParksFacilities tree
		for (int count = 1; count < nodeList.getLength(); count++) {

			Node tempNode = nodeList.item(count);
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				// get node name and value
				//System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
				//System.out.println("Node Value =" + tempNode.getTextContent());

				Long pid = null;
				String pname = null;
				boolean official = false;
				Integer streetNumber = null;
				String streetName = null;
				String ewStreet = null;
				String nsStreet = null;
				Coordinate coordinate = null;
				Double hectare = null;
				String neighbourhoodName = null;
				String neighbourhoodURL = null;

				if (tempNode.getNodeName().equals("Name")) {
					//System.out.println(tempNode.getTextContent());
					pname = tempNode.getTextContent();
				}
				if (tempNode.getNodeName().equals("Official")) {
					if (tempNode.getTextContent().equals("1")) {
						official = true;
					} else {
						if (tempNode.getTextContent().equals("0")) {
							official = false;
						} else {
							System.out.println("Unrecognized 'official' value for park.");
						}
					}
				}
				if (tempNode.getNodeName().equals("StreetNumber")) {
					streetNumber = Integer.valueOf(tempNode.getTextContent());
				}
				if (tempNode.getNodeName().equals("StreetName")) {
					streetName = tempNode.getTextContent();
				}
				if (tempNode.getNodeName().equals("EWStreet")) {
					ewStreet = tempNode.getTextContent();
				}
				if (tempNode.getNodeName().equals("NSStreet")) {
					nsStreet = tempNode.getTextContent();
				}
				if (tempNode.getNodeName().equals("GoogleMapDest")) {
					String coords = tempNode.getTextContent();
					String[] coordsArray = coords.split(",");
					coordinate = new Coordinate(Double.parseDouble(coordsArray[0]), Double.parseDouble(coordsArray[1]));
				}
				if (tempNode.getNodeName().equals("Hectare")) {
					hectare = Double.parseDouble(tempNode.getTextContent());
				}
				if (tempNode.getNodeName().equals("NeighbourhoodName")) {
					neighbourhoodName = tempNode.getTextContent();
				}
				if (tempNode.getNodeName().equals("NeighbourhoodURL")) {
					neighbourhoodURL = tempNode.getTextContent();
				}

				System.out.println(pname);
				Park park = new Park(pid, pname, official, streetNumber, streetName, ewStreet, nsStreet, coordinate, hectare, neighbourhoodName, neighbourhoodURL);
				System.out.println(park.getPname());
				parkList.add(park);
			}

			/*

			if (tempNode.hasAttributes()) {
				// get attributes names and values
				NamedNodeMap nodeMap = tempNode.getAttributes();
				for (int i = 0; i < nodeMap.getLength(); i++) {
					Node node = nodeMap.item(i);
					//System.out.println("attr name : " + node.getNodeName());
					//System.out.println("attr value : " + node.getNodeValue());
				}
			}

			if (tempNode.hasChildNodes()) {
				// loop again if has child nodes
				createParks(tempNode.getChildNodes());
			}
			//System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

			 */
		}
	}

	public static void main(String[] args) {
		DomXMLParser parser = new DomXMLParser();
		//parser.parse();
		for (Park park : parser.parse()) {
			System.out.println("park: ");
			System.out.println(park.getPname());
		}
	}

}