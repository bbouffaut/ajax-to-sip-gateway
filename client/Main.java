
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import java.util.ArrayList;

class Main implements EntryPoint {
  public VerticalPanel mainPanel;

  public String publicId;

  private String sessionId;

  private ChatroomAppli chatroomAppli;

  private LoginAppli loginAppli;

  public void onModuleLoad() {
    DOM.setInnerHTML(RootPanel.get("loading").getElement(), "");
    DOM.setStyleAttribute(RootPanel.get("loading").getElement(), "border", "0px;");
    
    startAppli();
    
    
    
    
    
    
    
  }

  public void launchChatRooms(String sessionId, ArrayList<String> rooms) {
    this.sessionId = sessionId;
    
    chatroomAppli = new ChatroomAppli(rooms,sessionId,this);
    mainPanel.remove(loginAppli);
    mainPanel.add(chatroomAppli);
  }

  public void resetAppli() {
    chatroomAppli.stop();
    RootPanel.get().remove(mainPanel);
    
    startAppli();
  }

  private void startAppli() {
    mainPanel = new VerticalPanel();
    mainPanel.setStyleName("main-panel");
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    
    /*
    ArrayList<String> rooms = new ArrayList<String>();
    rooms.add("chatroom.test");
    rooms.add("chatroom.perso");
    
    chatroomAppli = new ChatroomAppli(rooms);
    mainPanel.add(chatroomAppli);
    */
    
    loginAppli = new LoginAppli(this);
    mainPanel.add(loginAppli);
    
    RootPanel.get().add(mainPanel);
  }

  public void createPrivateChatroom(String user, boolean display) {
    chatroomAppli.addPrivateChatroom(user,display);
  }

  public void closeChatroom(String roomId) {
    chatroomAppli.closeChatroom(roomId);
  }

  public void makeCall(String user) {
    chatroomAppli.uac.makeCall(user);
  }

}
