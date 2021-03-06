
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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.sdp.SessionDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.Origin;
//import javax.sdp.Connection;
import javax.sdp.SdpConstants;
import javax.sdp.MediaDescription;
import javax.sdp.Media;

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

import fr.free.hd.bond.chatroom.client.SipEvent;

public class RoomsRegistrator {
	private static boolean DEBUG = false;
	
	private SipFactory factory;
	
	private static Logger logger;
	
	private ArrayList<String> roomsList;
	
	private SipResponseGenerator responseGenerator;
	
	private SipRequestGenerator requestGenerator;
	
	private Tools tools;
	
	private DocumentBuilder pidfDocument;
	
	private HashMap<String,Timer> timerList;
	
	private class SendRequestTimerTask extends TimerTask {
		private SipServletRequest request;
		
		private boolean oneShot = true;
		
		public void init(SipServletRequest request) {
			this.request = request;
		}
		
		public void init(SipServletRequest request, boolean oneShot) {
			this.request = request;
			this.oneShot = oneShot;
		}
		
		public void run() {
			try {
				request.send();
				if (oneShot) 
					cancel();
			} catch (IOException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			} 
		}
		
	}
	
	class RoomPeriodicRegistrator {
		private String room;
		
		public RoomPeriodicRegistrator(String room) {
			this.room = room;
		}
		
		public void start() {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
					public void run() {
						register();
					}
			},0,ChatroomConstants.REGISTER_RESEND_PERIOD);
			
			timerList.put(room,timer);
		}
		
		private void register() {
			try {
				SipServletRequest register = requestGenerator.generateRegister(room,ChatroomConstants.BOND_DOMAIN,ChatroomConstants.EXPIRES_DEFAULT);
				register.send();
			} catch (IOException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			} catch (ServletParseException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			}  catch (ServletException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			} 
		}
		
	}
	
	class RoomPeriodicPublisher {
		private String room;
		
		public RoomPeriodicPublisher(String room) {
			this.room = room;
		}
		
		public void start() {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
					public void run() {
						publish();
					}
			},0,ChatroomConstants.RESEND_PERIOD);
			
			timerList.put("PUBLISH/" + room,timer);
		}
		
		private void publish() {
			try {
				SipServletRequest publish = requestGenerator.generatePublish(room,ChatroomConstants.BOND_DOMAIN,ChatroomConstants.PRESENCE_ONLINE);
				publish.send();
			} catch (IOException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			} catch (ServletParseException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			}  catch (ServletException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			} 
		}
		
	}
	
	public RoomsRegistrator(SipFactory factory) throws ServletException {
		logger = Logger.getLogger(RoomsRegistrator.class.getName());
		this.factory = factory;
		
		tools = new Tools(this.factory);
		
		timerList = new HashMap<String,Timer>();
		
		responseGenerator = new SipResponseGenerator(factory);
		requestGenerator = new SipRequestGenerator(factory);
		
		try {
			pidfDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Error when creating pidfDocument: " + e.getMessage());
		}
	}
	
	public void start() {
		Timer timerRegister = new Timer();
		timerRegister.schedule(new TimerTask() {
				public void run() {
					
					for (int i=0,N=roomsList.size();i<N;i++) {
						try {
							initRoomUAC(roomsList.get(i));
						} catch (ServletException e) {
							System.err.println("Init UACSipServlet ServletException: " + e.getMessage());
						}
					}
				}
		}, 10000); 
	}
	
	public void stop() {
		Iterator timerIterator = timerList.values().iterator();
		while (timerIterator.hasNext()) {
			((Timer)timerIterator.next()).cancel();
		}
		
		Statement DBstatement;
		ResultSet rs = null;
		
		DBstatement = tools.getDBStatement();
		
		try {
			if(DBstatement.execute("select room from rooms;")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					try {
						destroyRoomUAC(rs.getString(1));
					} catch (ServletException e) {
						System.err.println("Destroy UACSipServlet ServletException: " + e.getMessage());
					}	
					
				} 
				rs.close();
				rs = null;
			} else {
			}
		} catch (SQLException e) {
			logger.info("CHATROOMSIPSERVLET roomsRegister SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
	}
	
	private void initRoomUAC(String room) throws ServletException {
		try {
			actionRegisterRoom(room,ChatroomConstants.EXPIRES_DEFAULT);
			actionPublishRoomPresence(room,ChatroomConstants.PRESENCE_ONLINE);
			actionSubscribeToUsers(room,ChatroomConstants.EXPIRES_DEFAULT);
		} catch (IOException e) {
			logger.info("CHATROOMSIPSERVLET init IOException: " + e.getMessage());
		} catch (ServletParseException e) {
			logger.info("CHATROOMSIPSERVLET init ServletParseException: " + e.getMessage());
		} catch (TooManyHopsException e) {
			logger.info("CHATROOMSIPSERVLET init TooManyHopsException: " + e.getMessage());
		} 
		
	}
	
	private void destroyRoomUAC(String room) throws ServletException {
		try {
			actionSubscribeToUsers(room,ChatroomConstants.EXPIRES_NULL);
			actionPublishRoomPresence(room,ChatroomConstants.PRESENCE_OFFLINE);	
			actionRegisterRoom(room,ChatroomConstants.EXPIRES_NULL);
			
		} catch (IOException e) {
			logger.info("CHATROOMSIPSERVLET init IOException: " + e.getMessage());
		} catch (ServletParseException e) {
			logger.info("CHATROOMSIPSERVLET init ServletParseException: " + e.getMessage());
		} catch (TooManyHopsException e) {
			logger.info("CHATROOMSIPSERVLET init TooManyHopsException: " + e.getMessage());
		} 
		
	}
	
	private void actionRegisterRoom(String room, int expires) throws IOException, ServletParseException, TooManyHopsException, ServletException {
		if (DEBUG) {
			logger.info("Entering actionRegisterRoom");
		}
		
		if (expires > 0) {
			RoomPeriodicRegistrator periodicRegistrator = new RoomPeriodicRegistrator(room);
			periodicRegistrator.start();
		}
		
	}
	
	private void actionPublishRoomPresence(String room, String presence) throws IOException, ServletParseException, TooManyHopsException, ServletException {
		if (DEBUG) {
			logger.info("Entering actionRegisterRoom");
		}
		
		RoomPeriodicPublisher periodicPublisher = new RoomPeriodicPublisher(room);
		periodicPublisher.start();
		
    	        
	}
	
	private void actionSubscribeToUsers(String room, int expires) throws IOException, ServletParseException, TooManyHopsException, ServletException {
		Statement DBstatement;
		ResultSet rs = null; // result set object
		
		DBstatement = tools.getDBStatement();
		
		ArrayList<String> usersList = new ArrayList<String>();
		ArrayList<String> domainsList = new ArrayList<String>();
		
		try {
			if(DBstatement.execute("select subscriber,domain from sessions where room='" + room + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					
					if (DEBUG) {
						logger.info("UACSipServlet actionSubscribeToUsers :" + rs.getString(1) + "/" + rs.getString(2));
					}
					
					usersList.add(rs.getString(1));
					domainsList.add(rs.getString(2));
				} 
				rs.close();
				rs=null;
			} else {
			}
		} catch (SQLException e) {
			logger.info("UACSIPSERVLET SubscribeToUsers SQL Exception:" + room + "/" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
		for (int i=0,N=usersList.size();i<N;i++) {
			SipServletRequest subscribe = requestGenerator.generateSubscribe(room,ChatroomConstants.BOND_DOMAIN,usersList.get(i),domainsList.get(i),expires,ChatroomConstants.PRESENCE_EVENT);
			subscribe.send();
		}
	}
	
	private void updateUserRegistration(String username, String domain, String room, int online) {
		Statement DBstatement;
		if (DEBUG) { logger.info("UACSIPSERVLET INFO: registerUser(): " + username + "/" + domain + "/" + room + "/" + online);}
		
		DBstatement = tools.getDBStatement();
		
		String mySQLRequest = null;
		
		mySQLRequest = "update sessions set online='" + online + "' where subscriber='" + username + "' and domain='" + domain + "' and room='" + room + "';";
		
		if (DEBUG) { logger.info("UACSIPSERVLET INFO: SQL request = " + mySQLRequest);}
		
		try {
			DBstatement.execute(mySQLRequest);
			if (DEBUG) { logger.info("UACSIPSERVLET INFO: SQLRequest OK");}
			
		} catch (SQLException e) {
			logger.info("CHATROOMSIPSERVLET INFO: SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
	}
	
	private int isPresent(String notifyContent) {
		int result = 0;
		String presenceValue = null;
		Document doc;
		
		try {
			doc = parserXML(notifyContent);
			NodeList list = doc.getElementsByTagName("note").item(0).getChildNodes();
			if (list != null) {
				presenceValue = list.item(0).getNodeValue();
			}
			
			if (DEBUG) {
				logger.info("UACSipServlet isPresent :" + presenceValue + "/" + notifyContent);
			}
			
			if (presenceValue != null) {
				if (ChatroomConstants.PRESENCE_ONLINE.indexOf(presenceValue) > -1) {
					result = 1;
				} else if (ChatroomConstants.PRESENCE_AWAY.indexOf(presenceValue) > -1) {
					result = 1;
				} else if (presenceValue.indexOf(ChatroomConstants.PRESENCE_BUSY) > -1) {
					result = -1000;
				} else {
					result = 0;
				}
			} else {
				result = 0;
			}
			
			
		} catch (SAXParseException err) {
			logger.info ("UACServlet doNotify Parsing error" + ", line " + err.getLineNumber() + ", uri " + 	err.getSystemId () + " " + err.getMessage());
		} catch (SAXException e) {
			Exception x = e.getException ();
			logger.info ("UACServlet doNotify: " + e.getMessage());
		} catch (Throwable t) {
		}
		
		return result;
	}
	
	private Document parserXML(String xmlString) throws SAXException, IOException, ParserConfigurationException {
		return pidfDocument.parse(new InputSource(new StringReader(xmlString)));
	}
	
	public void updateRoomsList(ArrayList<String> roomsList) {
		this.roomsList = roomsList;
	}
	
}
