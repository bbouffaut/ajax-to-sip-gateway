
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

class MyStackPanel extends StackPanel {
	
	private static int CHAT_PANEL = 1;
	private static int HISTORY_PANEL = 3;
	private static int BLOG_PANEL = 2;
	
	public void onBrowserEvent(Event event) {
		
		int index = -1;
		
		if (DOM.eventGetType(event) == Event.ONCLICK) {
			Element target = DOM.eventGetTarget(event);
			index = findDividerIndex(target);
			if (index != -1) {
				showStack(index);
			}
		}
			
		switch (index) {
		case 1: displayChatroom(); break;
		case 2: displayBlog(); break;
		case 3:
		default:
		}
	}
	
	private void displayChatroom() {
		showStack(CHAT_PANEL);
	}
	
	private void displayBlog() {
		showStack(BLOG_PANEL);
	}
	
	private int findDividerIndex(Element elem) {
		while (elem != getElement()) {
			String expando = DOM.getElementProperty(elem, "__index");
			if (expando != null) {
				// Make sure it belongs to me!
				int ownerHash = DOM.getElementPropertyInt(elem, "__owner");
				if (ownerHash == hashCode()) {
					// Yes, it's mine.
					return Integer.parseInt(expando);
				} else {
					//It must belong to some nested StackPanel.
					return -1;
				}
					
			}
			elem = DOM.getParent(elem);
		}
		return -1;
	}
		

}
		

