
package fr.free.hd.bond.chatroom.ServiceServlets;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.sdp.SessionDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.Origin;
import javax.sdp.Connection;
import javax.sdp.SdpConstants;
import javax.sdp.MediaDescription;
import javax.sdp.Media;

import javax.media.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.*;
import javax.media.control.TrackControl;

class SDPTools {
  private static Vector<AudioFormat> audioCodecSupportedList;

  private SdpFactory sdpFactory;

  public SDPTools() {
    audioCodecSupportedList = new Vector<AudioFormat>();
    audioCodecSupportedList.add(new AudioFormat(ChatroomConstants.AUDIO_CODEC));
    
    sdpFactory = SdpFactory.getInstance();
    
    
  }

  public static int getAudioPort(SessionDescription sessionDescription)
  {
    if (sessionDescription != null) {
    	Vector mediaDescriptionList = null;
    	try {                                                                                                  		
    		mediaDescriptionList = sessionDescription.getMediaDescriptions(true);
    	} catch (SdpException se) {                           
    		System.err.println("Error when parsing AudioPort in SDP: " + se.getMessage());
    	}
    
    	try {
    		for (int i = 0; i < mediaDescriptionList.size(); i++) {
    			MediaDescription mediaDescription = (MediaDescription) mediaDescriptionList.elementAt(i);
    			if (mediaDescription.getMedia().getMediaType().equals("audio"))
    				return mediaDescription.getMedia().getMediaPort();
    			}
    	} catch (SdpParseException spe) {
    		System.err.println("Error when parsing SDP AudioPort: " + spe.getMessage());
    	}
    
    	return -1;
    } else {
    	System.err.println("Error when parsing SDP: sessionDescription is NULL !!!");
    	return -1;    
    }
  }
  
  public static String getConnectionAddress(SessionDescription sessionDescription)
  {
	  String address = "";
	  if (sessionDescription != null) {
		  try {                                                                                                  		
			  address = sessionDescription.getConnection().getAddress();
			  return address;
		  } catch (SdpParseException se) {                           
			  System.err.println("Error when parsing ConnectionAddress in SDP: " + se.getMessage());
		  }
		  return address;
	  } else {
		  System.err.println("Error when parsing SDP: sessionDescription is NULL !!!");
		  return address;    
	  }
  }

  public static String getNegociatedJmfAudioCodec(SessionDescription sessionDescription)
  {
    String negotiatedSdpCodec = negotiateAudioCodec(sessionDescription);
    
    if (negotiatedSdpCodec != null)
    	return findCorrespondingJmfFormat(negotiatedSdpCodec);
    else 
    	return null;
  }

  private static String negotiateAudioCodec(SessionDescription sessionDescription)
  {
    List audioCodecList = extractAudioCodecs(sessionDescription);
    
    List<String> audioCodecSupportedSdpFormat = new Vector<String>();
    Iterator it = audioCodecSupportedList.iterator();
    
    while (it.hasNext()) {
    	String sdpCodecValue = findCorrespondingSdpFormat(((Format) it.next()).getEncoding());
            if (sdpCodecValue != null)
    	        audioCodecSupportedSdpFormat.add(sdpCodecValue);
    }
    
    //find the best codec(currently the first one which is in both list)
    Iterator iteratorSupportedCodec = audioCodecSupportedSdpFormat.iterator();
    
    while (iteratorSupportedCodec.hasNext()) {
    	String supportedCodec = (String) iteratorSupportedCodec.next();
            Iterator iteratorRemoteCodec = audioCodecList.iterator();
            
    	while (iteratorRemoteCodec.hasNext()) {
            	String remoteCodec = iteratorRemoteCodec.next().toString();
                    if (remoteCodec.equals(supportedCodec))
                    	return remoteCodec;
    	}
    }
    
    return null;
  }

  private static List extractAudioCodecs(SessionDescription sessionDescription)
  {
    List audioCodecList = new Vector();
    Vector mediaDescriptionList = null;
    
    try {
    	mediaDescriptionList = sessionDescription.getMediaDescriptions(true);
    } catch (SdpException se) {
    	System.err.println("Error when parsing SDP medias: " + se.getMessage());
    }
    
    try {
    	for (int i = 0; i < mediaDescriptionList.size(); i++) {
            	MediaDescription mediaDescription = (MediaDescription) mediaDescriptionList.elementAt(i);
                    Media media = mediaDescription.getMedia();
                    
    		if (media.getMediaType().equals("audio"))
                    	audioCodecList = media.getMediaFormats(true);
                    }
    } catch (SdpParseException spe) {
    	System.err.println("Error when parsing SDP Medias: "+ spe.getMessage());
    }
    
    return audioCodecList;
  }

  private static String findCorrespondingJmfFormat(String sdpFormatString)
  {
    int sdpFormat = -1;
    try {
    	sdpFormat = Integer.parseInt(sdpFormatString);
    } catch (NumberFormatException ex) {
    	return null;
    }
    
    switch (sdpFormat) {
    	case SdpConstants.PCMU :
            	return AudioFormat.ULAW_RTP;
            case SdpConstants.GSM :
                    return AudioFormat.GSM_RTP;
    	case SdpConstants.G723 :
                    return AudioFormat.G723_RTP;
            case SdpConstants.DVI4_8000 :
            	return AudioFormat.DVI_RTP;
            case SdpConstants.DVI4_16000 :
                    return AudioFormat.DVI_RTP;
            case SdpConstants.PCMA :
    		return AudioFormat.ALAW;
    	case SdpConstants.G728 :
                    return AudioFormat.G728_RTP;
            case SdpConstants.G729 :
                    return AudioFormat.G729_RTP;
            case SdpConstants.H263 :
                    return VideoFormat.H263_RTP;
            case SdpConstants.JPEG :
                    return VideoFormat.JPEG_RTP;
            case SdpConstants.H261 :
                    return VideoFormat.H261_RTP;
            case 99 :
            	return "mpegaudio/rtp, 48000.0 hz, 16-bit, mono";
            default :
                    return null;
    }
  }

  private static String findCorrespondingSdpFormat(String jmfFormat)
  {
    if (jmfFormat == null) {
    	return null;
    } else if (jmfFormat.equals(AudioFormat.ULAW_RTP)) {
    	return Integer.toString(SdpConstants.PCMU);
    } else if (jmfFormat.equals(AudioFormat.GSM_RTP)) {
    	return Integer.toString(SdpConstants.GSM);
    } else if (jmfFormat.equals(AudioFormat.G723_RTP)) {
    	return Integer.toString(SdpConstants.G723);
    } else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
    	return Integer.toString(SdpConstants.DVI4_8000);
    } else if (jmfFormat.equals(AudioFormat.DVI_RTP)) {
    	return Integer.toString(SdpConstants.DVI4_16000);
    } else if (jmfFormat.equals(AudioFormat.ALAW)) {
    	return Integer.toString(SdpConstants.PCMA);
    } else if (jmfFormat.equals(AudioFormat.G728_RTP)) {
    	return Integer.toString(SdpConstants.G728);
    } else if (jmfFormat.equals(AudioFormat.G729_RTP)) {
    	return Integer.toString(SdpConstants.G729);
    } else if (jmfFormat.equals(VideoFormat.H263_RTP)) {
    	return Integer.toString(SdpConstants.H263);
    } else if (jmfFormat.equals(VideoFormat.JPEG_RTP)) {
    	return Integer.toString(SdpConstants.JPEG);
    } else if (jmfFormat.equals(VideoFormat.H261_RTP)) {
    	return Integer.toString(SdpConstants.H261);
    } else {
    	return null;
    }
    
  }

  public SessionDescription generateResponseSdp(fr.free.hd.bond.chatroom.client.SipEvent sipEvent) {
    SessionDescription responseSessionDescription = null;
    
    try {
    	responseSessionDescription = sdpFactory.createSessionDescription();
    } catch (SdpParseException e) {
    	System.err.println("Error when creating Response SDP: "  + e.getMessage());
    } catch (SdpException se) {
    	System.err.println("Error when creating Response SDP: " + se.getMessage());
    } 
    
    try {
    	//Connection
            Connection connection = sdpFactory.createConnection(sipEvent.ipAdress);
            responseSessionDescription.setConnection(connection);
            //Owner
            long sdpSessionId=(long)(Math.random() * 1000000);
            Origin origin = sdpFactory.createOrigin(
            	sipEvent.subscriber,
                    sdpSessionId,
                    sdpSessionId+1369,
                    "IN",
                    "IP4",
                     sipEvent.ipAdress);
    	responseSessionDescription.setOrigin(origin);
    } catch (SdpException se) {
    	System.err.println("Error when creating Response SDP: " + se.getMessage());
    }
    
    //Media Description
    Vector<MediaDescription> mediaDescriptions = new Vector<MediaDescription>();
    if ((sipEvent.codec != null) && (sipEvent.codec != "")) {
    	MediaDescription mediaDescription = sdpFactory.createMediaDescription(
                                            "audio",
                                            sipEvent.audioPort,
                                            1,
                                            "RTP/AVP",
                                            new String[] { SDPTools.findCorrespondingSdpFormat(sipEvent.codec) });
            mediaDescriptions.add(mediaDescription);
    } 
    
    try {
    	responseSessionDescription.setMediaDescriptions(mediaDescriptions);
    } catch (SdpException se) {
    	System.err.println("Error when creating Response SDP: " + se.getMessage());
    }
    
    return responseSessionDescription;
    
    
    
  }

  public SessionDescription generateRequestSdp(fr.free.hd.bond.chatroom.client.SipEvent sipEvent) {
    SessionDescription requestSessionDescription = null;
    
    try {
    	requestSessionDescription = sdpFactory.createSessionDescription();
    } catch (SdpParseException e) {
    	System.err.println("Error when creating Response SDP: "  + e.getMessage());
    } catch (SdpException se) {
    	System.err.println("Error when creating Response SDP: " + se.getMessage());
    } 
    
    try {
    	//Connection
            Connection connection = sdpFactory.createConnection(sipEvent.ipAdress);
            requestSessionDescription.setConnection(connection);
            //Owner
            long sdpSessionId=(long)(Math.random() * 1000000);
            Origin origin = sdpFactory.createOrigin(
            	sipEvent.fromUser,
                    sdpSessionId,
                    sdpSessionId+1369,
                    "IN",
                    "IP4",
                     sipEvent.ipAdress);
    	requestSessionDescription.setOrigin(origin);
    } catch (SdpException se) {
    	System.err.println("Error when creating Response SDP: " + se.getMessage());
    }
    
    //Media Description
    Vector<MediaDescription> mediaDescriptions = new Vector<MediaDescription>();
    if ((sipEvent.codec != null) && (sipEvent.codec != "")) {
    	MediaDescription mediaDescription = sdpFactory.createMediaDescription(
                                            "audio",
                                            sipEvent.audioPort,
                                            1,
                                            "RTP/AVP",
                                            new String[] { SDPTools.findCorrespondingSdpFormat(sipEvent.codec) });
            mediaDescriptions.add(mediaDescription);
    } 
    
    try {
    	requestSessionDescription.setMediaDescriptions(mediaDescriptions);
    } catch (SdpException se) {
    	System.err.println("Error when creating Response SDP: " + se.getMessage());
    }
    
    return requestSessionDescription;
    
    
    
  }

}
