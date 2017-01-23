
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import java.util.ArrayList;
import java.util.HashMap;

class IncomingCall extends PopupPanel {
  private SipUAC sipUac;

  public SipCall call;

  private ToolBarButton acceptButton;

  private ToolBarButton rejectButton;

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
    
    Command acceptCommand = new Command() {
    	public void execute() {
    		sipUac.acceptCall(call);
    		hide();
    	}
    };
    
    Command rejectCommand = new Command() {
    	public void execute() {
    		cancel();
    	}
    };
    
    acceptButton = new ToolBarButton(new Image("./images/telephone_go.png"),"Accepter l'appel",acceptCommand);
    acceptButton.setEnabled(true);
    acceptButton.setButtonWidth(50);
    acceptButton.setButtonWidth(30);
    
    rejectButton = new ToolBarButton(new Image("./images/cancel.png"),"Rejeter l'appel",rejectCommand);
    rejectButton.setEnabled(true);
    rejectButton.setButtonWidth(50);
    rejectButton.setButtonWidth(30);
    
    buttonsPanel.add(acceptButton);
    buttonsPanel.add(rejectButton);
    
    mainPanel.add(new Label("Appel de " + sipCall.fromUser));
    mainPanel.add(buttonsPanel);
    
    setWidget(mainPanel);
    setStyleName("CallPopup");
    
  }

  public void display() {
    setPopupPosition(250,250);
    show();
  }

  public void cancel() {
    sipUac.rejectCall(call);
    hide();
  }

  public void timeout() {
    hide();
  }

}
