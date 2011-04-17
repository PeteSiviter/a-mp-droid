package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteImageMessage {
   private byte[] mImage;
   private String mImagePath;

   @JsonProperty("Image")
   public void setImage(byte[] image) {
      mImage = image;
   }

   @JsonProperty("Image")
   public byte[] getImage() {
      return mImage;
   }

   @JsonProperty("ImagePath")
   public void setImagePath(String imagePath) {
      mImagePath = imagePath;
   }

   @JsonProperty("ImagePath")
   public String getImagePath() {
      return mImagePath;
   }
}
