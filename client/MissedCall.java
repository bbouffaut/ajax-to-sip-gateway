
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import java.util.ArrayList;
import java.util.HashMap;

class MissedCall extends PopupPanel {
  private SipUAC sipUac;

  public SipCall call;

  private ToolBarButton acceptButton;

  public void init(SipUAC uac, SipCall sipCall, boolean reject) {
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
    		if (call != null) 
    			sipUac.removeMissedCall(call);
    		hide();
    	}
    };
    
    acceptButton = new ToolBarButton(new Image("./images/tick.png"),"Fermer",acceptCommand);
    acceptButton.setEnabled(true);
    acceptButton.setButtonWidth(50);
    acceptButton.setButtonWidth(30);
    
    buttonsPanel.add(acceptButton);
    
    if (!reject)
    	mainPanel.add(new HTML("1 appel manqu&#233; de:  " + sipCall.fromUser));
    else
    	mainPanel.add(new HTML("Appel rejet&#233"));
    mainPanel.add(buttonsPanel);
    
    
    setWidget(mainPanel);
    setStyleName("CallPopup");
    
  }

  public void display() {
    setPopupPosition(250,250);
    show();
  }

}
