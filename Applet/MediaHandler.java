
package fr.free.hd.bond.chatroom.Applet;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.Vector;

import javax.media.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;

import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.protocol.DataSource;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.Format;
import javax.media.control.BufferControl;
import javax.media.control.MpegAudioControl;
import javax.media.control.FrameRateControl;

import fr.free.hd.bond.chatroom.client.SessionDescription;

class MediaHandler {
  private StateListener stateListener = null;

  private MediaLocator audioLocator = null;

  private Processor processor = null;

  private RTPManager[] rtpMgrs = null;

  private ArrayList<MediaEventsListener> mediaEventsListeners;

  private DataSource dataOutput = null;

  private ChatroomAppletImpl applet;

  private String localIpAddress;

  public MediaHandler(ChatroomAppletImpl applet) {
    stateListener = new StateListener();
    
    this.applet = applet;
    
    mediaEventsListeners = new ArrayList<MediaEventsListener>();
    
    if(!initialize()){
    	System.out.println("Bad Session intialization");
    	//System.exit(0);
    }
  }

  protected boolean initialize() {
    CaptureDeviceInfo videoCDI=null;
    CaptureDeviceInfo audioCDI=null;
    Vector captureDevices=null;
    captureDevices= CaptureDeviceManager.getDeviceList(null);
    System.out.println("- number of capture devices: "+captureDevices.size() );
    CaptureDeviceInfo cdi=null;
    
    for (int i = 0; i < captureDevices.size(); i++) {
    	cdi = (CaptureDeviceInfo) captureDevices.elementAt(i);
            //System.out.println("    - name of the capture device: "+cdi.getName() );
    
            Format[] formatArray=cdi.getFormats();
            
    	for (int j = 0; j < formatArray.length; j++) {
            	Format format=formatArray[j];
                    if (format instanceof AudioFormat) {
                    	//System.out.println("         - format accepted by this AUDIO device: "+
                    	//format.toString().trim());
                    	if (audioCDI == null)
    				audioCDI=cdi;
                   	}
                   //else
                   	//System.out.println("         - format of type UNKNOWN");
            }
    }
    
    if(audioCDI!=null)
    	audioLocator=audioCDI.getLocator();
    
    
    return true;
  }

  public synchronized String start(SessionDescription sessionDescription) {
    String result;
    
    // Create a processor for the specified media locator
    // and program it to output JPEG/RTP
    result = createProcessor(sessionDescription);
    if (result != null)
    	return result;
    
    // Create an RTP session to transmit the output of the
    // processor to the specified IP address and port no.
    result = createTransmitters(sessionDescription);
    if (result != null) {
    	processor.close();
            processor = null;
            return result;
    }
    
    // Start the transmission
    processor.start();
    
    return null;
    
    
    
  }

  public void stop() {
    synchronized (this) {
    	if (processor != null) {
            	processor.stop();
                    processor.close();
                    processor = null;
            }
            
    	if(rtpMgrs!=null){
            	for (int i = 0; i < rtpMgrs.length; i++) {
                    	if(rtpMgrs[i]!=null){
                            	rtpMgrs[i].removeTargets( "Session ended.");
                                    rtpMgrs[i].dispose();
                                    rtpMgrs[i]=null;
                            }
                    }
            }
    
    	if(mediaEventsListeners != null){
            	for (int i = 0; i < mediaEventsListeners.size(); i++) {
                    	if((MediaEventsListener)mediaEventsListeners.get(i) != null)
                            	((MediaEventsListener)mediaEventsListeners.get(i)).stop();
                    }
            }
    }
  }

  private String createProcessor(SessionDescription sessionDescription) {
    DataSource audioDS=null;
    
    StateListener stateListener=new StateListener();
    
    //create the DataSource
    
    if (audioLocator != null){
    	try {
            	//create the 'audio' DataSource
                    audioDS= javax.media.Manager.createDataSource(audioLocator);
                } catch (Exception e) {
                    System.out.println("-> Couldn't connect to audio capture device");
                }
    }
    
    
    //if the processor has not been created from the merging DataSource
    if(processor==null){
    	try {
            	if(audioDS!=null)
                        //Create the processor from the 'audio' DataSource
                        processor = javax.media.Manager.createProcessor(audioDS);
                } catch (NoProcessorException npe) {
                    return "Couldn't create processor";
                } catch (IOException ioe) {
                    return "IOException creating processor";
                }
    }
    
    // Wait for it to configure
    boolean result = stateListener.waitForState(processor, Processor.Configured);
    if (result == false)
    	return "Couldn't configure processor";
    
    // Get the tracks from the processor
    TrackControl [] tracks = processor.getTrackControls();
    
    // Do we have atleast one track?
    if (tracks == null || tracks.length < 1)
    	return "Couldn't find tracks in processor";
    
    // Set the output content descriptor to RAW_RTP
    // This will limit the supported formats reported from
    // Track.getSupportedFormats to only valid RTP formats.
    ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
    processor.setContentDescriptor(cd);
    
    Format supported[];
    
    Format chosen=null;
    boolean atLeastOneTrack = false;
    
    // Program the tracks.
    for (int i = 0; i < tracks.length; i++) {
    	Format format = tracks[i].getFormat();
            if (tracks[i].isEnabled()) {
    	        supported = tracks[i].getSupportedFormats();
    
                    // We've set the output content to the RAW_RTP.
                    // So all the supported formats should work with RTP.
    
                    if (supported.length > 0) {
    			for(int j=0;j<supported.length;j++){
    	                        if (supported[j] instanceof AudioFormat) {
    					if(sessionDescription.audioFormat!=null)
    			if(supported[j].toString().toLowerCase().indexOf(sessionDescription.audioFormat.toLowerCase())!=-1)
    	chosen = supported[j];
    				}
                        	}
    		
    			if(chosen!=null){
    	                        tracks[i].setFormat(chosen);
            	                System.err.println("Track " + i + " is set to transmit as:");
                    	        System.err.println("  " + chosen);
                            	atLeastOneTrack = true;
                        	}
    		} else
    			tracks[i].setEnabled(false);
    	} else 
    		tracks[i].setEnabled(false);
    }
    
    if (!atLeastOneTrack)
    	return "Couldn't set any of the tracks to a valid RTP format";
    
    // Realize the processor. This will internally create a flow
    // graph and attempt to create an output datasource for JPEG/RTP
    // audio frames.
    result = stateListener.waitForState(processor, Controller.Realized);
    if (result == false)
    	return "Couldn't realize processor";
    
    // Get the output data source of the processor
    dataOutput = processor.getDataOutput();
    
    return null;
    			
    
    
    
    
  }

  private String createTransmitters(SessionDescription sessionDescription) {
    PushBufferDataSource pbds = (PushBufferDataSource)dataOutput;
    PushBufferStream pbss[] = pbds.getStreams();
    
    rtpMgrs = new RTPManager[pbss.length];
    SessionAddress localAddr, destAddr;
    SendStream sendStream;
    InetAddress destIpAddr, localIpAddr;
    int destPort;
    int localPort;
    
    for (int i = 0; i < pbss.length; i++) {
    	try {
            	//New instance of RTPManager
                    //to handle the RTP and RTCP transmission
                    rtpMgrs[i] = RTPManager.newInstance();
    
    // Initialize the RTPManager		
    
                    destPort = sessionDescription.remoteAudioPort + 2*i;
		    localPort = sessionDescription.localAudioPort + 2*i;
		    
                    localIpAddr = InetAddress.getByName(localIpAddress);
    		localAddr = new SessionAddress(localIpAddr,localPort);
    
    		rtpMgrs[i].initialize(localAddr);
    
    // Wait for receiving Media
    
    		MediaEventsListener mediaEventsListener = new MediaEventsListener(applet);
    		rtpMgrs[i].addReceiveStreamListener(mediaEventsListener);
    		rtpMgrs[i].addRemoteListener(mediaEventsListener);
    		rtpMgrs[i].addSendStreamListener(mediaEventsListener);
    		rtpMgrs[i].addSessionListener(mediaEventsListener);
    		mediaEventsListeners.add(mediaEventsListener);
    
    		BufferControl bc = (BufferControl) rtpMgrs[i].getControl("javax.media.control.BufferControl");
            	if (bc != null) {
            		bc.setBufferLength(0);
                    	//bc.setMinimumThreshold(0);
                    	System.out.println("Threshold enabled : "+bc.getEnabledThreshold());
                    	System.out.println("buf length : "+bc.getBufferLength());
                    	System.out.println("minimum Threshold : "+bc.getMinimumThreshold());
            	}
    
    // Connect to remote party and send data
    
                    destIpAddr = InetAddress.getByName(sessionDescription.remoteAddress);
                    destAddr = new SessionAddress( destIpAddr, destPort);
    
    
                    rtpMgrs[i].addTarget(destAddr); 
    
                    //Start the transmission with the remote host
                    sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
                    sendStream.start();
    
                } catch (Exception  e) {
    		return ("Error creating tramistters: " + e.getMessage());
                }
    }
    
    return null;
  }

  public void transmit(SessionDescription sessionDescription) {
    localIpAddress = sessionDescription.localIPAddress;
    
    String result = this.start(sessionDescription);
    
    if (result != null) {
    	System.out.println("Error when starting: " + result);
    } else {
    	System.err.println("Start transmission... ");
    	applet.setTransmitting();
    }
  }

}
