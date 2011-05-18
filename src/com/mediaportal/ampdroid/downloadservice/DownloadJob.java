package com.mediaportal.ampdroid.downloadservice;

public class DownloadJob {
   private int mJobId;
   private String mFileName;
   private String mDisplayName;
   private String mUrl;
   private long mLength;
   private boolean mUseAut;
   private String mUsername;
   private String mPassword;
   private int mGroupId;
   
   public DownloadJob(){
      
   }
   
   DownloadJob(int _jobId, int _groupId) {
      this();
      mJobId = _jobId;
      mGroupId = _groupId;
   }
   
   DownloadJob(int _jobId, int _groupId, String _displayName, String _fileName, String _url, long _length) {
      this(_jobId, _groupId);
      mDisplayName = _displayName;
      mFileName = _fileName;
      mUrl = _url;
      setLength(_length);
   }
   
   public void setAuth(String _username, String _password) {
      mUseAut = true;
      mUsername = _username;
      mPassword = _password;
   }

   public int getId() {
      return mJobId;
   }

   public void setId(int _id) {
      this.mJobId = _id;
   }

   public String getFileName() {
      return mFileName;
   }

   public String getShortenedName() {
      String name = mFileName;
      if (name.length() > 30) {
         name = name.substring(name.length() - 30);
      }
      return name;
   }

   public void setFileName(String _name) {
      this.mFileName = _name;
   }

   public String getUrl() {
      return mUrl;
   }

   public void setUrl(String _url) {
      this.mUrl = _url;
   }
   
   public void setLength(long length) {
      mLength = length;
   }

   public long getLength() {
      return mLength;
   }
   
   public boolean isUseAut() {
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

   public void setGroupId(int groupId) {
      mGroupId = groupId;
   }

   public int getGroupId() {
      return mGroupId;
   }

   public void setDisplayName(String displayName) {
      mDisplayName = displayName;
   }

   public String getDisplayName() {
      return mDisplayName;
   }
}

