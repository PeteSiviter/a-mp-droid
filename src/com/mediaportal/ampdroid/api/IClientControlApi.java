package com.mediaportal.ampdroid.api;

import com.mediaportal.ampdroid.data.commands.RemoteKey;

public interface IClientControlApi extends IApiInterface {
   boolean connect();
   void disconnect();
   boolean isConnected();
   
   void setTimeOut(int _timeout);
   int getTimeOut();
   
   void addApiListener(IClientControlListener _listener);
   void clearApiListener();
   void removeApiListener(IClientControlListener _listener);
   
   void sendKeyCommand(RemoteKey _key);
   void setVolume(int level);
   int getVolume();
   void startVideo(String _path);
   void startAudio(String _path);
   void sendPlayFileCommand(String _file);
   void sendKeyDownCommand(RemoteKey _key, int _timeout);
   void sendKeyUpCommand();
   void playChannelOnClient(int _channel);
   void requestPlugins();
   void openWindow(int _windowId, String _parameter);
   void sendPowerMode(PowerModes _mode);
   void sendPosition(int _position);
   void sendRemoteKey(int keyCode, int i);
   void getClientImage(String filePath);
   
}
