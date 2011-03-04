package com.mediaportal.ampdroid.api;

import java.util.List;

import com.mediaportal.ampdroid.data.ClientPlugin;
import com.mediaportal.ampdroid.data.commands.RemoteKey;

public interface IClientControlApi extends IApiInterface {
   boolean connect();
   void disconnect();
   boolean isConnected();
   
   void setTimeOut(int _timeout);
   int getTimeOut();
   
   void addApiListener(IClientControlListener _listener);
   
   void sendKeyCommand(RemoteKey _key);
   void setVolume(int level);
   int getVolume();
   void startVideo(String _path);
   void sendPlayFileCommand(String _file);
   void sendKeyDownCommand(RemoteKey _key, int _timeout);
   void sendKeyUpCommand();
   void playChannelOnClient(int _channel);
   List<ClientPlugin> getPlugins();
   void openPlugin(ClientPlugin _plugin);
   
}