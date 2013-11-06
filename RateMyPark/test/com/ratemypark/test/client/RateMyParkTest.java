package com.ratemypark.test.client;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.ratemypark.client.Park;
import com.ratemypark.client.RateMyPark;

import org.junit.Test;

import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;
import com.googlecode.gwt.test.utils.events.Browser;

@GwtModule("com.ratemypark.RateMyPark")
public class RateMyParkTest extends GwtTest {

	  @Before
	  public void setup() {
		RateMyPark app = new RateMyPark();
		app.onModuleLoad();
		this.getBrowserSimulator().fireLoopEnd(); 
	  }
	  
	  @Test
	  public void testLoad() {
		  
		// Test that the header login button is properly attached to page
		Button loginButton = (Button) RootPanel.get("loginButtonContainer").getWidget(0);
	    assertTrue(loginButton.isAttached());
	    
		// Test that the header login button is properly attached to page
		Button logoutButton = (Button) RootPanel.get("logoutButtonContainer").getWidget(0);
	    assertTrue(logoutButton.isAttached());
	  }

}
