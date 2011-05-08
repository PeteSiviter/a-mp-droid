package com.mediaportal.ampdroid.api.wifiremote;

import com.mediaportal.ampdroid.data.commands.RemoteKey;

public class WifiRemoteKeyDownMessage extends WifiRemoteMessage {
   public WifiRemoteKeyDownMessage(RemoteKey _key, int _pause){
      this.Type = "commandstartrepeat";
      this.Command = _key.getAction();
      this.Pause = _pause;
   }
   
   public String Command;
   public int Pause;
}
