package com.ratemypark.client;

import com.ratemypark.shared.BCrypt;
import com.ratemypark.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RateMyPark implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	//private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

	private static class LoginDialog extends DialogBox {

		public LoginDialog() {
			// Set the dialog box's caption
			setText("Login");
			setAnimationEnabled(true);
			VerticalPanel dialogVPanel = new VerticalPanel();
			
			// Username field setup[
			dialogVPanel.addStyleName("dialogVPanel");
			dialogVPanel.add(new HTML("<b>Username:</b>"));
			final TextBox usernameField = new TextBox();
			dialogVPanel.add(usernameField);

			// Password field setup
			dialogVPanel.add(new HTML("<b>Password:</b>"));
			final TextBox passwordField = new TextBox();
			dialogVPanel.add(passwordField);
			
			// Login button setup
			final Button login = new Button("Login");
			login.getElement().setId("loginButton");
			dialogVPanel.add(login);
			login.addClickHandler(new ClickHandler() {
				private final LoginServiceAsync loginSvc = GWT.create(LoginService.class);
				
				public void onClick(ClickEvent event) {
					String username = usernameField.getText();
					String password = passwordField.getText();
					
					// TODO
					System.out.println("Username: " + username + " Password: " + password);
					
					loginSvc.verifyLogin(username, password,
					new AsyncCallback<Boolean>() {
						public void onFailure(Throwable caught) {
							System.out.println("FAIL");
						}

						public void onSuccess(Boolean result) {
							System.out.println("SUCCESS");
							Window.alert("Logged in");
							LoginDialog.this.hide();
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
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button loginButton = new Button("Login");
		final Button newAccountButton = new Button("New Account");

		// We can add style names to widgets
		loginButton.addStyleName("loginButton");
		newAccountButton.addStyleName("newAccountButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		// RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("loginButtonContainer").add(loginButton);
		RootPanel.get("newAccountButtonContainer").add(newAccountButton);
	
		loginButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				LoginDialog loginDialog = new LoginDialog();
				loginDialog.show();
			}
		});

//		// Create a handler for the sendButton and nameField
//		class MyHandler implements ClickHandler, KeyUpHandler {
//			/**
//			 * Fired when the user clicks on the sendButton.
//			 */
//			public void onClick(ClickEvent event) {
//				sendNameToServer();
//			}
//
//			/**
//			 * Fired when the user types in the nameField.
//			 */
//			public void onKeyUp(KeyUpEvent event) {
//				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
//					sendNameToServer();
//				}
//			}
//
//			/**
//			 * Send the name from the nameField to the server and wait for a
//			 * response.
//			 */
//			private void sendNameToServer() {
//				// First, we validate the input.
//				errorLabel.setText("");
//				String textToServer = nameField.getText();
//				if (!FieldVerifier.isValidName(textToServer)) {
//					errorLabel.setText("Please enter at least four characters");
//					return;
//				}
//
//				// Then, we send the input to the server.
//				loginButton.setEnabled(false);
//				textToServerLabel.setText(textToServer);
//				serverResponseLabel.setText("");
//				greetingService.greetServer(textToServer,
//						new AsyncCallback<String>() {
//							public void onFailure(Throwable caught) {
//								// Show the RPC error message to the user
//								dialogBox
//										.setText("Remote Procedure Call - Failure");
//								serverResponseLabel
//										.addStyleName("serverResponseLabelError");
//								serverResponseLabel.setHTML(SERVER_ERROR);
//								dialogBox.center();
//								closeButton.setFocus(true);
//							}
//
//							public void onSuccess(String result) {
//								dialogBox.setText("Remote Procedure Call");
//								serverResponseLabel
//										.removeStyleName("serverResponseLabelError");
//								serverResponseLabel.setHTML(result);
//								dialogBox.center();
//								closeButton.setFocus(true);
//							}
//						});
//			}
//		}

	}
}