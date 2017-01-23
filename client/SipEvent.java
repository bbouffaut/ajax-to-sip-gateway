
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SipEvent implements IsSerializable {
  public String method;

  public String callId;

  public String time;

  public String subscriber;

  public String domain;

  public String fromUser;

  public String fromDomain;

  public String ipAdress;

  public int audioPort;

  public String codec;

  public int incoming;

  public int status;

}
