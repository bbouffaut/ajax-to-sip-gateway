
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

class CallHandler {
  private static Logger logger;

  private static boolean DEBUG = true;

  private SipEventsListener sipEventsListener;

  private SipRequestGenerator requestGenerator;

  private SipResponseGenerator responseGenerator;

  private SdpFactory sdpFactory;

  private Tools tools;

  private SipFactory factory;

  public CallHandler(SipFactory factory) {
    this.factory = factory;
    
    sdpFactory = SdpFactory.getInstance();
    
    responseGenerator = new SipResponseGenerator(factory);
    requestGenerator = new SipRequestGenerator(factory);
    
    logger = Logger.getLogger(CallHandler.class.getName());
    
    tools = new Tools(this.factory);
    
    
    
    
  }

  public void handleIncomingInvite(SipServletRequest request) throws IOException, ServletException {
    request.createResponse(SipServletResponse.SC_RINGING).send(); 
    
    String destUser = ((SipURI) request.getTo().getURI()).getUser();
    
    //Save the SDP content in a String
    String sdpContent = null;
    String charEncoding = request.getCharacterEncoding();
    if (charEncoding != null)
    	sdpContent = new String(request.getRawContent(),charEncoding);
    else 
    	sdpContent = new String(request.getRawContent(),"UTF-8");   
    
    //Use the static method of SdpFactory to parse the content
    
    try {
    	SessionDescription requestSDP = sdpFactory.createSessionDescription(sdpContent);
    	
    	String callId = request.getCallId();
    	String user = ((SipURI) request.getFrom().getURI()).getUser();
    	String domain = ((SipURI) request.getFrom().getURI()).getHost();
    	String destDomain = ((SipURI) request.getTo().getURI()).getHost();
    	String method = request.getMethod();
    	String ipAddress = ((SipURI)request.getAddressHeader("Contact").getURI()).getHost();
    
    	tools.processInviteInDB(method,user,domain,destUser,destDomain,callId,ipAddress,requestSDP,1,0);
    
    	sipEventsListener = new SipEventsListener(factory);
    	sipEventsListener.listenForResponseInvite(request);
    	
    } catch (SdpParseException e) {
    	System.err.println("Error when creating SDP for INVITE: " + e.getMessage());
    } catch (ServletParseException e) {
    	System.err.println("Error when creating SDP for INVITE: " + e.getMessage());
    }
    
    
    
    
  }

  public void cancelIncomingInvite() {
    sipEventsListener.stop();
  }

  public void handleOngoingCall(SipSession session) {
    if (sipEventsListener == null)
    	sipEventsListener = new SipEventsListener(factory);
    
    
    sipEventsListener.listenForBye(session);
  }

  public void handleOutgoingCall(SipApplicationSession applicationSession) {
    if (sipEventsListener == null)
    	sipEventsListener = new SipEventsListener(factory);
    
    
    sipEventsListener.listenForCancel(applicationSession);
  }

  public void stop() {
    sipEventsListener.stop();
  }

}
