
package fr.free.hd.bond.chatroom.Applet;

import java.io.*;
import java.net.*;
import java.util.*;

public class HostIpAddress {
  public static InetAddress m_addr;

  public static boolean m_foundAddr;

  public static String m_strAddress;

  private Vector<String> IPaddresses;

  public HostIpAddress() {
    m_foundAddr = false;
    m_addr = null;
    m_strAddress = "";
    
    IPaddresses = new Vector<String>();
    
    try{
    	Enumeration<NetworkInterface> enumface = NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface netface : Collections.list(enumface)) {
    
    		System.out.println("Display name: " + netface.getDisplayName());
                	System.out.println("Name: " + netface.getName());
    
                    if(netface.getName().compareToIgnoreCase("lo") != 0) {
                    	Enumeration enumaddr = netface.getInetAddresses();
    
    			System.out.println("Interface is not loopback");
    
                            while (enumaddr.hasMoreElements()){
                            	//enumaddr.nextElement();
                                    m_addr = (InetAddress) enumaddr.nextElement();
                                    m_strAddress = m_addr.getHostAddress();
                                    
    				if(m_strAddress.indexOf("%") == -1 ) {
                                    	m_foundAddr = true;
                                            IPaddresses.addElement(new String(m_strAddress));
    					System.out.println("IP Address: " + m_strAddress);
    				}
                            }
                     }
           }
    } catch(NoSuchElementException un_ex) {
    	System.out.println("NoSuchElementException: Could not get IP address of local machine");
    } catch(SocketException sock_ex){
    	System.out.println("SocketException: Could not get IP address of local machine");
    }
    
  }

  public Vector getAddresses() {
    return IPaddresses;
  }

  public String getFirstAddress() {
    try {
    	if (IPaddresses.size() > 0) 
    		return (String)IPaddresses.firstElement();
    } catch (NoSuchElementException e) {
    	System.err.println("IPaddresses error: " + e.getMessage());
    }
    
    return null;
  }
  
  public String getRightAddress() {
	  String result = null;
	  
	  try {
		  if (IPaddresses.size() > 0) {
			  for (int i=0,N=IPaddresses.size();i<N;i++) {
				  if (((String)IPaddresses.get(i)).indexOf("10.10.0") > -1)
					  result = (String)IPaddresses.get(i);
			  }
			  
			  if (result != null)
				  return result;
			  else 
				  return (String)IPaddresses.firstElement();
		  }
	  } catch (NoSuchElementException e) {
		  System.err.println("IPaddresses error: " + e.getMessage());
	  }
    
    return null;
  } 

  public boolean getFoundInterfaces() {
    return m_foundAddr;
  }

}
