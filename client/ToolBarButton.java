
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

public class ToolBarButton extends Composite {
  private HorizontalPanel buttonContainer;

  private boolean isActivated;

  private String buttonLegend = "";

  private AbstractImagePrototype imagePrototype;

  public Image image;

  public HTML buttonHTML;

  private PopupPanel legendPopup = new PopupPanel (true);

  private ClickListener clickListener;

  private MouseListener mouseListener;

  private boolean legendIsEnabled = true;

  public int isReleased = 1;

  private Command command;

  public ToolBarButton(AbstractImagePrototype imagePrototype, String legend) {
    this.imagePrototype = imagePrototype;
    //this.clickListener = clickListener;
    mouseListener = new MouseListener () {
    		public void onMouseDown (Widget sender, int x, int y) {
    			if (isActivated) {
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button-clicked");
    				isReleased = 0;
    			}
    		}
    
    		public void onMouseEnter (Widget sender) {
    			if (isActivated) {
    				//HTML buttonSender = (HTML) sender;
    				Element elem = sender.getElement();
    				legendPopup.setPopupPosition(DOM.getAbsoluteLeft(elem) + 10, DOM.getAbsoluteTop(elem) + 30);
    				legendPopup.setWidget(new HTML(buttonLegend));
    				legendPopup.setStyleName("toolbar-button-legend-popup");
    				if (legendIsEnabled) {
    					legendPopup.show();
    				}
    				buttonContainer.setStyleName("toolbar-button-over");
    			}			
    		}
    
    		public void onMouseLeave (Widget sender) {
    			if (isActivated) {
    				if (legendIsEnabled) {
    					legendPopup.hide();
    				}
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button");
    			}
    		}
    
    		public void onMouseMove (Widget sender, int x, int y) {
    		}
    
    		public void onMouseUp (Widget sender, int x, int y) {
    			if (isActivated) {		
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button-over");
    				isReleased = 1;
    			}
    		}
    };
    
    buttonLegend = legend;
    //buttonHTML = new HTML ("<center><img src=" + image.getUrl() + "></center>");
    buttonHTML = new HTML ("<center>" + imagePrototype.getHTML() + "</center>");
    //buttonHTML.setStyleName("toolbar-button");
    
    
    buttonHTML.addMouseListener(mouseListener);
    
    buttonContainer = new HorizontalPanel();
    buttonContainer.add(buttonHTML);
    buttonContainer.setStyleName("toolbar-button");
    
    initWidget(buttonContainer);
    
    
  }

  public ToolBarButton(Image image, String legend, Command commandInput) {
    this.image = image;
    this.command = commandInput;
    //this.clickListener = clickListener;
    mouseListener = new MouseListener () {
    		public void onMouseDown (Widget sender, int x, int y) {
    			if (isActivated) {
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button-clicked");
    				isReleased = 0;
    			}
    		}
    
    		public void onMouseEnter (Widget sender) {
    			if (isActivated) {
    				//HTML buttonSender = (HTML) sender;
    				Element elem = sender.getElement();
    				legendPopup.setPopupPosition(DOM.getAbsoluteLeft(elem) + 10, DOM.getAbsoluteTop(elem) + 30);
    				legendPopup.setWidget(new HTML(buttonLegend));
    				legendPopup.setStyleName("toolbar-button-legend-popup");
    				if (legendIsEnabled) {
    					legendPopup.show();
    				}
    				buttonContainer.setStyleName("toolbar-button-over");
    			}			
    		}
    
    		public void onMouseLeave (Widget sender) {
    			if (isActivated) {
    				if (legendIsEnabled) {
    					legendPopup.hide();
    				}
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button");
    			}
    		}
    
    		public void onMouseMove (Widget sender, int x, int y) {
    		}
    
    		public void onMouseUp (Widget sender, int x, int y) {
    			if (isActivated) {		
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button-over");
    				isReleased = 1;
    				if (command != null) {
    					command.execute();
    				}
    			}
    		}
    };
    
    buttonLegend = legend;
    buttonHTML = new HTML ("<center><img src=" + image.getUrl() + "></center>");
    //buttonHTML = new HTML ("<center>" + image.getHTML() + "</center>");
    //buttonHTML.setStyleName("toolbar-button");
    
    
    //buttonHTML.addMouseListener(mouseListener);
    image.addMouseListener(mouseListener);
    
    buttonContainer = new HorizontalPanel();
    //buttonContainer.add(buttonHTML);
    buttonContainer.add(image);
    buttonContainer.setStyleName("toolbar-button");
    
    initWidget(buttonContainer);
    
    
  }

  public ToolBarButton(Image image, String legend, ClickListener clickListener) {
    this.image = image;
    this.clickListener = clickListener;
    mouseListener = new MouseListener () {
    		public void onMouseDown (Widget sender, int x, int y) {
    			if (isActivated) {
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button-clicked");
    				isReleased = 0;
    			}
    		}
    
    		public void onMouseEnter (Widget sender) {
    			if (isActivated) {
    				//HTML buttonSender = (HTML) sender;
    				Element elem = sender.getElement();
    				legendPopup.setPopupPosition(DOM.getAbsoluteLeft(elem) + 10, DOM.getAbsoluteTop(elem) + 30);
    				legendPopup.setWidget(new HTML(buttonLegend));
    				legendPopup.setStyleName("toolbar-button-legend-popup");
    				if (legendIsEnabled) {
    					legendPopup.show();
    				}
    				buttonContainer.setStyleName("toolbar-button-over");
    			}			
    		}
    
    		public void onMouseLeave (Widget sender) {
    			if (isActivated) {
    				if (legendIsEnabled) {
    					legendPopup.hide();
    				}
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button");
    			}
    		}
    
    		public void onMouseMove (Widget sender, int x, int y) {
    		}
    
    		public void onMouseUp (Widget sender, int x, int y) {
    			if (isActivated) {		
    				//HTML buttonSender = (HTML) sender;
    				buttonContainer.setStyleName("toolbar-button-over");
    				isReleased = 1;
    				if (command != null) {
    					command.execute();
    				}
    			}
    		}
    };
    
    buttonLegend = legend;
    buttonHTML = new HTML ("<center><img src=" + image.getUrl() + "></center>");
    //buttonHTML = new HTML ("<center>" + image.getHTML() + "</center>");
    //buttonHTML.setStyleName("toolbar-button");
    
    
    //buttonHTML.addMouseListener(mouseListener);
    image.addMouseListener(mouseListener);
    image.addClickListener(clickListener);
    
    buttonContainer = new HorizontalPanel();
    //buttonContainer.add(buttonHTML);
    buttonContainer.add(image);
    buttonContainer.setStyleName("toolbar-button");
    
    initWidget(buttonContainer);
    
    
  }

  public void setEnabled(boolean enabled) {
    isActivated = enabled;
    if (!isActivated) {
    	buttonHTML.removeClickListener(clickListener);
    	//buttonHTML.setStyleName("toolbar-button-deactivated");
    	//image.removeClickListener(clickListener);
    	buttonContainer.setStyleName("toolbar-button-deactivated");	
    } else {
    	buttonHTML.addClickListener(clickListener);
    	//buttonHTML.setStyleName("toolbar-button");
    	//image.addClickListener(clickListener);
    	buttonContainer.setStyleName("toolbar-button");
    }
  }

  public void setButtonHeight(int height) {
    buttonHTML.setHeight(height + "px");
  }

  public void setButtonWidth(int width) {
    buttonHTML.setWidth(width + "px");
  }

  public void disableLegend() {
    legendIsEnabled = false;
  }

  public Image getImage() {
    return image;
  }

  public void addClickListener(ClickListener clickListener) {
    //buttonHTML.addClickListener(clickListener);
    image.addClickListener(clickListener);
  }

}
