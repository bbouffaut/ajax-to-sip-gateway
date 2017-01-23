
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

import javax.annotation.Resource;

class SipRequestGenerator {
  private static boolean DEBUG = false;

  private static Logger logger;

  //@Resource(mappedName="sip/fr.free.hd.bond.chatroom.ChatRoomApplication")
    private SipFactory factory;

  public SipRequestGenerator(SipFactory factory) {
    this.factory = factory;
    
    logger = Logger.getLogger(SipRequestGenerator.class.getName());
    
  }

  public SipServletRequest generateSubscribe(String room, String roomDomain, String user, String userDomain, int expires, String event) throws ServletParseException, ServletException {
    SipApplicationSession applicationSession;
    applicationSession = factory.createApplicationSession();
    
    if (DEBUG) {
    	logger.info("UACSipServlet generateSubscribe: From = " + room + "@" + roomDomain + " To= " + user + "@" + userDomain);
    }
    
    Address userAddress = factory.createAddress(factory.createSipURI(user,userDomain));
    Address roomAddress = factory.createAddress(factory.createSipURI(room,roomDomain));
    SipServletRequest subscribe = factory.createRequest(applicationSession,"SUBSCRIBE",roomAddress,userAddress);
    subscribe.setExpires(expires);
    subscribe.setHeader("Event",event);
    
    //subscribe.getSession().setHandler("UACSipServlet");
    
    return subscribe;
  }

  public SipServletRequest generateSubscribe(int expires, String event, SipSession sipSession) throws ServletParseException, ServletException {
    if (sipSession != null) {
    	if (DEBUG) {
    		logger.info("UACSipServlet generateSubscribe from SipSession");
    	}
    
    	SipServletRequest subscribe = sipSession.createRequest("SUBSCRIBE");
    	subscribe.setExpires(expires);
    	subscribe.setHeader("Event",event);
    
    	//subscribe.getSession().setHandler("UACSipServlet");
    
    	return subscribe;
    }
    
    return null;
  }

  public SipServletRequest generatePublish(String user, String domain, String presence) throws ServletParseException, ServletException {
    SipApplicationSession applicationSession;
    applicationSession = factory.createApplicationSession();
    
    if (DEBUG) {
    	logger.info("UACSipServlet generatePublish: " + user + "@" + domain + " with Presence= " + presence);
    }
    
    Address address = factory.createAddress(factory.createSipURI(user,domain));
    SipServletRequest publish = factory.createRequest(applicationSession,"PUBLISH",address,address);
    //publish.setContentType("application/pidf+xml");
    publish.setHeader("Event","presence");
    
    byte[] content = Tools.getXMLPidf(user + "@" + domain,presence);
    int contentLength = content.length;
    
    publish.setContentLength(contentLength);
    
    try {
    	publish.setContent((Object) content,"application/pidf+xml");
    } catch (UnsupportedEncodingException e) {
    	logger.info("UACSipServlet generatePuBlish Exception: " + e.getMessage());
    }
    
    //publish.getSession().setHandler("UACSipServlet");
    
    return publish;
  }

  public SipServletRequest generateRegister(String user, String domain, int expires) throws ServletParseException, ServletException {
    SipApplicationSession applicationSession;
    applicationSession = factory.createApplicationSession();
    
    if (DEBUG) {
    	logger.info("UACSipServlet generateRegister: " + user + "@" + domain);
    }
    
    Address address = factory.createAddress(factory.createSipURI(user,domain));
    SipServletRequest register = factory.createRequest(applicationSession,"REGISTER",address,address);
    register.setExpires(expires);
    
    //register.getSession().setHandler("UACSipServlet");
    
    return register;
  }

  public SipServletRequest generateMessage(String chatroom, String user, String domain, String message) throws ServletParseException, ServletException {
    SipApplicationSession applicationSession;
    applicationSession = factory.createApplicationSession();
    
    if (DEBUG) {
    	logger.info("RoomSipServlet generateMessage: " + user + "@" + domain);
    }
    
    Address destAddress = factory.createAddress(factory.createSipURI(user,domain));
    Address fromAddress = factory.createAddress(factory.createSipURI(chatroom,ChatroomConstants.BOND_DOMAIN));
    SipServletRequest messageRequest = factory.createRequest(applicationSession,"MESSAGE",fromAddress,destAddress);
    
    byte[] content = message.getBytes();
    int contentLength = content.length;
    
    messageRequest.setContentLength(contentLength);
    
    try {
    	messageRequest.setContent((Object) content,"text/html");
    } catch (UnsupportedEncodingException e) {
    	logger.info("UACSipServlet generatePuBlish Exception: " + e.getMessage());
    }
    
    //messageRequest.getSession().setHandler("UACSipServlet");
    
    return messageRequest;
  }

  public SipServletRequest generateInvite(fr.free.hd.bond.chatroom.client.SipEvent sipEvent) throws ServletParseException, UnsupportedEncodingException, IOException, ServletException {
    SipApplicationSession applicationSession;
    applicationSession = factory.createApplicationSession();
    
    if (DEBUG) {
    	logger.info("SipServlet generateINVITE: " + sipEvent.subscriber + "@" + sipEvent.domain);
    }
    
    Address destAddress = factory.createAddress(factory.createSipURI(sipEvent.subscriber,sipEvent.domain));
    Address fromAddress = factory.createAddress(factory.createSipURI(sipEvent.fromUser,sipEvent.fromDomain));
    SipServletRequest inviteRequest = factory.createRequest(applicationSession,"INVITE",fromAddress,destAddress);
    
    SDPTools sdpTools = new SDPTools();
    
    SessionDescription sessionDescription = sdpTools.generateRequestSdp(sipEvent);
    inviteRequest.setContent(sessionDescription,ChatroomConstants.SDP_CONTENT_TYPE);
    
    inviteRequest.getSession().setHandler("UACSipServlet");
    
    return inviteRequest;
  }

  public SipServletRequest generateInvite(fr.free.hd.bond.chatroom.client.SipEvent sipEvent, SipApplicationSession applicationSession) throws ServletParseException, UnsupportedEncodingException, IOException, ServletException {
    if (applicationSession != null) {
    	if (DEBUG) {
    		logger.info("SipServlet generateINVITE: " + sipEvent.subscriber + "@" + sipEvent.domain);
    	}
    
    	Address destAddress = factory.createAddress(factory.createSipURI(sipEvent.subscriber,sipEvent.domain));
    	Address fromAddress = factory.createAddress(factory.createSipURI(sipEvent.fromUser,sipEvent.fromDomain));
    	SipServletRequest inviteRequest = factory.createRequest(applicationSession,"INVITE",fromAddress,destAddress);
    
    	SDPTools sdpTools = new SDPTools();
    
    	SessionDescription sessionDescription = sdpTools.generateRequestSdp(sipEvent);
    	inviteRequest.setContent(sessionDescription,ChatroomConstants.SDP_CONTENT_TYPE);
    
    	inviteRequest.getSession().setHandler("UACSipServlet");
    
    	return inviteRequest;
    }
    
    return null;
  }

}
