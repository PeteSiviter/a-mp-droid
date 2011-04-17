package com.mediaportal.ampdroid.activities;

import java.util.List;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseActivity.ConnectClientControlTask;
import com.mediaportal.ampdroid.activities.settings.SettingsActivity;
import com.mediaportal.ampdroid.api.ConnectionState;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.api.IClientControlListener;
import com.mediaportal.ampdroid.data.RemoteClient;
import com.mediaportal.ampdroid.database.RemoteClientsDatabaseHandler;
import com.mediaportal.ampdroid.remote.RemoteNowPlaying;
import com.mediaportal.ampdroid.remote.RemoteNowPlayingUpdate;
import com.mediaportal.ampdroid.remote.RemotePropertiesUpdate;
import com.mediaportal.ampdroid.remote.RemoteStatusMessage;
import com.mediaportal.ampdroid.remote.RemoteVolumeMessage;
import com.mediaportal.ampdroid.remote.RemoteWelcomeMessage;
import com.mediaportal.ampdroid.settings.PreferencesManager;
import com.mediaportal.ampdroid.settings.PreferencesManager.StatusbarAutohide;
import com.mediaportal.ampdroid.utils.Util;

public class BaseTabActivity extends TabActivity implements IClientControlListener {
   protected class ConnectClientControlTask extends AsyncTask<String, String, Boolean> {
      private Context mContext;

      protected ConnectClientControlTask(Context _parent) {
         mContext = _parent;
      }

      @Override
      protected Boolean doInBackground(String... _keys) {
         if (!mService.isClientControlConnected()) {
            return mService.connectClientControl();
         }
         return false;
      }

      @Override
      protected void onPostExecute(Boolean _result) {
         super.onPostExecute(_result);
         if (_result) {
            Util.showToast(mContext, mContext.getString(R.string.connection_successful));
         } else {
            Util.showToast(mContext, mContext.getString(R.string.connection_failed));
         }
         mConnectTask = null;
      }
   }

   protected DataHandler mService;
   protected StatusBarActivityHandler mStatusBarHandler;
   private ConnectClientControlTask mConnectTask;
   private MenuItem mConnectItem;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);

      PreferencesManager.intitialisePreferencesManager(this);

      if (PreferencesManager.isFullscreen()) {
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
               WindowManager.LayoutParams.FLAG_FULLSCREEN);
      }

      mService = DataHandler.getCurrentRemoteInstance();
   }

   @Override
   protected void onStart() {
      super.onStart();
      mStatusBarHandler = new StatusBarActivityHandler(this, mService);
      mService.addClientControlListener(this);

      handleAutoHide();

   }

   private void handleAutoHide() {
      StatusbarAutohide autohide = PreferencesManager.getStatusbarAutohide();

      switch (autohide) {
      case AlwaysHide:
         mStatusBarHandler.hide();
         break;
      case AlwaysShow:
         mStatusBarHandler.show();
         break;
      case ShowWhenConnected:
         if (mService.isClientControlConnected()) {
            mStatusBarHandler.show();
         } else {
            mStatusBarHandler.hide();
         }
         break;
      }
   }

   @Override
   protected void onStop() {
      super.onStop();
      mService.removeClientControlListener(this);
   }

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      menu.setHeaderTitle(getString(R.string.menu_clients));

      RemoteClientsDatabaseHandler remoteClientsDb = new RemoteClientsDatabaseHandler(this);
      remoteClientsDb.open();
      List<RemoteClient> clients = remoteClientsDb.getClients();
      remoteClientsDb.close();

      for (int i = 0; i < clients.size(); i++) {
         MenuItem item = menu.add(0, Menu.FIRST, Menu.NONE, clients.get(i).getClientName());
         item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
               // TODO: Client switching
               notImplemented();
               return false;
            }
         });
      }
   }

   protected void notImplemented() {
      Util.showToast(this, getString(R.string.info_not_implemented));
   }

   @Override
   public void onAttachedToWindow() {
      super.onAttachedToWindow();
      Window window = getWindow();
      window.setFormat(PixelFormat.RGBA_8888);
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      if (mConnectItem != null) {
         String title = null;
         if (!mService.isClientControlConnected()) {
            title = getString(R.string.menu_connect);
         } else {
            title = getString(R.string.menu_disconnect);
         }
         mConnectItem.setTitle(title);
      }
      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu _menu) {
      MenuItem settingsItem = _menu
            .add(0, Menu.FIRST, Menu.NONE, getString(R.string.menu_settings));
      settingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            startSettings();
            return true;
         }
      });

      String title = null;
      if (!mService.isClientControlConnected()) {
         title = getString(R.string.menu_connect);
      } else {
         title = getString(R.string.menu_disconnect);
      }

      mConnectItem = _menu.add(0, Menu.FIRST, Menu.NONE, title);
      mConnectItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            if (!mService.isClientControlConnected()) {
               connectClientControl();
            } else {
               disconnectClientControl();
            }
            return true;
         }
      });

      return true;
   }

   protected void connectClientControl() {
      if (mConnectTask == null) {
         mConnectTask = new ConnectClientControlTask(this);
         mConnectTask.execute();
      }
   }

   private void disconnectClientControl() {
      mService.disconnectClientControl();
   }

   private void startSettings() {
      Intent settingsIntent = new Intent(this, SettingsActivity.class);
      startActivity(settingsIntent);
      // startActivityForResult(settingsIntent, 0);
   }

   @Override
   public void messageReceived(Object _message) {
      if (_message.getClass().equals(RemoteStatusMessage.class)) {
         mStatusBarHandler.setStatus((RemoteStatusMessage) _message);
      } else if (_message.getClass().equals(RemoteNowPlaying.class)) {
         mStatusBarHandler.setNowPlaying((RemoteNowPlaying) _message);
      } else if (_message.getClass().equals(RemoteNowPlayingUpdate.class)) {
         mStatusBarHandler.setNowPlaying((RemoteNowPlayingUpdate) _message);
      } else if (_message.getClass().equals(RemotePropertiesUpdate.class)) {
         mStatusBarHandler.setPropertiesUpdate((RemotePropertiesUpdate) _message);
      } else if (_message.getClass().equals(RemoteWelcomeMessage.class)) {
         RemoteVolumeMessage vol = ((RemoteWelcomeMessage) _message).getVolume();
         mStatusBarHandler.setVolume(vol);
      } else if (_message.getClass().equals(RemoteVolumeMessage.class)) {
         RemoteVolumeMessage vol = ((RemoteVolumeMessage) _message);
         mStatusBarHandler.setVolume(vol);
      }
   }

   @Override
   public void stateChanged(final ConnectionState _state) {
      runOnUiThread(new Runnable() {
         @Override
         public void run() {
            handleAutoHide();

            if (mConnectItem != null) {
               if (_state == ConnectionState.Disconnected) {
                  mConnectItem.setTitle(getString(R.string.menu_connect));
               } else if (_state == ConnectionState.Connected) {
                  mConnectItem.setTitle(getString(R.string.menu_disconnect));
               }
            }
         }
      });
   }
}
