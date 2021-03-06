
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

class SipEventsListener {
  private SipRequestGenerator requestGenerator;

  private SipResponseGenerator responseGenerator;

  private SipFactory factory;

  private Tools tools;

  private Timer sipEventsListener;

  private static long CHECK_INTERVAL = 500;

  class ResponseInviteListenerTask extends TimerTask {
    private int NUMBER_CHECKS = 30;

    private int counter;

    private SipServletRequest request;

    public void init(SipServletRequest request) {
      this.request = request;
      
      counter = 0;
      
    }

    public void run() {
      if (counter < NUMBER_CHECKS) {
      	checkAnswers();
      	counter++;
      } else {
      	responseInviteTimeout(request);
      	cancel();
      }
      
      
    }

    private void checkAnswers() {
      SipEvent sipAnswer = null;
      
      Statement DBstatement = null;
      ResultSet rs = null; // result set object
      
      String timeString = Tools.getTimeString();
      
      try {
      	
      	DBstatement = tools.getDBStatement();
      	
      	if(DBstatement.execute("select * from events where method = '" + ChatroomConstants.RESPONSE_INVITE + "' and callId = '" + request.getCallId() + "' and incoming = '0';")) {
      		rs = DBstatement.getResultSet();
      
      		while (rs.next()) {	
      			if (sipAnswer == null) {
      				sipAnswer = new SipEvent();
      			}
      			sipAnswer.method = rs.getString(2);
      			sipAnswer.callId = rs.getString(3);
      			sipAnswer.time = rs.getString(4);
      			sipAnswer.subscriber = rs.getString(5);
      			sipAnswer.domain = rs.getString(6);
      			sipAnswer.fromUser = rs.getString(7);
      			sipAnswer.fromDomain = rs.getString(8);
      			sipAnswer.ipAdress = rs.getString(9);
      			sipAnswer.audioPort = rs.getInt(10);
      			sipAnswer.codec = rs.getString(11);
      			sipAnswer.incoming = rs.getInt(12);
      			sipAnswer.status = rs.getInt(13);
      			tools.deleteEvent(sipAnswer);
      	       } 
      		rs.close();
      		rs = null;
      	} else {
      	}
      
      
      } catch (SQLException e) {
      	System.err.println("SQL Exception when checking SipResponse: "+ e.getMessage());
      } finally {
	      tools.freeDBConnection(DBstatement);
      }
      
      if (sipAnswer != null) {
      	responseInviteReceived(sipAnswer);
      	cancel();
      }
      
      
      
      
      
    }

    private void responseInviteReceived(fr.free.hd.bond.chatroom.client.SipEvent sipAnswer) {
      try {
      
      	SipServletRequest publish;
      	SipServletResponse inviteAnswer;
      
      	if (sipAnswer.status == SipServletResponse.SC_OK) {
      		publish = requestGenerator.generatePublish(sipAnswer.subscriber,sipAnswer.domain,ChatroomConstants.PRESENCE_BUSY);
      		inviteAnswer = responseGenerator.generateInviteAnswer(request,sipAnswer);
      	} else {
      		inviteAnswer = responseGenerator.generateBusyAnswer(request);
      		publish = requestGenerator.generatePublish(sipAnswer.subscriber,sipAnswer.domain,ChatroomConstants.PRESENCE_ONLINE);
      	}
      
      	inviteAnswer.send();
      	publish.send();
      
      } catch (ServletParseException e) {
      	System.err.println("Error in CALL: " + e.getMessage());
      } catch (IOException e) {
      	System.err.println("Error in CALL: " + e.getMessage());
      } catch (ServletException e) {
      	System.err.println("Error in CALL: " + e.getMessage());
      } 
      	
    }

    private void responseInviteTimeout(SipServletRequest request) {
      try {
      	SipServletResponse timeout = responseGenerator.generate408Timeout(request);
      	timeout.send();
      
      	String callId = request.getCallId();
      	String user = ((SipURI) request.getFrom().getURI()).getUser();
      	String domain = ((SipURI) request.getFrom().getURI()).getHost();
      	String destUser = ((SipURI) request.getTo().getURI()).getUser();
      	String destDomain = ((SipURI) request.getTo().getURI()).getHost();
      
      	tools.processInviteInDB(ChatroomConstants.MISSED_CALL,user,domain,destUser,destDomain,callId,"",null,1,0);	
      
      } catch (IOException e) {
      	System.err.println("Error in CALL: " + e.getMessage());
      } 
      
    }

  }

  class ByeListenerTask extends TimerTask {
    private SipServletRequest request;

    private SipSession session;

    public void init(SipSession session) {
      this.session = session;
      
      
    }

    public void run() {
      checkAnswers();
      
      
      
    }

    private void checkAnswers() {
      SipEvent sipAnswer = null;
      
      Statement DBstatement = null;
      ResultSet rs = null; // result set object
      
      String timeString = Tools.getTimeString();
      
      try {
      	
      	DBstatement = tools.getDBStatement();
      	
      	if(DBstatement.execute("select * from events where method = '" + ChatroomConstants.BYE_CALL + "' and callId = '" + session.getCallId() + "' and incoming = '0';")) {
      		rs = DBstatement.getResultSet();
      
      		while (rs.next()) {	
      			if (sipAnswer == null) {
      				sipAnswer = new SipEvent();
      			}
      			sipAnswer.method = rs.getString(2);
      			sipAnswer.callId = rs.getString(3);
      			sipAnswer.time = rs.getString(4);
      			sipAnswer.subscriber = rs.getString(5);
      			sipAnswer.domain = rs.getString(6);
      			sipAnswer.fromUser = rs.getString(7);
      			sipAnswer.fromDomain = rs.getString(8);
      			sipAnswer.ipAdress = rs.getString(9);
      			sipAnswer.audioPort = rs.getInt(10);
      			sipAnswer.codec = rs.getString(11);
      			sipAnswer.incoming = rs.getInt(12);
      			sipAnswer.status = rs.getInt(13);
      			tools.deleteEvent(sipAnswer);
      	       } 
      		rs.close();
      		rs = null;
      	} else {
      	}
      
      
      } catch (SQLException e) {
      	System.err.println("SQL Exception when checking Bye: " + e.getMessage());
      } finally {
	      tools.freeDBConnection(DBstatement);
      }
      
      if (sipAnswer != null) {
      	byeReceived();
      	cancel();
      }
      
      
      
      
      
    }

    private void byeReceived() {
      try {
      	SipServletRequest byeRequest = session.createRequest("BYE");
      	byeRequest.send();
      
      	//session.invalidate();
      
      } catch (IOException e) {
      	System.err.println("Error in CALL: " + e.getMessage());
      } 
    }

  }

  class CancelListenerTask extends TimerTask {
    private SipApplicationSession applicationSession;

    public void init(SipApplicationSession applicationSession) {
      this.applicationSession = applicationSession;
      
      
    }

    public void run() {
      checkAnswers();
      
      
      
    }

    private void checkAnswers() {
      SipEvent sipAnswer = null;
      
      Statement DBstatement = null;
      ResultSet rs = null; // result set object
      
      String timeString = Tools.getTimeString();
      
      try {
      	
      	DBstatement = tools.getDBStatement();
      	
      	if(DBstatement.execute("select * from events where method = '" + ChatroomConstants.CANCEL_CALL + "' and callId = '" + applicationSession.getId() + "' and subscriber = '" + (String)applicationSession.getAttribute(ChatroomConstants.CALLER_ID) + "' and incoming = '0';")) {
      		rs = DBstatement.getResultSet();
      
      		while (rs.next()) {	
      			if (sipAnswer == null) {
      				sipAnswer = new SipEvent();
      			}
      			sipAnswer.method = rs.getString(2);
      			sipAnswer.callId = rs.getString(3);
      			sipAnswer.time = rs.getString(4);
      			sipAnswer.subscriber = rs.getString(5);
      			sipAnswer.domain = rs.getString(6);
      			sipAnswer.fromUser = rs.getString(7);
      			sipAnswer.fromDomain = rs.getString(8);
      			sipAnswer.ipAdress = rs.getString(9);
      			sipAnswer.audioPort = rs.getInt(10);
      			sipAnswer.codec = rs.getString(11);
      			sipAnswer.incoming = rs.getInt(12);
      			sipAnswer.status = rs.getInt(13);
      			tools.deleteEvent(sipAnswer);
      	       } 
      		rs.close();
      		rs = null;
      	} else {
      	}
      
      
      } catch (SQLException e) {
      	System.err.println("SQL Exception when checking Cancel: " + e.getMessage());
      } finally {
	      tools.freeDBConnection(DBstatement);
      }
      
      if (sipAnswer != null) {
      	cancelReceived();
      	cancel();
      }
      
      
      
      
      
    }

    private void cancelReceived() {
      try {
      
      	ArrayList sessionsList = (ArrayList) applicationSession.getAttribute(ChatroomConstants.SESSIONS_LIST);
      		
      	if (sessionsList != null) {
      
      		for(int i=0,N=sessionsList.size();i<N;i++) {
      			SipServletRequest cancelRequest = ((SipServletRequest)sessionsList.get(i)).createCancel();
      			cancelRequest.send();
      		}		
      	}
      
      } catch (IOException e) {
      	System.err.println("Error in CALL: " + e.getMessage());
      } 
    }

  }

  public SipEventsListener(SipFactory factory) {
    this.factory = factory;
    
    requestGenerator = new SipRequestGenerator(this.factory);
    responseGenerator = new SipResponseGenerator(this.factory);
    
    tools = new Tools(this.factory);
  }

  public void listenForResponseInvite(SipServletRequest request) {
    sipEventsListener = new Timer();
    ResponseInviteListenerTask task = new ResponseInviteListenerTask();
    task.init(request);
    sipEventsListener.schedule(task,0,CHECK_INTERVAL);
     
  }

  public void listenForBye(SipSession session) {
    sipEventsListener = new Timer();
    ByeListenerTask task = new ByeListenerTask();
    task.init(session);
    sipEventsListener.schedule(task,0,CHECK_INTERVAL);
  }

  public void listenForCancel(SipApplicationSession applicationSession) {
    sipEventsListener = new Timer();
    CancelListenerTask task = new CancelListenerTask();
    task.init(applicationSession);
    sipEventsListener.schedule(task,0,CHECK_INTERVAL);
  }

  public void stop() {
    if (sipEventsListener != null)
    	sipEventsListener.cancel();
    
    
  }

}
