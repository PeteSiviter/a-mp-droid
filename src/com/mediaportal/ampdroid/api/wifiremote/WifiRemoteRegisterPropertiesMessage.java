package com.mediaportal.ampdroid.api.wifiremote;

import java.util.ArrayList;
import java.util.List;

public class WifiRemoteRegisterPropertiesMessage  extends WifiRemoteMessage {
   public WifiRemoteRegisterPropertiesMessage(){
      this.Type = "properties";
      this.Properties = new ArrayList<String>();
   }
   
   public List<String> Properties;
   
}
