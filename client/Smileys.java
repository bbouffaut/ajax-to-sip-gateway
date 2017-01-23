
package fr.free.hd.bond.chatroom.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.*;
import com.google.gwt.user.client.*;

class Smileys {
  public static int NB_BUTTONS = 7;

  public static int NB_SMILEYS = 13;

  public Image[] commentButtonsImages;

  public String[] commentButtonsSmileys;

  public String[] commentButtonsRegexp;

  public Smileys() {
    commentButtonsImages = new Image[NB_SMILEYS];
    commentButtonsSmileys = new String[NB_BUTTONS];
    commentButtonsRegexp = new String[NB_SMILEYS];
    
    commentButtonsImages[0] = new Image("./images/emoticons/eyeroll.png");
    commentButtonsImages[1] = new Image("./images/emoticons/megasmile.png");
    commentButtonsImages[2] = new Image("./images/emoticons/regular.png");
    commentButtonsImages[3] = new Image("./images/emoticons/sad.png");
    commentButtonsImages[4] = new Image("./images/emoticons/shade.png");
    commentButtonsImages[5] = new Image("./images/emoticons/tongue.png");
    commentButtonsImages[6] = new Image("./images/emoticons/wink.png");
    commentButtonsImages[7] = new Image("./images/emoticons/eyeroll.png");
    commentButtonsImages[8] = new Image("./images/emoticons/regular.png");
    commentButtonsImages[9] = new Image("./images/emoticons/sad.png");
    commentButtonsImages[10] = new Image("./images/emoticons/shade.png");
    commentButtonsImages[11] = new Image("./images/emoticons/tongue.png");
    commentButtonsImages[12] = new Image("./images/emoticons/wink.png");
    
    commentButtonsSmileys[0] = "!-)";
    commentButtonsSmileys[1] = "MDR";
    commentButtonsSmileys[2] = ":-)";
    commentButtonsSmileys[3] = ":-(";
    commentButtonsSmileys[4] = "8-)";
    commentButtonsSmileys[5] = ":-p";
    commentButtonsSmileys[6] = ";-)";
    
    commentButtonsRegexp[0] = "!-\\)";
    commentButtonsRegexp[1] = "MDR";
    commentButtonsRegexp[2] = ":-\\)";
    commentButtonsRegexp[3] = ":-\\(";
    commentButtonsRegexp[4] = "8-\\)";
    commentButtonsRegexp[5] = ":-p";
    commentButtonsRegexp[6] = "\\;-\\)";
    commentButtonsRegexp[7] = "!\\)";
    commentButtonsRegexp[8] = ":\\)";
    commentButtonsRegexp[9] = ":\\(";
    commentButtonsRegexp[10] = "8\\)";
    commentButtonsRegexp[11] = ":p";
    commentButtonsRegexp[12] = "\\;\\)";
  }

}
