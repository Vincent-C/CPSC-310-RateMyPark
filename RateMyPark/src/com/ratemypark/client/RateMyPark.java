package com.ratemypark.client;

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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RateMyPark implements EntryPoint {
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
		loadHeader();
		loadParksMethod(); // DB related stuff
		// loadParksContent();
	}

	private void loadHeader() {
		final Button loginButton = new Button("Login");
		final Button logoutButton = new Button("Logout");
		final Button newAccountButton = new Button("New Account");

		// We can add style names to widgets
		loginButton.addStyleName("loginButton");
		logoutButton.addStyleName("logoutButton");
		logoutButton.setVisible(false); // Hide logout button on initial load
		newAccountButton.addStyleName("newAccountButton");

		// Add the loginButton and newAccountButton to the RootPanel
		RootPanel.get("loginButtonContainer").add(loginButton);
		RootPanel.get("logoutButtonContainer").add(logoutButton);
		RootPanel.get("newAccountButtonContainer").add(newAccountButton);

		String sessionID = Cookies.getCookie("sid");
		if (sessionID != null) {
			loginSvc.doLogin(sessionID, new AsyncCallback<LoginInfo>() {
				public void onFailure(Throwable caught) {
					System.out.println("Not logged in");
				}

				// result is the session ID from doLogin
				public void onSuccess(LoginInfo result) {
					final long DURATION = 1000 * 60 * 60 * 24 * 14; // duration remembering login. 2 weeks in this
																	// example.
					Date expires = new Date(System.currentTimeMillis() + DURATION);
					Cookies.setCookie("sid", result.getSessionId(), expires, null, "/", false);

					Window.alert("Logged in as" + result.getUsername());
					System.out.println("Client side cookie login: " + Cookies.getCookie("sid"));
					toggleLoginButtons();
				}

				private void toggleLoginButtons() {
					loginButton.setVisible(false);
					newAccountButton.setVisible(false);
					logoutButton.setVisible(true);
				}
			});
		}

		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LoginDialog loginDialog = new LoginDialog(loginButton, logoutButton, newAccountButton);
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
						System.out.println("Client side cookie logout: " + Cookies.getCookie("sid"));

						toggleLoginButtons();
					}

					private void toggleLoginButtons() {
						loginButton.setVisible(true);
						newAccountButton.setVisible(true);
						logoutButton.setVisible(false);
					}
				});

			}
		});

		newAccountButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				NewAccountDialog newAccontDialog = new NewAccountDialog(loginButton, logoutButton, newAccountButton);
				newAccontDialog.show();
			}
		});
	}

	private void loadParksMethod() {
		final Button loadParksButton = new Button("Load Parks");
		final TextBox loadParksTextBox = new TextBox();

		// We can add style names to widgets
		loadParksButton.addStyleName("loadParkButton");

		RootPanel.get("loadParksButtonContainer").add(loadParksButton);
		RootPanel.get("loadParksTextBoxContainer").add(loadParksTextBox);
		
		final FlexTable table = new FlexTable();

		// Boolean HACK: true if you want to (re)load the database from the XML, else keep at false
		Boolean loadDB = false;
		if (loadDB) {
			loadParksSvc.loadParks(new AsyncCallback<List<Park>>() {
				public void onFailure(Throwable caught) {
					System.out.println("Parks did not load properly");
				}

				public void onSuccess(List<Park> parks) {
					Window.alert("Parks loaded.");
					System.out.println("Parks loaded.");
				}
			});
		}
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
						Window.alert(park.getPname() + " loaded, with coordinates (" + park.getLatitude()+ "," + park.getLongitude() + ").");
						
						System.out.println(park.getPname() + " loaded.");
					}
				});
			}
		});
		
		// Still working on this, does not seem to be working properly yet.
		loadParksSvc.getParks(new AsyncCallback<List<Park>>() {
			public void onFailure(Throwable caught) {
				System.out.println("Parks did not get properly");
			}
			public void onSuccess(List<Park> parkList) {
				
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
				for (Park p: parkList) {
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
//					System.out.println("Adding index" + index);
				}
				RootPanel.get("body").add(table);
				
			}
		});
		// Code to test getParks
		// loadParksSvc.getParks(new AsyncCallback<List<Park>>() {
		// public void onFailure(Throwable caught) {
		// System.out.println("Error occured: " + caught.getMessage());
		// handleError(caught);
		// }
		//
		// public void onSuccess(List<Park> parks) {
		// Window.alert(parks.get(3).getPname() + " loaded.");
		// System.out.println(parks.get(5).getPname() + " loaded.");
		// }
		// });
		// Code to test getParkNames
		// loadParksSvc.getParkNames(new AsyncCallback<String[]>() {
		// public void onFailure(Throwable caught) {
		// System.out.println("Error occured: " + caught.getMessage());
		// handleError(caught);
		// }
		//
		// public void onSuccess(String[] parks) {
		// Window.alert("Park " + parks[1] + " loaded.");
		// System.out.println("Park " + parks[1] + " loaded.");
		// }
		// });
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
	
	private void loadParksContent() {
		// TODO show park data here
	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
		if (error instanceof DatabaseException) {
		}
	}

	private static class LoginDialog extends DialogBox {
		private Button loginButton;
		private Button logoutButton;
		private Button newAccountButton;

		public LoginDialog(Button loginMain, Button logoutMain, Button newAccountMain) {
			this.loginButton = loginMain; // The login button to show this dialog, NOT the login button in this dialog
			this.newAccountButton = newAccountMain;
			this.logoutButton = logoutMain;

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

					loginSvc.doLogin(username, password, new AsyncCallback<LoginInfo>() {
						public void onFailure(Throwable caught) {
							handleError(caught);
							System.out.println("LOGIN FAIL username " + caught.getMessage());
						}

						// result is the session ID from doLogin
						public void onSuccess(LoginInfo result) {
							// duration remembering login. 2 weeks in this example.
							final long DURATION = 1000 * 60 * 60 * 24 * 14;
							Date expires = new Date(System.currentTimeMillis() + DURATION);
							Cookies.setCookie("sid", result.getSessionId(), expires, null, "/", false);

							Window.alert("Logged in as " + result.getUsername());
							System.out.println("Client side cookie login: " + Cookies.getCookie("sid"));

							LoginDialog.this.hide();
							toggleLoginButtons();
						}

						private void toggleLoginButtons() {
							LoginDialog.this.loginButton.setVisible(false);
							LoginDialog.this.newAccountButton.setVisible(false);
							LoginDialog.this.logoutButton.setVisible(true);
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
					LoginDialog.this.hide();
				}
			});

			setWidget(dialogVPanel);
		}

		private void handleError(Throwable error) {
			Window.alert(error.getMessage());
			if (error instanceof UserNameException) {

			} else if (error instanceof BadPasswordException) {

			}
		}
	}

	private static class NewAccountDialog extends DialogBox {
		private Button loginButton;
		private Button logoutButton;
		private Button newAccountButton;

		public NewAccountDialog(Button loginMain, Button logoutMain, Button newAccountMain) {
			this.loginButton = loginMain;
			this.logoutButton = logoutMain;
			this.newAccountButton = newAccountMain;

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

					newAccountSvc.createNewAccount(username, password, new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							System.out.println("Error occured: " + caught.getMessage());
							handleError(caught);
						}

						public void onSuccess(String result) {
							final long DURATION = 1000 * 60 * 60 * 24 * 14; // duration remembering login. 2 weeks in
																			// this example.
							Date expires = new Date(System.currentTimeMillis() + DURATION);
							Cookies.setCookie("sid", result, expires, null, "/", false);

							System.out.println("Client side cookie new account: " + Cookies.getCookie("sid"));
							Window.alert("NEW ACCOUNT CREATED");

							NewAccountDialog.this.hide();
							toggleLoginButtons();
						}

						private void toggleLoginButtons() {
							NewAccountDialog.this.loginButton.setVisible(false);
							NewAccountDialog.this.newAccountButton.setVisible(false);
							NewAccountDialog.this.logoutButton.setVisible(true);
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

		private void handleError(Throwable error) {
			Window.alert(error.getMessage());
			if (error instanceof UserNameException) {
			}
		}
	}

}
