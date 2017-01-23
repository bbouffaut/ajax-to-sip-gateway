
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import java.util.ArrayList;
import java.util.HashMap;

class AppletWidget extends Composite {
  public AppletWidget(String remoteAddress, int remotePort, String audioFormat, int localPort) {
    HTML appletContainer = new HTML("<applet code='fr/free/hd/bond/chatroom/Applet/ChatroomAppletImpl.class' archive='chatroom-applet.jar' width='150' height='60' codebase='Applet/'><param name='REMOTE_ADDRESS' value='" + remoteAddress + "'><param name='REMOTE_PORT' value='" + remotePort + "'><param name='LOCAL_PORT' value='" + localPort + "'><param name='AUDIO_FORMAT' value='" + audioFormat + "'></applet>");
    
    initWidget(appletContainer);
  }

}
