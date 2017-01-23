
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;

public class AppliEvents implements IsSerializable {
  public ArrayList<BuddyData> buddies;

  public ArrayList<String[]> messages;

  public ArrayList<SipEvent> sipEvents;

}
