
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

class LoadingPopup {
  private AbsolutePanel mask;

  private PopupPanel popupMask;

  private String loadingMessage;

  private PopupPanel loadingMessageContainer = new PopupPanel();

  public LoadingPopup(PopupContent popupContent) {
    //loadingMessage = "<img src='images/large-loading.gif' width='32' height='32'  style='margin-right:8px;float:left;vertical-align:top;'/><br/><span>" + message + "</span>";
    
    mask = new AbsolutePanel();
    mask.setWidth(Window.getClientWidth() + "px");
    mask.setHeight(Window.getClientHeight() + "px");
    
    popupMask = new PopupPanel();
    popupMask.setWidget(mask);
    popupMask.setPopupPosition(0,0);
    popupMask.setStyleName("loading-popup-mask");
    
    loadingMessageContainer.setPopupPosition((int)(Window.getClientWidth()/2) - 80,130);
    //loadingMessageContainer.setWidget(new HTML(loadingMessage));
    loadingMessageContainer.setWidget(popupContent);
    loadingMessageContainer.setStyleName("loading-popup");
  }

  public void show() {
    DOM.setStyleAttribute(popupMask.getElement(),"zIndex","2001");
    DOM.setStyleAttribute(loadingMessageContainer.getElement(),"zIndex","2002");
    popupMask.show();
    loadingMessageContainer.show();
  }

  public void hide() {
    popupMask.hide();
    loadingMessageContainer.hide();
  }

}
