package com.mediaportal.ampdroid.data;

import org.codehaus.jackson.annotate.JsonProperty;

import com.mediaportal.ampdroid.database.ColumnProperty;

public class StreamTranscodingInfo {
   private double mCurrentBitrate;
   private int mCurrentTime;
   private int mEncodingFPS;
   private int mEncodedFrames;

   @ColumnProperty(value="CurrentBitrate", type="integer")
   @JsonProperty("CurrentBitrate")
   public double getCurrentBitrate() {
      return mCurrentBitrate;
   }

   
   @ColumnProperty(value="CurrentBitrate", type="integer")
   @JsonProperty("CurrentBitrate")
   public void setCurrentBitrate(double currentBitrate) {
      mCurrentBitrate = currentBitrate;
   }

   
   @ColumnProperty(value="CurrentTime", type="integer")
   @JsonProperty("CurrentTime")
   public int getCurrentTime() {
      return mCurrentTime;
   }

   @ColumnProperty(value="CurrentTime", type="integer")
   @JsonProperty("CurrentTime")
   public void setCurrentTime(int currentTime) {
      mCurrentTime = currentTime;
   }

   @ColumnProperty(value="EncodingFPS", type="integer")
   @JsonProperty("EncodingFPS")
   public int getEncodingFPS() {
      return mEncodingFPS;
   }

   @ColumnProperty(value="EncodingFPS", type="integer")
   @JsonProperty("EncodingFPS")
   public void setEncodingFPS(int encodingFPS) {
      mEncodingFPS = encodingFPS;
   }

   @ColumnProperty(value="EncodedFrames", type="integer")
   @JsonProperty("EncodedFrames")
   public int getEncodedFrames() {
      return mEncodedFrames;
   }

   @ColumnProperty(value="EncodedFrames", type="integer")
   @JsonProperty("EncodedFrames")
   public void setEncodedFrames(int encodedFrames) {
      mEncodedFrames = encodedFrames;
   }

}
