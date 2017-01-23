
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

import fr.free.hd.bond.chatroom.client.SipEvent;

class SipResponseGenerator {
  private static boolean DEBUG = false;

  private static Logger logger;

  //@Resource(mappedName="sip/fr.free.hd.bond.chatroom.ChatRoomApplication")
    private SipFactory factory;

  private SDPTools sdpTools;

  public SipResponseGenerator(SipFactory factory) {
    this.factory = factory;
    sdpTools = new SDPTools();
    
    logger = Logger.getLogger(SipResponseGenerator.class.getName());
    
  }

  public SipServletResponse generate408Timeout(SipServletRequest request) throws IOException {
    return request.createResponse(SipServletResponse.SC_REQUEST_TIMEOUT);
  }

  public SipServletResponse generateInviteAnswer(SipServletRequest request, fr.free.hd.bond.chatroom.client.SipEvent sipEvent) throws IOException {
    SipServletResponse response = request.createResponse(sipEvent.status);
    
    if (sipEvent.status == SipServletResponse.SC_OK) {
    	SessionDescription sessionDescription = sdpTools.generateResponseSdp(sipEvent);
    	response.setContent(sessionDescription,ChatroomConstants.SDP_CONTENT_TYPE);
    } else if (sipEvent.status == SipServletResponse.SC_DECLINE) {
    }
    
    return response;
  }

  public SipServletResponse generateBusyAnswer(SipServletRequest request) throws IOException {
    return request.createResponse(SipServletResponse.SC_BUSY_HERE);
  }

}
