
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WebServicesAsync {
  void processLogin(String login, AsyncCallback callback) ;
  void processPassword(String sessionId, String password, AsyncCallback callback) ;
  void getUser(String sessionId, AsyncCallback callback) ;
  void unregisterUser(String sessionId, AsyncCallback callback) ;
  void registerUser(String sessionId, AsyncCallback callback) ;
  void sendMessage(String sessionId, String destId, String message, AsyncCallback callback) ;
  void getHistoryMonths(String sessionId, String roomId, AsyncCallback callback) ;
  void getHistoryMessages(String sessionId, String roomId, int month, int year, AsyncCallback callback) ;
  void retrieveEvents(String sessionId, AsyncCallback callback) ;
  void processCall(String sessionId, SipCall call, String method, int status, AsyncCallback callback) ;
  void call(String sessionId, String who, AsyncCallback callback) ;
  void setPresence(String sessionId, String presence, AsyncCallback callback) ;
  void cancelCall(String sessionId, String applicationSessionId, AsyncCallback callback) ;
}
