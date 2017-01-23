
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import com.google.gwt.user.client.rpc.RemoteService;

import java.util.ArrayList;
import java.util.HashMap;

public interface WebServices extends RemoteService {
  String processLogin(String login) ;
  ArrayList<String> processPassword(String sessionId, String password) ;
  ArrayList<String> getUser(String sessionId) ;
  void unregisterUser(String sessionId) ;
  void registerUser(String sessionId) ;
  void sendMessage(String sessionId, String destId, String message) ;
  ArrayList<BuddyData> getHistoryMonths(String sessionId, String roomId) ;
  ArrayList<String[]> getHistoryMessages(String sessionId, String roomId, int month, int year) ;
  AppliEvents retrieveEvents(String sessionId) ;
  void processCall(String sessionId, SipCall call, String method, int status) ;
  String call(String sessionId, String who) ;
  String setPresence(String sessionId, String presence) ;
  void cancelCall(String sessionId, String applicationSessionId) ;
}
