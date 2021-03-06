
package fr.free.hd.bond.chatroom.ServiceServlets;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;

public class RoomSipServlet extends SipServlet {
	class MessageHandler {
		private String MESSAGE_TIMEOUT = "MESSAGE_TIMEOUT_TIMER";
		
		public void handleChatRoomMessage(String chatroom, String user, String domain, String message) throws ServletException {
			if(message.equalsIgnoreCase("/who")) {
				String users = "<b>List of users:</b><br>";
				ArrayList<String> usersList = getOnlinePublicEntitiesList(chatroom);
				
				for (int i=0,N=usersList.size();i<N;i++) {
					users = users + usersList.get(i) + "\n";
				}
				
				try {        
					sendToUser(chatroom, user,domain,users);
				} catch (IOException e) {
					logger.info("CHATROOMSIPSERVLET handleChatRoomMessage IOException: " + e.getMessage());
				} catch (ServletParseException e) {
					logger.info("CHATROOMSIPSERVLET handleChatRoomMessage ServletParseException: " + e.getMessage());
				} 
				return;
			}
			
			try {
				saveInHistory(chatroom, user, domain, message);
				String destMessage = prepareChatMessage(user, domain, message,"chatroom-message-normal");
				sendToAll(chatroom, destMessage);
			} catch (IOException e) {
				logger.info("CHATROOMSIPSERVLET handleChatRoomMessage IOException: " + e.getMessage());
			} catch (ServletParseException e) {
				logger.info("CHATROOMSIPSERVLET handleChatRoomMessage ServletParseException: " + e.getMessage());
			} 
		}
		
		public void handlePeerMessage(String destUser, String destDomain, String user, String domain, String message) {
			Connection DBconnection;
			ResultSet rs = null; // result set object
			
			String timeString = Tools.getTimeString();
			String fromUser = null;
			
			if (!tools.isInRoomsList(user,roomsList)) {
				fromUser = tools.getPublicId(user,domain);
			} else {
				fromUser = user;
			}
			
			DBconnection = tools.getDBConnection();
			
			PreparedStatement DBpreparedStatement = null;
			
			try {
				DBpreparedStatement = DBconnection.prepareStatement("insert into messages(subscriber,domain,time,message,fromUser,fromDomain,isRead) values(?,?,?,?,?,?,?);");
				DBpreparedStatement.setString(1,destUser);
				DBpreparedStatement.setString(2,destDomain);
				DBpreparedStatement.setString(3,timeString);
				DBpreparedStatement.setString(4,message);
				DBpreparedStatement.setString(5,fromUser);
				DBpreparedStatement.setString(6,domain);
				DBpreparedStatement.setInt(7,0);
				
				DBpreparedStatement.execute();
			} catch (SQLException e) {
				System.err.println("SQL Exception:" + e.getMessage());
			} finally {
				tools.freeDBConnection(DBpreparedStatement);
			}
			
		}
		
		public boolean isApplicationMessage(String contentType) {
			if (contentType.indexOf("application") > -1) {
				return true;
			} else {
				return false;
			}
		}
		
		private void sendToUser(String chatroom, String user, String domain, String message) throws ServletParseException, IOException, ServletException {
			SipServletRequest messageRequest = requestGenerator.generateMessage(chatroom,user,domain,message);
			messageRequest.send();
			
			/*
			SipApplicationSession session = messageRequest.getApplicationSession();
			ServletTimer messageTimeoutTimer = timerService.createTimer(session, 3000, false, null);
			session.setAttribute(MESSAGE_TIMEOUT,messageTimeoutTimer);
			*/
		}
		
		private void sendToAll(String chatroom, String message) throws ServletParseException, IOException, ServletException {
			ArrayList<String[]> usersList = getOnlineUsersList(chatroom);
			
			if (usersList.size() > 0) {
				
				for (int i=0,N=usersList.size();i<N;i++) {
					String destUser = (usersList.get(i))[0];
					String destDomain = (usersList.get(i))[1];
					sendToUser(chatroom,destUser,destDomain,message);		
				}
			} 
		}
		
		private void saveInHistory(String chatroom, String user, String domain, String message) {
			Connection DBconnection;
			ResultSet rs = null; // result set object
			String result = null;
			
			Calendar calendar = Calendar.getInstance();
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
			
			
			DBconnection = tools.getDBConnection();
			
			PreparedStatement DBpreparedStatement = null;
			int number = 0;
			
			try {
				DBpreparedStatement = DBconnection.prepareStatement("insert into history(time,sender,chatroom,message,domain,month,year) values (?,?,?,?,?,?,?);");
				DBpreparedStatement.setString(1,date + " : " + time);
				DBpreparedStatement.setString(2,user);
				DBpreparedStatement.setString(3,chatroom);
				DBpreparedStatement.setString(4,message);
				DBpreparedStatement.setString(5,domain);
				DBpreparedStatement.setInt(6,month);
				DBpreparedStatement.setInt(7,year);
				
				DBpreparedStatement.execute();
				
			} catch (SQLException e) {
				System.err.println("SQL Exception:" + e.getMessage());
			} finally {
				tools.freeDBConnection(DBpreparedStatement);
			}
			
		}
		
		private String prepareChatMessage(String user, String domain, String message, String stylename) throws ServletParseException, IOException {
			String userPublicId = tools.getPublicId(user,domain);
			
			String time = Tools.getTimeHumanReadable();
			
			String destMessage = "<div class='" + stylename + "'><b>(" + time + ") " + userPublicId + " a &#233;crit: </b>" + message + "</div>";
			
			return destMessage;
		}
		
		private ArrayList<String[]> getOnlineUsersList(String chatroom) {
			Statement DBstatement;
			ResultSet rs = null;
			ArrayList<String[]> usersList = new ArrayList<String[]>();
			
			DBstatement = tools.getDBStatement();
			
			try {
				if(DBstatement.execute("select subscriber,domain from sessions where room='" + chatroom + "' && online!='0';")) {
					rs = DBstatement.getResultSet();
					
					while (rs.next()) {
						String[] binome = new String[2];
						binome[0] = rs.getString(1);
						binome[1] = rs.getString(2);	
						usersList.add(binome);
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
			
			return usersList;
		}
		
		private ArrayList<String> getOnlinePublicEntitiesList(String chatroom) {
			Statement DBstatement;
			ResultSet rs = null;
			ArrayList<String> usersList = new ArrayList<String>();
			
			DBstatement = tools.getDBStatement();
			
			try {
				if(DBstatement.execute("select distinct(public_entity) from sessions where room='" + chatroom + "' && online='1';")) {
					rs = DBstatement.getResultSet();
					
					while (rs.next()) {
						usersList.add(rs.getString(1));
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
			
			return usersList;
		}
		
	}
	
	private static boolean DEBUG = false;
	
	private SipFactory factory;
	
	private String serverAddress;
	
	private String bondDomain;
	
	private static Logger logger;
	
	private Timer timerUpdateRoomsList;
	
	private RoomSipServlet.MessageHandler msgHandler;
	
	private SipRequestGenerator requestGenerator;
	
	private ArrayList<String> roomsList;
	
	private Tools tools;
	
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		serverAddress = getServletConfig().getInitParameter(ChatroomConstants.SERVER_ADDRESS);
		logger = Logger.getLogger(RoomSipServlet.class.getName());
		bondDomain = getServletConfig().getInitParameter(ChatroomConstants.BOND_DOMAIN_NAME);
		
		factory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		//timerService = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);
		
		tools = new Tools(factory);
		
		timerUpdateRoomsList = new Timer();
		timerUpdateRoomsList.schedule(new TimerTask() {
				public void run() {
					roomsList = getRoomsList();
				}
		}, 5000,30000);
		
		msgHandler = new MessageHandler();
		//responseGenerator = new SipResponseGenerator();
		requestGenerator = new SipRequestGenerator(factory);
	}
	
	public void destroy() {
		timerUpdateRoomsList.cancel(); 
		
		super.destroy();
	}
	
	protected void doMessage(SipServletRequest request) throws ServletException, IOException {
		request.createResponse(SipServletResponse.SC_OK).send();            
		
		String message = request.getContent().toString();
		String user = ((SipURI) request.getFrom().getURI()).getUser();
		String domain = ((SipURI) request.getFrom().getURI()).getHost();
		String destUser = ((SipURI) request.getTo().getURI()).getUser();
		String destDomain = ((SipURI) request.getTo().getURI()).getHost();
		
		if (tools.isInRoomsList(destUser,roomsList)) {
			if (!msgHandler.isApplicationMessage(request.getContentType())) {
				msgHandler.handleChatRoomMessage(destUser,user,domain,message);
			}
		} else {
			if (!msgHandler.isApplicationMessage(request.getContentType())) {
				msgHandler.handlePeerMessage(destUser,destDomain,user,domain,message);
			}
		}
		
		
	}
	
	private ArrayList<String> getRoomsList() {
		Statement DBstatement;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		
		DBstatement = tools.getDBStatement();
		
		try {
			if(DBstatement.execute("select room from rooms;")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {	
					list.add(rs.getString(1));
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
		
		return list;
	}
	
	private boolean isInRoomsList(String user) {
		if (DEBUG) {
			logger.info("isInRoomList RoomSipServlet: " + user);
		}
		
		int counter = 0;
		for (int i=0,N=roomsList.size();i<N;i++) {
			if (user.compareTo(roomsList.get(i)) == 0) {
				counter++;
			}
		}
		
		if (counter > 0) {
			return true;
		} else {
			return false;
		}
		
		
		
	}
	
}
