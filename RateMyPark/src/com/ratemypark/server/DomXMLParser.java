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
import org.w3c.dom.Element;
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
		/*DomXMLParser parser = new DomXMLParser();
		//parser.parse();
		for (Park park : parser.parse()) {
			System.out.println("park: ");
			System.out.println(park.getPname());
		}*/

		try {

			DataFetch fetch = new DataFetch();
			String xmlString = fetch.getHTML();
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));

			doc.getDocumentElement().normalize();

			DomXMLParser parser = new DomXMLParser();
			ArrayList<Park> myParkList = new ArrayList<Park>();

			
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nodeList = doc.getElementsByTagName("Park");

			for (int i = 0; i < nodeList.getLength(); i++) {

				Node node = nodeList.item(i);

				System.out.println("\nCurrent Element :" + node.getNodeName());

				if (node.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) node;

					System.out.println("id : " + element.getAttribute("ID"));
					System.out.println("official : " + element.getElementsByTagName("Official").item(0).getTextContent());
					System.out.println("street # : " + element.getElementsByTagName("StreetNumber").item(0).getTextContent());
					
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
					Coordinate coordinate = new Coordinate(Double.parseDouble(coordsArray[0]), Double.parseDouble(coordsArray[1]));
					
					Double hectare = Double.parseDouble(element.getElementsByTagName("Hectare").item(0).getTextContent());
					
					String neighbourhoodName = element.getElementsByTagName("NeighbourhoodName").item(0).getTextContent();
					
					String neighbourhoodURL = element.getElementsByTagName("NeighbourhoodURL").item(0).getTextContent();
				


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

					
					Park park = new Park(pid, pname, official, streetNumber, streetName, ewStreet, nsStreet, coordinate, hectare, neighbourhoodName, neighbourhoodURL);
					System.out.println(park.getPname());
					myParkList.add(park);
					
				}
			}
			
			for (Park park : myParkList) {
				System.out.println(myParkList.size());
				System.out.println("park: ");
				System.out.println(park.getPname());
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
