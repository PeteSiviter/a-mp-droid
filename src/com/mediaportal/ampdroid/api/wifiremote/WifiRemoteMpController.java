package com.mediaportal.ampdroid.api.wifiremote;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.os.AsyncTask;

import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;
import com.mediaportal.ampdroid.api.ConnectionState;
import com.mediaportal.ampdroid.api.IClientControlApi;
import com.mediaportal.ampdroid.api.IClientControlListener;
import com.mediaportal.ampdroid.api.PowerModes;
import com.mediaportal.ampdroid.data.commands.RemoteKey;
import com.mediaportal.ampdroid.remote.RemoteImageMessage;
import com.mediaportal.ampdroid.remote.RemoteNowPlaying;
import com.mediaportal.ampdroid.remote.RemoteNowPlayingUpdate;
import com.mediaportal.ampdroid.remote.RemotePluginMessage;
import com.mediaportal.ampdroid.remote.RemoteProperties;
import com.mediaportal.ampdroid.remote.RemotePropertiesUpdate;
import com.mediaportal.ampdroid.remote.RemoteStatusMessage;
import com.mediaportal.ampdroid.remote.RemoteVolumeMessage;
import com.mediaportal.ampdroid.remote.RemoteWelcomeMessage;
import com.mediaportal.ampdroid.utils.SoftkeyboardUtils;

public class WifiRemoteMpController implements IClientControlApi {
   private String mServer;
   private int mPort;

   private String mUser;
   private String mPass;
   private boolean mUseAuth;

   private Socket mSocket;
   private DataInputStream mInstream;
   private DataOutputStream mOutstream;
   private TcpListenerTask mTcpReader;
   private List<IClientControlListener> mListeners;
   private ObjectMapper mJsonObjectMapper;
   private Context mContext;
   private GsonBuilder gsonb;
   private Gson gson;
   private BufferedReader input;;

   private class TcpListenerTask extends AsyncTask<DataInputStream, Object, ConnectionState> {
      private List<IClientControlListener> listeners;
      private boolean listening = false;
      

      public boolean isListening() {
         return listening;
      }

      public void setListening(boolean listening) {
         this.listening = listening;
      }

      public TcpListenerTask(List<IClientControlListener> _listeners) {
         listeners = _listeners;
      }

      @Override
      @SuppressWarnings("unchecked")
      protected ConnectionState doInBackground(DataInputStream... params) {
         listening = true;
         try {
            input = new BufferedReader(new InputStreamReader(mInstream, "UTF8"));
            
            while (listening) {
               String response = input.readLine();

               ObjectMapper mapper = new ObjectMapper();
               Map<String, Object> userData = mapper.readValue(response, Map.class);
               if (userData.containsKey("Type")) {
                  String type = (String) userData.get("Type");
                  if (type != null) {
                     if (type.equals("status")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemoteStatusMessage.class);
                        publishProgress(returnObject);
                     } else if (type.equals("nowplaying")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemoteNowPlaying.class);
                        publishProgress(returnObject);
                     } else if (type.equals("nowplayingupdate")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemoteNowPlayingUpdate.class);
                        publishProgress(returnObject);
                     } else if (type.equals("properties")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemoteProperties.class);

                        RemoteProperties props = (RemoteProperties) returnObject;
                        for (RemotePropertiesUpdate p : props.getProperties()) {
                           publishProgress(p);
                        }
                     } else if (type.equals("propertychanged")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemotePropertiesUpdate.class);
                        publishProgress(returnObject);
                     } else if (type.equals("plugins")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemotePluginMessage.class);
                        publishProgress(returnObject);
                     } else if (type.equals("welcome")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemoteWelcomeMessage.class);
                        publishProgress(returnObject);
                     } else if (type.equals("volume")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemoteVolumeMessage.class);
                        publishProgress(returnObject);
                     } else if (type.equals("image")) {
                        Object returnObject = mJsonObjectMapper.readValue(response,
                              RemoteImageMessage.class);
                        publishProgress(returnObject);
                     }
                  }
               }
            }
         } catch (IOException e) {
            e.printStackTrace();
            listening = false;
         } catch (Exception e) {
            e.printStackTrace();
            listening = false;
         }
         this.listening = false;
         mInstream = null;
         mSocket = null;
         return ConnectionState.Disconnected;
      }

      @Override
      protected void onPostExecute(ConnectionState result) {
         publishState(result);
      }
      
      

      @Override
      protected void onCancelled() {
         super.onCancelled();
         publishState(ConnectionState.Disconnected);
      }

      /*
       * (non-Javadoc)
       * 
       * @see android.os.AsyncTask#onPreExecute()
       */
      @Override
      protected void onPreExecute() {
         // Things to be done before execution of long running operation. For
         // example showing ProgessDialog
      }

      /*
       * (non-Javadoc)
       * 
       * @see android.os.AsyncTask#onProgressUpdate(Progress[])
       */
      @Override
      protected void onProgressUpdate(Object... values) {
         // Things to be done while execution of long running operation is in
         // progress. For example updating ProgessDialog
         for (IClientControlListener l : listeners) {
            l.messageReceived(values[0]);
         }
      }
   }

   public WifiRemoteMpController(Context _context, String _server, int _port, String _user,
         String _pass, boolean _auth) {
      super();
      mServer = _server;
      mPort = _port;

      mUser = _user;
      mPass = _pass;
      mUseAuth = _auth;

      mListeners = new ArrayList<IClientControlListener>();
      mContext = _context;

      gsonb = new GsonBuilder();
      gson = gsonb.create();

      mJsonObjectMapper = new ObjectMapper();
      mJsonObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public WifiRemoteMpController(Context _context, String _server, int _port) {
      this(_context, _server, _port, "", "", false);
   }

   @Override
   public String getServer() {
      return mServer;
   }

   @Override
   public int getPort() {
      return mPort;
   }

   @Override
   public String getAddress() {
      return mServer;
   }

   @Override
   public String getUserName() {
      return mUser;
   }

   @Override
   public String getUserPass() {
      return mPass;
   }

   @Override
   public boolean getUseAuth() {
      return mUseAuth;
   }

   @Override
   public int getTimeOut() {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public void setTimeOut(int timeout) {
      // TODO Auto-generated method stub

   }

   /**
    * Connect to client
    */
   public boolean connect() {
      try {
         mSocket = new Socket();
         SocketAddress socketAddress = new InetSocketAddress(mServer, mPort);

         mSocket.connect(socketAddress, 2000);

         // outgoing stream redirect to socket
         mOutstream = new DataOutputStream(mSocket.getOutputStream());
         mInstream = new DataInputStream(mSocket.getInputStream());

         mTcpReader = new TcpListenerTask(mListeners);
         mTcpReader.execute(mInstream);

         if (mSocket.isConnected()) {
            publishState(ConnectionState.Connected);
         } else {
            publishState(ConnectionState.Disconnected);
         }

         registerProperties();

         return true;

      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return false;
   }

   private void registerProperties() {
      WifiRemoteRegisterPropertiesMessage register = new WifiRemoteRegisterPropertiesMessage();
      register.Properties.add("#Play.Current.Title");
      register.Properties.add("#Play.Current.File");
      register.Properties.add("#Play.Current.Thumb");
      register.Properties.add("#Play.Current.Plot");
      register.Properties.add("#Play.Current.PlotOutline");
      register.Properties.add("#Play.Current.Channel");
      register.Properties.add("#Play.Current.Genre");
      register.Properties.add("#Play.Current.Title");
      register.Properties.add("#Play.Current.Artist");
      register.Properties.add("#Play.Current.Album");
      register.Properties.add("#Play.Current.Track");
      register.Properties.add("#Play.Current.Year");
      register.Properties.add("#TV.View.channel");
      register.Properties.add("#TV.View.thumb");
      register.Properties.add("#TV.View.start");
      register.Properties.add("#TV.View.stop");
      register.Properties.add("#TV.View.remaining");
      register.Properties.add("#TV.View.genre");
      register.Properties.add("#TV.View.title");
      register.Properties.add("#TV.View.description");
      register.Properties.add("#TV.Next.start");
      register.Properties.add("#TV.Next.stop");
      register.Properties.add("#TV.Next.title");
      register.Properties.add("#TV.Next.description");

      String msgString = gson.toJson(register);
      writeLine(msgString);
   }

   private void publishState(ConnectionState _state) {
      for (IClientControlListener l : mListeners) {
         l.stateChanged(_state);
      }

   }

   @Override
   public void disconnect() {
      try {

         if (mSocket != null) {
            mSocket.close();
         }
         if (mTcpReader != null) {
            mTcpReader.cancel(true);
         }
         mSocket = null;
         mTcpReader = null;
         mInstream = null;
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   @Override
   public void addApiListener(IClientControlListener _listener) {
      mListeners.add(_listener);
   }

   @Override
   public void clearApiListener() {
      mListeners.clear();
   }

   @Override
   public void removeApiListener(IClientControlListener _listener) {
      mListeners.remove(_listener);

   }

   @Override
   public void sendKeyCommand(RemoteKey _key) {
      String msgString = gson.toJson(new WifiRemoteMessageKey(_key));
      writeLine(msgString);
   }

   @Override
   public void sendKeyDownCommand(RemoteKey _key, int _timeout) {
      String msgString = gson.toJson(new WifiRemoteKeyDownMessage(_key, _timeout));
      writeLine(msgString);
   }

   @Override
   public void sendKeyUpCommand() {
      String msgString = gson.toJson(new WifiRemoteKeyUpMessage());
      writeLine(msgString);
   }

   @Override
   public void sendPowerMode(PowerModes _mode) {
      String msgString = gson.toJson(new WifiRemotePowermodeMessage(_mode));
      writeLine(msgString);
   }

   @Override
   public void sendPlayFileCommand(String _file) {
      String msgString = gson.toJson(new WifiRemotePlayFileMessage(_file));
      writeLine(msgString);
   }

   @Override
   public void sendPosition(int _position) {
      String msgString = gson.toJson(new WifiRemotePositionMessage(_position));
      writeLine(msgString);
   }

   private boolean writeLine(String msgString) {
      try {
         if (mOutstream != null) {
            byte[] buffer = msgString.getBytes();
            mOutstream.write(buffer, 0, buffer.length);
            // outstream.writeUTF(msgString);
            // outstream.writeChars(msgString);
            mOutstream.writeByte(13);
            mOutstream.writeByte(10);

            mOutstream.flush();
            return true;
         }
      } catch (IOException e) {
      }
      return false;
   }

   @Override
   public boolean isConnected() {
      return (mSocket != null && mSocket.isConnected() && mTcpReader != null && mTcpReader
            .isListening());
   }

   @Override
   public int getVolume() {
      return 0;
   }

   @Override
   public void setVolume(int level) {
      String msgString = gson.toJson(new WifiRemoteMessageSetVolume(level));
      writeLine(msgString);
   }

   @Override
   public void startVideo(String _path) {
      String msgString = gson.toJson(new WifiRemotePlayFileMessage(_path));
      writeLine(msgString);
   }

   @Override
   public void playChannelOnClient(int _channel) {
      String msgString = gson.toJson(new WifiRemoteStartTvMessage(_channel));
      writeLine(msgString);
   }

   @Override
   public void requestPlugins() {
      String msgString = gson.toJson(new WifiRemoteMessage("plugins"));
      writeLine(msgString);
   }

   @Override
   public void openWindow(int _windowId, String _parameter) {
      String msgString = gson.toJson(new WifiRemoteOpenWindowMessage(_windowId));
      writeLine(msgString);
   }

   @Override
   public void sendRemoteKey(int keyCode, int i) {
      String s = SoftkeyboardUtils.getChar(keyCode);
      if(s != null){
         String msgString = gson.toJson(new WifiRemoteKeyMessage(s));
         writeLine(msgString);
      }
   }

   @Override
   public void getClientImage(String _filePath) {
      String msgString = gson.toJson(new WifiRemoteImageMessage(_filePath));
      writeLine(msgString);
   }
}
