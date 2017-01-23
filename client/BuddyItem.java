
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

class BuddyItem extends Composite {
  public class HistoryLink extends HTML {
    public int month;

    public int year;

  }

  public class BuddyLink extends HTML {
    public String public_entity;

  }

  public static int HISTORY_BUDDY = 0;

  public static int CONTACT_BUDDY = 1;

  private HorizontalPanel mainPanel;

  private HorizontalPanel statusIconPanel;

  private HorizontalPanel namePanel;

  public BuddyItem(BuddyData buddyData) {
    mainPanel = new HorizontalPanel();
    statusIconPanel = new HorizontalPanel();
    namePanel = new HorizontalPanel();
    
    statusIconPanel.setWidth("40px");
    namePanel.setWidth("140px");
    
    statusIconPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    namePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    
    Image statusImage = new Image();
    statusIconPanel.add(statusImage);
    
    HTML htmlName = new HTML(buddyData.name);
    htmlName.setStyleName("chatroom-buddy-normal");
    namePanel.add(htmlName);
    
    mainPanel.add(statusIconPanel);
    mainPanel.add(namePanel);
    
    mainPanel.setStyleName("buddy-item");
    
    int status = buddyData.status;
    
    if (status > 0) {
    	statusImage.setUrl(Constants.ONLINE_ICON);
    	namePanel.setStyleName("buddy-name-item");
    } else if (status == 0) {
    	statusImage.setUrl(Constants.OFFLINE_ICON);
    	namePanel.setStyleName("buddy-name-item-unavailable");
    } else {
    	statusImage.setUrl(Constants.HISTORY_ICON);
    }
    
    initWidget(mainPanel);
    
  }

  public BuddyItem(BuddyData buddyData, ClickListener clickListener, int type, String publicId) {
    mainPanel = new HorizontalPanel();
    statusIconPanel = new HorizontalPanel();
    namePanel = new HorizontalPanel();
    
    statusIconPanel.setWidth("40px");
    
    
    statusIconPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    namePanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    
    Image statusImage = new Image();
    statusIconPanel.add(statusImage);
    
    if (type == HISTORY_BUDDY) {
    	mainPanel.setWidth("140px");
    	HistoryLink historyLink = new HistoryLink();
    	historyLink.setHTML(buddyData.name);
    	historyLink.setStyleName("chatroom-buddy-link");
    	historyLink.addClickListener(clickListener);
    	historyLink.month = buddyData.month;
    	historyLink.year = buddyData.year;
    	namePanel.add(historyLink);
    	statusImage.setUrl(Constants.HISTORY_ICON);
    } else if (type == CONTACT_BUDDY) {
    
    	int width = buddyData.name.length()*7;
    	mainPanel.setWidth(width + "px");
    	BuddyLink buddyLink = new BuddyLink();
    	buddyLink.setHTML(buddyData.name);
    	buddyLink.public_entity = buddyData.name;
    	namePanel.add(buddyLink);
    
    	int status = buddyData.status;
    
    	if ((status > 0) && (!buddyData.name.equals(publicId))) {
    		statusImage.setUrl(Constants.ONLINE_ICON);
    		buddyLink.setStyleName("chatroom-buddy-link");
    		buddyLink.addClickListener(clickListener);
    	} else if ((status > 0) && (buddyData.name.equals(publicId))) {
    		statusImage.setUrl(Constants.ONLINE_ICON);
    		buddyLink.setStyleName("chatroom-buddy-nolink");
    	} else if (status == 0) {
    		statusImage.setUrl(Constants.OFFLINE_ICON);
    		buddyLink.setStyleName("chatroom-buddy-unavailable");
    	} else if (status < 0) {
    		statusImage.setUrl(Constants.BUSY_ICON);
    		buddyLink.setStyleName("chatroom-buddy-nolink");
    	}
    }
    
    mainPanel.add(statusIconPanel);
    mainPanel.add(namePanel);
    
    mainPanel.setStyleName("buddy-item");
    
    initWidget(mainPanel);
    
  }

}
