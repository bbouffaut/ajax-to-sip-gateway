
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

class LoginAppli extends Composite {
  private VerticalPanel mainPanel;

  private String sessionId;

  private static int LOGIN_WIDTH = 200;

  private static int LOGIN_HEIGHT = 150;

  private static int LOGIN_PAGE_HEIGHT = 400;

  private TextBox loginBox;

  private PasswordTextBox passwordBox;

  private Button sendButton;

  private LoadingPopup loadingPopup;

  private Main mainModule;

  private LoadingPopup failurePopup;

  public LoginAppli(Main mainModule) {
    this.mainModule = mainModule;
    
    mainPanel = new VerticalPanel();
    mainPanel.setHeight(LOGIN_PAGE_HEIGHT + "px");
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    VerticalPanel appliPanel = new VerticalPanel();
    appliPanel.setHeight(LOGIN_HEIGHT + "px");
    appliPanel.setWidth(LOGIN_WIDTH + "px");
    appliPanel.setStyleName("login-appli");
    
    VerticalPanel titlePanel = new VerticalPanel();
    int titleWidth = LOGIN_WIDTH - 10;
    titlePanel.setWidth(titleWidth + "px");
    titlePanel.setStyleName("login-appli-title-container");
    
    HTML title = new HTML("Fen&#234;tre de Connection");
    title.setStyleName("login-appli-title");
    titlePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    titlePanel.add(title);
    
    HTML loginTitle = new HTML("Identifiant: ");
    loginBox = new TextBox();
    loginBox.setMaxLength(25);
    loginBox.addKeyboardListener(new KeyboardListenerAdapter() {
          public void onKeyPress(Widget sender, char keyCode, int modifiers) {
            if (keyCode == (char) KEY_ENTER) {
            	((TextBox)sender).cancelKey();
    		passwordBox.setFocus(true);
            }
          }
    });
    
    HorizontalPanel blank = new HorizontalPanel();
    blank.setHeight("15px");
    
    HTML passwordTitle = new HTML("Mot de passe: ");
    passwordBox = new PasswordTextBox();
    passwordBox.setMaxLength(25);
    passwordBox.addKeyboardListener(new KeyboardListenerAdapter() {
          public void onKeyPress(Widget sender, char keyCode, int modifiers) {
            if (keyCode == (char) KEY_ENTER) {
            	((TextBox)sender).cancelKey();
    		sendButton.setFocus(true);
    		registerProcedure();
            }
          }
    });
    
    sendButton = new Button("Connexion...",new ClickListener() {
    	public void onClick(Widget sender) {
    		registerProcedure();
    	}
    });
    sendButton.addKeyboardListener(new KeyboardListener() {
    	public void onKeyPress(Widget sender, char keyCode, int modifiers) {
    		if (keyCode == (char) KEY_ENTER) {
    			((Button)sender).click();
    		}
    	}
    
    	public void onKeyDown(Widget sender, char keyCode, int modifiers) {
    	}
    
    	public void onKeyUp(Widget sender, char keyCode, int modifiers) {
    	}
    });
    
    
    appliPanel.add(titlePanel);
    appliPanel.add(loginTitle);
    appliPanel.add(loginBox);
    appliPanel.add(blank);
    appliPanel.add(passwordTitle);
    appliPanel.add(passwordBox);
    appliPanel.add(blank);
    appliPanel.add(sendButton);
    
    mainPanel.add(appliPanel);
    
    initWidget(mainPanel);
    
  }

  private void registerProcedure() {
    String login = loginBox.getText();
    String password = passwordBox.getText();
    
    PopupContent popupContent = new PopupContent("<img src='images/large-loading.gif' width='32' height='32'  style='margin-right:8px;float:left;vertical-align:top;'/><br/><span>Connexion...</span>");
    loadingPopup = new LoadingPopup(popupContent);
    loadingPopup.show();
    
    processLogin();
    
    
  }

  private void processLogin() {
    AsyncCallback loginCallback = new AsyncCallback() {
    	public void onSuccess(Object result) {
    		sessionId = (String)result;
    		if ((sessionId != null) && (sessionId != "")) {
    			processPassword();
    		} else {
    			ClickListener listener = new ClickListener() {
    				public void onClick(Widget sender) {
    					loginBox.setText("");
    					passwordBox.setText("");
    					failurePopup.hide();
    				}
    			};
    			PopupContent popupContent = new PopupContent("La connexion a &#233;chou&#233;e. V&#233;rifier vos param&#232;tres.\n Details: sessionId est vide",listener);
    			failurePopup = new LoadingPopup(popupContent);
    			loadingPopup.hide();
    			failurePopup.show();
    		}
    	}
    
    	public void onFailure(Throwable caught) {
    		ClickListener listener = new ClickListener() {
    				public void onClick(Widget sender) {
    					loginBox.setText("");
    					passwordBox.setText("");
    					failurePopup.hide();
    				}
    			};
    		PopupContent popupContent = new PopupContent("La connexion a &#233;chou&#233;e. V&#233;rifier vos param&#232;tres.",listener);
    		failurePopup = new LoadingPopup(popupContent);
    		loadingPopup.hide();
    		failurePopup.show();
    	}
    };
    
    ProxyRPC.processLogin(loginBox.getText(),loginCallback);
    
  }

  private void processPassword() {
    AsyncCallback passwordCallback = new AsyncCallback() {
    	public void onSuccess(Object result) {
    		if (result != null) {
    			loadingPopup.hide();
    			mainModule.launchChatRooms(sessionId,(ArrayList<String>)result);
    		} else {
    			ClickListener listener = new ClickListener() {
    				public void onClick(Widget sender) {
    					loginBox.setText("");
    					passwordBox.setText("");
    					failurePopup.hide();
    				}
    			};
    			PopupContent popupContent = new PopupContent("La connexion a &#233;chou&#233;e. V&#233;rifier vos param&#232;tres.",listener);
    			failurePopup = new LoadingPopup(popupContent);
    			loadingPopup.hide();
    			failurePopup.show();
    		}
    	}
    
    	public void onFailure(Throwable caught) {
    		ClickListener listener = new ClickListener() {
    				public void onClick(Widget sender) {
    					loginBox.setText("");
    					passwordBox.setText("");
    					failurePopup.hide();
    				}
    			};
    		PopupContent popupContent = new PopupContent("La connexion a &#233;chou&#233;e. V&#233;rifier vos param&#232;tres.",listener);
    		failurePopup = new LoadingPopup(popupContent);
    		loadingPopup.hide();
    		failurePopup.show();
    	}
    };
    
    ProxyRPC.processPassword(sessionId,passwordBox.getText(),passwordCallback);
    
    
  }

}
