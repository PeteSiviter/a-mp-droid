package com.mediaportal.ampdroid.api.wifiremote;

public class WifiRemoteImageMessage extends WifiRemoteMessage {
   public WifiRemoteImageMessage(String _path){
      this.Type = "image";
      this.ImagePath = _path;
   }
   
   public String ImagePath;
}
