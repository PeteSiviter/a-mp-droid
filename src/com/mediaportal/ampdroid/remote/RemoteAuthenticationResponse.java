package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteAuthenticationResponse {
   private boolean mSuccess;
   private String mErrorMessage;

   @JsonProperty("Success")
   public boolean isSuccess() {
      return mSuccess;
   }

   @JsonProperty("Success")
   public void setSuccess(boolean success) {
      mSuccess = success;
   }

   @JsonProperty("ErrorMessage")
   public String getErrorMessage() {
      return mErrorMessage;
   }

   @JsonProperty("ErrorMessage")
   public void setErrorMessage(String errorMessage) {
      mErrorMessage = errorMessage;
   }

}
