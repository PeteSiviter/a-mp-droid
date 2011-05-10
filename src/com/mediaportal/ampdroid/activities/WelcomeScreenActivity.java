package com.mediaportal.ampdroid.activities;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.settings.SettingsActivity;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.RemoteClient;
import com.mediaportal.ampdroid.database.RemoteClientsDatabaseHandler;
import com.mediaportal.ampdroid.settings.PreferencesManager;
import com.mediaportal.ampdroid.utils.Util;

public class WelcomeScreenActivity extends Activity {
   private class StartupTask extends AsyncTask<RemoteClient, String, Boolean> {

      private Context mContext;

      public StartupTask(Context _context) {
         mContext = _context;
      }

      @Override
      protected Boolean doInBackground(RemoteClient... _clients) {

         PreferencesManager.intitialisePreferencesManager(mContext);

         DataHandler.setupRemoteHandler(_clients[0], mContext, false);
         
         if(PreferencesManager.connectOnStartup()){
            DataHandler client = DataHandler.getCurrentRemoteInstance();
            boolean success = client.connectClientControl();
            return success;
         }
         return true;
      }

      @Override
      protected void onProgressUpdate(String... _result) {

      }

      @Override
      protected void onPostExecute(Boolean _result) {
         if (_result) {
            Util.showToast(mContext, mContext.getString(R.string.connection_successful));
         } else {
            Util.showToast(mContext, mContext.getString(R.string.connection_failed));
         }
         Intent myIntent = new Intent(mContext, HomeActivity.class);
         startActivity(myIntent);
      }
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.welcomescreen);

      // RemoteClientFactory.openDatabase(this);
      // Client for diebagger -> until setting screen ready
      // final RemoteClient client = new RemoteClient(0, "Bagga Server");

      // GmaWebserviceApi api = new GmaWebserviceApi("10.1.0.247", 4322);
      // client.setRemoteAccessApi(api);

      // Tv4HomeApi tvApi = new Tv4HomeApi("10.1.0.166", 4321);
      // client.setTvControlApi(tvApi);

      // WifiRemoteMpController clientApi = new
      // WifiRemoteMpController("10.1.0.247", 8017);
      // client.setClientControlApi(clientApi);
      // RemoteClientFactory.createRemoteClient(client);
      // RemoteClientFactory.closeDatabase();

      // RemoteClientFactory.closeDatabase();

   }

   @Override
   protected void onStart() {
    //disconnect client if one is currently connected
      DataHandler client = DataHandler.getCurrentRemoteInstance();
      if(client != null){
         client.disconnectClientControl();
      }
      
      final ProgressBar progress = (ProgressBar) findViewById(R.id.ProgressBarWelcomeScreen);
      progress.setVisibility(View.INVISIBLE);
      final Button connectButton = (Button) findViewById(R.id.ButtonConnect);
      connectButton.setEnabled(true);

      final Spinner spinner = (Spinner) findViewById(R.id.SpinnerSelectClients);

      

      RemoteClientsDatabaseHandler remoteClientsDb = new RemoteClientsDatabaseHandler(this);
      remoteClientsDb.open();
      List<RemoteClient> clients = remoteClientsDb.getClients();
      remoteClientsDb.close();

      connectButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View _view) {
            StartupTask task = new StartupTask(_view.getContext());
            RemoteClient client = (RemoteClient) spinner.getSelectedItem();
            if (client != null) {
               task.execute(client);
               progress.setVisibility(View.VISIBLE);
               progress.setIndeterminate(true);
               connectButton.setEnabled(false);
            } else {
               Util.showToast(_view.getContext(), getString(R.string.welcome_no_client));
            }
         }
      });

      if (clients != null && clients.size() > 0) {
         ArrayAdapter<RemoteClient> adapter = new ArrayAdapter<RemoteClient>(this,
               android.R.layout.simple_spinner_item, clients);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
      }

      super.onStart();
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

      return true;
   }

   private void startSettings() {
      Intent settingsIntent = new Intent(this, SettingsActivity.class);
      startActivity(settingsIntent);
      // startActivityForResult(settingsIntent, 0);
   }

   @Override
   public void onAttachedToWindow() {
      super.onAttachedToWindow();
      Window window = getWindow();
      window.setFormat(PixelFormat.RGBA_8888);
   }
}