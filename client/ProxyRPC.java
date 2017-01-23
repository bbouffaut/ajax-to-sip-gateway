
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.core.client.GWT;

class ProxyRPC {
  private static WebServicesAsync webServices;

  private static ServiceDefTarget webEndpoint;

static {
  webServices = (WebServicesAsync) GWT.create(WebServices.class);
  webEndpoint = (ServiceDefTarget) webServices;
  webEndpoint.setServiceEntryPoint("/webapps-chatroom/WebServices");
  
}

  public static void processLogin(String login, AsyncCallback callback)
  {
    webServices.processLogin(login,callback);
  }

  public static void processPassword(String sessionId, String password, AsyncCallback callback)
  {
    webServices.processPassword(sessionId,password,callback);
  }

  public static void getUser(String sessionId, AsyncCallback callback)
  {
    webServices.getUser(sessionId,callback);
  }

  public static void unregisterUser(String sessionId, AsyncCallback callback)
  {
    webServices.unregisterUser(sessionId,callback);
  }

  public static void registerUser(String sessionId, AsyncCallback callback)
  {
    webServices.registerUser(sessionId,callback);
  }

  public static void sendMessage(String sessionId, String destId, String message, AsyncCallback callback)
  {
    webServices.sendMessage(sessionId,destId,message,callback);
  }

  public static void getHistoryMonths(String sessionId, String roomId, AsyncCallback callback)
  {
    webServices.getHistoryMonths(sessionId,roomId,callback);
  }

  public static void getHistoryMessages(String sessionId, String roomId, int month, int year, AsyncCallback callback)
  {
    webServices.getHistoryMessages(sessionId,roomId,month,year,callback);
  }

  public static void retrieveEvents(String sessionId, AsyncCallback callback)
  {
    webServices.retrieveEvents(sessionId,callback);
  }

  public static void processCall(String sessionId, SipCall call, String method, int status, AsyncCallback callback)
  {
    webServices.processCall(sessionId,call,method,status,callback);
  }

  public static void call(String sessionId, String who, AsyncCallback callback)
  {
    webServices.call(sessionId,who,callback);
  }

  public static void setPresence(String sessionId, String presence, AsyncCallback callback)
  {
    webServices.setPresence(sessionId,presence,callback);
  }

  public static void cancelCall(String sessionId, String applicationSessionId, AsyncCallback callback)
  {
    webServices.cancelCall(sessionId,applicationSessionId,callback);
  }

}
