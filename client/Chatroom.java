
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

class Chatroom {
  public class AppliPanel extends Composite {
    public boolean isSelected = false;

    private HorizontalPanel mainPanel;

    public AppliPanel() {
      mainPanel = new HorizontalPanel();
      
      initWidget(mainPanel);
    }

    public void updateTitle() {
      if (!isSelected) {
      	titleWidget.setHTML(roomId + ":<br><b> " + nbMessages + " nouveaux messages</b>");
      	titleWidget.setWidth(TITLE_WIDTH_NOT_SELECTED + "px");
      	titleH.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      
      	if (isPrivateChatroom) {
      		titleH.remove(closedButton);
      		titleH.remove(callButton);
      	}
      } else {
      	titleWidget.setHTML("<b>" + roomId + "</b><br>");
      	titleWidget.setWidth(TITLE_WIDTH + "px");
      	titleH.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      	nbMessages = 0;
      	
      	if (isPrivateChatroom) {
      		int width = TITLE_WIDTH - 60;
      		titleWidget.setWidth(width + "px");
      		titleH.add(callButton);
      		titleH.add(closedButton);
      		titleH.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      	}
      }
    }

    public void addClickListener(ClickListener listener) {
    }

    public void removeClickListener(ClickListener listener) {
    }

    public void focusOnTextInput() {
      messagesPanel.messagesWritePanel.setFocus(true);
    }

  }

  private class MessagesPanel extends Composite {
    private VerticalPanel messagesPanel;

    public VerticalPanel messagesReceivedPanel;

    public ScrollPanel messagesReceivedPanelContainer;

    public TextArea messagesWritePanel;

    private HTML legendWriteMessage;

    private boolean writePanelAlreadyDisplayed = true;

    public MessagesPanel() {
      messagesPanel = new VerticalPanel();
      messagesReceivedPanel = new VerticalPanel();
      VerticalPanel messagesReceivedPanel2 = new VerticalPanel();
      messagesReceivedPanel2.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
      messagesReceivedPanel2.setHeight(MESSAGES_PANEL_HEIGHT);
      messagesReceivedPanel2.add(messagesReceivedPanel);
      messagesReceivedPanelContainer = new ScrollPanel(messagesReceivedPanel2);
      messagesWritePanel = new TextArea();
      messagesWritePanel.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
              if (keyCode == (char) KEY_ENTER) {
              	((TextArea)sender).cancelKey();
      		String message = ((TextArea)sender).getText();
      		if (isPrivateChatroom) {
      			String time = Tools.getTimeHumanReadable();
      			HTML messageHTML = new HTML("<div class='chatroom-message-normal'><b>(" + time + " / Moi): </b>" + parseMessage(message) + "</div>");
      			messagesReceivedPanel.add(messageHTML);
      			messagesReceivedPanelContainer.ensureVisible((UIObject)messageHTML);
      		}
      		sendMessage(message);
      		((TextArea)sender).setText("");
              }
            }
      });
      
      messagesWritePanel.setVisibleLines(MESSAGES_WRITE_PANEL_NB_LINES);
      
      if (!isPrivateChatroom) {
      	messagesReceivedPanelContainer.setWidth(MESSAGES_PANEL_WIDTH + "px");
      } else {
      	int width = MESSAGES_PANEL_WIDTH + STACK_WIDTH;
      	messagesReceivedPanelContainer.setWidth(width + "px");
      }
      messagesReceivedPanelContainer.setHeight(MESSAGES_PANEL_HEIGHT);
      //messagesWritePanel.setMaxLength(MESSAGES_WRITE_PANEL_LENGTH);
      //messagesWritePanel.setVisibleLength(MESSAGES_WRITE_PANEL_LENGTH);
      messagesWritePanel.setCharacterWidth(MESSAGES_WRITE_PANEL_LENGTH);
      messagesWritePanel.setStyleName("messages-panel");
      messagesReceivedPanelContainer.setStyleName("messages-panel");
      
      messagesPanel.add(messagesReceivedPanelContainer);
      legendWriteMessage = new HTML("Appuyer sur la touche \"Entrer\" pour poster le message:");
      legendWriteMessage.setStyleName("legend");
      messagesPanel.add(legendWriteMessage);
      SmileysBar bar = new SmileysBar(messagesWritePanel);
      messagesPanel.add(bar);
      messagesPanel.add(messagesWritePanel);
      
      initWidget(messagesPanel);
    }

    private void sendMessage(String message) {
      AsyncCallback callback = new AsyncCallback() {
         public void onSuccess (Object callResult)
         {	
      	
         }
         public void onFailure (Throwable ex)
         {
         }
      };
      
      ProxyRPC.sendMessage(sessionId,roomId,message,callback);
    }

    public void displayWriteMessagePanel(boolean display) {
      if (display) {
      	if (!writePanelAlreadyDisplayed) {
      		messagesPanel.add(legendWriteMessage);
      		messagesPanel.add(messagesWritePanel);
      		writePanelAlreadyDisplayed = true;
      	}
      } else {
      	if (writePanelAlreadyDisplayed) {
      		messagesPanel.remove(legendWriteMessage);
      		messagesPanel.remove(messagesWritePanel);
      		writePanelAlreadyDisplayed = false;
      	}
      }
    }

  }

  private class ListUsersPanel extends Composite {
    private VerticalPanel listUsersPanel;

    private ScrollPanel listUsersPanelContainer;

    public ListUsersPanel() {
      listUsersPanel = new VerticalPanel();
      listUsersPanelContainer = new ScrollPanel(listUsersPanel);
      
      listUsersPanelContainer.setWidth(USERS_LIST_WIDTH);
      listUsersPanelContainer.setHeight(USERS_LIST_HEIGHT);
      listUsersPanelContainer.setStyleName("users-list");
      
      initWidget(listUsersPanelContainer);
    }

    public void resetList() {
      listUsersPanelContainer.remove(listUsersPanel);
    }

    public void setList(VerticalPanel list) {
      listUsersPanelContainer.add(list);
      listUsersPanel = list;
    }

  }

  private class HistoryPanel extends Composite {
    public VerticalPanel monthsList;

    private ScrollPanel historyPanelContainer;

    private ClickListener historyClickListener;

    public HistoryPanel() {
      monthsList = new VerticalPanel();
      historyPanelContainer = new ScrollPanel(monthsList);
      
      historyPanelContainer.setWidth(USERS_LIST_WIDTH);
      historyPanelContainer.setHeight(USERS_LIST_HEIGHT);
      historyPanelContainer.setStyleName("users-list");
      
      historyClickListener = new ClickListener() {
      	public void onClick(Widget sender) {
      		BuddyItem.HistoryLink link = (BuddyItem.HistoryLink) sender;
      		Chatroom.HistoryMessages historyMessages = new HistoryMessages();
      		historyMessages.init(sessionId,roomId,link.month,link.year);
      		/*
      		PopupPanel popup = new PopupPanel(true);
      		popup.setWidget(new HTML("Month = " + link.month + " / Year = " + link.year));
      		popup.show();
      		*/
      	}
      };
      
      AsyncCallback callback = new AsyncCallback() {
         public void onSuccess (Object callResult)
         {	
      	ArrayList<BuddyData> list = new ArrayList<BuddyData>();
      	list = (ArrayList<BuddyData>) callResult;
      	for (int i=0,N=list.size();i<N;i++) {
      		monthsList.add(new BuddyItem((BuddyData)list.get(i),historyClickListener,BuddyItem.HISTORY_BUDDY,""));
      	}
         }
         public void onFailure (Throwable ex)
         {
      	//mainModule.resetAppli();
         }
      };
      
      ProxyRPC.getHistoryMonths(roomId,sessionId,callback);
      
      initWidget(historyPanelContainer);
    }

  }

  private class HistoryMessages extends PopupPanel {
    private int WIDTH = 600;

    private int HEIGHT = 450;

    private int month;

    private int year;

    public void display(ArrayList<String[]> messages) {
      VerticalPanel mainPanel = new VerticalPanel();
      
      HorizontalPanel title = new HorizontalPanel();
      title.setStyleName("toolbar");
      title.setWidth(WIDTH + "px");
      title.add(new HTML("Historique des messages du mois de " + adjust(month) + " / " + year));
      title.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
      
      Command command = new Command() {
      	public void execute() {
      		hide();
      	}
      };
      
      ToolBarButton button = new ToolBarButton(new Image("./images/cancel.png"),"Fermer la fen&#234;tre",command);
      button.setEnabled(true);
      
      title.add(button);
      
      VerticalPanel messagesPanel = new VerticalPanel();
      
      for (int i=0,N=messages.size();i<N;i++) {
      	messagesPanel.add(new HTML("<div class='chatroom-message-normal'><b>(" + messages.get(i)[0] + ")</b> " + messages.get(i)[1] + " a &#233;crit: " + parseMessage(messages.get(i)[2]) + "</div>"));
      }
      
      ScrollPanel containerMessages = new ScrollPanel(messagesPanel);
      containerMessages.setWidth(WIDTH + "px");
      containerMessages.setHeight(HEIGHT + "px");
      
      mainPanel.add(title);
      mainPanel.add(containerMessages);
      
      setWidget(mainPanel);
      
      show();
      
      
    }

    public void init(String sessionId, String roomId, int month, int year) {
      this.month = month;
      this.year = year;
      
      AsyncCallback callback = new AsyncCallback() {
         public void onSuccess (Object callResult)
         {	
      	ArrayList<String[]> list = new ArrayList<String[]>();
      	list = (ArrayList<String[]>) callResult;
      	display(list);
         }
         public void onFailure (Throwable ex)
         {
      	//mainModule.resetAppli();
         }
      };
      
      
      ProxyRPC.getHistoryMessages(sessionId,roomId,month,year,callback);
    }

    private String adjust(int month) {
      if (month < 10) {
      	return ("0" + month);
      }
      
      return new Integer(month).toString();
    }

  }

  private class BuddyToolsPopup extends Composite {
    private String clickedUser;

    private PopupPanel popupContainer;

    public BuddyToolsPopup(String user, PopupPanel popup) {
      popupContainer = popup;
      
      VerticalPanel mainPanel = new VerticalPanel();
      clickedUser = user;
      
      ClickListener privateChatClickListener = new ClickListener() {
      	public void onClick(Widget sender) {
      		mainModule.createPrivateChatroom(clickedUser,true);
      		popupContainer.hide();
      	}
      };
      
      ClickListener callClickListener = new ClickListener() {
      	public void onClick(Widget sender) {
      		mainModule.makeCall(clickedUser);
      		popupContainer.hide();
      	}
      };
      
      BuddyTools line = new BuddyTools("Conversation priv&#233;e","./images/comment.png",privateChatClickListener);
      BuddyTools call = new BuddyTools("Appeler","./images/telephone_go.png",callClickListener);
      
      mainPanel.add(line);
      mainPanel.add(call);
      
      initWidget(mainPanel);
      
    }

  }

  private class BuddyTools extends Composite {
    public BuddyTools(String title, String image, ClickListener clickListener) {
      HorizontalPanel mainPanel = new HorizontalPanel();
      HorizontalPanel statusIconPanel = new HorizontalPanel();
      HorizontalPanel namePanel = new HorizontalPanel();
      
      statusIconPanel.setWidth("40px");
      namePanel.setWidth("140px");
      
      statusIconPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      namePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
      
      Image statusImage = new Image(image);
      statusIconPanel.add(statusImage);
      
      mainPanel.add(statusIconPanel);
      mainPanel.add(namePanel);
      
      HTML htmlName = new HTML(title);
      htmlName.setStyleName("buddy-tools");
      htmlName.addClickListener(clickListener);
      namePanel.add(htmlName);
      
      mainPanel.setStyleName("buddy-item");
      
      initWidget(mainPanel);
    }

  }
  
  public VerticalPanel title;

  private HorizontalPanel titleH;

  private HTML titleWidget;

  private Main mainModule;

  public Chatroom.AppliPanel appliPanel;

  private String roomId;

  private String sessionId;

  public int nbMessages;

  private ArrayList<BuddyData> buddies;

  private Chatroom.MessagesPanel messagesPanel;

  private Chatroom.ListUsersPanel listUsersPanel;

  private Chatroom.HistoryPanel historyPanel;
  
  private StackPanel stackMenuPanel;

  private ClickListener appliClickListener;

  private static String USERS_LIST_WIDTH = "200px";

  private static String USERS_LIST_HEIGHT = "315px";

  private static int MESSAGES_PANEL_WIDTH = 700;

  private static String MESSAGES_PANEL_HEIGHT = "415px";

  private static int MESSAGES_WRITE_PANEL_LENGTH = 85 ;
  
  private static int MESSAGES_WRITE_PANEL_NB_LINES = 5;

  private static String TITLE_HEIGHT = "40px";

  private static int TITLE_WIDTH = 250;

  private static int TITLE_WIDTH_NOT_SELECTED = 180;

  private static String STACK_HEIGHT = "415px";

  private static int STACK_WIDTH = 230;

  private static int STACK_MENU_MESSAGES_INDEX = 0;

  private static int STACK_MENU_HISTORY_INDEX = 1;

  private static int PRIVATE_CHATROOM = 1;

  private boolean isPrivateChatroom = false;

  private ToolBarButton closedButton;

  private ToolBarButton callButton;

  public Chatroom(String roomId, String sessionId, Main mainModule) {
    this.roomId = roomId;
    this.sessionId = sessionId;
    this.mainModule = mainModule;
    nbMessages = 0;
    
    messagesPanel = new MessagesPanel();
    listUsersPanel = new ListUsersPanel();
    historyPanel = new HistoryPanel();
    
    String titleUsersString = "<table><td align=center><img src='./images/conference.gif'></td><td><b>Chatroom</b></td></table>";
    String titleHistoryString = "<table><td align=center><img src='./images/chat.gif'></td><td><b>Historique</b></td></table>";
    String titleBlogString = "<table><td align=center><img src='./images/languages.gif'></td><td><b>Blog</b></td></table>";
    
    stackMenuPanel = new MyStackPanel();
    stackMenuPanel.add(listUsersPanel,titleUsersString,true);
    stackMenuPanel.add(new VerticalPanel(),titleBlogString,true);
    stackMenuPanel.add(historyPanel,titleHistoryString,true);
    stackMenuPanel.setWidth(STACK_WIDTH + "px");
    stackMenuPanel.setHeight(STACK_HEIGHT);
    
    buddies = new ArrayList<BuddyData>();
    
    appliPanel = new AppliPanel();
    
    appliPanel.mainPanel.add(stackMenuPanel);
    appliPanel.mainPanel.add(messagesPanel);
    
    titleWidget = new HTML();
    
    title = new VerticalPanel();
    title.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    titleH = new HorizontalPanel();
    titleH.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    titleH.add(titleWidget);
    title.setHeight(TITLE_HEIGHT);
    title.add(titleH);
    
    //updateUsers();
    
    //setHistoryMonthsList();
    
    
    
    
  }

  public Chatroom(String roomId, String sessionId, Main mainModule, int style) {
    this.roomId = roomId;
    this.sessionId = sessionId;
    this.mainModule = mainModule;
    nbMessages = 0;
    isPrivateChatroom = true;
    
    messagesPanel = new MessagesPanel();
    
    appliPanel = new AppliPanel();
    
    appliPanel.mainPanel.add(messagesPanel);
    
    titleWidget = new HTML();
    
    title = new VerticalPanel();
    title.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    titleH = new HorizontalPanel();
    titleH.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    titleH.add(titleWidget);
    title.setHeight(TITLE_HEIGHT);
    title.add(titleH);
    
    Command command = new Command() {
    	public void execute() {
    		closeChatroom();
    	}
    };
    
    Command callCommand = new Command() {
    	public void execute() {
    		makeCall();
    	}
    };
    
    closedButton = new ToolBarButton(new Image("./images/cancel.png"),"Fermer la fen&#234;tre de conversation",command);
    closedButton.setEnabled(true);
    
    callButton = new ToolBarButton(new Image("./images/telephone_go.png"),"Appeler " + roomId,callCommand);
    callButton.setEnabled(true);
    
    appliPanel.updateTitle();
    
    
    
    
  }

  public void updateUsers(ArrayList<BuddyData> list) {
    parseBuddiesList(list);
    buddies = list;
    VerticalPanel vertPanel = new VerticalPanel();
    
    ClickListener clickListener = new ClickListener() {
    	public void onClick(Widget sender) {
    		BuddyItem.BuddyLink buddyLink = (BuddyItem.BuddyLink) sender;
    		PopupPanel popup = new PopupPanel(true);
    		BuddyToolsPopup toolsPopup = new BuddyToolsPopup(buddyLink.public_entity,popup);
    		popup.setWidget(toolsPopup);
    		popup.setPopupPosition(DOM.getAbsoluteLeft(buddyLink.getElement()) + buddyLink.getOffsetWidth(),DOM.getAbsoluteTop(buddyLink.getElement()));
    		popup.setStyleName("buddy-tools-popup");
    		popup.show();
    	}
    };
    
    for (int i=0,N=buddies.size();i<N;i++) {
    	vertPanel.add(new BuddyItem((BuddyData)buddies.get(i),clickListener,1,mainModule.publicId));
    }
    
    listUsersPanel.resetList();
    listUsersPanel.setList(vertPanel);
    
  }

  public void updateMessages(String message) {
    HTML messageHTML = new HTML();
    
    if (!isPrivateChatroom) {
    	messageHTML.setHTML(parseMessage(message));
    } else {
    	String time = Tools.getTimeHumanReadable();
    	messageHTML.setHTML("<div class='chatroom-message-normal'><b>(" + time + " / " + roomId+ "): </b>" + parseMessage(message) + "</div>");
    }
    messagesPanel.messagesReceivedPanel.add(messageHTML);
    messagesPanel.messagesReceivedPanelContainer.ensureVisible((UIObject)messageHTML);
    nbMessages  = nbMessages + 1;
    appliPanel.updateTitle();
    
    
  }

  private void parseBuddiesList(ArrayList<BuddyData> newBuddiesList) {
    if (buddies.size() == newBuddiesList.size()) {
    	for (int i=0,N=newBuddiesList.size();i<N;i++) {
    		if ((buddies.get(i).status == 0) && (newBuddiesList.get(i).status > 0)) {
    			notifyNewRegistration(buddies.get(i).name,1);
    		} else if ((buddies.get(i).status > 0) && (newBuddiesList.get(i).status == 0)) {
    			notifyNewRegistration(buddies.get(i).name,0);
    		}	
    	}	
    }
  }

  private void notifyNewRegistration(String name, int status) {
    String message = null;
    String time = Tools.getTimeHumanReadable();
    
    if (status > 0) {
    	message = "<div class='chatroom-message-system'><b>(" + time + ")</b> " + name + " a rejoint la conf&#233;rence</div>";
    } else {
    	message = "<div class='chatroom-message-system'><b>(" + time + ")</b> " + name + " a quitt&#233; la conf&#233;rence</div>";
    }
    
    HTML messageHTML = new HTML(message);
    messagesPanel.messagesReceivedPanel.add(messageHTML);
    messagesPanel.messagesReceivedPanelContainer.ensureVisible((UIObject)messageHTML);
  }

  private void closeChatroom() {
    mainModule.closeChatroom(roomId);
  }

  private String parseMessage(String message) {
    Smileys smileys = new Smileys();
    String result = message;
    
    for (int i=0,N=smileys.NB_SMILEYS;i<N;i++) {
    	result = result.replaceAll(smileys.commentButtonsRegexp[i],"<img src='" + smileys.commentButtonsImages[i].getUrl() + "'>");
    }
    
    return result;
  }

  private void makeCall() {
    mainModule.makeCall(roomId);
  }

}
