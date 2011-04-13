package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteNowPlayingUpdate {
   private int mDuration;
   private int mPosition;
   private int mSpeed;
   
   @JsonProperty("Duration")
   public int getDuration() {
      return mDuration;
   }
   
   @JsonProperty("Duration")
   public void setDuration(int duration) {
      mDuration = duration;
   }
   
   @JsonProperty("Position")
   public int getPosition() {
      return mPosition;
   }
   
   @JsonProperty("Position")
   public void setPosition(int position) {
      mPosition = position;
   }
   
   @JsonProperty("Speed")
   public int getSpeed() {
      return mSpeed;
   }
   
   @JsonProperty("Speed")
   public void setSpeed(int speed) {
      mSpeed = speed;
   }

}
