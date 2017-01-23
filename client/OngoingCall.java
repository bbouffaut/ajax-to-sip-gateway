
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import java.util.ArrayList;
import java.util.HashMap;

class OngoingCall extends PopupPanel {
  private VerticalPanel mainPanel;

  private SipUAC sipUac;

  public SipCall call;

  private ToolBarButton stopButton;

  private AppletWidget chatroomApplet;

  public void init(SipUAC uac, SipCall sipCall) {
    this.sipUac = uac;
    this.call = sipCall;
    
    mainPanel = new VerticalPanel();
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    //mainPanel.setSize("200px","50px");
    HorizontalPanel buttonsPanel = new HorizontalPanel();
    buttonsPanel.setSize("200px","50px");
    buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    buttonsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    
    Command stopCommand = new Command() {
    	public void execute() {
    		sipUac.stopOngoingCall();
    	}
    };
    
    stopButton = new ToolBarButton(new Image("./images/cancel.png"),"Raccrocher",stopCommand);
    stopButton.setEnabled(true);
    stopButton.setButtonWidth(50);
    stopButton.setButtonWidth(30);
    
    buttonsPanel.add(stopButton);
    
    chatroomApplet = new AppletWidget(call.ipAdress,call.audioPort,call.codec,Constants.AUDIO_PORT);
    
    /*
    chatroomApplet = (ChatroomApplet) GWT.create(ChatroomApplet.class);
    Widget widgetApplet = AppletJSUtil.createAppletWidget(chatroomApplet);
    AppletJSUtil.registerAppletCallback(chatroomApplet, new ChatroomAppletCallback(this));
    */
    
    mainPanel.add(new Label("Appel de " + sipCall.fromUser + " en cours"));
    //mainPanel.add(new Label(call.ipAdress + " / " + call.audioPort + " / " + call.codec));
    mainPanel.add(chatroomApplet);
    mainPanel.add(buttonsPanel);
    
    
    setWidget(mainPanel);
    setStyleName("CallPopup");
    
  }

  public void display() {
    setPopupPosition(250,250);
    show();
  }

  public void stop() {
    //chatroomApplet.stopMediaSession();
    if (chatroomApplet != null) {
    	mainPanel.remove(chatroomApplet);
    	chatroomApplet = null;
    }
    
    hide();
  }

}
