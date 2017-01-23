
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

class SmileysBar extends Composite {
  private TextArea box;

  private static int NB_BUTTONS = 7;

  private ToolBarButton[] commentButtons;

  private ClickListener listener;

  private Smileys smileys;

  public SmileysBar(TextArea textBox) {
    this.box = textBox;
    smileys = new Smileys();
    
    HorizontalPanel buttonsBar = new HorizontalPanel();
    
    commentButtons = new ToolBarButton[NB_BUTTONS];
    
    this.listener = new ClickListener () {
    		public void onClick(Widget sender) {
    			for (int i = 0, n = NB_BUTTONS; i < n; i++) {
    				if (sender == commentButtons[i].image) {	
    					insertText(smileys.commentButtonsSmileys[i]);
    					box.setFocus(true);
    				} 
    			}
    			
    		}
    };
    
    for (int i = 0, n = NB_BUTTONS; i < n; i++) {
    	commentButtons[i] = new ToolBarButton(smileys.commentButtonsImages[i],"",this.listener);
    	commentButtons[i].disableLegend();
    	buttonsBar.add(commentButtons[i]);
    }
    
    initWidget(buttonsBar);
  }

  private void insertText(String text) {
    String currentText = box.getText();
    String newText = currentText + text;
    
    box.setText(newText);
  }

}
