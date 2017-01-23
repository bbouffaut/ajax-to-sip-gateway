
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

class PopupContent extends Composite {
  private HTML message;

  private Button button;

  public PopupContent(String message, ClickListener listener) {
    VerticalPanel panel = new VerticalPanel();
    this.message = new HTML(message);
    this.message.setStyleName("comment-writer-popup-message");
    button = new Button("OK",listener);
    
    panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    panel.add(this.message);
    panel.add(button);
    panel.setStyleName("comment-writer-popup");
    
    initWidget(panel);
  }

  public PopupContent(String message) {
    VerticalPanel panel = new VerticalPanel();
    this.message = new HTML(message);
    this.message.setStyleName("comment-writer-popup-message");
    
    panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    panel.add(this.message);
    panel.setStyleName("comment-writer-popup");
    
    initWidget(panel);
  }

}
