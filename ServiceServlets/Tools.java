
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

class Tools {
	private static boolean DEBUG = false;
	
	private static Logger logger;
	
	private static String TIMEZONE = "Etc/GMT";
	
	private Context DBcontext;
	
	private DataSource DBdatasource;
	
	public SdpFactory sdpFactory;
	
	private SipFactory factory;
	
	private SipRequestGenerator requestGenerator;
	
	private SipResponseGenerator responseGenerator;
	
	public Tools(SipFactory factory) {
		this.factory = factory;
		
		try {
			DBcontext = new InitialContext();
			DBdatasource = (DataSource)DBcontext.lookup("java:comp/env/jdbc/chatroom");
		} catch (NamingException e) {
			logger.info("CHATROOMSIPSERVLET INFO: SQL Error: Boom - No Context!: " + e.getMessage());
		}
		
		sdpFactory = SdpFactory.getInstance();
		
		responseGenerator = new SipResponseGenerator(factory);
		requestGenerator = new SipRequestGenerator(factory);
		
		logger = Logger.getLogger(Tools.class.getName());
	}
	
	public Tools(SipFactory factory, DataSource DBdatasource) {
		this.factory = factory;
		
		if (DBdatasource != null) {
			this.DBdatasource = DBdatasource;
		} else {
			try {
				DBcontext = new InitialContext();
				DBdatasource = (DataSource)DBcontext.lookup("java:comp/env/jdbc/chatroom");
			} catch (NamingException e) {
				logger.info("CHATROOMSIPSERVLET INFO: SQL Error: Boom - No Context!: " + e.getMessage());
			}
		}
		
		sdpFactory = SdpFactory.getInstance();
		
		responseGenerator = new SipResponseGenerator(factory);
		requestGenerator = new SipRequestGenerator(factory);
		
		logger = Logger.getLogger(Tools.class.getName());
	}
	
	public static Calendar getCalendar()
	{
		TimeZone tz = TimeZone.getTimeZone(TIMEZONE);
		Calendar calendar = Calendar.getInstance(tz,Locale.FRANCE);
		
		return calendar;
		
	}
	
	public static String getTimeHumanReadable()
	{
		Calendar calendar = getCalendar();
		
		String month = adaptTime(new Integer(calendar.get(Calendar.MONTH)).toString(),2);
		
		String day = adaptTime(new Integer(calendar.get(Calendar.DAY_OF_MONTH)).toString(),2);
		
		String hour = adaptTime(new Integer(calendar.get(Calendar.HOUR_OF_DAY)).toString(),2);
		
		String minute = adaptTime(new Integer(calendar.get(Calendar.MINUTE)).toString(),2);
		
		String result = day.concat("/" + month).concat(" - " + hour).concat(":" + minute);
		
		return result;
	}
	
	public static String adaptTime(String timeString, int size)
	{
		int delta = size - timeString.length();
		String result = timeString;
		
		for (int i=0;i<delta;i++) {
			result = "0" + result;
		}
		
		return result;
	}
	
	public static String getTimeString()
	{
		Calendar calendar = getCalendar();
		
		String year = new Integer(calendar.get(Calendar.YEAR)).toString();
		
		String month = adaptTime(new Integer(calendar.get(Calendar.MONTH)).toString(),2);
		
		String day = adaptTime(new Integer(calendar.get(Calendar.DAY_OF_MONTH)).toString(),2);
		
		String hour = adaptTime(new Integer(calendar.get(Calendar.HOUR_OF_DAY)).toString(),2);
		
		String minute = adaptTime(new Integer(calendar.get(Calendar.MINUTE)).toString(),2);
		
		String second = adaptTime(new Integer(calendar.get(Calendar.SECOND)).toString(),2);
		
		String millisecond = adaptTime(new Integer(calendar.get(Calendar.MILLISECOND)).toString(),4);
		
		String result = year.concat(month).concat(day).concat(hour).concat(minute).concat(second).concat(millisecond);
		
		return result;
	}
	
	public static byte[] getXMLPidf(String contact, String presence)
	{
		File file = new File("/mnt/server/webapps/webapps-chatroom/pidf.xml");
		BufferedReader dis = null;
		String concat = "";
		byte[] result = null;
		String currentLine;
		
		try {
			dis = new BufferedReader(new FileReader(file));
			
			while ((currentLine = dis.readLine()) != null) {
				concat = concat + currentLine.replaceAll("%contact%",contact).replaceAll("%presence%",presence);
			}
			
			dis.close();
			
		} catch (FileNotFoundException e) {
			System.err.println("UACSipServlet getXMLPidf Exception: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("UACSipServlet getXMLPidf Exception: " + e.getMessage());
		}
		
		try {
			result = concat.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.err.println("UACSipServlet getXMLPidf Exception: " + e.getMessage());
		}
		
		return result;
	}
	
	public String getIpAddress(String user, String domain) {
		Statement DBstatement;
		ResultSet rs = null;
		String result = null;
		
		DBstatement = getDBStatement();
		
		try {
			if(DBstatement.execute("select ipAddress from users where subscriber = '" + user + "' && domain='" + domain + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					result = rs.getString(1);
				} 
				rs.close();
				rs = null;
				
			} else {
			}
			
		} catch (SQLException e) {
			logger.info("UACSIPSERVLET init SQL Exception:" + e.getMessage());
		} finally {
			freeDBConnection(DBstatement);
		}
		
		return result;
	}
	
	public String getPublicId(String user, String domain) {
		Statement DBstatement;
		ResultSet rs = null;
		String result = null;
		
		DBstatement = getDBStatement();
		
		try {
			if(DBstatement.execute("select public_entity from sessions where subscriber = '" + user + "' && domain='" + domain + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					result = rs.getString(1);
				} 
				rs.close();
				rs = null;
				
			} else {
			}
			
		} catch (SQLException e) {
			logger.info("UACSIPSERVLET init SQL Exception:" + e.getMessage());
		} finally {
			freeDBConnection(DBstatement);
		}
		
		return result;
	}
	
	public boolean isUserAvailable(String user, String domain) {
		Statement DBstatement;
		ResultSet rs = null;
		int result = 0;
		
		DBstatement = getDBStatement();
		
		try {
			if(DBstatement.execute("select sum(online) from sessions where subscriber = '" + user + "' && domain='" + domain + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					result = rs.getInt(1);
				} 
				rs.close();
				rs = null;
				
			} else {
			}
			
		} catch (SQLException e) {
			logger.info("UACSIPSERVLET init SQL Exception:" + e.getMessage());
			return false;
		} finally {
			freeDBConnection(DBstatement);
		}
		
		
		if (result > 0) 
			return true;
		else
			return false;
	}
	
	public boolean isInRoomsList(String user, ArrayList<String> roomsList) {
		if (DEBUG) {
			logger.info("isInRoomList RoomSipServlet: " + user);
		}
		
		int counter = 0;
		if (roomsList != null) {
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
		
		return false;
		
	}
	
	public void processInviteInDB(String method, String user, String domain, String destUser, String destDomain, String callId, String ipAddress, SessionDescription requestSDP, int incoming, int status) {
		if (DEBUG) {
			logger.info("PROCESS-INVITE-DB for " + method);
		}
		
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		String timeString = Tools.getTimeString();
		String fromUser = null;
		String ipAddr = "";
		int audioPort = 0;
		String codec = "";
		
		if (requestSDP != null) {
			audioPort = SDPTools.getAudioPort(requestSDP);
			codec = SDPTools.getNegociatedJmfAudioCodec(requestSDP);
			ipAddr = SDPTools.getConnectionAddress(requestSDP);
		}
		
		fromUser = getPublicId(user,domain);
		if (ipAddr == null) {
			String ipAddressInDB = getIpAddress(user,domain);
			if (ipAddressInDB != null) 
				ipAddr = ipAddressInDB;
			else
				ipAddr = ipAddress;
		}
		
		DBconnection = getDBConnection();
		PreparedStatement DBpreparedStatement = null;
		
		try {
			DBpreparedStatement = DBconnection.prepareStatement("insert into events(method,callId,time,subscriber,domain,fromUser,fromDomain,ipAdress,audioPort,codec,incoming,status) values(?,?,?,?,?,?,?,?,?,?,?,?);");
			DBpreparedStatement.setString(1,method);
			DBpreparedStatement.setString(2,callId);
			DBpreparedStatement.setString(3,timeString);
			DBpreparedStatement.setString(4,destUser);
			DBpreparedStatement.setString(5,destDomain);
			DBpreparedStatement.setString(6,user);
			DBpreparedStatement.setString(7,domain);
			DBpreparedStatement.setString(8,ipAddr);
			DBpreparedStatement.setInt(9,audioPort);
			DBpreparedStatement.setString(10,codec);
			DBpreparedStatement.setInt(11,incoming);
			DBpreparedStatement.setInt(12,status);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception in processInviteInDB: " + e.getMessage());
		} finally {
			freeDBConnection(DBpreparedStatement);
		}
		
		
		
	}
	
	public Statement getDBStatement() {
		Connection DBconnection;
		Statement DBstatement;
		
		try {
			DBconnection = DBdatasource.getConnection();
			/*	String userName = "mysql";
			String password = "";
			String url = "jdbc:mysql://172.16.166.1:3306/portal?autoReconnect=true";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			DBconnection = DriverManager.getConnection (url, userName, password);
			*/
			DBstatement = DBconnection.createStatement();
			
			return DBstatement;
			
		} catch (SQLException e) {
			System.err.println("Tools SQL Exception:" + e.getMessage());
		}
		
		return null;
	}
	
	public Connection getDBConnection() {
		Connection DBconnection;
		
		try {
			DBconnection = DBdatasource.getConnection();
			/*	String userName = "mysql";
			String password = "";
			String url = "jdbc:mysql://172.16.166.1:3306/portal?autoReconnect=true";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			DBconnection = DriverManager.getConnection (url, userName, password);
			*/
			
			return DBconnection;
			
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		}
		
		return null;
	}
	
	public void freeDBConnection(Statement s) {
		try {
			ResultSet rs = s.getResultSet();
			Connection con = s.getConnection();
			
			if (rs != null)
			{
				try {
					rs.close();
				} catch (SQLException e) {}
				rs = null;
			}
			if (s != null) {
				try {
					s.close();
				} catch (SQLException e) {}
				s = null;
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {}
				con = null;
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		}
		
	}
	
	public ArrayList<HashMap<String,String>> getAllConnectedIdentities(String publicId) {
		Statement DBstatement;
		ResultSet rs = null;
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		
		DBstatement = getDBStatement();
		try {
			if(DBstatement.execute("select distinct(subscriber),domain from sessions where public_entity = '" + publicId + "' && online > 0;")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					HashMap<String,String> hashMap = new HashMap<String,String>();
					hashMap.put(ChatroomConstants.USER,rs.getString(1));
					hashMap.put(ChatroomConstants.DOMAIN,rs.getString(2));
					result.add(hashMap);
				} 
				rs.close();
				rs = null;
				
			} else {
			}
			
			if(DBstatement.execute("select subscriber,domain from static_sessions where public_entity = '" + publicId + "';")) {
				rs = DBstatement.getResultSet();
				
				while (rs.next()) {
					HashMap<String,String> hashMap = new HashMap<String,String>();
					hashMap.put(ChatroomConstants.USER,rs.getString(1));
					hashMap.put(ChatroomConstants.DOMAIN,rs.getString(2));
					result.add(hashMap);
				} 
				rs.close();
				rs = null;
				
			} else {
			}
			
		} catch (SQLException e) {
			logger.info("UACSIPSERVLET init SQL Exception:" + e.getMessage());
		} finally {
			freeDBConnection(DBstatement);
		}
		
		return result;
	}
	
	public void deleteEvent(fr.free.hd.bond.chatroom.client.SipEvent sipEvent) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		PreparedStatement DBpreparedStatement = null;
		
		try {
			
			DBconnection = getDBConnection();
			
			DBpreparedStatement = DBconnection.prepareStatement("delete from events where callId=? and method=? and incoming=?;");
			DBpreparedStatement.setString(1,sipEvent.callId);
			DBpreparedStatement.setString(2,sipEvent.method);
			DBpreparedStatement.setInt(3,0);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			freeDBConnection(DBpreparedStatement);
		}
	}
	
}
