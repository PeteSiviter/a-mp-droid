package com.mediaportal.ampdroid.api;

public class ApiCredentials {
   private boolean mUseAut;
   private String mUsername;
   private String mPassword;
   
   
   public boolean useAut() {
      return mUseAut;
   }
   public void setUseAut(boolean useAut) {
      mUseAut = useAut;
   }
   public String getUsername() {
      return mUsername;
   }
   public void setUsername(String username) {
      mUsername = username;
   }
   public String getPassword() {
      return mPassword;
   }
   public void setPassword(String password) {
      mPassword = password;
   }
   
   
}
