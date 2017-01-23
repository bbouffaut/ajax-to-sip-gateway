
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

import fr.free.hd.bond.chatroom.client.SipEvent;

class SipAnswersListener {
  private static boolean DEBUG = true;

  private String method;

  private String callId;

  private SipEvent sipAnswer;

  private Context DBcontext;

  private DataSource DBdatasource;

  private static Logger logger;

  private class SipAnswersMonitor {
    private Timer sipAnswersMonitorTimer;

    private long TASK_REPEAT_PERIOD = 1500;

    private int NUMBER_CHECKS = 15;

    private int counter;

    public void start() {
      counter = 0;
      
      sipAnswersMonitorTimer = new Timer();
      sipAnswersMonitorTimer.schedule(new TimerTask() {
      	public void run() {
      		timerTaskAction();	
      	}
      }, 1000,TASK_REPEAT_PERIOD); 
    }

    private void timerTaskAction() {
      counter++;
      if (counter < NUMBER_CHECKS)
      	checkAnswer();
      else {
      	stop();
      	setFailed();
      	notifyEvent();
      }
      	
    }

    private void checkAnswer() {
      if (DEBUG) {
      	logger.info("Check SipResponse for callId: " + callId);
      }
      
      ResultSet rs = null; // result set object
      
      String timeString = Tools.getTimeString();
      
      if (DEBUG) {
      	System.err.println("SipAnswersMonitor = "  + timeString);
      }
      
      try {
      	Connection connection = DBdatasource.getConnection();
      	Statement statement = connection.createStatement();
      	
      	if(statement.execute("select * from events where method = '" + method + "' and callId = '" + callId + "' and incoming = '0';")) {
      		rs = statement.getResultSet();
      
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
      			deleteEvent(sipAnswer);
      	       } 
      		rs.close();
      		rs = null;
      	} else {
      	}
      
      	if (connection != null) {
            		try { connection.close(); } catch (SQLException e) { ; }
      	}
      
      } catch (SQLException e) {
      	System.err.println("SQL Exception when checking SipResponse for callId " + callId + ": " + e.getMessage());
      } finally {
      	if (rs != null) {
            		try { rs.close(); } catch (SQLException e) { ; }
            		rs = null;
      	}
      
      }
      
      if (sipAnswer != null) {
      	stop();
      	setReceived();
      	notifyEvent();
      }
      
      	
      
      
    }

    public void stop() {
      sipAnswersMonitorTimer.cancel();
    }

    private void deleteEvent(SipEvent sipEvent) {
      ResultSet rs = null; // result set object
      
      PreparedStatement DBpreparedStatement;
      
      try {
      	Connection DBconnection = DBdatasource.getConnection();
      
      	DBpreparedStatement = DBconnection.prepareStatement("delete from events where callId=? and subscriber=? and method=? and time=?;");
      	DBpreparedStatement.setString(1,sipEvent.callId);
      	DBpreparedStatement.setString(2,sipEvent.subscriber);
      	DBpreparedStatement.setString(3,sipEvent.method);
      	DBpreparedStatement.setString(4,sipEvent.time);
      
      	DBpreparedStatement.execute();
      } catch (SQLException e) {
      	System.err.println("SQL Exception:" + e.getMessage());
      } finally {
      	if (rs != null) {
            		try { rs.close(); } catch (SQLException e) { ; }
            		rs = null;
      	}
      }
    }

  }

  private Integer stateLock = new Integer(0);

  private boolean failed = false;

  private boolean received = false;

  private SipAnswersListener.SipAnswersMonitor sipAnswersMonitor;

  public SipAnswersListener() {
    logger = Logger.getLogger(SipAnswersListener.class.getName());
    
    try {
    	DBcontext = new InitialContext();
    	DBdatasource = (DataSource)DBcontext.lookup("java:comp/env/jdbc/chatroom");
    	if (DEBUG) { logger.info("SipAnswersListener INFO: DBContext created");	}
    } catch (NamingException e) {
    	logger.info("SipAnswersListener INFO: SQL Error: Boom - No Context!");
    }
  }

  public Integer getStateLock() {
    return stateLock;
  }

  public void setFailed() {
    if (DEBUG) {
    	logger.info("SipAnswerListener SET-FAILED: " + callId);
    }
    
    failed = true;
    sipAnswersMonitor.stop();
    notifyEvent();
  }

  private void setReceived() {
    received = true;
  }

  private void notifyEvent() {
    synchronized (getStateLock()) {
    	getStateLock().notifyAll();
    }
    
    
    
  }

  public boolean waitForSipAnswer(String method, String callId) {
    if (DEBUG) {
    	logger.info("WaitForSipResponse for callId / method: " + callId + " / " + method);
    }
    
    this.method = method;
    this.callId = callId;
    
    sipAnswersMonitor = new SipAnswersMonitor();
    sipAnswersMonitor.start();
    
    while (!received && !failed) {
    	synchronized (getStateLock()) {
            	try {
    	        	getStateLock().wait();
           	        } catch (InterruptedException ie) {
    			return false;
    	        }
           	 }
    }
    
    if (failed)
    	return false;
    else {
    	if (received)
    		return true;
    	else 
    		return false;
    }
    
    
  }

  public SipEvent getSipAnswer() {
    return sipAnswer;
  }

}
