package com.mediaportal.ampdroid.api;

public interface IApiInterface {
   public String getServer();
   public int getPort();
   public String getAddress();
   public String getMac();
   
   public String getUserName();
   public String getUserPass();
   public boolean getUseAuth();
}
