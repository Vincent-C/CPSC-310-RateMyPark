package com.ratemypark.client;

import java.util.ArrayList;
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
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

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

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		loadParksIntoDatastore(false); // DB related stuff (moved the boolean hack into this function)
		// Load the header
		loadHeader();
		// Load the body
		// loadParksBody();
		
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
				loginDialog.show();
			}
		});

		logoutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				logoutSvc.logout(new AsyncCallback<Void>() {
					public void onFailure(Throwable caught) {
						System.out.println("LOGOUT FAILED");
					}

					public void onSuccess(Void ignore) {
						Cookies.removeCookie("sid");
						Window.alert("Logged out");
						// System.out.println("Client side cookie logout: " + Cookies.getCookie("sid"));
						// Clear username text
						RootPanel.get("username").getElement().setInnerText("");
						toggleLoginButtons();
						loadParksBody();
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
				newAccountDialog.center();
				newAccountDialog.show();
			}
		});
	}

	// This method is used to clear the body only once, then add the 'body' after, using the methods
	private void loadParksBody() {
		RootPanel.get("body").clear();
		loadParksTable();
		loadParksTextandButton();
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

	private void loadParksTable() {
		final FlexTable table = new FlexTable();
		final Button compareButton = new Button("Compare");
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
				table.setText(0, 5, "Neighbourhood URL");
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
					table.setText(index, 5, p.getNeighbourhoodURL());
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

	private void loadSpecificParkTable(final String parkID) {
		final FlexTable table = new FlexTable();
		loadParksSvc.getParks(new AsyncCallback<List<Park>>() {
			public void onFailure(Throwable caught) {
				System.out.println("Park did not get properly");
			}

			public void onSuccess(List<Park> parkList) {
				if (parkID != "" && parkID != null) {
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
					table.insertRow(1);
					Park p = null;
					for(Park park : parkList) {
						if (park.getPid() == Long.parseLong(parkID))
							p = park;

					}
					if (p != null) {
						//System.out.println(p.getPid());
						//System.out.println(p.getPname());
						table.setText(1, 1, String.valueOf(p.getPid()));
						table.setText(1, 2, p.getPname());
						table.setText(1, 3, isOfficialString(p));
						table.setText(1, 4, String.valueOf(p.getStreetNumber()));
						table.setText(1, 5, p.getStreetName());
						table.setText(1, 6, p.getEwStreet());
						table.setText(1, 7, p.getNsStreet());
						table.setText(1, 8, getCoordinateString(p));
						table.setText(1, 9, String.valueOf(p.getHectare()));
						table.setText(1, 10, p.getNeighbourhoodName());
						table.setText(1, 11, p.getNeighbourhoodURL());

						RootPanel.get("body").add(table);
					}
					else System.out.println("Park is null");
				}
				else System.out.println("ParkID is empty");
			}
		});
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
						+ profile.getUsername() + "</div>");
				RootPanel.get("body").add(header);

				VerticalPanel vPanel = new VerticalPanel();

				vPanel.add(new HTML("<b>First Name:</b>"));
				final TextBox firstName = new TextBox();
				firstName.setText(profile.getFirstName());
				vPanel.add(firstName);

				vPanel.add(new HTML("<b>Last Name:</b>"));
				final TextBox lastName = new TextBox();
				lastName.setText(profile.getLastName());
				vPanel.add(lastName);

				final Button editProfile = new Button("Edit Profile");
				editProfile.addClickHandler(new ClickHandler() {

					public void onClick(ClickEvent event) {
						String first = firstName.getText();
						String last = lastName.getText();
						LoginInfo newProfile = new LoginInfo(currentUsername, Cookies.getCookie("sid"));
						newProfile.setFirstName(first);
						newProfile.setLastName(last);

						profileSvc.editProfile(newProfile, new AsyncCallback<LoginInfo>() {
							public void onFailure(Throwable caught) {
								System.out.println("Error occured: " + caught.getMessage());
								handleError(caught);
							}

							public void onSuccess(LoginInfo result) {
								Window.alert("Profile updated!");
								loadProfilePage();
							}
						});
					}
				});
				vPanel.add(editProfile);

				final Button back = new Button("Back to Main Page");
				back.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						loadParksBody();
					}
				});
				vPanel.add(back);
				RootPanel.get("body").add(vPanel);
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
	
	public void onValueChange(ValueChangeEvent<String> event) {
	    // This method is called whenever the application's history changes. Set
	    // the label to reflect the current history token.
		if (!event.getValue().isEmpty()) {
			RootPanel.get("body").clear();
			if (event.getValue().equals("profile")) {
				//RootPanel.get("body").clear();
				loadProfilePage();
			}
			else  {
				//RootPanel.get("body").clear();
				loadSpecificParkTable(event.getValue());
			}
		}
		else loadParksBody();
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

		public CompareDialog(List<Park> parks) {
			setAnimationEnabled(true);
			VerticalPanel dialogVPanel = new VerticalPanel();

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

}
