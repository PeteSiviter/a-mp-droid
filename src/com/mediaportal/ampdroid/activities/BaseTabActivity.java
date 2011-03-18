package com.mediaportal.ampdroid.activities;

import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.settings.SettingsActivity;
import com.mediaportal.ampdroid.data.RemoteClient;
import com.mediaportal.ampdroid.database.RemoteClientsDatabaseHandler;
import com.mediaportal.ampdroid.settings.PreferencesManager;
import com.mediaportal.ampdroid.utils.Util;

public class BaseTabActivity extends TabActivity {
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      PreferencesManager.intitialisePreferencesManager(this);

      if (PreferencesManager.isFullscreen()) {
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
               WindowManager.LayoutParams.FLAG_FULLSCREEN);
      }
   }

   @Override
   protected void onStart() {
      super.onStart();
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
               notImplemented();
               // TODO: Client switching
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
   public boolean onCreateOptionsMenu(Menu _menu) {
      MenuItem settingsItem = _menu.add(0, Menu.FIRST, Menu.NONE, getString(R.string.menu_settings));
      settingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            startSettings();
            return true;
         }
      });

      return true;
   }

   private void startSettings() {
      Intent settingsIntent = new Intent(this, SettingsActivity.class);
      startActivity(settingsIntent);
      // startActivityForResult(settingsIntent, 0);
   }
}
