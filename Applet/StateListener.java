
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

class StateListener implements ControllerListener {
  private Integer stateLock = new Integer(0);

  private boolean failed = false;

  public Integer getStateLock() {
    return stateLock;
  }

  public void setFailed() {
    failed = true;
  }

  public void controllerUpdate(ControllerEvent ce) {
    // If there was an error during configure or
    // realize, the processor will be closed
    
    if (ce instanceof ControllerClosedEvent) 
    	setFailed();
    
    // All controller events, send a notification
    // to the waiting thread in waitForState method.
    
    if (ce instanceof ControllerEvent) {
    	synchronized (getStateLock()) {
            	getStateLock().notifyAll();
            }
    }
    
    
  }

  public boolean waitForState(Processor p, int state) {
    if (p != null) {
    	p.addControllerListener(this);
    	failed = false;
    
    	// Call the required method on the processor
    	if (state == Processor.Configured) {
    		p.configure();
    	} else if (state == Processor.Realized) {
    		p.realize();
    	}
    
    	// Wait until we get an event that confirms the
    	// success of the method, or a failure event.
    	// See StateListener inner class
    	while (p.getState() < state && !failed) {
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
    	else
    		return true;
    } else 
    	return false;
    
  }

}
