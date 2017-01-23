
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SipCall implements IsSerializable {
  public SipEvent sipEvent;

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

  public void init(SipEvent sipEvent) {
    this.callId = sipEvent.callId;
    this.time = sipEvent.time;
    this.subscriber = sipEvent.subscriber;
    this.domain = sipEvent.domain;
    this.fromUser = sipEvent.fromUser;
    this.fromDomain = sipEvent.fromDomain;
    this.ipAdress = sipEvent.ipAdress;
    this.audioPort = sipEvent.audioPort;
    this.codec = sipEvent.codec;
    this.incoming = sipEvent.incoming;
    
    this.sipEvent = sipEvent;
  }

}
