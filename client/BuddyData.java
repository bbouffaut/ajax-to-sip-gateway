
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BuddyData implements IsSerializable {
  public String name;

  public String avatar;

  public int status;

  public int year;

  public int month;

  public String chatroom;

}
