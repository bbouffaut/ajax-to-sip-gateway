
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

public class UACSipServlet extends SipServlet {
	private static boolean DEBUG = false;
	
	private SipFactory factory;
	
	private String serverAddress;
	
	private static Logger logger;
	
	private Timer timerUpdateRoomsList;
	
	private Timer sessionsMonitorTimer;
	
	private String bondDomain;
	
	private ArrayList<String> roomsList;
	
	private GarbageInactiveWebSessions garbage;
	
	private SipResponseGenerator responseGenerator;
	
	private SipRequestGenerator requestGenerator;
	
	private Tools tools;
	
	private TimerService timerService;
	
	private RoomsRegistrator roomsRegistrator;
	
	private class GarbageInactiveWebSessions {
		private int MAX_FLAG = 5;
		
		private long APPLI_TIMEOUT = 5000;
		
		public void start() {
			sessionsMonitorTimer = new Timer();
			sessionsMonitorTimer.schedule(new TimerTask() {
					public void run() {
						garbageSessions();
					}
			}, 30000,APPLI_TIMEOUT); 
		}
		
		private void garbageSessions() {
			ResultSet rs = null; // result set object
			Statement statement = null;
			
			String timeString = Tools.getTimeString();
			long newTime = Long.parseLong(timeString);
			
			if (DEBUG) {
				System.err.println("SessionsMonitor = "  + timeString);
			}
			
			try {
				
				statement = tools.getDBStatement();
				
				if(statement.execute("select sessionId,lastUpdate,flag from presence;")) {
					rs = statement.getResultSet();
					
					while (rs.next()) {	
						long lastUpdateTime = Long.parseLong(rs.getString(2));
						long delta = newTime - lastUpdateTime;
						
						if (rs.getInt(3) > MAX_FLAG) {
							try {
								unregisterUser(rs.getString(1));
							} catch (ServletException e) {
								System.err.println("garbageSessions ServletException: " + e.getMessage());
							}
							
							if (DEBUG) {
								System.err.println("SessionsMonitorDelta = "  + rs.getString(1) + " / delta = " + delta);
							}
							
						} else if ((newTime - lastUpdateTime) > (APPLI_TIMEOUT * 6)) {
							updatePresence(rs.getString(1),rs.getInt(3));
						}
						
					} 
					rs.close();
					rs = null;
				} else {
				}
				
			
			} catch (SQLException e) {
				System.err.println("SQL Exception:" + e.getMessage());
			} finally {
				tools.freeDBConnection(statement);				
			}
			
		}
		
		private void unregisterUser(String sessionId) throws ServletException {
			String login = getLogin(sessionId);
			
			if (login != null) {
				updateUserInDB("","","","",login);
				deletePresence(sessionId);
				eraseMessages(login);
				
				try {
					actionRegisterUser(login,ChatroomConstants.EXPIRES_NULL);
				} catch (IOException e) {
					logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
				} catch (ServletParseException e) {
					logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
				} catch (TooManyHopsException e) {
					logger.info("WebServicesImpl ProcessPassword IOException: " + e.getMessage());
				} 
				
			} else {
			}
			
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
		
		private String getLogin(String sessionId) {
			Statement DBstatement;
			ResultSet rs = null;
			String result = null;
			
			DBstatement = tools.getDBStatement();
			
			try {
				if(DBstatement.execute("select subscriber from users where sessionId='" + sessionId + "';")) {
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
				tools.freeDBConnection(DBstatement);
			}
			
			return result;
		}
		
		private void actionRegisterUser(String user, int expires) throws IOException, ServletParseException, TooManyHopsException, ServletException {
			SipServletRequest register = requestGenerator.generateRegister(user,bondDomain,expires);
			register.send();
			
			SipServletRequest publish;
			if (expires == ChatroomConstants.EXPIRES_DEFAULT) {
				publish = requestGenerator.generatePublish(user,bondDomain,ChatroomConstants.PRESENCE_ONLINE);
			} else {
				publish = requestGenerator.generatePublish(user,bondDomain,ChatroomConstants.PRESENCE_OFFLINE);
			}
			
			publish.send();
			
		}
		
		public void stop() {
			sessionsMonitorTimer.cancel();
		}
		
		private void updatePresence(String sessionId, int flag) {
			Connection DBconnection;
			ResultSet rs = null; // result set object
			
			DBconnection = tools.getDBConnection();
			
			PreparedStatement DBpreparedStatement = null;
			
			try {
				DBpreparedStatement = DBconnection.prepareStatement("update presence set flag=? where sessionId=? ;");
				DBpreparedStatement.setInt(1,flag + 1);
				DBpreparedStatement.setString(2,sessionId);
				
				DBpreparedStatement.execute();
			} catch (SQLException e) {
				System.err.println("SQL Exception:" + e.getMessage());
			} finally {
				tools.freeDBConnection(DBpreparedStatement);
			}
		}
		
	}
	
	class RoomPeriodicSubscriptor {
		private SipSession sipSession;
		
		private String event;
		
		private Timer timer;
		
		public RoomPeriodicSubscriptor(String event, SipSession sipSession) {
			this.event = event;
			this.sipSession = sipSession;
		}
		
		public void start() {
			timer = new Timer();
			timer.schedule(new TimerTask() {
					public void run() {
						subscribe();
					}
			},ChatroomConstants.SUBSCRIBE_RESEND_PERIOD);
			
		}
		
		private void subscribe() {
			try {
				sipSession.getApplicationSession().removeAttribute("SubscriptorTimer");
				
				SipServletRequest subscribe = requestGenerator.generateSubscribe(ChatroomConstants.EXPIRES_DEFAULT,event,sipSession);
				subscribe.send();
			} catch (IOException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			} catch (ServletParseException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			}  catch (ServletException e) {
				logger.info("RESEND Request Exception: " + e.getMessage());
			} 
		}
		
		public void cancel() {
			if (timer != null) 
				timer.cancel();
		}
		
	}
	
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		serverAddress = getServletConfig().getInitParameter(ChatroomConstants.SERVER_ADDRESS);
		logger = Logger.getLogger(UACSipServlet.class.getName());
		bondDomain = getServletConfig().getInitParameter(ChatroomConstants.BOND_DOMAIN_NAME);
		
		factory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		timerService = (TimerService) getServletContext().getAttribute(ChatroomConstants.TIMER_SERVICE);
		
		tools = new Tools(factory);
		
		roomsRegistrator = new RoomsRegistrator(factory);
		
		timerUpdateRoomsList = new Timer();
		timerUpdateRoomsList.schedule(new TimerTask() {
				public void run() {
					roomsList = getRoomsList();
					roomsRegistrator.updateRoomsList(roomsList);
				}
		}, 5000,30000); 
		
		sessionsMonitorTimer = new Timer();
		garbage = new GarbageInactiveWebSessions();
		garbage.start();
		
		responseGenerator = new SipResponseGenerator(factory);
		requestGenerator = new SipRequestGenerator(factory);
		
		roomsRegistrator.start();
		
	}
	
	public void destroy() {
		roomsRegistrator.stop();
		
		timerUpdateRoomsList.cancel();
		garbage.stop();
		
		super.destroy();
	}
	
	private ArrayList<String> getRoomsList() {
		Statement DBstatement;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		
		if (DEBUG) {
			logger.info("GetRoomsList");
		}
		
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
	
	protected void doNotify(SipServletRequest request) throws ServletException, IOException {
		SipServletResponse ok = request.createResponse(SipServletResponse.SC_OK);
		ok.send();
		
		String contentXML;
		
		String user = ((SipURI) request.getFrom().getURI()).getUser();
		String domain = ((SipURI) request.getFrom().getURI()).getHost();
		
		String chatroom = ((SipURI) request.getTo().getURI()).getUser();
		
		String contentType = request.getContentType();
		int contentLength = request.getContentLength();
		String event = request.getHeader("Event");
		
		if (DEBUG) {
			logger.info("UACSipServlet doNOTIFY :" + event + " Length=" + contentLength);
		}
		
		if (event.indexOf("presence") > -1) {
			String state = request.getHeader("Subscription-State");
			
			if (contentLength > 0) {
				if (contentType.indexOf("pidf") > -1) {
					byte[] content = (byte[]) request.getContent();
					contentXML = new String(content);
					
					if (isPresent(contentXML) == 1)
						updateUserRegistration(user,domain,chatroom,ChatroomConstants.REGISTER_USER);
					else if (isPresent(contentXML) < 0)
						updateUserRegistration(user,domain,chatroom,ChatroomConstants.BUSY_USER);
					else if (isPresent(contentXML) == 2)
						updateUserRegistration(user,domain,chatroom,ChatroomConstants.UNREGISTER_USER);
					else
						updateUserRegistration(user,domain,chatroom,ChatroomConstants.UNREGISTER_USER);
				}
			}
			
			if (state.indexOf("terminated") > -1) {
				RoomPeriodicSubscriptor timer = (RoomPeriodicSubscriptor) request.getApplicationSession().getAttribute("SubscriptorTimer");
				if (timer != null) {
					timer.cancel();
					request.getApplicationSession().removeAttribute("SubscriptorTimer");
				}
				
				request.getApplicationSession().invalidate();
				SipServletRequest reSubscribe = requestGenerator.generateSubscribe(chatroom,ChatroomConstants.BOND_DOMAIN,user,domain,ChatroomConstants.EXPIRES_DEFAULT,event);
				reSubscribe.send();
				
			} else {
				
			}
		}				
	}
	
	protected void doMessage(SipServletRequest request) throws ServletException, IOException {
		RequestDispatcher dispatcher = this.getServletConfig().getServletContext().getNamedDispatcher(ChatroomConstants.ROOM_SERVICE_LOGIC_SIP_SERVLET);
		logger.info("UACSipServlet MESSAGE dispatcher");
		
		if (dispatcher != null) {
			request.getSession().setHandler(ChatroomConstants.ROOM_SERVICE_LOGIC_SIP_SERVLET);
			dispatcher.forward((ServletRequest) request,null);
			logger.info("UACSipServlet MESSAGE dispatcher: Request Dispatcher is not null");
		}
		
		
		
		
		
	}
	
	protected void doInvite(SipServletRequest request) throws ServletException, IOException {
		if (DEBUG) {
			logger.info("Entering doInvite");
		}
		
		request.createResponse(SipServletResponse.SC_TRYING).send(); 
		
		CallHandler callHandler = new CallHandler(factory);
		request.getApplicationSession().setAttribute(ChatroomConstants.CALLEE_HANDLER,callHandler);
		
		String destUser = ((SipURI) request.getTo().getURI()).getUser();
		String destDomain = ((SipURI) request.getTo().getURI()).getHost();
		
		if (tools.isUserAvailable(destUser,destDomain))
			callHandler.handleIncomingInvite(request);
		else {
			SipServletResponse busyAnswer = responseGenerator.generateBusyAnswer(request);	 
			busyAnswer.send();
		}
		
	}
	
	protected void doCancel(SipServletRequest request) throws ServletException, IOException {
		request.createResponse(SipServletResponse.SC_OK).send();
		
		if (((CallHandler)request.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)) != null) {
			((CallHandler)request.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)).cancelIncomingInvite();
		}
		
		String destUser = ((SipURI) request.getTo().getURI()).getUser();
		String callId = request.getCallId();
		String user = ((SipURI) request.getFrom().getURI()).getUser();
		String domain = ((SipURI) request.getFrom().getURI()).getHost();
		String destDomain = ((SipURI) request.getTo().getURI()).getHost();
		String method = request.getMethod();
		
		tools.processInviteInDB(method,user,domain,destUser,destDomain,callId,"",null,1,0);
	}
	
	protected void doBye(SipServletRequest request) throws ServletException, IOException {
		request.createResponse(SipServletResponse.SC_OK).send(); 
		
		String destUser = ((SipURI) request.getTo().getURI()).getUser();
		String callId = request.getCallId();
		String user = ((SipURI) request.getFrom().getURI()).getUser();
		String domain = ((SipURI) request.getFrom().getURI()).getHost();
		String destDomain = ((SipURI) request.getTo().getURI()).getHost();
		String method = request.getMethod();
		
		tools.processInviteInDB(method,user,domain,destUser,destDomain,callId,"",null,1,0);
		
		if (!(((String)request.getApplicationSession().getAttribute(ChatroomConstants.CALLER_ID)).indexOf(destUser) > -1)) {
			if (((CallHandler)request.getApplicationSession().getAttribute(ChatroomConstants.CALLER_HANDLER)) != null) {
				((CallHandler)request.getApplicationSession().getAttribute(ChatroomConstants.CALLER_HANDLER)).stop();
			}
		} else {
			if (((CallHandler)request.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)) != null) {
				((CallHandler)request.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)).stop();
			}
		}
		
		try {
			SipServletRequest publish = requestGenerator.generatePublish(destUser,destDomain,ChatroomConstants.PRESENCE_ONLINE);
			publish.send();
		} catch (ServletException e) {
			logger.info("UACSipServlet doBye: " + e.getMessage());
		} 
		
		
		
		request.getSession().invalidate();
	}
	
	protected void doAck(SipServletRequest request) throws ServletException, IOException {
		String destUser = ((SipURI) request.getTo().getURI()).getUser();
		
		String callId = request.getCallId();
		String user = ((SipURI) request.getFrom().getURI()).getUser();
		String domain = ((SipURI) request.getFrom().getURI()).getHost();
		String destDomain = ((SipURI) request.getTo().getURI()).getHost();
		
		tools.processInviteInDB(ChatroomConstants.RESPONSE_ACK,user,domain,destUser,destDomain,callId,"",null,1,0);
		((CallHandler)request.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)).handleOngoingCall(request.getSession());
		
		
	}
	
	protected void doSuccessResponse(SipServletResponse response) throws ServletException, IOException {
		if ((response.getMethod().indexOf("INVITE") > -1) && (response.getStatus() == SipServletResponse.SC_OK)) {
			if (DEBUG) {
				logger.info("Entering doInvite");
			}
			
			logger.info("200OK RECEIVED");
			
			response.createAck().send();
			
			String destUser = ((SipURI) response.getTo().getURI()).getUser();
			
			//Save the SDP content in a String
			String sdpContent = null;
			String charEncoding = response.getCharacterEncoding();
			if (charEncoding != null)
				sdpContent = new String(response.getRawContent(),charEncoding);
			else 
				sdpContent = new String(response.getRawContent(),"UTF-8");   
			
			//Use the static method of SdpFactory to parse the content
			
			try {
				SessionDescription requestSDP = tools.sdpFactory.createSessionDescription(sdpContent);
				
				String callId = response.getCallId();
				String user = ((SipURI) response.getFrom().getURI()).getUser();
				String domain = ((SipURI) response.getFrom().getURI()).getHost();
				String destDomain = ((SipURI) response.getTo().getURI()).getHost();
				String ipAddress = ((SipURI)response.getAddressHeader("Contact").getURI()).getHost();
				
				tools.processInviteInDB(ChatroomConstants.RESPONSE_INVITE,destUser,destDomain,user,domain,callId,ipAddress,requestSDP,1,SipServletResponse.SC_OK);
				ArrayList sessionsList = (ArrayList) response.getApplicationSession().getAttribute(ChatroomConstants.SESSIONS_LIST);
				
				if (sessionsList != null) {
					
					for(int i=0,N=sessionsList.size();i<N;i++) {
						if (((SipServletRequest)sessionsList.get(i)).getSession() != response.getSession()) {
							SipServletRequest cancelRequest = ((SipServletRequest)sessionsList.get(i)).createCancel();
							cancelRequest.send();
						}
					}		
				}
				
				((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLER_HANDLER)).stop();	
				((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLER_HANDLER)).handleOngoingCall(response.getSession());	
				
			} catch (SdpParseException e) {
				System.err.println("Error when creating SDP for INVITE: " + e.getMessage());
			} catch (ServletParseException e) {
				System.err.println("Error when creating SDP for INVITE: " + e.getMessage());
			}
			
			//if (response.getApplicationSession() != null)
			//	response.getApplicationSession().invalidate();
			
			
		} else if (response.getMethod().indexOf("SUBSCRIBE") > -1) {
			response.getApplicationSession().setExpires(0);
			SipSession sipSession = response.getSession();
			RoomPeriodicSubscriptor timer = (RoomPeriodicSubscriptor) response.getApplicationSession().getAttribute("SubscriptorTimer");
			if (timer == null) {
				RoomPeriodicSubscriptor subscriptor = new RoomPeriodicSubscriptor(ChatroomConstants.PRESENCE_EVENT,sipSession);
				subscriptor.start();
				response.getApplicationSession().setAttribute("SubscriptorTimer",subscriptor);
			} else {
			}
		} else if (response.getMethod().indexOf("BYE") > -1) {
			response.getSession().invalidate();
		}
		
	}
	
	protected void doErrorResponse(SipServletResponse response) throws ServletException, IOException {
		if (response.getMethod().indexOf("INVITE") > -1) {
			if (DEBUG) {
				logger.info("Entering doErrorResponse");
			}
			
			logger.info(response.getStatus() + " RECEIVED");
			
			String destUser = ((SipURI) response.getTo().getURI()).getUser();
			
			String callId = response.getCallId();
			String user = ((SipURI) response.getFrom().getURI()).getUser();
			String domain = ((SipURI) response.getFrom().getURI()).getHost();
			String destDomain = ((SipURI) response.getTo().getURI()).getHost();
			
			if (response.getStatus() == 486) {
				
				tools.processInviteInDB(ChatroomConstants.RESPONSE_INVITE,destUser,destDomain,user,domain,callId,"",null,1,response.getStatus());
				((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLER_HANDLER)).stop();
				ArrayList sessionsList = (ArrayList) response.getApplicationSession().getAttribute(ChatroomConstants.SESSIONS_LIST);
				
				if (sessionsList != null) {
					
					for(int i=0,N=sessionsList.size();i<N;i++) {
						if (((SipServletRequest)sessionsList.get(i)).getSession() != response.getSession()) {
							SipServletRequest cancelRequest = ((SipServletRequest)sessionsList.get(i)).createCancel();
							cancelRequest.send();
						}
					}		
				}
				
				SipServletRequest publish = requestGenerator.generatePublish(user,domain,ChatroomConstants.PRESENCE_ONLINE);
				publish.send();
				
			} else {
				
				ArrayList sessionsList = (ArrayList) response.getApplicationSession().getAttribute(ChatroomConstants.SESSIONS_LIST);
				
				if (sessionsList != null) {
					
					for(int i=0,N=sessionsList.size();i<N;i++) {
						if (((SipServletRequest)sessionsList.get(i)).getSession() == response.getSession()) {
							SipServletRequest cancelRequest = ((SipServletRequest)sessionsList.get(i)).createCancel();
							cancelRequest.send();
							sessionsList.remove(i);
						}
					}		
				}
				
			}
			
			
		} else if (response.getMethod().indexOf("SUBSCRIBE") > -1) {
			String user = ((SipURI) response.getTo().getURI()).getUser();
			String domain = ((SipURI) response.getTo().getURI()).getHost();
			String chatroom = ((SipURI) response.getFrom().getURI()).getUser();
			String eventFromResponse = response.getHeader("Event");
			String event = null;
			
			if (eventFromResponse != null)
				event = eventFromResponse;
			else 
				event = ChatroomConstants.PRESENCE_EVENT;
			
			response.getApplicationSession().invalidate();
			
			SipServletRequest reSubscribe = requestGenerator.generateSubscribe(chatroom,ChatroomConstants.BOND_DOMAIN,user,domain,ChatroomConstants.EXPIRES_DEFAULT,event);
			reSubscribe.send();
			
		} else if (response.getMethod().indexOf("BYE") > -1) {
			
			String destUser = ((SipURI) response.getTo().getURI()).getUser();
			String callId = response.getCallId();
			String user = ((SipURI) response.getFrom().getURI()).getUser();
			String domain = ((SipURI) response.getFrom().getURI()).getHost();
			String destDomain = ((SipURI) response.getTo().getURI()).getHost();
			String method = response.getMethod();
			
			tools.processInviteInDB(method,user,domain,destUser,destDomain,callId,"",null,1,0);
			tools.processInviteInDB(method,destUser,destDomain,user,domain,callId,"",null,1,0);
			
			if (((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLER_HANDLER)) != null) {
				((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLER_HANDLER)).stop();
			}
			if (((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)) != null) {
				((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)).stop();
			}
			
			try {
				SipServletRequest publish = requestGenerator.generatePublish(destUser,destDomain,ChatroomConstants.PRESENCE_ONLINE);
				publish.send();
			} catch (ServletException e) {
				logger.info("UACSipServlet doBye: " + e.getMessage());
			} 
			
			
			
			response.getSession().invalidate();
			
		} else if (response.getMethod().indexOf("CANCEL") > -1) {
			if (((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)) != null) {
				((CallHandler)response.getApplicationSession().getAttribute(ChatroomConstants.CALLEE_HANDLER)).cancelIncomingInvite();
			}
			
			String destUser = ((SipURI) response.getTo().getURI()).getUser();
			String callId = response.getCallId();
			String user = ((SipURI) response.getFrom().getURI()).getUser();
			String domain = ((SipURI) response.getFrom().getURI()).getHost();
			String destDomain = ((SipURI) response.getTo().getURI()).getHost();
			String method = response.getMethod();
			
			tools.processInviteInDB(method,user,domain,destUser,destDomain,callId,"",null,1,0);
		}
		
		//response.getApplicationSession().invalidate(); 
		
		
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
			if (doc != null) {
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
					} else if (presenceValue.indexOf(ChatroomConstants.PRESENCE_OFFLINE) > -1) {
						result = 2;
					} else {
						result = 0;
					} 
				} else {
					result = 0;
				}
				
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
		try {
			DocumentBuilder pidfDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return pidfDocument.parse(new InputSource(new StringReader(xmlString)));
		} catch (ParserConfigurationException e) {
			System.err.println("Error when creating pidfDocument: " + e.getMessage());
		}
		
		return null;
		
		
	}
	
}
