
package fr.free.hd.bond.chatroom.Applet;

import java.applet.Applet;
import java.awt.*;

import java.lang.String;
import java.util.Vector;

import java.net.URL;
import java.net.InetAddress;
import java.net.MalformedURLException;

import java.io.IOException;

import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;
import javax.media.rtp.event.*;
import javax.media.protocol.*;
import javax.media.control.*;
import javax.media.format.*;

import com.sun.media.ui.*;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fr.free.hd.bond.chatroom.client.SessionDescription;


public class ChatroomAppletImpl extends JApplet implements ActionListener, Runnable {
  private String remoteAddress;

  private int remotePort;

  private int localPort;

  private String audioFormat;

  private JTextArea textArea;

  private SessionDescription sessionDescription;

  private MediaHandler mediaHandler;

  private Thread chronometer;

  private int second;

  private JPanel mainPanel;

  private JLabel chronoContainer;

  private boolean transmittingStarted = true;

  private boolean receivingStarted = false;

  private Integer stateLock = new Integer(0);

  private HostIpAddress hostIpAddress;

  private String localIpAddress;

  public void init() {
    if (!checkForJMF()) {
    	JEditorPane jEditorPane = new JEditorPane();
            JOptionPane.showMessageDialog(this,
                                    "Please install the latest version of JMF from "
                                            + "the link provided on the webpage\n",
                                    "Java Media Framework not installed on your computer",
                                    JOptionPane.ERROR_MESSAGE);
    return;
    }
    
    if ((remoteAddress = getParameter("REMOTE_ADDRESS")) == null) {
    	Fatal("Invalid REMOTE_ADDRESS parameter");
    }
    
    String remotePortString = null;
    if ((remotePortString = getParameter("REMOTE_PORT")) != null) {
    	remotePort = Integer.parseInt(remotePortString);
    } else {
    	Fatal("Invalid REMOTE_PORT parameter");
    }
    
    String localPortString = null;
    if ((localPortString = getParameter("LOCAL_PORT")) != null) {
    	localPort = Integer.parseInt(localPortString);
    } else {
    	Fatal("Invalid LOCAL_PORT parameter");
    }
    
    audioFormat = null;
    if ((audioFormat = getParameter("AUDIO_FORMAT")) == null) {
    	Fatal("Invalid AUDIO_FORMAT parameter");
    }
    
    createGUI();
    
    
    
    
  }

  public void start() {
    //AppletUtil.callback(ChatroomAppletImpl.this, "OK");
    startMediaSession();
  }

  public void stop() {
    stopMediaSession();
  }

  public void destroy() {
    super.destroy();
  }

  public void startMediaSession(String remoteAddress, int remotePort, int localPort, String audioFormat) {
    mediaHandler = new MediaHandler(this);
    
    hostIpAddress = new HostIpAddress();
    localIpAddress = hostIpAddress.getRightAddress();
    
    if ((localIpAddress != null) && (localIpAddress != "")) {
    
    	sessionDescription = new SessionDescription();
    	sessionDescription.remoteAddress = remoteAddress;
    	sessionDescription.remoteAudioPort = remotePort;
    	sessionDescription.localAudioPort = localPort;
    	sessionDescription.audioFormat = audioFormat;
    	sessionDescription.localIPAddress = localIpAddress;
    
    	if (mediaHandler != null) {
    		mediaHandler.transmit(sessionDescription);
    		//receiver.receive(sessionDescription);
    
    		boolean result = waitForMedia();
    
    		if (result) {
    			chronometer = new Thread (this);
    			chronometer.start ();
    			System.err.println("Media starting....");
    		}
    
    	} else 
    		Fatal("Transmit is null");
    
    }
  }

  public void startMediaSession() {
    mediaHandler = new MediaHandler(this);
    
    hostIpAddress = new HostIpAddress();
    localIpAddress = hostIpAddress.getRightAddress();
    
    if ((localIpAddress != null) && (localIpAddress != "")) {
    
    	sessionDescription = new SessionDescription();
    	sessionDescription.remoteAddress = remoteAddress;
    	sessionDescription.remoteAudioPort = remotePort;
    	sessionDescription.localAudioPort = localPort;
    	sessionDescription.audioFormat = audioFormat;
    	sessionDescription.localIPAddress = localIpAddress;
    
    	if (mediaHandler != null) {
    		mediaHandler.transmit(sessionDescription);
    		//receiver.receive(sessionDescription);
    
    		boolean result = waitForMedia();
    
    		if (result) {
    			chronometer = new Thread (this);
    			chronometer.start ();
    			System.err.println("Media starting....");
    		}
    
    	} else 
    		Fatal("Transmit is null");
    
    }
  }

  public void actionPerformed(ActionEvent event) {
    
  }

  public void run() {
    Thread thisThread = Thread.currentThread();
    while (chronometer == thisThread) {
    	try
        	{
    	        chronometer.sleep (1000);
    	} catch (InterruptedException e) { }
    
    	updateChrono();
           	second++;
    }
    
    
  }

  private void updateChrono() {
    //chronoContainer.setFont (new Font ("Helvetica", Font.BOLD, 14));
    
    if (chronometer != null)
    	chronoContainer.setText(second / 3600 + ":" + (second / 600) % 6 + (second / 60) % 10 + ":" + (second / 10) % 6  + (second) % 10);
    else {
    	chronoContainer.setFont (new Font ("Helvetica", Font.PLAIN, 12));
    	chronoContainer.setText("Appel fini.");
    }
  }

  private void Fatal(String message) {
    stopMediaSession();
    
    System.err.println("FATAL ERROR: " + message);
    //throw new Error(message);
    
  }

  private void createGUI() {
    mainPanel = new JPanel();
    mainPanel.setLayout(null);
    
    chronoContainer = new JLabel();
    chronoContainer.setFont (new Font ("Helvetica", Font.PLAIN, 12));
    chronoContainer.setText("Connexion en cours...");
    
    mainPanel.add(chronoContainer);
    chronoContainer.setBounds(10, 5, 140, 20);
    chronoContainer.setHorizontalAlignment(SwingConstants.CENTER);
    
    Container content = getContentPane();
    content.add(mainPanel);
    content.setLocation(0,0);
    
    
    
    
    
    
  }

  public void setTransmitting() {
    System.err.println("Transmitting started! ");
    
    transmittingStarted = true;
    
    synchronized (getStateLock()) {
    	getStateLock().notifyAll();
    }
  }

  public void setReceiving() {
    receivingStarted = true;
    
    synchronized (getStateLock()) {
    	getStateLock().notifyAll();
    }
  }

  private boolean waitForMedia() {
    while (!receivingStarted || !transmittingStarted) {
    	synchronized (getStateLock()) {
            	try {
    	        	getStateLock().wait();
            	} catch (InterruptedException ie) {
    			return false;
    	        }
    	}
    }
    
    return true;
    
    
  }

  private Integer getStateLock() {
    return stateLock;
  }

  private boolean checkForJMF() {
    Class clz;
    
    //Check for basic JMF class.
    try {
    	Class.forName("javax.media.Player");
    } catch (Throwable throwable2) {
    	return false;
    }
    
    return true;
  }

  public void stopMediaSession() {
    if (mediaHandler != null) 
    	mediaHandler.stop();
    
    /*
    if (receiver != null) 
    	receiver.stop();
    */
    	
    chronometer = null;
    chronoContainer.setText("Appel fini.");
    //System.out.println("Stop action triggered");
  }

}
