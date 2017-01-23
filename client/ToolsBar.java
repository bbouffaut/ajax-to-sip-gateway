
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;

class ToolsBar extends Composite {
  private static int TITLE_WIDTH = 950;

  private Main mainModule;

  private HorizontalPanel mainPanel;

  private HTML detailsInfo;

  private String sessionId;

  private AsyncCallback callback;

  public PresenceSelector presenceSelector;

  public ToolsBar(String sessionId, Main mainModule) {
    String name = null;
    this.sessionId = sessionId;
    this.mainModule = mainModule;
    
    mainPanel = new HorizontalPanel();
    mainPanel.setStyleName("toolbar");
    mainPanel.setWidth(TITLE_WIDTH + "px");
    
    detailsInfo = new HTML();
    int infosWidth = TITLE_WIDTH - 310;
    detailsInfo.setWidth(infosWidth + "px");
    mainPanel.add(detailsInfo);
    
    callback = new AsyncCallback() {
       public void onSuccess (Object callResult)
       {	
    	ArrayList<String> values = (ArrayList<String>) callResult;
    	if (values != null) {
    		if (values.size() == 2) {
    			detailsInfo.setHTML("Bienvenue <b>" + values.get(1) + "</b> - Heure de connexion: <b>" + values.get(0) + "</b>");
    			setPublicId(values.get(1));
    		} else {
    			restart();
    		}
    	}
       }
       public void onFailure (Throwable ex)
       {
    	restart();
       }
    };
    
    
    Command command = new Command() {
    	public void execute() {
    		resetAppli();
    	}
    };
    
    presenceSelector = new PresenceSelector(sessionId);
    
    ToolBarButton button = new ToolBarButton(new Image("./images/cancel.png"),"Quitter",command);
    button.setEnabled(true);
    
    mainPanel.add(presenceSelector);
    mainPanel.add(button);
    
    ProxyRPC.getUser(sessionId,callback);
    
    initWidget(mainPanel);
  }

  private void restart() {
    ProxyRPC.getUser(sessionId,callback);
  }

  private void resetAppli() {
    mainModule.resetAppli();
  }

  private void setPublicId(String publicId) {
    mainModule.publicId = publicId;
  }

}
