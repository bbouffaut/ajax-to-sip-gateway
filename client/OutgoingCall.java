
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import java.util.ArrayList;
import java.util.HashMap;

class OutgoingCall extends PopupPanel {
  private SipUAC sipUac;

  public SipCall call;

  private ToolBarButton cancelButton;

  public void init(SipUAC uac, SipCall sipCall) {
    this.sipUac = uac;
    this.call = sipCall;
    
    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    mainPanel.setSize("200px","50px");
    HorizontalPanel buttonsPanel = new HorizontalPanel();
    buttonsPanel.setSize("200px","50px");
    buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    Command cancelCommand = new Command() {
    	public void execute() {
    		cancel();
    	}
    };
    
    cancelButton = new ToolBarButton(new Image("./images/cancel.png"),"Rejeter l'appel",cancelCommand);
    cancelButton.setEnabled(true);
    cancelButton.setButtonWidth(50);
    cancelButton.setButtonWidth(30);
    
    buttonsPanel.add(cancelButton);
    
    
    mainPanel.add(new Label("Tuuuuuut...Tuuuuuuuut..."));
    mainPanel.add(buttonsPanel);
    
    setWidget(mainPanel);
    setStyleName("CallPopup");
    
  }

  public void display() {
    setPopupPosition(250,250);
    show();
  }

  public void cancel() {
    sipUac.cancelOutgoingCall(true);
    
  }

  public void timeout() {
    hide();
  }

}
