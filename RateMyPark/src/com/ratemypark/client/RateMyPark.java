package com.ratemypark.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.ratemypark.exception.BadPasswordException;
import com.ratemypark.exception.DatabaseException;
import com.ratemypark.exception.UserNameException;
import com.ratemypark.shared.BCrypt;
import com.ratemypark.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.MapImpl;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RateMyPark implements EntryPoint, ValueChangeHandler<String> {
	/**
	 * The message displayed to the user when the server cannot be reached or returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network " + "connection and try again.";

	private final LoginServiceAsync loginSvc = GWT.create(LoginService.class);

	private final LogoutServiceAsync logoutSvc = GWT.create(LogoutService.class);

	private final LoadParksServiceAsync loadParksSvc = GWT.create(LoadParksService.class);

	private LoginInfo loginInfo = null;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		loadParksIntoDatastore(false); // DB related stuff (moved the boolean hack into this function)
		// Load the header
		loadHeader();
		// Load the body
		// loadParksBody();
		// loadMapApi(); // called in loadSpecificParkTable() instead

		// Add history listener
		History.addValueChangeHandler(this);

		// Now that we've setup our listener, fire the initial history state.
		History.fireCurrentHistoryState();
		// loadParkTestMethods(); // used to test that the datastore contains the correct info
	}

	private void loadHeader() {

		// RootPanel.get("header").clear();

		final Button loginButton = new Button("Login");
		final Button logoutButton = new Button("Logout");
		final Button newAccountButton = new Button("New Account");

		// We can add style names to widgets
		loginButton.addStyleName("loginButton");
		logoutButton.addStyleName("logoutButton");
		newAccountButton.addStyleName("newAccountButton");

		// Add the loginButton and newAccountButton to the RootPanel
		RootPanel.get("loginButtonContainer").add(loginButton);
		RootPanel.get("logoutButtonContainer").add(logoutButton);
		RootPanel.get("logoutButtonContainer").getElement().setAttribute("style", "display:none");
		RootPanel.get("newAccountButtonContainer").add(newAccountButton);

		loginButton.getElement().setId("loginButtonId");
		logoutButton.getElement().setId("logoutButtonId");
		newAccountButton.getElement().setId("newAccountButtonId");

		final AsyncCallback<LoginInfo> loginCallback = new AsyncCallback<LoginInfo>() {
			public void onFailure(Throwable caught) {
				handleError(caught);
			}

			// result is the session ID from doLogin
			public void onSuccess(final LoginInfo result) {
				// duration remembering login. 2 weeks in this example.
				final long DURATION = 1000 * 60 * 60 * 24 * 14;
				Date expires = new Date(System.currentTimeMillis() + DURATION);
				Cookies.setCookie("sid", result.getSessionID(), expires, null, "/", false);
				loginInfo = result;

				String username = result.getUsername();

				Anchor profileLink = new Anchor(username, "#profile");
				// GWT wraps links in a div, so make it inline...

				profileLink.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						// event.preventDefault();
						loadProfilePage();
					}
				});

				RootPanel.get("username").getElement().setInnerHTML("Logged in as: ");
				RootPanel.get("username").add(profileLink);

				System.out.println("Client side cookie login: " + Cookies.getCookie("sid"));
				toggleLoginButtons();

				// Reload page if user was on a park page
				if (!History.getToken().isEmpty() && !History.getToken().equals("profile")) {
					RootPanel.get("body").clear();
					loadSpecificParkPage(History.getToken());
				}
			}

			private void toggleLoginButtons() {
				Element loginContainer = RootPanel.get("loginButtonContainer").getElement();
				Element logoutContainer = RootPanel.get("logoutButtonContainer").getElement();
				Element newAccountContainer = RootPanel.get("newAccountButtonContainer").getElement();

				if (loginContainer.getAttribute("style") == "display:none") {
					loginContainer.setAttribute("style", "");
					logoutContainer.setAttribute("style", "display:none");
					newAccountContainer.setAttribute("style", "");
				} else {
					loginContainer.setAttribute("style", "display:none");
					logoutContainer.setAttribute("style", "");
					newAccountContainer.setAttribute("style", "display:none");
				}
			}

			private void handleError(Throwable error) {
				if (error instanceof UserNameException) {
					Window.alert(error.getMessage());
				} else if (error instanceof BadPasswordException) {
					Window.alert(error.getMessage());
				}
			}
		};

		String sessionID = Cookies.getCookie("sid");
		if (sessionID != null) {
			loginSvc.doLogin(sessionID, loginCallback);
		}

		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LoginDialog loginDialog = new LoginDialog(loginCallback);
				loginDialog.setGlassEnabled(true);
				loginDialog.center();
				loginDialog.showRelativeTo(newAccountButton);
			}
		});

		logoutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				logoutSvc.logout(new AsyncCallback<Void>() {
					public void onFailure(Throwable caught) {
						System.out.println("LOGOUT FAILED");
					}

					public void onSuccess(Void ignore) {
						loginInfo = null;
						Cookies.removeCookie("sid");
						Window.alert("Logged out");
						// System.out.println("Client side cookie logout: " + Cookies.getCookie("sid"));
						// Clear username text
						RootPanel.get("username").getElement().setInnerText("");
						toggleLoginButtons();

						History.newItem("");
					}

					private void toggleLoginButtons() {
						RootPanel.get("loginButtonContainer").getElement().setAttribute("style", "");
						RootPanel.get("logoutButtonContainer").getElement().setAttribute("style", "display:none");
						RootPanel.get("newAccountButtonContainer").getElement().setAttribute("style", "");
					}
				});

			}
		});

		newAccountButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				NewAccountDialog newAccountDialog = new NewAccountDialog(loginCallback);
				newAccountDialog.setGlassEnabled(true);
				newAccountDialog.showRelativeTo(newAccountButton);
			}
		});
	}

	// This method is used to clear the body only once, then add the 'body' after, using the methods
	private void loadParksBody() {
			
		clearBodyAndFooter();
		
		RootPanel.get("body").getElement().setAttribute("style", "width:750px;margin: auto;");
		
		loadDirectionsButton();
		loadSearchButton();
		loadSuggestedPark();
		loadParksTable();

		// loadParksTextandButton(); // commenting this out, because we dont need it
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		// This method is called whenever the application's history changes. Set
		// the label to reflect the current history token.
		if (!event.getValue().isEmpty()) {
			if (event.getValue().equals("profile")) {
				// RootPanel.get("body").clear();
				loadProfilePage();
			} else {
				// RootPanel.get("body").clear();
				loadSpecificParkPage(event.getValue());
			}
		} else {
			loadParksBody();
		}
	}

	private void clearBodyAndFooter() {
		RootPanel.get("body").clear();
		RootPanel.get("fb-footer").clear();
		RootPanel.get("fb-footer").getElement().setAttribute("style", "display:none");
		RootPanel.get("body").getElement().setAttribute("style", "");
	}

	private void loadSpecificParkPage(final String parkID) {
		loadParksSvc.getPark(Long.parseLong(parkID), new AsyncCallback<Park>() {
			public void onFailure(Throwable caught) {
				System.out.println("Park did not get properly");
			}

			public void onSuccess(Park park) {
				if (park != null) {
					clearBodyAndFooter();
					NodeList<Element> tags = Document.get().getElementsByTagName("meta");
					for (int i = 0; i < tags.getLength(); i++) {
						if (tags.getItem(i).getAttribute("property").equals("og:title")) {
							tags.getItem(i).setAttribute("content", park.getPname());
						}
					}
					
					// Create a HorizontalSplitPanel to place map and ratings/reviews widgets
					@SuppressWarnings("deprecation")
					HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
					splitPanel.setSize("1600px", "500px");
					splitPanel.setSplitPosition("750px");

					// VerticalPanel to hold the reviews and ratings as a single widget
					// (HorizontalSplitWidget only allows one widget on each side of the split
					VerticalPanel ratingsAndReviews = new VerticalPanel();
					
					
					// Create table of data related to this specific park
					loadSpecificParkTable(park);
					loadMapApi(park, splitPanel);
					loadFacebookButtons(park);
					loadParkReviews(park, ratingsAndReviews);
					loadParkRatings(park, ratingsAndReviews);
					
					splitPanel.setRightWidget(ratingsAndReviews);
					RootPanel.get("body").add(splitPanel);
					
				} else {
					System.out.println("Park is null");
				}
			}
		});

	}

	private void loadParksTextandButton() {
		final Button loadParksButton = new Button("Load Parks with PID:");
		final TextBox loadParksTextBox = new TextBox();

		// We can add style names to widgets
		loadParksButton.addStyleName("loadParkButton");
		loadParksTextBox.setWidth("35px");
		RootPanel.get("body").add(loadParksButton);
		RootPanel.get("body").add(loadParksTextBox);

		// Code that gets the park from a user-inputted pid
		loadParksButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// Assume input is valid
				String s = loadParksTextBox.getText();
				Long pid = Long.valueOf(s);

				loadParksSvc.getPark(pid, new AsyncCallback<Park>() {
					public void onFailure(Throwable caught) {
						System.out.println("Error occured: " + caught.getMessage());
						handleError(caught);
					}

					public void onSuccess(Park park) {
						Window.alert(park.getPname() + " loaded, with coordinates (" + park.getLatitude() + ","
								+ park.getLongitude() + ").");

						System.out.println(park.getPname() + " loaded.");
					}
				});
			}
		});
	}

	private void loadDirectionsButton() {
		final Button loadDirectionsButton = new Button("Map directions");
		// final TextBox loadLatitudeTextBox = new TextBox();
		// final TextBox loadLongitudeTextBox = new TextBox();
		final TextBox loadLocationBox = new TextBox();
		final TextBox loadParkTextBox = new TextBox();

		loadLocationBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadLocationBox.setText("");
			}
		});

		loadParkTextBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadParkTextBox.setText("");
			}
		});

		// We can add style names to widgets
		// loadDirectionsButton.setStyleName("loadDirectionsButton");
		loadDirectionsButton.setStyleName("our-gwt-Button");
		// loadLatitudeTextBox.setWidth("55px");
		// loadLongitudeTextBox.setWidth("55px");
		loadLocationBox.setWidth("110px");
		loadParkTextBox.setWidth("50px");
		// loadLatitudeTextBox.setText("Latitude");
		// loadLongitudeTextBox.setText("Longitude");
		loadLocationBox.setText("Insert your address here");
		loadParkTextBox.setText("Park ID");
		// RootPanel.get("body").add(loadLatitudeTextBox);
		// RootPanel.get("body").add(loadLongitudeTextBox);
		RootPanel.get("body").add(loadLocationBox);
		RootPanel.get("body").add(loadParkTextBox);
		RootPanel.get("body").add(loadDirectionsButton);

		loadDirectionsButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// Assume input is valid
				// String s1 = loadLatitudeTextBox.getText();
				// String s2 = loadLongitudeTextBox.getText();
				if (loadLocationBox.getText().isEmpty()) {
					Window.alert("No address specified");
					return;
				}
				if (loadParkTextBox.getText().isEmpty()) {
					Window.alert("No park ID specified");
					return;
				}
				
				final String location = loadLocationBox.getText();
				String s3 = loadParkTextBox.getText();
				
				if (!s3.matches("^[0-9]+")) {
					Window.alert("Invalid park ID specified");
				}
				// final Double latitude = Double.valueOf(s1);
				// final Double longitude = Double.valueOf(s2);
				Long parkID = Long.valueOf(s3);

				loadParksSvc.getPark(parkID, new AsyncCallback<Park>() {
					public void onFailure(Throwable caught) {
						System.out.println("Error occured: " + caught.getMessage());
						handleError(caught);
					}

					public void onSuccess(Park result) {
						DirectionsDialog dd = new DirectionsDialog(location, result);
						dd.showRelativeTo(loadDirectionsButton);

					}
				});
			}
		});
	}

	private void loadSearchButton() {
		final Button loadSearchButton = new Button("Search");
		// final TextBox loadLatitudeTextBox = new TextBox();
		// final TextBox loadLongitudeTextBox = new TextBox();
		final TextBox loadSearchBox = new TextBox();
		final ListBox listOfParkAttributes = new ListBox();

		loadSearchBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadSearchBox.setText("");
			}
		});
		
		listOfParkAttributes.addItem("Park ID");
		listOfParkAttributes.addItem("Park Name");
		listOfParkAttributes.addItem("Official");
		listOfParkAttributes.addItem("Street Number");
		listOfParkAttributes.addItem("Street Name");
		listOfParkAttributes.addItem("East-West Street Name");
		listOfParkAttributes.addItem("North-South Street Name");
		listOfParkAttributes.addItem("Latitude");
		listOfParkAttributes.addItem("Longitude");
		listOfParkAttributes.addItem("Size in Hectares");
		listOfParkAttributes.addItem("Neighbourhood Name");
		listOfParkAttributes.addItem("Neighbourhood URL");

		listOfParkAttributes.setVisibleItemCount(1);
		listOfParkAttributes.setSelectedIndex(1);

		// We can add style names to widgets
		loadSearchButton.setStyleName("our-gwt-Button");
		// loadLatitudeTextBox.setWidth("55px");
		// loadLongitudeTextBox.setWidth("55px");
		loadSearchBox.setWidth("110px");
		// loadLatitudeTextBox.setText("Latitude");
		// loadLongitudeTextBox.setText("Longitude");
		loadSearchBox.setText("Enter search term");
		// RootPanel.get("body").add(loadLatitudeTextBox);
		// RootPanel.get("body").add(loadLongitudeTextBox);
		RootPanel.get("body").add(loadSearchBox);
		RootPanel.get("body").add(listOfParkAttributes);
		RootPanel.get("body").add(loadSearchButton);

		loadSearchButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// Assume input is valid
				// String s1 = loadLatitudeTextBox.getText();
				// String s2 = loadLongitudeTextBox.getText();
				final String searchTerm = loadSearchBox.getText();
				// String s3 = loadParkTextBox.getText();
				// final Double latitude = Double.valueOf(s1);
				// final Double longitude = Double.valueOf(s2);
				// Long parkID = Long.valueOf(s3);
				int selectedIndex = listOfParkAttributes.getSelectedIndex();
				final String chosenAttribute = listOfParkAttributes.getValue(selectedIndex);

				loadParksSvc.getParks(new AsyncCallback<List<Park>>() {
					public void onFailure(Throwable caught) {
						System.out.println("Error occured: " + caught.getMessage());
						handleError(caught);
					}

					public void onSuccess(List<Park> result) {
						SearchDialog sd = new SearchDialog(searchTerm, chosenAttribute, result);
						sd.center();

					}
				});
			}
		});
	}

	private void loadFacebookButtons(Park park) {
		RootPanel.get("fb-footer").clear();
		RootPanel.get("fb-footer").getElement().setAttribute("style", "");
		String pageURL = Window.Location.getHref();

		HTMLPanel newFBLike = new HTMLPanel("<fb:like id='fb-like-button' href=" + pageURL
				+ " layout='standard' action='like' show_faces='true' share='true'></fb:like>");
		RootPanel.get("fb-footer").add(newFBLike);

		Button invisButton = new Button("");
		invisButton.getElement().setAttribute("onclick", "FB.XFBML.parse();");
		invisButton.click();

	}

	private void loadParksTable() {
		final FlexTable table = new FlexTable();
		table.setStyleName("flexTable-frontpage");
		final Button compareButton = new Button("Compare");
		compareButton.setStyleName("our-gwt-Button");
		loadParksSvc.getParks(new AsyncCallback<List<Park>>() {
			public void onFailure(Throwable caught) {
				System.out.println("Parks did not get properly");
			}

			public void onSuccess(List<Park> parkList) {
				table.setBorderWidth(6);
				table.setText(0, 0, "");
				table.setText(0, 1, "Park ID");
				table.setText(0, 2, "Park Name");
				table.setText(0, 3, "Address");
				table.setText(0, 4, "Neighbourhood Name");
				table.getRowFormatter().setStyleName(0, "tableheader");
				int index = 1;
				for (Park p : parkList) {
					final CheckBox cb = new CheckBox("");
					table.insertRow(index);
					cb.getElement().setAttribute("pid", String.valueOf(p.getPid()));
					table.setWidget(index, 0, cb);
					table.setText(index, 1, String.valueOf(p.getPid()));
					Hyperlink link = new Hyperlink(p.getPname(), String.valueOf(p.getPid()));
					table.setWidget(index, 2, link);
					table.setText(index, 3, String.valueOf(p.getStreetNumber()) + " " + p.getStreetName());
					table.setText(index, 4, p.getNeighbourhoodName());
					index++;
					// System.out.println("Adding index" + index);
				}

				compareButton.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						// Assume input is valid
						List<Long> pids = new ArrayList<Long>();
						for (int i = 1; i < table.getRowCount(); i++) {
							CheckBox cb = (CheckBox) table.getWidget(i, 0);
							if (cb.getValue()) {
								pids.add(Long.parseLong(cb.getElement().getAttribute("pid")));
							}
						}
						if (!pids.isEmpty()) {
							loadParksSvc.getParks(pids, new AsyncCallback<List<Park>>() {
								public void onFailure(Throwable caught) {
									System.out.println("Error occured: " + caught.getMessage());
									handleError(caught);
								}

								public void onSuccess(List<Park> parks) {
									for (Park park : parks) {
										System.out.println(park.getPname() + " loaded.");
									}
									CompareDialog cd = new CompareDialog(parks);
									cd.showRelativeTo(compareButton);
								}
							});
						}
					}
				});
				RootPanel.get("body").add(new FlexTable());
				RootPanel.get("body").add(compareButton);
				RootPanel.get("body").add(table);
			}
		});
	}

	private void loadSuggestedPark() {
		final SuggestedParkServiceAsync suggestedParkSvc = GWT.create(SuggestedParkService.class);

		final VerticalPanel suggestedParkPanel = new VerticalPanel();
		suggestedParkPanel.setStyleName("suggestedParkPanel");
		java.util.Random rng = new java.util.Random();

		int pref = 1; // 0 = no pref (random 1,2,3), 1 = highest rated, 2 = most rated, 3 = random
		if (loginInfo == null || (loginInfo != null && loginInfo.getSuggestionPreference() == 0)) {
			pref = 1 + rng.nextInt(3); // Random number between 1 and 3
		} else {
			pref = loginInfo.getSuggestionPreference();
		}

		switch (pref) {
		case 1: // Show highest rated park
			suggestedParkSvc.getHighestRated(new AsyncCallback<SuggestedPark>() {
				public void onFailure(Throwable caught) {
					Window.alert("Failed to get a highest suggested park");
				}

				public void onSuccess(SuggestedPark result) {
					Park park = result.getPark();
					suggestedParkPanel.add(new HTML("<b>" + "You should visit our highest rated park:" + "</b>"));
					Hyperlink link = new Hyperlink(park.getPname(), String.valueOf(park.getPid()));
					suggestedParkPanel.add(link);
					suggestedParkPanel.add(new HTML("Average Rating: " + result.getRating() + " out of 5"));
					suggestedParkPanel.add(new HTML("Number of ratings: " + result.getNumRatings()));
					RootPanel.get("body").add(suggestedParkPanel);
				}
			});
			break;
		case 2: // Show most rated park
			suggestedParkSvc.getMostRated(new AsyncCallback<SuggestedPark>() {
				public void onFailure(Throwable caught) {
					Window.alert("Failed to get a most rated suggested park");
				}

				public void onSuccess(SuggestedPark result) {
					Park park = result.getPark();
					suggestedParkPanel.add(new HTML("<b>" + "You should visit our most rated park:" + "</b>"));
					Hyperlink link = new Hyperlink(park.getPname(), String.valueOf(park.getPid()));
					suggestedParkPanel.add(link);
					suggestedParkPanel.add(new HTML("Average Rating: " + result.getRating() + " out of 5"));
					suggestedParkPanel.add(new HTML("Number of ratings: " + result.getNumRatings()));
					RootPanel.get("body").add(suggestedParkPanel);
				}
			});
			break;
		case 3: // Show random park
			suggestedParkSvc.getRandomPark(new AsyncCallback<SuggestedPark>() {
				public void onFailure(Throwable caught) {
					Window.alert("Failed to get a random suggested park");
				}

				public void onSuccess(SuggestedPark result) {
					Park park = result.getPark();
					suggestedParkPanel.add(new HTML("<b>" + "You should visit this park:" + "</b>"));
					Hyperlink link = new Hyperlink(park.getPname(), String.valueOf(park.getPid()));
					suggestedParkPanel.add(link);
					if (result.getNumRatings() == 0) {
						suggestedParkPanel.add(new HTML("This park has yet to be rated."));
					} else {
						suggestedParkPanel.add(new HTML("Average Rating: " + result.getRating() + " out of 5"));
						suggestedParkPanel.add(new HTML("Number of ratings: " + result.getNumRatings()));
					}
					RootPanel.get("body").add(suggestedParkPanel);
				}
			});
			break;
		default:
			// shouldnt run
		}
	}

	private void loadSpecificParkTable(Park park) {
		final FlexTable table = new FlexTable();
		table.setBorderWidth(12);
		table.setText(0, 0, "");
		table.setText(0, 1, "Park ID");
		table.setText(0, 2, "Park Name");
		table.setText(0, 3, "Official");
		table.setText(0, 4, "Street Number");
		table.setText(0, 5, "Street Name");
		table.setText(0, 6, "East-West Street Name");
		table.setText(0, 7, "North-South Street Name");
		table.setText(0, 8, "Coordinates");
		table.setText(0, 9, "Size in Hectares");
		table.setText(0, 10, "Neighbourhood Name");
		table.setText(0, 11, "Neighbourhood URL");
		table.getRowFormatter().setStyleName(0, "tableheader");
		table.insertRow(1);
		// Create table of data related to this specific park
		table.setText(1, 1, String.valueOf(park.getPid()));
		table.setText(1, 2, park.getPname());
		table.setText(1, 3, isOfficialString(park));
		table.setText(1, 4, String.valueOf(park.getStreetNumber()));
		table.setText(1, 5, park.getStreetName());
		table.setText(1, 6, park.getEwStreet());
		table.setText(1, 7, park.getNsStreet());
		table.setText(1, 8, getCoordinateString(park));
		table.setText(1, 9, String.valueOf(park.getHectare()));
		table.setText(1, 10, park.getNeighbourhoodName());
		table.setText(1, 11, park.getNeighbourhoodURL());

		RootPanel.get("body").add(table);
	}

	private void loadMapApi(Park park, final HorizontalSplitPanel splitPanel) {
		if (park != null) {
			boolean sensor = true;

			// Testing:
			System.out.println(park.getPname());
			final Double latitude = park.getLatitude();
			System.out.println(latitude);
			final Double longitude = park.getLongitude();

			// load all the libs for use in the maps
			ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
			loadLibraries.add(LoadLibrary.ADSENSE);
			loadLibraries.add(LoadLibrary.DRAWING);
			loadLibraries.add(LoadLibrary.GEOMETRY);
			loadLibraries.add(LoadLibrary.PANORAMIO);
			loadLibraries.add(LoadLibrary.PLACES);
			loadLibraries.add(LoadLibrary.WEATHER);
			loadLibraries.add(LoadLibrary.VISUALIZATION);

			Runnable onLoad = new Runnable() {
				@Override
				public void run() {
					drawMap(latitude, longitude, splitPanel);
				}
			};

			LoadApi.go(onLoad, loadLibraries, sensor);
			// ignore this; map added to body with addMapWidget() instead
			// RootPanel.get("body").add(table);
		}

		else
			System.out.println("Park is null");
	}

	private void loadParkReviews(Park park, VerticalPanel vertPanel) {
		final ReviewServiceAsync reviewSvc = GWT.create(ReviewService.class);
		final Long pid = park.getPid();
		final String parkName = park.getPname();

		VerticalPanel contentPanel = new VerticalPanel();
		
		HTMLPanel header = new HTMLPanel("<div class='contentHeader'>" + "Reviews for " + park.getPname() + "</div>");
		contentPanel.add(header);

		final VerticalPanel reviewsPanel = new VerticalPanel();
		reviewsPanel.setStyleName("reviewsPanel");
		final VerticalPanel newReviewsPanel = new VerticalPanel();
		newReviewsPanel.setStyleName("reviewsPanel");
		contentPanel.add(reviewsPanel);
		contentPanel.add(newReviewsPanel);
		
		vertPanel.add(contentPanel);

		reviewSvc.getReviews(pid, new AsyncCallback<List<Review>>() {
			public void onFailure(Throwable caught) {
				Window.alert("Failed to get reviews");
			}

			public void onSuccess(List<Review> result) {
				if (result.isEmpty()) {
					reviewsPanel.add(new HTMLPanel("<div id='noReviews'>" + "There are no reviews for this park yet."
							+ "</div>"));
				} else {
					for (Review r : result) {
						reviewsPanel.add(new HTML("<b>" + "Review by: " + r.getUsername() + "</b> "
								+ r.getDateCreated().toString()));
						reviewsPanel.add(new HTML(r.getReviewText()));
					}
				}
			}
		});

		// If user is logged in, allow the user to write a review
		newReviewsPanel.getElement().setId("newReviewsPanel");
		if (loginInfo != null) {
			newReviewsPanel.add(new HTML("<b>Write a new review:</b>"));

			final TextArea reviewTextArea = new TextArea();
			reviewTextArea.setCharacterWidth(100);
			reviewTextArea.setVisibleLines(5);
			newReviewsPanel.add(reviewTextArea);

			final Button submitReview = new Button("Submit Review");
			submitReview.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					String reviewText = reviewTextArea.getText();
					if (reviewText.length() > 500) {
						Window.alert("Your review is too long.");
					} else {
						// Only logged in users be able to see this, so loginInfo should not be null
						reviewSvc.newReview(loginInfo.getUsername(), pid, parkName, reviewText,
								new AsyncCallback<Review>() {
									public void onFailure(Throwable caught) {
										Window.alert("Could not create your review!");
									}

									public void onSuccess(Review newReview) {
										Element noReviews = Document.get().getElementById("noReviews");
										if (noReviews != null) {
											noReviews.setAttribute("style", "display:none");
										}
										reviewTextArea.setText("");
										reviewsPanel.add(new HTML("<b>" + "Review by: " + newReview.getUsername()
												+ "</b> " + newReview.getDateCreated().toString()));
										reviewsPanel.add(new HTML(newReview.getReviewText()));
									}
								});
					}
				}
			});
			newReviewsPanel.add(submitReview);
		}
	}

	private void loadParkRatings(Park park, VerticalPanel vertPanel) {
		final RatingServiceAsync ratingSvc = GWT.create(RatingService.class);
		final Long pid = park.getPid();
		
		VerticalPanel contentPanel = new VerticalPanel();

		HTMLPanel header = new HTMLPanel("<div class='contentHeader'>" + "Ratings for " + park.getPname() + "</div>");
		contentPanel.add(header);

		final VerticalPanel ratingsPanel = new VerticalPanel();
		final VerticalPanel addRatingPanel = new VerticalPanel();

		// TODO: change style name later, to rating something
		ratingsPanel.setStyleName("reviewsPanel");

		contentPanel.add(ratingsPanel);
		contentPanel.add(addRatingPanel);

		vertPanel.add(contentPanel);
		
		ratingSvc.totalNumRatings(pid, new AsyncCallback<Integer>() {
			public void onFailure(Throwable caught) {
				Window.alert("Failed to get number of ratings for the park");
			}

			public void onSuccess(Integer result) {
				// do something with the average
				if (result == 0) {
					ratingsPanel.add(new HTML("No one has rated this park yet. Be the first to rate it!"));
				} else {
					ratingSvc.averageRating(pid, new AsyncCallback<Float>() {
						public void onFailure(Throwable caught) {
							Window.alert("Failed to get average rating for the park");
						}

						public void onSuccess(Float result) {
							// do something with the average
							if (result == null) {
							} else {
								ratingsPanel.add(new HTML("<b>" + "Average rating: " + result + "</b> "));
							}
						}
					});
					ratingsPanel.add(new HTML("<b>" + "Number of ratings: " + result + "</b>"));
				}
			}
		});

		// If user is logged in, allow the user to rate the park
		addRatingPanel.getElement().setId("addRatingPanel");
		if (loginInfo != null) {
			addRatingPanel.add(new HTML("<b>Rate this park:</b>"));

			final RadioButton rb0 = new RadioButton("NewRating", "0");
			final RadioButton rb1 = new RadioButton("NewRating", "1");
			final RadioButton rb2 = new RadioButton("NewRating", "2");
			final RadioButton rb3 = new RadioButton("NewRating", "3");
			final RadioButton rb4 = new RadioButton("NewRating", "4");
			final RadioButton rb5 = new RadioButton("NewRating", "5");

			addRatingPanel.add(rb0);
			addRatingPanel.add(rb1);
			addRatingPanel.add(rb2);
			addRatingPanel.add(rb3);
			addRatingPanel.add(rb4);
			addRatingPanel.add(rb5);

			final ArrayList<RadioButton> rbList = new ArrayList<RadioButton>();
			rbList.add(rb0);
			rbList.add(rb1);
			rbList.add(rb2);
			rbList.add(rb3);
			rbList.add(rb4);
			rbList.add(rb5);

			// Get the index that is selected

			final Button submitRating = new Button("Submit Rating");
			submitRating.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					int ratingNum = getSelectedIndex(rbList);

					if (ratingNum == -1) {
						Window.alert("Please select a rating before submitting!");
					} else {

						// Only logged in users be able to see this, so loginInfo should not be null
						ratingSvc.createRating(pid, loginInfo.getUsername(), ratingNum, new AsyncCallback<Void>() {
							public void onFailure(Throwable caught) {
								Window.alert("Could not submit your rating!");
							}

							public void onSuccess(Void ignore) {
								ratingsPanel.clear();
								ratingSvc.totalNumRatings(pid, new AsyncCallback<Integer>() {
									public void onFailure(Throwable caught) {
										Window.alert("Failed to get number of ratings for the park");
									}

									public void onSuccess(Integer result) {
										// do something with the average
										if (result == 0) {
											ratingsPanel.add(new HTML("<b>"
													+ "No one has rated this park yet. Be the first to rate it!"
													+ "</b> "));
										} else {
											ratingSvc.averageRating(pid, new AsyncCallback<Float>() {
												public void onFailure(Throwable caught) {
													Window.alert("Failed to get average rating for the park");
												}

												public void onSuccess(Float result) {
													// do something with the average
													if (result == null) {
													} else {
														ratingsPanel.add(new HTML("<b>" + "Average rating: " + result
																+ "</b> "));
													}
												}
											});
											ratingsPanel.add(new HTML("<b>" + "Number of ratings: " + result + "</b>"));
										}
									}
								});

							}
						});
					}
				}
			});
			addRatingPanel.add(submitRating);
		}
	}

	private void loadParksIntoDatastore(Boolean loadDB) {
		// Boolean HACK: true if you want to (re)load the database from the XML, else keep at false
		if (loadDB) {
			loadParksSvc.loadXMLParks(new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					System.out.println("Parks did not load properly");
				}

				public void onSuccess(Void ignore) {
					Window.alert("Parks loaded.");
					System.out.println("Parks loaded.");
				}
			});
		}
	}

	private void loadParkTestMethods() {
		// Code to test getParks
		loadParksSvc.getParks(new AsyncCallback<List<Park>>() {
			public void onFailure(Throwable caught) {
				System.out.println("Error occured: " + caught.getMessage());
				handleError(caught);
			}

			public void onSuccess(List<Park> parks) {
				Window.alert(parks.get(3).getPname() + " loaded.");
				System.out.println(parks.get(5).getPname() + " loaded.");
			}
		});
		// Code to test getParkNames
		loadParksSvc.getParkNames(new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				System.out.println("Error occured: " + caught.getMessage());
				handleError(caught);
			}

			public void onSuccess(String[] parks) {
				Window.alert("Park " + parks[1] + " loaded.");
				System.out.println("Park " + parks[1] + " loaded.");
			}
		});
	}

	private void loadProfilePage() {
		clearBodyAndFooter();
		final EditProfileServiceAsync profileSvc = GWT.create(EditProfileService.class);

		profileSvc.getCurrentProfile(new AsyncCallback<LoginInfo>() {
			public void onFailure(Throwable caught) {
				handleError(caught);
			}

			public void onSuccess(LoginInfo profile) {

				final String currentUsername = profile.getUsername();

				RootPanel.get("body").clear();
				// Add title to page
				HTMLPanel header = new HTMLPanel("<div class='contentHeader'>" + "Profile Page for "
						+ profile.getDisplayName() + "</div>");
				RootPanel.get("body").add(header);

				VerticalPanel vPanel = new VerticalPanel();
				vPanel.addStyleName("profile-panel");

				vPanel.add(new HTML("<b>First Name:</b>"));
				final TextBox firstName = new TextBox();
				firstName.setText(profile.getFirstName());
				firstName.addStyleName("profile-textbox");
				vPanel.add(firstName);

				vPanel.add(new HTML("<b>Last Name:</b>"));
				final TextBox lastName = new TextBox();
				lastName.setText(profile.getLastName());
				lastName.addStyleName("profile-textbox");
				vPanel.add(lastName);
				
				vPanel.add(new HTML("<b>Email:</b>"));
				final TextBox emailBox = new TextBox();
				emailBox.setText(profile.getEmail());
				emailBox.addStyleName("profile-textbox");
				vPanel.add(emailBox);

				// Radio button code... ew
				VerticalPanel preferencePanel = new VerticalPanel();
				preferencePanel.add(new HTML("<b>My Park Suggestion Preferences</b>"));

				final RadioButton rb0 = new RadioButton("pref", "No preference.");
				final RadioButton rb1 = new RadioButton("pref", "Show me the highest rated park.");
				final RadioButton rb2 = new RadioButton("pref", "Show me the most rated park.");
				final RadioButton rb3 = new RadioButton("pref", "Show me a random park. #rngesus #permabash4life #yolo");

				int pref = profile.getSuggestionPreference();
				switch (pref) {
				case 1:
					rb1.setValue(true);
					break;
				case 2:
					rb2.setValue(true);
					break;
				case 3:
					rb3.setValue(true);
					break;
				default:
					rb0.setValue(true);
					break;
				}

				preferencePanel.add(rb0);
				preferencePanel.add(rb1);
				preferencePanel.add(rb2);
				preferencePanel.add(rb3);

				final ArrayList<RadioButton> rbList = new ArrayList<RadioButton>();
				rbList.add(rb0);
				rbList.add(rb1);
				rbList.add(rb2);
				rbList.add(rb3);
				vPanel.add(preferencePanel);

				final Button editProfile = new Button("Edit Profile");
				editProfile.addClickHandler(new ClickHandler() {

					public void onClick(ClickEvent event) {
						String first = firstName.getText();
						String last = lastName.getText();
						String email = emailBox.getText();
						LoginInfo newProfile = new LoginInfo(currentUsername, Cookies.getCookie("sid"));
						newProfile.setFirstName(first);
						newProfile.setLastName(last);
						newProfile.setEmail(email);
						newProfile.setSuggestionPreference(getSelectedIndex(rbList));

						profileSvc.editProfile(newProfile, new AsyncCallback<LoginInfo>() {
							public void onFailure(Throwable caught) {
								System.out.println("Error occured: " + caught.getMessage());
								handleError(caught);
							}

							public void onSuccess(LoginInfo result) {
								Window.alert("Profile updated!");
								loginInfo = result;
								loadProfilePage();
							}
						});
					}
				});
				vPanel.add(editProfile);

				final VerticalPanel reviewsPanel = new VerticalPanel();
				reviewsPanel.setStyleName("reviewsPanel");
				final ReviewServiceAsync reviewSvc = GWT.create(ReviewService.class);
				reviewsPanel.add(new HTMLPanel("<div class='contentHeader'>" + "Reviews by " + profile.getDisplayName()
						+ "</div>"));
				reviewSvc.getReviewsForUser(profile.getUsername(), new AsyncCallback<List<Review>>() {
					public void onFailure(Throwable caught) {
						Window.alert("Failed to get reviews");
					}

					public void onSuccess(List<Review> result) {
						if (result.isEmpty()) {
							reviewsPanel
									.add(new HTMLPanel("<div>" + "You have not yet reviewed any parks." + "</div>"));
						} else {
							for (Review r : result) {
								reviewsPanel.add(new HTML("<b>" + "Review for " + r.getParkName() + "</b> "
										+ r.getDateCreated().toString()));
								reviewsPanel.add(new HTML(r.getReviewText()));
							}
						}
					}
				});

				final SuggestedParkServiceAsync suggestSvc = GWT.create(SuggestedParkService.class);
				final VerticalPanel ratedParksPanel = new VerticalPanel();
				ratedParksPanel.add(new HTMLPanel("<div class='contentHeader'>" + "Rated by " + profile.getDisplayName()
						+ "</div>"));

				suggestSvc.getRatedParks(currentUsername, new AsyncCallback<List<SuggestedPark>>() {
					public void onFailure(Throwable caught) {
						Window.alert("Failed to get ratings");
					}

					public void onSuccess(List<SuggestedPark> result) {
						if (result.isEmpty()) {
							ratedParksPanel
									.add(new HTMLPanel("<div>" + "You have not yet rated any parks." + "</div>"));
						} else {
							for (SuggestedPark sp : result) {
								ratedParksPanel
										.add(new HTML("<b>" + "Rating for " + sp.getPark().getPname() + "</b> "));
								ratedParksPanel.add(new HTML("   You rated: " + sp.getRating()));
							}
						}
					}
				});

				final VerticalPanel suggestedParkPanel = new VerticalPanel();
				//suggestedParkPanel.setStyleName("suggestedParkPanel");
				suggestedParkPanel.add(new HTML("<b>" + "Parks you have not rated yet: " + "</b>"));
				final Button notYetRatedButton = new Button("Show 10");
				final VerticalPanel tenParksPanel = new VerticalPanel();
				notYetRatedButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						suggestSvc.getNotYetRatedParks(currentUsername, new AsyncCallback<List<Park>>() {
							public void onFailure(Throwable caught) {
								System.out.println("Error occured: " + caught.getMessage());
								handleError(caught);
							}

							public void onSuccess(List<Park> parks) {
								java.util.Random rng = new java.util.Random();
								tenParksPanel.clear();
								for (int i = 0; i < 10; i++) {
									int rn = rng.nextInt(parks.size());
									Park park = parks.get(rn);
									Hyperlink link = new Hyperlink(park.getPname(), String.valueOf(park.getPid()));
									tenParksPanel.add(link);
								}
							}
						});
					}
				});
				suggestedParkPanel.add(notYetRatedButton);
				suggestedParkPanel.add(tenParksPanel);
				
				FlexTable ratedSplitPanel = new FlexTable();
				ratedSplitPanel.setText(0, 0, "");
				ratedSplitPanel.setText(0, 1, "");
				ratedSplitPanel.setWidget(0,0,ratedParksPanel);
				ratedSplitPanel.setWidget(0,1,suggestedParkPanel);
				ratedSplitPanel.getFlexCellFormatter().getElement(0, 0).setAttribute("style", "width:500px;vertical-align:top;");
				ratedSplitPanel.getFlexCellFormatter().getElement(0, 1).setAttribute("style", "vertical-align:top;");
				
			    TabLayoutPanel tabLayout = new TabLayoutPanel(32, Unit.PX);
			    tabLayout.setHeight("1000px");
			    tabLayout.add(vPanel, "My Profile");
			    tabLayout.addStyleName("profile-tabs");
			    tabLayout.add(reviewsPanel, "My Reviews");
			    tabLayout.add(ratedSplitPanel, "My Ratings");
			    RootPanel.get("body").add(tabLayout);
			}
		});
	}

	private String isOfficialString(Park p) {
		if (p.isOfficial())
			return "Yes";
		else
			return "No";
	}

	private String getCoordinateString(Park p) {
		double latitude = p.getLatitude();
		double longitude = p.getLongitude();
		return String.valueOf(latitude) + ", " + String.valueOf(longitude);
	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof DatabaseException) {
		}
	}

	private void drawMap(Double latitude, Double longitude, HorizontalSplitPanel splitPanel) {
		drawStreetViewSideBySide(latitude, longitude, splitPanel);
	}

	private void addMapWidget(Widget widget, HorizontalSplitPanel splitPanel) {
		splitPanel.setLeftWidget(widget);
	}

	private void drawStreetViewSideBySide(Double latitude, Double longitude, HorizontalSplitPanel splitPanel) {
		StreetViewSideBySideMapWidget wMap = new StreetViewSideBySideMapWidget(latitude, longitude);
		addMapWidget(wMap, splitPanel);
		wMap.mapWidget.triggerResize();
	}

	private int getSelectedIndex(List<RadioButton> rbList) {
		int ret = -1;
		for (int i = 0; i < rbList.size(); i++) {
			if (rbList.get(i).getValue()) {
				ret = i;
			}
		}
		return ret;
	}

	private static class LoginDialog extends DialogBox {
		AsyncCallback<LoginInfo> loginCallback;

		public LoginDialog(AsyncCallback<LoginInfo> loginCallback) {
			this.loginCallback = loginCallback;

			// Set the dialog box's caption
			setText("Login");
			setAnimationEnabled(true);
			VerticalPanel dialogVPanel = new VerticalPanel();

			// Username field setup
			dialogVPanel.addStyleName("dialogVPanel");
			dialogVPanel.add(new HTML("<b>Username:</b>"));
			final TextBox usernameField = new TextBox();
			dialogVPanel.add(usernameField);

			// Password field setup
			dialogVPanel.add(new HTML("<b>Password:</b>"));
			final PasswordTextBox passwordField = new PasswordTextBox();
			dialogVPanel.add(passwordField);

			// Login button setup
			final Button login = new Button("Login");
			login.getElement().setId("loginButton");
			dialogVPanel.add(login);

			login.addClickHandler(new ClickHandler() {

				/**
				 * Create a remote service proxy to talk to the server-side login service.
				 */
				private final LoginServiceAsync loginSvc = GWT.create(LoginService.class);

				public void onClick(ClickEvent event) {
					String username = usernameField.getText();
					String password = passwordField.getText();
					LoginDialog.this.hide();

					loginSvc.doLogin(username, password, LoginDialog.this.loginCallback);
				}
			});

			// Close button setup
			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
			final Button closeButton = new Button("Close");
			closeButton.getElement().setId("closeButton");
			dialogVPanel.add(closeButton);
			closeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					LoginDialog.this.hide();
				}
			});

			setWidget(dialogVPanel);
		}
	}

	private static class NewAccountDialog extends DialogBox {
		AsyncCallback<LoginInfo> loginCallback;

		public NewAccountDialog(AsyncCallback<LoginInfo> loginCallback) {
			this.loginCallback = loginCallback;

			// Set the dialog box's caption
			setText("New Account");
			setAnimationEnabled(true);
			VerticalPanel dialogVPanel = new VerticalPanel();

			// Username field setup
			dialogVPanel.addStyleName("dialogVPanel");
			dialogVPanel.add(new HTML("<b>Enter your username:</b>"));
			final TextBox usernameField = new TextBox();
			dialogVPanel.add(usernameField);

			// Password field setup
			dialogVPanel.add(new HTML("<b>Enter a password:</b>"));
			final PasswordTextBox passwordField = new PasswordTextBox();
			dialogVPanel.add(passwordField);

			// New Account button setup
			final Button createAccount = new Button("CreateAccount");
			createAccount.getElement().setId("createAccountButton");
			dialogVPanel.add(createAccount);
			createAccount.addClickHandler(new ClickHandler() {

				/**
				 * Create a remote service proxy to talk to the server-side account creation service.
				 */
				private final NewAccountServiceAsync newAccountSvc = GWT.create(NewAccountService.class);

				public void onClick(ClickEvent event) {
					String username = usernameField.getText();
					String password = passwordField.getText();
					NewAccountDialog.this.hide();

					newAccountSvc.createNewAccount(username, password, new AsyncCallback<LoginInfo>() {
						public void onFailure(Throwable caught) {
							System.out.println("Error occured: " + caught.getMessage());
							handleError(caught);
						}

						public void onSuccess(LoginInfo result) {
							NewAccountDialog.this.loginCallback.onSuccess(result);
							Window.alert("New account created: " + result.getUsername());
						}

						private void handleError(Throwable error) {
							Window.alert(error.getMessage());
							if (error instanceof UserNameException) {
							}
						}
					});
				}
			});

			// Close button setup
			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
			final Button closeButton = new Button("Close");
			closeButton.getElement().setId("closeButton");
			dialogVPanel.add(closeButton);
			closeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					NewAccountDialog.this.hide();
				}
			});
			setWidget(dialogVPanel);

		}
	}

	private static class CompareDialog extends DecoratedPopupPanel {

		public CompareDialog(final List<Park> parks) {
			setAnimationEnabled(true);
			final VerticalPanel dialogVPanel = new VerticalPanel();

			FlexTable table = new FlexTable();

			table.setBorderWidth(12);
			table.setText(0, 0, "");
			table.setText(0, 1, "Park ID");
			table.setText(0, 2, "Park Name");
			table.setText(0, 3, "Official");
			table.setText(0, 4, "Street Number");
			table.setText(0, 5, "Street Name");
			table.setText(0, 6, "East-West Street Name");
			table.setText(0, 7, "North-South Street Name");
			table.setText(0, 8, "Coordinates");
			table.setText(0, 9, "Size in Hectares");
			table.setText(0, 10, "Neighbourhood Name");
			table.setText(0, 11, "Neighbourhood URL");
			table.getRowFormatter().setStyleName(0, "tableheader");
			int index = 1;
			for (Park p : parks) {
				table.insertRow(index);
				table.setText(index, 1, String.valueOf(p.getPid()));
				table.setText(index, 2, p.getPname());
				table.setText(index, 3, isOfficialString(p));
				table.setText(index, 4, String.valueOf(p.getStreetNumber()));
				table.setText(index, 5, p.getStreetName());
				table.setText(index, 6, p.getEwStreet());
				table.setText(index, 7, p.getNsStreet());
				table.setText(index, 8, getCoordinateString(p));
				table.setText(index, 9, String.valueOf(p.getHectare()));
				table.setText(index, 10, p.getNeighbourhoodName());
				table.setText(index, 11, p.getNeighbourhoodURL());
				index++;
				// System.out.println("Adding index" + index);
			}

			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
			dialogVPanel.add(table);

			// Directions map setup

			boolean sensor = true;

			// Testing:
			System.out.println(parks.get(0).getPname());
			final Double latitude = parks.get(0).getLatitude();
			System.out.println(latitude);
			final Double longitude = parks.get(0).getLongitude();
			System.out.println(longitude);

			// load all the libs for use in the maps
			ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
			loadLibraries.add(LoadLibrary.ADSENSE);
			loadLibraries.add(LoadLibrary.DRAWING);
			loadLibraries.add(LoadLibrary.GEOMETRY);
			loadLibraries.add(LoadLibrary.PANORAMIO);
			loadLibraries.add(LoadLibrary.PLACES);
			loadLibraries.add(LoadLibrary.WEATHER);
			loadLibraries.add(LoadLibrary.VISUALIZATION);

			Runnable onLoad = new Runnable() {
				@Override
				public void run() {
					// Temporary replacement
					DirectionsServiceMapWidget wMap = new DirectionsServiceMapWidget(parks);
					dialogVPanel.add(wMap);
					wMap.mapWidget.triggerResize();
				}
			};

			LoadApi.go(onLoad, loadLibraries, sensor);

			// Close button setup
			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
			final Button closeButton = new Button("Close");
			closeButton.getElement().setId("closeButton");
			dialogVPanel.add(closeButton);

			closeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					CompareDialog.this.hide();
				}
			});
			setGlassEnabled(true);
			setWidget(dialogVPanel);
			// setWidget(closeButton);

		}

		private String isOfficialString(Park p) {
			if (p.isOfficial())
				return "Yes";
			else
				return "No";
		}

		private String getCoordinateString(Park p) {
			double latitude = p.getLatitude();
			double longitude = p.getLongitude();
			return String.valueOf(latitude) + ", " + String.valueOf(longitude);
		}
	}

	private static class DirectionsDialog extends DecoratedPopupPanel {
		public DirectionsDialog(final String location, Park park) {

			setAnimationEnabled(true);
			final VerticalPanel dialogVPanel = new VerticalPanel();

			if (park != null) {
				boolean sensor = true;

				final Double latitude = park.getLatitude();
				final Double longitude = park.getLongitude();

				// load all the libs for use in the maps
				ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
				loadLibraries.add(LoadLibrary.ADSENSE);
				loadLibraries.add(LoadLibrary.DRAWING);
				loadLibraries.add(LoadLibrary.GEOMETRY);
				loadLibraries.add(LoadLibrary.PANORAMIO);
				loadLibraries.add(LoadLibrary.PLACES);
				loadLibraries.add(LoadLibrary.WEATHER);
				loadLibraries.add(LoadLibrary.VISUALIZATION);

				Runnable onLoad = new Runnable() {
					@Override
					public void run() {
						DirectionsServiceMapWidget wMap = new DirectionsServiceMapWidget(location, latitude, longitude);
						dialogVPanel.add(wMap);
						wMap.mapWidget.triggerResize();
					}
				};

				LoadApi.go(onLoad, loadLibraries, sensor);
				// ignore this; map added to body with addMapWidget() instead
				// RootPanel.get("body").add(table);
			}

			else
				System.out.println("Park is null");

			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
			final Button closeButton = new Button("Close");
			closeButton.getElement().setId("closeButton");
			dialogVPanel.add(closeButton);

			closeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					DirectionsDialog.this.hide();
				}
			});
			setGlassEnabled(true);
			setWidget(dialogVPanel);
		}
	}

	private static class SearchDialog extends DecoratedPopupPanel {

		FlexTable table = new FlexTable();

		public SearchDialog(String searchTerm, String attribute, List<Park> parks) {
			setAnimationEnabled(true);
			final VerticalPanel dialogVPanel = new VerticalPanel();

			table.setBorderWidth(12);
			table.setText(0, 0, "");
			table.setText(0, 1, "Park ID");
			table.setText(0, 2, "Park Name");
			table.setText(0, 3, "Official");
			table.setText(0, 4, "Street Number");
			table.setText(0, 5, "Street Name");
			table.setText(0, 6, "East-West Street Name");
			table.setText(0, 7, "North-South Street Name");
			table.setText(0, 8, "Coordinates");
			table.setText(0, 9, "Size in Hectares");
			table.setText(0, 10, "Neighbourhood Name");
			table.setText(0, 11, "Neighbourhood URL");
			table.getRowFormatter().setStyleName(0, "tableheader");

			int index = 1;
			if (!parks.isEmpty()) {
				for (Park p : parks) {
					if (attribute.equals("Park Name")) {
						if (p.getPname().toLowerCase().contains(searchTerm.toLowerCase())) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Park ID")) {
						if (searchTerm.equals(String.valueOf(p.getPid()))) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Official")) {
						if (searchTerm.toLowerCase().equals("yes")) {
							if (p.isOfficial()) {
								table.insertRow(index);
								addParkData(index, p);
								index++;
							}
						}
						if (searchTerm.toLowerCase().equals("no")) {
							if (!p.isOfficial()) {
								table.insertRow(index);
								addParkData(index, p);
								index++;
							}
						}
					}
					if (attribute.equals("Street Number")) {
						if ((String.valueOf(p.getStreetNumber()).contains(searchTerm))) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Street Name")) {
						if (p.getStreetName().toLowerCase().contains(searchTerm.toLowerCase())) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("East-West Street Name")) {
						if (p.getEwStreet().toLowerCase().contains(searchTerm.toLowerCase())) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("North-South Street Name")) {
						if (p.getNsStreet().toLowerCase().contains(searchTerm.toLowerCase())) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Latitude")) {
						if (searchTerm.equals(String.valueOf(p.getLatitude()))
								|| searchTerm.equals(String.valueOf(p.getLatitude().intValue()))) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Longitude")) {
						if (searchTerm.equals(String.valueOf(p.getLongitude()))
								|| searchTerm.equals(String.valueOf(p.getLongitude().intValue()))) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Size in Hectares")) {
						if (searchTerm.equals(String.valueOf(p.getHectare()))) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Neighbourhood Name")) {
						if (p.getNeighbourhoodName().toLowerCase().contains(searchTerm.toLowerCase())) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}
					if (attribute.equals("Neighbourhood URL")) {
						if (searchTerm.toLowerCase().equals(p.getNeighbourhoodURL().toLowerCase())) {
							table.insertRow(index);
							addParkData(index, p);
							index++;
						}
					}

				}

			}

			if (table.getRowCount() > 1) {
				// dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
				dialogVPanel.add(table);
			} else {
				Label label = new Label();
				label.setText("No Parks matched query");
				dialogVPanel.add(label);
			}
			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
			final Button closeButton = new Button("Close");
			closeButton.getElement().setId("closeButton");
			dialogVPanel.add(closeButton);

			closeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					SearchDialog.this.hide();
				}
			});
			setGlassEnabled(true);
			setWidget(dialogVPanel);

		}

		private String isOfficialString(Park p) {
			if (p.isOfficial())
				return "Yes";
			else
				return "No";
		}

		private String getCoordinateString(Park p) {
			double latitude = p.getLatitude();
			double longitude = p.getLongitude();
			return String.valueOf(latitude) + ", " + String.valueOf(longitude);
		}

		private void addParkData(int index, Park p) {
			table.setText(index, 1, String.valueOf(p.getPid()));
			table.setText(index, 2, p.getPname());
			table.setText(index, 3, isOfficialString(p));
			table.setText(index, 4, String.valueOf(p.getStreetNumber()));
			table.setText(index, 5, p.getStreetName());
			table.setText(index, 6, p.getEwStreet());
			table.setText(index, 7, p.getNsStreet());
			table.setText(index, 8, getCoordinateString(p));
			table.setText(index, 9, String.valueOf(p.getHectare()));
			table.setText(index, 10, p.getNeighbourhoodName());
			table.setText(index, 11, p.getNeighbourhoodURL());
		}
	}

}
