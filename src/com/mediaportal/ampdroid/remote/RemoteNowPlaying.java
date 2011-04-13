package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteNowPlaying {
   private int mDuration;
   private String mFile;
   private int mPosition;
   private String mTitle;
   private String mDescription;

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

   @JsonProperty("Title")
   public String getTitle() {
      return mTitle;
   }

   @JsonProperty("Title")
   public void setTitle(String title) {
      mTitle = title;
   }

   @JsonProperty("Description")
   public String getDescription() {
      return mDescription;
   }

   @JsonProperty("Description")
   public void setDescription(String description) {
      mDescription = description;
   }

}
