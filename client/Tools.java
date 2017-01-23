
package fr.free.hd.bond.chatroom.client;

import java.util.Date;

class Tools {
static {
}

  public static String getTimeHumanReadable()
  {
    Date calendar = new Date();
    
    String month = adaptTime(new Integer(calendar.getMonth() + 1).toString(),2);
    
    String day = adaptTime(new Integer(calendar.getDate()).toString(),2);
    
    String hour = adaptTime(new Integer(calendar.getHours()).toString(),2);
    
    String minute = adaptTime(new Integer(calendar.getMinutes()).toString(),2);
    
    String result = day.concat("/" + month).concat(" - " + hour).concat(":" + minute);
    
    return result;
  }

  private static String adaptTime(String timeString, int size)
  {
    int delta = size - timeString.length();
    String result = timeString;
    
    for (int i=0;i<delta;i++) {
    	result = "0" + result;
    }
    
    return result;
  }

}
