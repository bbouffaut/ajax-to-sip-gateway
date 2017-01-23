
package fr.free.hd.bond.chatroom.Applet;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.Vector;

import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.protocol.DataSource;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.control.MpegAudioControl;
import javax.media.control.FrameRateControl;

public class MediaEventsListener implements ControllerListener, ReceiveStreamListener, SessionListener, SendStreamListener, RemoteListener {
  private ChatroomAppletImpl applet;

  private Player player;
  
  private String audioFormat;

  public MediaEventsListener(ChatroomAppletImpl applet) {
    this.applet = applet;
    
    audioFormat = AudioFormat.ULAW_RTP;
    
  }

  public synchronized void update(SessionEvent event) {
  }

  public void update(SendStreamEvent event) {
    if (event instanceof NewSendStreamEvent)
                System.out.println("NewSendStreamEvent received");
    else if (event instanceof ActiveSendStreamEvent)
    	System.out.println("ActiveSendStreamEvent received");
    else if (event instanceof InactiveSendStreamEvent)
    	System.out.println("InactiveSendStreamEvent received");
    else if (event instanceof LocalPayloadChangeEvent)
    	System.out.println("LocalPayloadChangeEvent received");
  }

  public void update(RemoteEvent event) {
    if (event instanceof ReceiverReportEvent)
                System.out.println("ReceiverReportEvent received");
    else if (event instanceof SenderReportEvent)
    	System.out.println("SenderReportEvent received");
    else if (event instanceof RemoteCollisionEvent)
    	System.out.println("RemoteCollisionEvent received");
  }

  public synchronized void update(ReceiveStreamEvent event) {
    RTPManager mgr = (RTPManager)event.getSource();
    Participant participant = event.getParticipant(); // could be null.
    ReceiveStream stream = event.getReceiveStream(); 
    
    if (event instanceof ApplicationEvent) {
                System.out.println("applciation event New stream received!!!!!!!!!");
    }
    else if (event instanceof NewReceiveStreamEvent) {
    	try {
    		//Pull out the stream
    		stream = ((NewReceiveStreamEvent)event).getReceiveStream();
            	DataSource ds = stream.getDataSource();
    
    	        // Find out the formats.
    	        Object[] format = mgr.getControls();
    	        System.out.println(format.length);
    	        RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
    
            	if (ctl != null){
    		        System.err.println("  - Received new RTP stream: " + ctl.getFormat());
            		//ctl.addFormat(new AudioFormat(AudioFormat.MPEG_RTP,48000,16,1),22);
            	        // The following one works:
			//ctl.addFormat(new AudioFormat(AudioFormat.MPEG_RTP,48000,16,1),14);
			ctl.addFormat(new AudioFormat(audioFormat),14);
            	        Format[] formats=ctl.getFormatList();
            	        System.out.println("format list : " + formats.length);
            	} else
            		System.err.println("  - Received new RTP stream");
    
                    if (participant == null)
                        System.err.println("      The sender of this stream had yet to be identified.");
                    else {
                        System.err.println("      The stream comes from: " + participant.getCNAME());
                    }
                    // create a player by passing datasource to the Media Manager
                    player = javax.media.Manager.createPlayer(ds);
                    
    		if (player == null)
                        return;
    
                    player.addControllerListener(this);
                    player.realize();
    
                    
    	} catch (Exception e) {
            	System.err.println("NewReceiveStreamEvent exception " + e.getMessage());
                    return;
            }
    
    }
    else if (event instanceof ByeEvent) {
    	System.err.println("  - Got \"bye\" from: " + event.getParticipant().getCNAME());
    	
    	//if (player != null)
    	//	player.close();
    	applet.stopMediaSession();
    }
    
    
  }

  public synchronized void controllerUpdate(ControllerEvent event) {
    Player p = (Player)event.getSourceController();
    MpegAudioControl mpegControl=(MpegAudioControl)p.getControl("javax.media.control.MpegAudioControl");
    System.out.println("mpegControl::::" + mpegControl);
    
    if (p == null)
    	return;
    
    // Get this when the internal players are realized.
    if (event instanceof RealizeCompleteEvent) {
    	p.start();
    	applet.setReceiving();
    }
    
    
    if (event instanceof ControllerErrorEvent) {
    	p.removeControllerListener(this);
            System.err.println("Receiver internal error: " + event);
    }
    
    if (event instanceof StartEvent) {
    	GainControl gc = p.getGainControl();
            System.out.println("Class for gain contol" + gc);
            if(gc!=null){
    	        Component c = gc.getControlComponent();
                    System.out.println("Class for component" + c);
             }
    }
  }

  public void stop() {
    if (player != null) 
    	player.stop();
    
  }

}
