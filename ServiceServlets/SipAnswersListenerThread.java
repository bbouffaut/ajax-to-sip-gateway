
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

import java.lang.Thread;
import java.lang.InterruptedException;

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

class SipAnswersListenerThread extends Thread {
	private static boolean DEBUG = false;
	
	private String method;
	
	private String callId;
	
	private SipEvent sipAnswer;
	
	private static Logger logger;
	
	private long CHECK_INTERVAL = 50;
	
	private boolean failed = false;
	
	private boolean received = false;
	
	private Tools tools;
	
	public void init(String method, String callId) {
		this.method = method;
		this.callId = callId;
		
		logger = Logger.getLogger(SipAnswersListenerThread.class.getName());
		
		tools = new Tools(null);
	}
	
	public void run() {
		if (DEBUG) {
			logger.info("WaitForSipResponse for callId / method: " + callId + " / " + method);
		}
		
		while (true) {
			if (received || failed) {
				return;
			}
			
			try {
				sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
				System.err.println("Error when waiting for Sip Answer: " + e.getMessage());
			}
			
			checkAnswer();
		}
		
		
		
	}
	
	public void setFailed() {
		if (DEBUG) {
			logger.info("SipAnswerListener SET-FAILED: " + callId);
		}
		
		failed = true;
		
	}
	
	private void setReceived() {
		received = true;
	}
	
	private void checkAnswer() {
		if (DEBUG) {
			logger.info("Check SipResponse for callId: " + callId);
		}
		
		Statement DBstatement = null;
		ResultSet rs = null; // result set object
		
		String timeString = Tools.getTimeString();
		
		if (DEBUG) {
			System.err.println("SipAnswersMonitor = "  + timeString);
		}
		
		try {
			
			DBstatement = tools.getDBStatement();
			
			if(DBstatement.execute("select * from events where method = '" + method + "' and callId = '" + callId + "' and incoming = '0';")) {
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
					deleteEvent(sipAnswer);
				} 
				rs.close();
				rs = null;
			} else {
			}
			
			
		} catch (SQLException e) {
			System.err.println("SQL Exception when checking SipResponse for callId " + callId + ": " + e.getMessage());
		} finally {
			tools.freeDBConnection(DBstatement);
		}
		
		if (sipAnswer != null) {
			setReceived();
		}
		
		
		
		
	}
	
	private void deleteEvent(SipEvent sipEvent) {
		Connection DBconnection;
		ResultSet rs = null; // result set object
		
		PreparedStatement DBpreparedStatement = null;
		
		try {
			
			DBconnection = tools.getDBConnection();
			
			DBpreparedStatement = DBconnection.prepareStatement("delete from events where callId=? and method=? and incoming=?;");
			DBpreparedStatement.setString(1,sipEvent.callId);
			DBpreparedStatement.setString(2,sipEvent.method);
			DBpreparedStatement.setInt(3,0);
			
			DBpreparedStatement.execute();
		} catch (SQLException e) {
			System.err.println("SQL Exception:" + e.getMessage());
		} finally {
			tools.freeDBConnection(DBpreparedStatement);
		}
	}
	
	public SipEvent getSipAnswer() {
		return sipAnswer;
	}
	
}
