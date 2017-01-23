
package fr.free.hd.bond.chatroom.ServiceServlets;

import javax.media.format.*;

public class ChatroomConstants {
  public static String RESPONSE_ACK = "ACK";

  public static String RESPONSE_INVITE = "INVITE-RESPONSE";

  public static String MISSED_CALL = "MISSED-CALL";

  public static String CANCEL_CALL = "CANCEL";

  public static String BYE_CALL = "BYE";

  public static String INVITE_CALL = "INVITE";

  public static String ROOM_SERVICE_LOGIC_SIP_SERVLET = "RoomSipServlet";

  public static String NOTIFY_TERMINATED = "terminated";

  public static String NOTIFY_ACTIVE = "active";

  public static int BUSY_USER = -1000;

  public static int UNREGISTER_USER = 0;

  public static int REGISTER_USER = 1;

  public static String PRESENCE_EVENT = "presence";

  public static String PRESENCE_BUSY = "DND";

  public static String PRESENCE_AWAY = "Away";

  public static String PRESENCE_OFFLINE = "Offline";

  public static String PRESENCE_ONLINE = "Online-available";

  public static String TIMER_SUBSCRIBE_ROOM = "SUBSCRIBE_ROOM";

  public static String TIMER_DOMAIN = "DOMAIN";

  public static String TIMER_USER = "USER";

  public static String TIMER_NAME = "TIMER_NAME";

  public static long REGISTER_TIMEOUT = 3600000;

  public static String SUBSCRIBE_TIMER_NAME = "SUBSCRIBE_TIMER";

  public static String PUBLISH_TIMER_NAME = "PUBLISH_TIMER";

  public static String REGISTER_TIMER_NAME = "REGISTER_TIMER";

  public static int EXPIRES_NULL = 0;

  public static int EXPIRES_DEFAULT = 3600;

  public static String SUBSCRIBE_TERMINATE = "terminated";

  public static String SUBSCRIBE_ACTIVATE = "active";

  public static String ALL_ROOMS = "All";

  public static String BOND_DOMAIN_NAME = "BondDomain";

  public static String SERVER_ADDRESS = "ServerAddress";

  public static String BOND_DOMAIN = "bond.hd.free.fr";

  public static String CALLER_HANDLER = "CALLER-HANDLER";
  
  public static String CALLEE_HANDLER = "CALLEE-HANDLER";

  public static int AUDIO_PORT = 30000;
  
  public static String AUDIO_CODEC = AudioFormat.ULAW_RTP;

  public static String SDP_CONTENT_TYPE = "application/sdp";

  public static int INVITE_ANSWER_TIMEOUT = 15000;

  public static String USER = "USER";

  public static String DOMAIN = "DOMAIN";

  public static String SESSIONS_LIST = "SESSIONS_LIST";

  public static String SIP_SESSION_ID = "SIP_SESSION_ID";

  public static String TIMER_INFOS = "TIMER_INFOS";

  public static String TIMER_SERVICE = "javax.servlet.sip.TimerService";

  public static long RESEND_PERIOD = 900000;

  public static long REGISTER_RESEND_PERIOD = 900000;

  public static long SUBSCRIBE_RESEND_PERIOD = 300000;

  public static String CALLER_ID = "CALLER_ID";

}
