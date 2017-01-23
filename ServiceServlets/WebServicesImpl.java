
package fr.free.hd.bond.chatroom.ServiceServlets;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import java.lang.Integer;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;

import java.security.MessageDigest;
import java.security.Security;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.URI;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.TimerListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;

import javax.media.*;
import javax.media.protocol.*;
import javax.media.format.*;
import javax.media.control.TrackControl;

import javax.annotation.Resource;

import fr.free.hd.bond.chatroom.client.BuddyData;
import fr.free.hd.bond.chatroom.client.AppliEvents;
import fr.free.hd.bond.chatroom.client.SipCall;
import fr.free.hd.bond.chatroom.client.SipEvent;

public class WebServicesImpl extends RemoteServiceServlet implements fr.free.hd.bond.chatroom.client.WebServices {
	private static boolean DEBUG = false;
	
	@Resource(mappedName="sip/fr.free.hd.bond.chatroom.ChatRoomApplication")
	private SipFactory factory;
	
	private static Logger logger;
	
	private SipRequestGenerator requestGenerator;
	
	private Tools tools;
	
	private Context DBcontext;
	
	private DataSource DBdatasource;
	
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		logger = Logger.getLogger(WebServicesImpl.class.getName());
		
		//factory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		
		requestGenerator = new SipRequestGenerator(factory);
		
		try {
			DBcontext = new InitialContext();
			DBdatasource = (DataSource)DBcontext.lookup("java:comp/env/jdbc/chatroom");
		} catch (NamingException e) {
			logger.info("WebServiceImpl INFO: SQL Error: Boom - No Context!: " + e.getMessage());
		}
		
		tools = new Tools(factory,DBdatasource);
		
	}
	
	public void destroy() {
		super.destroy();
	}
	
	public String processLogin(String login) {
		HttpServletRequest request = getThreadLocalRequest();
		
		String ipAddress = request.getRemoteAddr();
		String browser = request.getHeader("User-Agent");
		String timeString = Tools.getTimeString();
		String password = getPasswordFromDB(login);
		
		String inputDigest = ipAddress.concat(browser).concat(timeString).concat(login).concat(password);
		
		String sessionId = processDigest(inputDigest,"SHA-256");
		
		if (DEBUG) {
			System.err.println("ipAddress = " + ipAddress + "\n browser = " + browser + "\n timeString = " + timeString + "\n password = " + password + "\n sessionId = " + sessionId + "\n");
		}
		
		if (sessionId != null) {
			updateUserInDB(timeString,browser,ipAddress,sessionId,login);
			createPresence(sessionId);
			return sessionId;
		}
		
		return null;
	}
	
	public ArrayList<String> processPassword(String sessionId, String password) {
		HttpServletRequest request = getThreadLocalRequest();
		String login = isSessionValid(sessionId,request,password);
		
		if (login != null) {
			ArrayList<String> list = listOfRooms(login);
			
			try {
				actionRegisterUser(login,ChatroomConstants.EXPIRES_DEFAULT,true);
			} catch (IOException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (ServletParseException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (TooManyHopsException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (ServletException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} 
			
			return list;
		} else {
		}
		
		return null;
		
	}
	
	public ArrayList<String> getUser(String sessionId) {
		Statement DBstatement;
		ResultSet rs = null; // result set object
		ArrayList<String> result = new ArrayList<String>();
		
		HttpServletRequest request = getThreadLocalRequest();
		
		if (isSessionValid(sessionId,request,null) != null) {
			
			String time = getDate();
			
			result.add(time);
			
			DBstatement = tools.getDBStatement();
			
			try {
				if(DBstatement.execute("select public_entity from users where sessionId='" + sessionId + "';")) {
					rs = DBstatement.getResultSet();
					
					while (rs.next()) {	
						result.add(rs.getString(1));
					} 
					rs.close();
					rs = null;
				} else {
				}
			} catch (SQLException e) {
				System.err.println("SQL Exception:" + e.getMessage());
			} finally {
				tools.freeDBConnection(DBstatement);
			}
			
			return result;
		}
		
		return null;
		
	}
	
	public AppliEvents retrieveEvents(String sessionId) {
		Statement DBstatement;
		ResultSet rs1 = null; // result set object
		ResultSet rs2 = null; // result set object
		ResultSet rs3 = null; // result set object
		ArrayList<BuddyData> buddies = new ArrayList<BuddyData>();
		ArrayList<String[]> messages = new ArrayList<String[]>();
		ArrayList<SipEvent> sipEvents = new ArrayList<SipEvent>();
		
		AppliEvents result = new AppliEvents();
		
		HttpServletRequest request = getThreadLocalRequest();
		
		String login = isSessionValid(sessionId,request,null);
		
		if (login != null) {
			
			DBstatement = tools.getDBStatement();
			
			try {
				if(DBstatement.execute("select id,message,fromUser from messages where subscriber='" + login + "' and isRead='0';")) {
					rs1 = DBstatement.getResultSet();
					
					while (rs1.next()) {
						String[] message = new String[2];
						message[0] = rs1.getString(3);
						message[1] = rs1.getString(2);
						messages.add(message);
						updateMessageStatus(rs1.getInt(1));
					} 
					rs1.close();
					rs1 = null;
				} else {
				}
				
				if(DBstatement.execute("select public_entity,sum(online),room from sessions where room=any (select room from sessions where subscriber='" + login + "') group by public_entity,room;")) {
					rs2 = DBstatement.getResultSet();
					
					while (rs2.next()) {	
						BuddyData buddy = new BuddyData();
						buddy.name = rs2.getString(1);
						buddy.status = rs2.getInt(2);
						buddy.chatroom = rs2.getString(3);
						buddies.add(buddy);
					} 
					rs2.close();
					rs2 = null;
				} else {
				}
				
				if(DBstatement.execute("select * from events where subscriber = '" + login + "' and incoming = '1';")) {
					rs3 = DBstatement.getResultSet();
					
					while (rs3.next()) {	
						SipEvent event = new SipEvent();
						event.method = rs3.getString(2);
						event.callId = rs3.getString(3);
						event.time = rs3.getString(4);
						event.subscriber = rs3.getString(5);
						event.domain = rs3.getString(6);
						event.fromUser = rs3.getString(7);
						event.fromDomain = rs3.getString(8);
						event.ipAdress = rs3.getString(9);
						event.audioPort = rs3.getInt(10);
						event.codec = rs3.getString(11);
						event.incoming = rs3.getInt(12);
						event.status = rs3.getInt(13);
						sipEvents.add(event);
						deleteEvent(event);
					} 
					rs3.close();
					rs3 = null;
				} else {
				}
			} catch (SQLException e) {
				System.err.println("SQL Exception:" + e.getMessage());
			} finally {
				if (rs1 != null) {
					try { rs1.close(); } catch (SQLException e) { ; }
					rs1 = null;
				}
				if (rs2 != null) {
					try { rs2.close(); } catch (SQLException e) { ; }
					rs2 = null;
				}
				if (rs3 != null) {
					try { rs3.close(); } catch (SQLException e) { ; }
					rs3 = null;
				}
				
				tools.freeDBConnection(DBstatement);
			}
			
			updatePresence(sessionId);
			
			result.buddies = buddies;
			result.messages = messages;
			result.sipEvents = sipEvents;
			
			return result;
		}
		
		return null;
	}
	
	public void registerUser(String sessionId) {
		HttpServletRequest request = getThreadLocalRequest();
		String login = isSessionValid(sessionId,request,null);
		
		if (login != null) {
			
			try {
				actionRegisterUser(login,ChatroomConstants.EXPIRES_DEFAULT,false);
			} catch (IOException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (ServletParseException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (TooManyHopsException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (ServletException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} 
			
		} else {
		}
		
	}
	
	public void unregisterUser(String sessionId) {
		HttpServletRequest request = getThreadLocalRequest();
		String login = isSessionValid(sessionId,request,null);
		
		if (login != null) {
			updateUserInDB("","","","",login);
			deletePresence(sessionId);
			eraseMessages(login);
			
			try {
				actionRegisterUser(login,ChatroomConstants.EXPIRES_NULL,false);
			} catch (IOException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (ServletParseException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (TooManyHopsException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} catch (ServletException e) {
				logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
			} 
			
		} else {
		}
		
	}
	
	public void sendMessage(String sessionId, String destId, String message) {
		HttpServletRequest request = getThreadLocalRequest();
		
		String login = isSessionValid(sessionId,request,null);
		
		if (login != null) {
			if (isInRoomsList(destId)) {
				try {
					SipServletRequest messageRequest = requestGenerator.generateMessage(login,destId,ChatroomConstants.BOND_DOMAIN,message);
					messageRequest.send();
				} catch (IOException e) {
					logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
				} catch (ServletParseException e) {
					logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
				} catch (ServletException e) {
					logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
				} 
			} else {
				try {
					forkMessage(login,destId,ChatroomConstants.BOND_DOMAIN,message);
				} catch (ServletException e) {
					logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
				} 
			}
		}
	}
	
	public ArrayList<BuddyData> getHistoryMonths(String roomId, String sessionId) {
		Statement DBstatement;
		ResultSet rs = null; // result set object
		ArrayList<BuddyData> buddies = new ArrayList<BuddyData>();
		
		HttpServletRequest request = getThreadLocalRequest();
		
		if (isSessionValid(sessionId,request,null) != null) {
			
			DBstatement = tools.getDBStatement();
			
			try {
				if(DBstatement.execute("select distinct(month),year from history where chatroom='" + roomId + "' and year=any (select distinct(year) from history);")) {
					rs = DBstatement.getResultSet();
					
					while (rs.next()) {	
						BuddyData buddy = new BuddyData();
						buddy.name = Tools.adaptTime(rs.getString(2),4) + " / "  + Tools.adaptTime(rs.getString(1),2);
						buddy.status = -1;
						buddy.year = rs.getInt(2);
						buddy.month = rs.getInt(1);
						buddies.add(buddy);
					} 
					rs.close();
					rs = null;
				} else {
				}
			} catch (SQLException e) {
				System.err.println("SQL Exception:" + e.getMessage());
			} finally {
				tools.freeDBConnection(DBstatement);
			}
			
			return buddies;
		}
		
		return null;
		
	}
	
	public ArrayList<String[]> getHistoryMessages(String sessionId, String roomId, int month, int year) {
		Statement DBstatement;
		ResultSet rs = null; // result set object
		ArrayList<String[]> messages = new ArrayList<String[]>();
		String mysqlRequest = null;
		
		HttpServletRequest request = getThreadLocalRequest();
		
		String login = isSessionValid(sessionId,request,null);
		
		if (login != null) {
			
			DBstatement = tools.getDBStatement();
			
			try {
				mysqlRequest = "select t1.time,t2.public_entity,t1.message from history as t1, users as t2 where chatroom='" + roomId + "' and month='" + month + "' and year='" + year + "' and t1.sender = t2.subscriber order by time desc;";
				if(DBstatement.execute(mysqlRequest)) {
					rs = DBstatement.getResultSet();
					
					if (DEBUG) {
						System.err.println("GetHistoryMessages - SQL Request: " + mysqlRequest);	
					}
					
					while (rs.next()) {
						String[] message = new String[3];
						message[0] = rs.getString(1);
						message[1] = rs.getString(2);
						message[2] = rs.getString(3);
						messages.add(message);
					} 
					rs.close();
					rs = null;
				} else {
				}
			} catch (SQLException e) {
				logger.info("WebServices getHistoryMessages SQL Exception:" + e.getMessage());
			} finally {
				tools.freeDBConnection(DBstatement);
			}
			
			return messages;
		}
		
		return null;
		
	}
	
	public void processCall(String sessionId, fr.free.hd.bond.chatroom.client.SipCall call, String method, int status) {
		HttpServletRequest request = getThreadLocalRequest();
		
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		String login = isSessionValid(sessionId,request,null);
		
		String codec = "";
		String ipAddress = "";
		int audioPort = 0;
		
		if ((status == SipServletResponse.SC_OK) && (method.indexOf(ChatroomConstants.RESPONSE_INVITE) > -1)) {
			codec = AudioFormat.DVI_RTP;
			ipAddress = tools.getIpAddress(call.subscriber,call.domain);
			audioPort = ChatroomConstants.AUDIO_PORT;
		}
		
		if (login != null) {
			
			DBconnection = tools.getDBConnection();
						
			PreparedStatement DBpreparedStatement = null;
			
			try {
				DBpreparedStatement = DBconnection.prepareStatement("insert into events(method,callId,time,subscriber,domain,fromUser,fromDomain,ipAdress,audioPort,codec,incoming,status) values(?,?,?,?,?,?,?,?,?,?,?,?);");
				DBpreparedStatement.setString(1,method);
				DBpreparedStatement.setString(2,call.callId);
				DBpreparedStatement.setString(3,call.time);
				DBpreparedStatement.setString(4,call.subscriber);
				DBpreparedStatement.setString(5,call.domain);
				DBpreparedStatement.setString(6,call.fromUser);
				DBpreparedStatement.setString(7,call.fromDomain);
				DBpreparedStatement.setString(8, ipAddress);
				DBpreparedStatement.setInt(9,audioPort);
				DBpreparedStatement.setString(10,codec);
				DBpreparedStatement.setInt(11,0);
				DBpreparedStatement.setInt(12,status);
				
				DBpreparedStatement.execute();
			} catch (SQLException e) {
				System.err.println("SQL Exception in acceptCall: " + e.getMessage());
			} finally {
				tools.freeDBConnection(DBpreparedStatement);
			}
			
			try {
				if (method.indexOf(ChatroomConstants.BYE_CALL) > -1) {
					SipServletRequest publish = requestGenerator.generatePublish(call.subscriber,call.domain,ChatroomConstants.PRESENCE_ONLINE);
					publish.send();
				}
			} catch (ServletParseException e) {
				System.err.println("Error in ProcessCall: " + e.getMessage());
			} catch (IOException e) {
				System.err.println("Error in ProcessCall: " + e.getMessage());
			} catch (ServletException e) {
				System.err.println("WebServicesImpl ProcessCall: " + e.getMessage());
			} 
		}
	}
	
	public String call(String sessionId, String who) {
		HttpServletRequest request = getThreadLocalRequest();
		
		ArrayList<HashMap<String,String>> identities = new ArrayList<HashMap<String,String>>();
		ArrayList<SipServletRequest> invitesList = new ArrayList<SipServletRequest>();
		
		String login = isSessionValid(sessionId,request,null);
		
		if (login != null) {
			
			try {
				
				SipApplicationSession applicationSession;
				applicationSession = factory.createApplicationSession();
				
				identities = tools.getAllConnectedIdentities(who);
				for (int i=0,N=identities.size();i<N;i++) {
					SipEvent sipEvent = new SipEvent();
					sipEvent.method = ChatroomConstants.INVITE_CALL;
					sipEvent.fromUser = login;
					sipEvent.fromDomain = ChatroomConstants.BOND_DOMAIN;
					sipEvent.subscriber = ((HashMap<String,String>)identities.get(i)).get(ChatroomConstants.USER);
					sipEvent.domain = ((HashMap<String,String>)identities.get(i)).get(ChatroomConstants.DOMAIN);
					sipEvent.codec = ChatroomConstants.AUDIO_CODEC;
					sipEvent.ipAdress = tools.getIpAddress(login,ChatroomConstants.BOND_DOMAIN);
					sipEvent.audioPort = ChatroomConstants.AUDIO_PORT;
					
					SipServletRequest inviteRequest = requestGenerator.generateInvite(sipEvent,applicationSession);
					invitesList.add(inviteRequest);
					inviteRequest.send();
					
				}
				
				
				CallHandler callHandler = new CallHandler(factory);
				applicationSession.setAttribute(ChatroomConstants.CALLER_HANDLER,callHandler);
				applicationSession.setAttribute(ChatroomConstants.SESSIONS_LIST,invitesList);
				applicationSession.setAttribute(ChatroomConstants.CALLER_ID,login);
				
				callHandler.handleOutgoingCall(applicationSession);
				
				SipServletRequest publish = requestGenerator.generatePublish(login,ChatroomConstants.BOND_DOMAIN,ChatroomConstants.PRESENCE_BUSY);
				publish.send();
				
				return applicationSession.getId();
				
			} catch (ServletParseException e) {
				System.err.println("Error in CALL: " + e.getMessage());
			} catch (IOException e) {
				System.err.println("Error in CALL: " + e.getMessage());
			} catch (ServletException e) {
				System.err.println("Error in CALL: " + e.getMessage());
			} 
			
		}
		
		return null;
	}
	
	public String setPresence(String sessionId, String presence) {
		HttpServletRequest request = getThreadLocalRequest();
		String login = isSessionValid(sessionId,request,null);
		
		SipServletRequest publish;
		
		if (login != null) {
			
			try {
				publish = requestGenerator.generatePublish(login,ChatroomConstants.BOND_DOMAIN,presence);
				publish.send();
			} catch (ServletParseException e) {
				System.err.println("Error when running setPresence: " + e.getMessage());
			} catch (IOException e) {
				System.err.println("Error when running setPresence: " + e.getMessage());
			} catch (ServletException e) {
				System.err.println("Error when running setPresence: " + e.getMessage());
			}
		}
		
		return presence;
	}
	
	public void cancelCall(String sessionId, String applicationSessionId) {
		HttpServletRequest request = getThreadLocalRequest();
		
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		String login = isSessionValid(sessionId,request,null);
		
		if (login != null) {
			
			DBconnection = tools.getDBConnection();
			
			PreparedStatement DBpreparedStatement = null;
			
			try {
				DBpreparedStatement = DBconnection.prepareStatement("insert into events(method,callId,subscriber,incoming) values(?,?,?);");
				DBpreparedStatement.setString(1,ChatroomConstants.CANCEL_CALL);
				DBpreparedStatement.setString(2,applicationSessionId);
				DBpreparedStatement.setString(3,login);
				DBpreparedStatement.setInt(4,0);
				
				DBpreparedStatement.execute();
			} catch (SQLException e) {
				System.err.println("SQL Exception in cancelCall: " + e.getMessage());
			} finally {
				tools.freeDBConnection(DBpreparedStatement);
			}
			
			try {
				SipServletRequest publish = requestGenerator.generatePublish(login,ChatroomConstants.BOND_DOMAIN,ChatroomConstants.PRESENCE_ONLINE);
				publish.send();
			} catch (ServletParseException e) {
				System.err.println("Error in ProcessCall: " + e.getMessage());
			} catch (IOException e) {
				System.err.println("Error in ProcessCall: " + e.getMessage());
			} catch (ServletException e) {
				System.err.println("WebServicesImpl ProcessCall: " + e.getMessage());
			} 
		}
	}
	
	private String processDigest(String input, String algo) {
		try {
			MessageDigest sha = MessageDigest.getInstance(algo);
			
			byte[] data1 = input.getBytes();
			StringBuffer hexString = new StringBuffer();
			
			sha.update(data1);
			byte[] msgDigest = sha.digest();
			
			for(int i=0; i<msgDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & msgDigest[i]);
				if (hex.length() == 1)
				{
					hexString.append('0');
				}
				hexString.append(hex);
			}
			
			String result = hexString.toString();
			return result;
			
		} catch(NoSuchAlgorithmException e) {
			//System.out.println(e.getMessage());
		}
		
		return null;
	}
	
	private String getPasswordFromDB(String login) {
		Statement DBstatement;
		ResultSet rs = null; // result set object
		String result = null;
		
		DBstatement = tools.getDBStatement();
		
		try {
			if(DBstatement.execute("select password from users where subscriber='" + login + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {	
					result = rs.getString(1);
				} 
				rs.close();
				rs = null;
			} else {
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
		return result;
	}
	
	private ArrayList<String> getInputsFromDB(String sessionId) {
		Statement DBstatement;
		ResultSet rs = null; // result set object
		ArrayList<String> result = new ArrayList<String>();
		
		DBstatement = tools.getDBStatement();
		
		String mysqlRequest = "select time,subscriber,password from users where sessionId='" + sessionId + "';";
		
		if (DEBUG) {
			System.err.println("GetInputsFrom DB: " + mysqlRequest);
			System.err.println("GetInputsFromDB sessionId = " + sessionId);
		}
		
		try {
			if(DBstatement.execute(mysqlRequest)) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {	
					result.add(rs.getString(1));
					result.add(rs.getString(2));
					result.add(rs.getString(3));
				} 
				rs.close();
				rs = null;
			} else {
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
		return result;
	}
	
	private ArrayList<String> listOfRooms(String login) {
		Statement DBstatement;
		ResultSet rs = null; // result set object
		ArrayList<String> result = new ArrayList<String>();
		
		DBstatement = tools.getDBStatement();
		
		try {
			if(DBstatement.execute("select room from sessions where subscriber='" + login + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {	
					result.add(rs.getString(1));
				} 
				rs.close();
				rs = null;
			} else {
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
		
		return result;
	}
	
	private String isSessionValid(String sessionId, HttpServletRequest request, String password) {
		String login = null;
		String passwordComputed = null;
		ArrayList<String> inputs = new ArrayList<String>();
		
		if (sessionId != null) {
			inputs = getInputsFromDB(sessionId);
			
			int counter = 0;
			while ((inputs.size() != 3) && (counter < 5)) {
				inputs = getInputsFromDB(sessionId);
				counter++;
			}
			
			if (DEBUG) {
				System.err.println("isSessionValid sessionId = " + sessionId);
			}
			
			if (inputs != null) {
				login = inputs.get(1);
				String timeString = inputs.get(0);
				if (password != null) {
					passwordComputed = password;
				} else {
					passwordComputed = inputs.get(2);
				}
				
				String ipAddress = request.getRemoteAddr();
				String browser = request.getHeader("User-Agent");
				
				String inputDigest = ipAddress.concat(browser).concat(timeString).concat(login).concat(passwordComputed);
				
				String sessionIdCompute = processDigest(inputDigest,"SHA-256");
				
				if (sessionId.compareTo(sessionIdCompute) == 0) {
					return login;
				}
			}
		}
		
		return null;
		
	}
	
	private String getDate() {
		Calendar calendar = Tools.getCalendar();
		
		int day = calendar.get(Calendar.DATE);
		String dayString = "";
		if (day < 10) {
			dayString = "0" + day;
		} else {
			dayString = "" + day;
		}
		int month = calendar.get(Calendar.MONTH) + 1;
		String monthString = "";
		if (month < 10) {
			monthString = "0" + month;
		} else {
			monthString = "" + month;
		}
		
		int year = calendar.get(Calendar.YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		String hourString = "";
		if (hour < 10) {
			hourString = "0" + hour;
		} else {
			hourString = "" + hour;
		}
		int minute = calendar.get(Calendar.MINUTE);
		String minuteString = "";
		if (minute < 10) {
			minuteString = "0" + minute;
		} else {
			minuteString = "" + minute;
		}
		
		String time = hourString + ":" + minuteString;
		String date = dayString + "-" + monthString + "-" + year;
		
		String result = date + " " + time;
		
		return result;
	}
	
	private void updateUserInDB(String timeString, String browser, String ipAddress, String sessionId, String login) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		DBconnection = tools.getDBConnection();
		
		PreparedStatement DBpreparedStatement = null;
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("update users set time=?, browser=?, ipAddress=?, sessionId=? where subscriber=? ;");
			DBpreparedStatement.setString(1,timeString);
			DBpreparedStatement.setString(2,browser);
			DBpreparedStatement.setString(3,ipAddress);
			DBpreparedStatement.setString(4,sessionId);
			DBpreparedStatement.setString(5,login);
			
			DBpreparedStatement.execute();
			
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
	}
	
	private void updatePresence(String sessionId) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		String timeString = Tools.getTimeString();
		
		if (DEBUG) {
			System.err.println("UpdatePresence = " + sessionId + " / " + timeString);
		}
		
		DBconnection = tools.getDBConnection();
		
		PreparedStatement DBpreparedStatement = null;
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("update presence set lastUpdate=?,flag=0 where sessionId=? ;");
			DBpreparedStatement.setString(1,timeString);
			DBpreparedStatement.setString(2,sessionId);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
	}
	
	private void updateMessageStatus(int messageId) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		DBconnection = tools.getDBConnection();
		
		PreparedStatement DBpreparedStatement = null;
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("update messages set isRead=? where id=? ;");
			DBpreparedStatement.setInt(1,1);
			DBpreparedStatement.setInt(2,messageId);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
	}
	
	private void createPresence(String sessionId) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		String timeString = Tools.getTimeString();
		
		DBconnection = tools.getDBConnection();
		
		PreparedStatement DBpreparedStatement = null;
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("insert into presence(sessionId,lastUpdate,flag) values(?,?,?);");
			DBpreparedStatement.setString(1,sessionId);
			DBpreparedStatement.setString(2,timeString);
			DBpreparedStatement.setInt(3,0);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
	}
	
	private void deletePresence(String sessionId) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		DBconnection = tools.getDBConnection();
		
		PreparedStatement DBpreparedStatement = null;
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("delete from presence where sessionId=?;");
			DBpreparedStatement.setString(1,sessionId);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
	}
	
	private void actionRegisterUser(String user, int expires, boolean initial) throws IOException, ServletParseException, TooManyHopsException, ServletException {
		SipServletRequest register = requestGenerator.generateRegister(user,ChatroomConstants.BOND_DOMAIN,expires);
		
		SipServletRequest publish;
		if ((expires == ChatroomConstants.EXPIRES_DEFAULT) && initial) {
			publish = requestGenerator.generatePublish(user,ChatroomConstants.BOND_DOMAIN,ChatroomConstants.PRESENCE_ONLINE);
			publish.send();
		} else if (expires == ChatroomConstants.EXPIRES_NULL) {
			publish = requestGenerator.generatePublish(user,ChatroomConstants.BOND_DOMAIN,ChatroomConstants.PRESENCE_OFFLINE);
			publish.send();
		}
		
		register.send();
		
		
	}
	
	private void eraseMessages(String login) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		DBconnection = tools.getDBConnection();
	
		PreparedStatement DBpreparedStatement = null;
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("delete from messages where subscriber=?;");
			DBpreparedStatement.setString(1,login);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
		
	}
	
	private boolean isInRoomsList(String roomId) {
		Statement DBstatement;
		ResultSet rs = null;
		
		DBstatement = tools.getDBStatement();
		
		try {
			if(DBstatement.execute("select room from rooms;")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					if (roomId.compareTo(rs.getString(1)) == 0) {
						return true;
					}	
				} 
				rs.close();
				rs = null;
				
			} else {
			}
			
		} catch (SQLException e) {
			logger.info("UACSIPSERVLET init SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
		return false;
	}
	
	private void forkMessage(String from, String destPublic, String domain, String message) throws ServletException {
		Statement DBstatement;
		ResultSet rs = null;
		String result = null;
		
		DBstatement = tools.getDBStatement();
		
		try {
			if(DBstatement.execute("select subscriber,domain from users where public_entity = '" + destPublic + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					try {
						SipServletRequest messageRequest = requestGenerator.generateMessage(from,rs.getString(1),rs.getString(2),message);
						messageRequest.send();
					} catch (IOException e) {
						logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
					} catch (ServletParseException e) {
						logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
					} 
					
				} 
				rs.close();
				rs = null;
				
			} else {
			}
			
		} catch (SQLException e) {
			logger.info("UACSIPSERVLET init SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
	}
	
	private void deleteEvent(fr.free.hd.bond.chatroom.client.SipEvent sipEvent) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		PreparedStatement DBpreparedStatement = null;
		
		DBconnection = tools.getDBConnection();
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("delete from events where callId=? and subscriber=? and method=? and time=?;");
			DBpreparedStatement.setString(1,sipEvent.callId);
			DBpreparedStatement.setString(2,sipEvent.subscriber);
			DBpreparedStatement.setString(3,sipEvent.method);
			DBpreparedStatement.setString(4,sipEvent.time);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
	}
	
}
