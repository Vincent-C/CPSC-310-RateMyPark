package com.ratemypark.client;

/*
 * Source: Example code from GWT v3 Google Maps API, modified
 * https://github.com/branflake2267/GWT-Maps-V3-Api/blob/master/gwt-maps-showcase/src/main/java/com/google/gwt/maps/testing/client/maps/DirectionsServiceMapWidget.java
 * 
 * #%L
 * GWT Maps API V3 - Showcase
 * %%
 * Copyright (C) 2011 - 2012 GWT Maps API V3
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

import com.google.gwt.ajaxloader.client.ArrayHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.services.DirectionsRenderer;
import com.google.gwt.maps.client.services.DirectionsRendererOptions;
import com.google.gwt.maps.client.services.DirectionsRequest;
import com.google.gwt.maps.client.services.DirectionsResult;
import com.google.gwt.maps.client.services.DirectionsResultHandler;
import com.google.gwt.maps.client.services.DirectionsService;
import com.google.gwt.maps.client.services.DirectionsStatus;
import com.google.gwt.maps.client.services.DirectionsWaypoint;
import com.google.gwt.maps.client.services.Distance;
import com.google.gwt.maps.client.services.DistanceMatrixElementStatus;
import com.google.gwt.maps.client.services.DistanceMatrixRequest;
import com.google.gwt.maps.client.services.DistanceMatrixRequestHandler;
import com.google.gwt.maps.client.services.DistanceMatrixResponse;
import com.google.gwt.maps.client.services.DistanceMatrixResponseElement;
import com.google.gwt.maps.client.services.DistanceMatrixResponseRow;
import com.google.gwt.maps.client.services.DistanceMatrixService;
import com.google.gwt.maps.client.services.DistanceMatrixStatus;
import com.google.gwt.maps.client.services.Duration;
import com.google.gwt.maps.client.services.TravelMode;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * See <a href=
 * "https://developers.google.com/maps/documentation/javascript/layers.html#FusionTables"
 * >FusionTables API Doc</a>
 */
public class DirectionsServiceMapWidget extends Composite {

  private VerticalPanel pWidget;
  MapWidget mapWidget;
  private HTML htmlDistanceMatrixService = new HTML("&nbsp;");
  
  private String origin;
  private Double destinationLat;
  private Double destinationLong;
  private List<Park> parks;
  
  // used to differentiate between code paths for compare dialog vs mapping directions
  private boolean isCompareDialog;

  // Constructor used in compare dialog
  public DirectionsServiceMapWidget(List<Park> parks) {
	this.parks = parks;
	this.isCompareDialog = true;
	
	pWidget = new VerticalPanel();
	initWidget(pWidget);
	
	draw();
  }
  
  // Constructor used for mapping directions (with user input)
  public DirectionsServiceMapWidget(String origin, Double destinationLat, Double destinationLong) {
    this.origin = origin;
    this.destinationLat = destinationLat;
    this.destinationLong = destinationLong;
    this.isCompareDialog = false;
	
	pWidget = new VerticalPanel();
    initWidget(pWidget);

    draw();
  }

  private void draw() {
    pWidget.clear();
    pWidget.add(new HTML("<br/>"));

    HorizontalPanel hp = new HorizontalPanel();
    pWidget.add(hp);
    hp.add(new HTML("Directions Service&nbsp;&nbsp;&nbsp;&nbsp;"));
    hp.add(htmlDistanceMatrixService);

    drawMap();
    drawDirectionsWithMidPoint();
  }

  private void drawMap() {
	// Let the center of the map be an arbitrary pair of coords
	// in the middle of Vancouver, BC
    LatLng center = LatLng.newInstance(49.23, -123.08);
    MapOptions opts = MapOptions.newInstance();
    opts.setZoom(8);
    opts.setCenter(center);
    opts.setMapTypeId(MapTypeId.HYBRID);

    mapWidget = new MapWidget(opts);
    pWidget.add(mapWidget);
    mapWidget.setSize("750px", "500px");

    mapWidget.addClickHandler(new ClickMapHandler() {
      public void onEvent(ClickMapEvent event) {
      }
    });
  }

  private void drawDirectionsWithMidPoint() {
    DirectionsRendererOptions options = DirectionsRendererOptions.newInstance();
    final DirectionsRenderer directionsDisplay = DirectionsRenderer.newInstance(options);
    directionsDisplay.setMap(mapWidget);

    // Hardcoded locations provided by example code
    //String origin = "Arlington, WA";
    //String destination = "Seattle, WA";
    
    DirectionsRequest request = DirectionsRequest.newInstance();
    
    if (isCompareDialog) {
    	// Arbitrarily pick 1st park to be origin, 2nd park to be destination,
    	// and other selected parks, if any, to be waypoints
    	
    	Park parkOrigin = parks.get(0);
    	Park parkDest = parks.get(1);
    	
    	LatLng actualOrigin = LatLng.newInstance(parkOrigin.getLatitude(), parkOrigin.getLongitude());
        LatLng destination = LatLng.newInstance(parkDest.getLatitude(), parkDest.getLongitude());
        
    	JsArray<DirectionsWaypoint> waypoints = JsArray.createArray().cast();

        // Stop over these waypoints
    	// i = 2 because the first 2 parks are already accounted for,
    	// as "origin" and "destination"
        for(int i = 2; i < parks.size(); i++ ) {
        	//System.out.println(parks.size());
        	//System.out.println(parks.get(i).getPname());
        	LatLng stopOverWayPoint = LatLng.newInstance(parks.get(i).getLatitude(), parks.get(i).getLongitude());
        	DirectionsWaypoint waypoint = DirectionsWaypoint.newInstance();
        	waypoint.setStopOver(true);
        	waypoint.setLocation(stopOverWayPoint);

        	waypoints.push(waypoint);
        	request.setWaypoints(waypoints);
        }
        
        request.setOrigin(actualOrigin);
        request.setDestination(destination);
        request.setTravelMode(TravelMode.DRIVING);
        request.setOptimizeWaypoints(true);
        
    }
    else {
        LatLng destination = LatLng.newInstance(destinationLat, destinationLong);
        
        request.setOrigin(origin);
        request.setDestination(destination);
        request.setTravelMode(TravelMode.DRIVING);
        request.setOptimizeWaypoints(true);
    }
 

    DirectionsService o = DirectionsService.newInstance();
    o.route(request, new DirectionsResultHandler() {
      public void onCallback(DirectionsResult result, DirectionsStatus status) {
        if (status == DirectionsStatus.OK) {
          directionsDisplay.setDirections(result);
        } else if (status == DirectionsStatus.INVALID_REQUEST) {

        } else if (status == DirectionsStatus.MAX_WAYPOINTS_EXCEEDED) {

        } else if (status == DirectionsStatus.NOT_FOUND) {

        } else if (status == DirectionsStatus.OVER_QUERY_LIMIT) {

        } else if (status == DirectionsStatus.REQUEST_DENIED) {

        } else if (status == DirectionsStatus.UNKNOWN_ERROR) {

        } else if (status == DirectionsStatus.ZERO_RESULTS) {

        }

      }
    });
  }

 
}