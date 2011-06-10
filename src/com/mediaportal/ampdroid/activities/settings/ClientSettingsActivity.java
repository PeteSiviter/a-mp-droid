package com.mediaportal.ampdroid.activities.settings;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.barcodes.IntentIntegrator;
import com.mediaportal.ampdroid.barcodes.IntentResult;
import com.mediaportal.ampdroid.data.RemoteClient;
import com.mediaportal.ampdroid.database.RemoteClientsDatabaseHandler;
import com.mediaportal.ampdroid.utils.Util;

public class ClientSettingsActivity extends PreferenceActivity {
   public static final int MENU_ADD_HOST = 1;
   public static final int MENU_EXIT = 2;
   public static final int MENU_ADD_HOST_SCAN = 3;
   private RemoteClientsDatabaseHandler mDbHandler;
   private PreferenceManager mPrefsManager;
   private PreferenceScreen mRoot;
   private QRClientDescription mQrDescription = null;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTitle("MediaPortal Clients");

   }

   @Override
   protected void onResume() {
      mPrefsManager = getPreferenceManager();
      mRoot = mPrefsManager.createPreferenceScreen(this);

      mDbHandler = new RemoteClientsDatabaseHandler(this);
      mDbHandler.open();
      List<RemoteClient> clients = mDbHandler.getClients();

      if (clients != null) {
         for (RemoteClient c : clients) {
            ClientPreference pref = new ClientPreference(this);
            pref.setTitle(c.getClientName());
            pref.setSummary("Id: " + c.getClientId());
            pref.setClient(c);
            pref.setDbHandler(mDbHandler);

            mRoot.addPreference(pref);
         }
      }
      if (clients == null || clients.size() == 0) {
         Util.showToast(this, getString(R.string.settings_clients_noclients));
      }

      setPreferenceScreen(mRoot);

      if (mQrDescription != null) {
         ClientPreference pref = new ClientPreference(this);
         pref.setTitle("New Client");
         pref.create(mPrefsManager, mRoot);
         mRoot.addPreference(pref);
         pref.fillFromQRCode(mQrDescription);
         pref.setDbHandler(mDbHandler);
         mQrDescription = null;
      }
      super.onResume();
   }

   @Override
   protected void onPause() {
      mDbHandler.close();
      super.onPause();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      menu.addSubMenu(0, MENU_ADD_HOST, 0, getString(R.string.menu_addhost)).setIcon(
            R.drawable.ic_menu_addclients);
      menu.addSubMenu(0, MENU_ADD_HOST_SCAN, 0, getString(R.string.menu_addhost_scan)).setIcon(
            R.drawable.ic_menu_barcode);
      // menu.addSubMenu(0, MENU_EXIT, 0, "Exit");//
      // .setIcon(R.drawable.menu_exit);

      return true;
   }

   @Override
   public boolean onMenuItemSelected(int featureId, MenuItem item) {
      switch (item.getItemId()) {
      case MENU_ADD_HOST:
         ClientPreference pref = new ClientPreference(this);
         pref.setTitle("New Client");
         pref.create(mPrefsManager, mRoot);
         mRoot.addPreference(pref);
         pref.setDbHandler(mDbHandler);
         break;
      case MENU_ADD_HOST_SCAN:
         scan();
         break;
      case MENU_EXIT:
         System.exit(0);
         break;
      }
      return true;
   }

   protected void scan() {
      IntentIntegrator.initiateScan(this);
   }

   public void onActivityResult(int requestCode, int resultCode, Intent intent) {
      IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode,
            intent);
      if (scanResult != null) {
         if (scanResult.getContents() != null) {

            try {
               String content = scanResult.getContents();
               ObjectMapper mapper = new ObjectMapper();
               mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

               // add a client from QR code information
               mQrDescription = mapper.readValue(content, QRClientDescription.class);
            } catch (JsonParseException e) {
               e.printStackTrace();
            } catch (JsonMappingException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
         // handle scan result
      }
   }
}
