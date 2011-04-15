package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteNowPlaying {
   private int mDuration;
   private String mFile;
   private int mPosition;
   private boolean mIsTv;

   @JsonProperty("Duration")
   public int getDuration() {
      return mDuration;
   }

   @JsonProperty("Duration")
   public void setDuration(int duration) {
      mDuration = duration;
   }

   @JsonProperty("File")
   public String getFile() {
      return mFile;
   }

   @JsonProperty("File")
   public void setFile(String file) {
      mFile = file;
   }

   @JsonProperty("Position")
   public int getPosition() {
      return mPosition;
   }

   @JsonProperty("Position")
   public void setPosition(int position) {
      mPosition = position;
   }
   
   @JsonProperty("IsTv")
   public void setIsTv(boolean isTv) {
      mIsTv = isTv;
   }

   @JsonProperty("IsTv")
   public boolean isTv() {
      return mIsTv;
   }

}
