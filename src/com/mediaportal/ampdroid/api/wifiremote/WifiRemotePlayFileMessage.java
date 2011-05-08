package com.mediaportal.ampdroid.api.wifiremote;

public class WifiRemotePlayFileMessage extends WifiRemoteMessage {
   public enum FileType {
      video, audio
   };

   public WifiRemotePlayFileMessage(String _file, FileType _type) {
      this.Type = "playfile";
      this.FileType = _type.toString();
      this.Filepath = _file;
   }

   public String FileType;
   public String Filepath;
}
