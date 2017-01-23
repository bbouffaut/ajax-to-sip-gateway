
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

class PresenceSelector extends Composite {
  class MyCommand implements Command {
    private String type;

    public MyCommand(String type) {
      this.type = type;
    }

    public void execute() {
      AsyncCallback callback = new AsyncCallback() {
         public void onSuccess (Object callResult)
         {	
      	status = (String) callResult;
      	updatePresentityTitle();
         }
         public void onFailure (Throwable ex)
         {
      
         }
      };
      
      ProxyRPC.setPresence(sessionId,type,callback);
    }

  }

  private String sessionId;

  private HTML presentityTitle;

  private String status;

  public PresenceSelector(String sessionId) {
    this.sessionId = sessionId;
    status = Constants.PRESENCE_ONLINE;
    
    HorizontalPanel mainPanel = new HorizontalPanel();
    
    MenuBar mainMenu = new MenuBar();
    MenuBar selectorList = new MenuBar(true);
    presentityTitle = new HTML();
    presentityTitle.setWidth("180px");
    
    String selectorTitle = "<img src='./images/selector.png'>";
    mainMenu.addItem(selectorTitle,true,selectorList);
    
    String itemOnline = "<div><img src=" + Constants.ONLINE_ICON + "><span> Disponible </span></div>";
    String itemBusy = "<div><img src=" + Constants.BUSY_ICON + "><span> Occup&#233; </span></div>";
    
    selectorList.addItem(itemOnline,true,new MyCommand(Constants.PRESENCE_ONLINE));
    selectorList.addItem(itemBusy,true,new MyCommand(Constants.PRESENCE_BUSY));
    
    updatePresentityTitle();
    
    mainPanel.add(presentityTitle);
    mainPanel.add(mainMenu);
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    
    initWidget(mainPanel);
    
  }

  private void updatePresentityTitle() {
    if (status == Constants.PRESENCE_ONLINE)
    	presentityTitle.setHTML("<div>Status: <span><b>Disponible</b></span> <span><img src=" + Constants.ONLINE_ICON + "></span>  </div>");
    else if (status == Constants.PRESENCE_BUSY)
    	presentityTitle.setHTML("<div>Status: <span><b>Occup&#233</b></span>  <span><img src=" + Constants.BUSY_ICON + "></span>  </div>");
    else 
    	presentityTitle.setHTML("<div>Status: <span><b>Disponible</b></span>  <span><img src=" + Constants.ONLINE_ICON + "></span>  </div>");
  }

  public void setPresence(int statusInt) {
    if (statusInt > 0)
    	status = Constants.PRESENCE_ONLINE;
    else if (statusInt < 0)
    	status = Constants.PRESENCE_BUSY;
    else 
    	status = Constants.PRESENCE_ONLINE;
    
    updatePresentityTitle();
  }

}
