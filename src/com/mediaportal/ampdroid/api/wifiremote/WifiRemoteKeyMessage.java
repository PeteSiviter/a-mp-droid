package com.mediaportal.ampdroid.api.wifiremote;


public class WifiRemoteKeyMessage extends WifiRemoteMessage {
      public WifiRemoteKeyMessage(String _key){
         this.Type = "key";
         this.Key = _key;
      }
      
      public String Key;
}
