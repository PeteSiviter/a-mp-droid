package com.mediaportal.ampdroid.api.wifiremote;


public class WifiRemotePositionMessage extends WifiRemoteMessage {
   public WifiRemotePositionMessage(int _position){
      this.Type = "position";
      this.Position = _position;
      this.SeekType = 0;
   }
   
   public int Position;
   public int SeekType;
}
