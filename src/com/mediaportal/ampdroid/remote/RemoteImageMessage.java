package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteImageMessage {
   private byte[] mImage;

   @JsonProperty("Image")
   public void setImage(byte[] image) {
      mImage = image;
   }

   @JsonProperty("Image")
   public byte[] getImage() {
      return mImage;
   }
}
