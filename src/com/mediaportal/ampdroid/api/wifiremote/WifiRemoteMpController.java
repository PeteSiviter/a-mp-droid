package com.mediaportal.ampdroid.api.wifiremote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.os.AsyncTask;

import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;
import com.mediaportal.ampdroid.api.IClientControlApi;
import com.mediaportal.ampdroid.api.IClientControlListener;
import com.mediaportal.ampdroid.api.PowerModes;
import com.mediaportal.ampdroid.data.ClientPlugin;
import com.mediaportal.ampdroid.data.NowPlaying;
import com.mediaportal.ampdroid.data.commands.RemoteKey;
import com.mediaportal.ampdroid.remote.RemoteImageMessage;
import com.mediaportal.ampdroid.remote.RemoteNowPlaying;
import com.mediaportal.ampdroid.remote.RemoteNowPlayingUpdate;
import com.mediaportal.ampdroid.remote.RemotePlugin;
import com.mediaportal.ampdroid.remote.RemotePluginMessage;
import com.mediaportal.ampdroid.remote.RemoteStatusMessage;
import com.mediaportal.ampdroid.remote.RemoteVolumeMessage;
import com.mediaportal.ampdroid.remote.RemoteWelcomeMessage;

public class WifiRemoteMpController implements IClientControlApi {
   private String server;
   private int port;
   
   private String m_user;
   private String m_pass;
   private boolean m_useAuth;
   
   private Socket socket;
   private DataInputStream instream;
   private DataOutputStream outstream;
   private TcpListenerTask tcpreader;
   private List<IClientControlListener> listeners;
   private ObjectMapper mJsonObjectMapper;

   private class TcpListenerTask extends AsyncTask<DataInputStream, Object, String> {
      private List<IClientControlListener> listeners;
      private boolean listening = false;;

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
      protected String doInBackground(DataInputStream... params) {
         listening = true;
         while (listening) {
            try {
               String response = instream.readLine();

               ObjectMapper mapper = new ObjectMapper();
               Map<String, Object> userData = mapper.readValue(response, Map.class);
               if (userData.containsKey("Type")) {
                  String type = (String) userData.get("Type");
                  if (type != null) {
                     if (type.equals("status")) {
                        Object returnObject = mJsonObjectMapper.readValue(response, RemoteStatusMessage.class);
                        publishProgress(returnObject);
                     } else if (type.equals("nowplaying")) {
                        Object returnObject = mJsonObjectMapper.readValue(response, RemoteNowPlaying.class);
                        publishProgress(returnObject);
                     }else if (type.equals("nowplayingupdate")) {
                        Object returnObject = mJsonObjectMapper.readValue(response, RemoteNowPlayingUpdate.class);
                        publishProgress(returnObject);
                     }else if (type.equals("plugins")){
                        Object returnObject = mJsonObjectMapper.readValue(response, RemotePluginMessage.class);
                        publishProgress(returnObject);
                     }else if (type.equals("welcome")){
                        Object returnObject = mJsonObjectMapper.readValue(response, RemoteWelcomeMessage.class);
                        publishProgress(returnObject);
                     }else if(type.equals("volume")){
                        Object returnObject = mJsonObjectMapper.readValue(response, RemoteVolumeMessage.class);
                        publishProgress(returnObject);
                     }else if(type.equals("image")){
                        Object returnObject = mJsonObjectMapper.readValue(response, RemoteImageMessage.class);
                        publishProgress(returnObject);
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
         }
         this.listening = false;
         return "Socket closed";
      }

      /*
       * (non-Javadoc)
       * 
       * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
       */
      @Override
      protected void onPostExecute(String result) {
         publishState(result);
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

   public WifiRemoteMpController(String _server, int _port, String _user, String _pass, boolean _auth) {
      super();
      this.server = _server;
      this.port = _port;
      this.listeners = new ArrayList<IClientControlListener>();
      
      mJsonObjectMapper = new ObjectMapper();
      mJsonObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }
   
   public WifiRemoteMpController(String _server, int _port) {
      this(_server, _port, "", "", false);
   }

   @Override
   public String getServer() {
      return server;
   }

   @Override
   public int getPort() {
      return port;
   }

   @Override
   public String getAddress() {
      return server;
   }
   
   @Override
   public String getUserName() {
      return m_user;
   }

   @Override
   public String getUserPass() {
      return m_pass;
   }

   @Override
   public boolean getUseAuth() {
      return m_useAuth;
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
         socket = new Socket();
         SocketAddress socketAddress = new InetSocketAddress(server, port);

         socket.connect(socketAddress, 2000);

         // outgoing stream redirect to socket
         outstream = new DataOutputStream(socket.getOutputStream());
         instream = new DataInputStream(socket.getInputStream());

         tcpreader = new TcpListenerTask(listeners);
         tcpreader.execute(instream);

         publishState("State: " + socket.isConnected());

         return true;

      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return false;
   }

   private void publishState(String _state) {
      for (IClientControlListener l : listeners) {
         l.stateChanged(_state);
      }

   }

   @Override
   public void disconnect() {
      try {
         tcpreader.cancel(true);
         socket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   @Override
   public void addApiListener(IClientControlListener _listener) {
      listeners.add(_listener);
   }

   @Override
   public void sendKeyCommand(RemoteKey _key) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      // String msgString = gson.toJson(new WifiRemoteMessage("command","",
      // _key.getAction()));
      String msgString = gson.toJson(new WifiRemoteMessageKey(_key));
      writeLine(msgString);
   }

   @Override
   public void sendKeyDownCommand(RemoteKey _key, int _timeout) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      // String msgString = gson.toJson(new WifiRemoteMessage("command","",
      // _key.getAction()));
      String msgString = gson.toJson(new WifiRemoteKeyDownMessage(_key, _timeout));
      writeLine(msgString);
   }

   @Override
   public void sendKeyUpCommand() {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      // String msgString = gson.toJson(new WifiRemoteMessage("command","",
      // _key.getAction()));
      String msgString = gson.toJson(new WifiRemoteKeyUpMessage());
      writeLine(msgString);
   }

   @Override
   public void sendPowerMode(PowerModes _mode) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      // String msgString = gson.toJson(new WifiRemoteMessage("command","",
      // _key.getAction()));
      String msgString = gson.toJson(new WifiRemotePowermodeMessage(_mode));
      writeLine(msgString);
   }

   @Override
   public void sendPlayFileCommand(String _file) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      String msgString = gson.toJson(new WifiRemotePlayFileMessage(_file));

      writeLine(msgString);
   }
   
   @Override
   public void sendPosition(int _position) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      String msgString = gson.toJson(new WifiRemotePositionMessage(_position));

      writeLine(msgString);
   }

   private boolean writeLine(String msgString) {
      try {
         if (outstream != null) {
            byte[] buffer = msgString.getBytes();
            outstream.write(buffer, 0, buffer.length);
            // outstream.writeUTF(msgString);
            // outstream.writeChars(msgString);
            outstream.writeByte(13);
            outstream.writeByte(10);

            outstream.flush();
            return true;
         }
      } catch (IOException e) {
      }
      return false;
   }

   @Override
   public boolean isConnected() {
      return (socket != null && socket.isConnected() && tcpreader != null && tcpreader
            .isListening());
   }

   @Override
   public int getVolume() {
      return 0;
   }

   @Override
   public void setVolume(int level) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      String msgString = gson.toJson(new WifiRemoteMessageSetVolume(level));

      writeLine(msgString);
   }

   @Override
   public void startVideo(String _path) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      String msgString = gson.toJson(new WifiRemotePlayFileMessage(_path));

      writeLine(msgString);
   }

   @Override
   public void playChannelOnClient(int _channel) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      String msgString = gson.toJson(new WifiRemoteStartTvMessage(_channel));

      writeLine(msgString);
   }

   @Override
   public void requestPlugins() {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      String msgString = gson.toJson(new WifiRemoteMessage("plugins"));

      writeLine(msgString);
   }

   @Override
   public void openWindow(int _windowId, String _parameter) {
      GsonBuilder gsonb = new GsonBuilder();
      Gson gson = gsonb.create();
      String msgString = gson.toJson(new WifiRemoteOpenWindowMessage(_windowId));

      writeLine(msgString);
   }

}
