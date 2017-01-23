
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;

class ChatroomAppli extends Composite {
  private class RegisterTimer extends Timer {
    public void run() {
      AsyncCallback callback = new AsyncCallback() {
         public void onSuccess (Object callResult)
         {	
      	
         }
         public void onFailure (Throwable ex)
         {
      
         }
      };
      
      if (reRegisterCounter == 0)
      	reRegisterTimer.scheduleRepeating(Constants.REGISTER_TIMEOUT);
      
      reRegisterCounter++;
      ProxyRPC.registerUser(sessionId,callback);
    }

  }

  private class AppliTimer extends Timer {
    public void run() {
      appliUpdater.retrieveEvents();
    }

  }

  private class AppliUpdater {
    private int APPLI_UPDATE_DELAY = 1000;

    private ChatroomAppli.AppliTimer appliTimer;

    public AppliUpdater() {
      appliTimer = new AppliTimer();
      appliTimer.scheduleRepeating(APPLI_UPDATE_DELAY);
    }

    public void retrieveEvents() {
      AsyncCallback callback = new AsyncCallback() {
         public void onSuccess (Object callResult)
         {	
      	AppliEvents events = (AppliEvents) callResult;
      	getMessages(events.messages);
      	updateUsers(events.buddies);
      	uac.parseSipEvents(events.sipEvents);
         }
         public void onFailure (Throwable ex)
         {
      
      	//mainModule.resetAppli();
         }
      };
      
      ProxyRPC.retrieveEvents(sessionId,callback);
    }

    public void getMessages(ArrayList<String[]> messages) {
      for (int i=0,N=messages.size();i<N;i++) {
      	if (isChatroomExist(messages.get(i)[0]) > -1) {
      		((Chatroom)roomsInstanceList.get(messages.get(i)[0])).updateMessages(messages.get(i)[1]);
      	} else {
      		addPrivateChatroom(messages.get(i)[0],false);
      		((Chatroom)roomsInstanceList.get(messages.get(i)[0])).updateMessages(messages.get(i)[1]);
      	}
      }
      
    }

    public void updateUsers(ArrayList<BuddyData> buddies) {
      parseBuddiesList(buddies);
         
    }

    private void parseBuddiesList(ArrayList<BuddyData> buddies) {
      HashMap<String,ArrayList<BuddyData>> map = new HashMap<String,ArrayList<BuddyData>>();
      boolean ownerPresentityFound = false;
      
      for (int i=0,N=buddies.size();i<N;i++) {
      	String chatroom = buddies.get(i).chatroom;
      	
      	if ((buddies.get(i).name.indexOf(mainModule.publicId) > -1) && (!ownerPresentityFound)) {
      		toolsBar.presenceSelector.setPresence(buddies.get(i).status);
      		ownerPresentityFound = true;
      	}
      
      	if (map.containsKey((String)chatroom)) {
      		((ArrayList<BuddyData>)map.get(chatroom)).add(buddies.get(i));
      	} else {
      		ArrayList<BuddyData> tempList = new ArrayList<BuddyData>();
      		tempList.add(buddies.get(i));
      		map.put(chatroom,tempList);
      	}
      }
      
      for (int i=0,N=roomsList.size();i<N;i++) {
      	String key = (String) roomsList.get(i);
      	if (map.containsKey(key)) {
      		((Chatroom)roomsInstanceList.get(key)).updateUsers((ArrayList<BuddyData>)map.get(key));
      	}
      }
    }

    public void stop() {
      appliTimer.cancel();
    }

  }

  private VerticalPanel mainPanel;

  private Main mainModule;

  private ArrayList<String> roomsList;

  private HashMap<String,Chatroom> roomsInstanceList;

  private String sessionId;

  private TabPanel tabPanel;

  private ToolsBar toolsBar;

  private ChatroomAppli.AppliUpdater appliUpdater;

  private ChatroomAppli.RegisterTimer reRegisterTimer;

  private int reRegisterCounter;

  public SipUAC uac;

  public ChatroomAppli(ArrayList<String> roomsList, String sessionId, Main mainModule) {
    this.roomsList = roomsList;
    this.sessionId = sessionId;
    this.mainModule = mainModule;
    
    uac = new SipUAC(sessionId);
    
    mainPanel = new VerticalPanel();
    toolsBar = new ToolsBar(sessionId,mainModule);
    
    roomsInstanceList = new HashMap<String,Chatroom>();
    
    tabPanel = new TabPanel();
    
    for (int i=0,N=this.roomsList.size();i<N;i++) {
    	Chatroom room = new Chatroom((String)this.roomsList.get(i),sessionId,mainModule);
    	tabPanel.add(room.appliPanel,room.title);
    	roomsInstanceList.put(this.roomsList.get(i),room);
    }
    
    tabPanel.addTabListener(new TabListener () {
    	public void onTabSelected (SourcesTabEvents sender, int tabIndex) {
    		resetTabsTitle();
    		((Chatroom.AppliPanel)(((TabPanel)sender).getWidget(tabIndex))).isSelected = true;
    		((Chatroom.AppliPanel)(((TabPanel)sender).getWidget(tabIndex))).updateTitle();
    		((Chatroom.AppliPanel)(((TabPanel)sender).getWidget(tabIndex))).focusOnTextInput();
    	}
    
    	public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
    		return true;
    	}
    });
    
    tabPanel.selectTab(0);
    
    appliUpdater = new AppliUpdater();
    
    reRegisterTimer = new RegisterTimer();
    reRegisterTimer.schedule(Constants.REGISTER_TIMEOUT);
    reRegisterCounter = 0;
    
    mainPanel.add(toolsBar);
    mainPanel.add(tabPanel);
    initWidget(mainPanel);
  }

  public void stop() {
    appliUpdater.stop();
    uac.stop();
    
    reRegisterTimer.cancel();
    
    AsyncCallback callback = new AsyncCallback() {
       public void onSuccess (Object callResult)
       {	
    	
       }
       public void onFailure (Throwable ex)
       {
    
       }
    };
    
    ProxyRPC.unregisterUser(sessionId,callback);
  }

  private void resetTabsTitle() {
    for (int i=0,N=tabPanel.getWidgetCount();i<N;i++) {
    	((Chatroom.AppliPanel)tabPanel.getWidget(i)).isSelected = false;
    	((Chatroom.AppliPanel)tabPanel.getWidget(i)).updateTitle();
    }
  }

  public void addPrivateChatroom(String roomToAddId, boolean display) {
    int index = isChatroomExist(roomToAddId);
    
    if (index == -1) {
    	Chatroom room = new Chatroom(roomToAddId,sessionId,mainModule,1);
    	tabPanel.add(room.appliPanel,room.title);
    	roomsInstanceList.put(roomToAddId,room);
    	roomsList.add(roomToAddId);
    
    	if (display) {
    		int newIndex = isChatroomExist(roomToAddId);
    		tabPanel.selectTab(newIndex);
    	}
    }
  }

  public void closeChatroom(String roomId) {
    tabPanel.remove(roomsInstanceList.get(roomId).appliPanel);
    roomsInstanceList.remove(roomId);
    
    int i = 0;
    while (!roomsList.get(i).equals(roomId)) {
    	i++;
    }
    
    roomsList.remove(i);
    tabPanel.selectTab(0);
    ((Chatroom.AppliPanel)tabPanel.getWidget(0)).focusOnTextInput();
  }

  private int isChatroomExist(String user) {
    for (int i=0,N=roomsList.size();i<N;i++) {
    	if (user.compareTo(roomsList.get(i)) == 0) {
    		return i;
    	}
    }
    
    return -1;
  }

}
