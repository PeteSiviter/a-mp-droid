package com.mediaportal.ampdroid.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mediaportal.ampdroid.api.gmawebservice.GmaJsonWebserviceApi;
import com.mediaportal.ampdroid.api.tv4home.Tv4HomeJsonApi;
import com.mediaportal.ampdroid.api.wifiremote.WifiRemoteMpController;
import com.mediaportal.ampdroid.data.RemoteClient;
import com.mediaportal.ampdroid.data.RemoteClientSetting;

public class RemoteClientsDatabaseHandler {
   Context mContext;
   private SettingsDatabaseHelper mDbHelper;
   private SQLiteDatabase mDatabase;

   private static final String REMOTE_CLIENTS_TABLE = "RemoteClients";

   public RemoteClientsDatabaseHandler(Context _context) {
      mContext = _context;
      mDbHelper = new SettingsDatabaseHelper(mContext, "ampdroid_settings", null, 10);
   }

   public void open() {
      mDatabase = mDbHelper.getWritableDatabase();
   }

   public void close() {
      mDatabase.close();
   }

   public List<RemoteClient> getClients() {
      try {
         Cursor result = mDatabase.query(REMOTE_CLIENTS_TABLE, null, null, null, null, null, null);
         ArrayList<RemoteClientSetting> clientSettings = null;
         if (result.moveToFirst()) {
            clientSettings = (ArrayList<RemoteClientSetting>) SqliteAnnotationsHelper
                  .getObjectsFromCursor(result, RemoteClientSetting.class, 0);
         }

         result.close();

         List<RemoteClient> returnList = new ArrayList<RemoteClient>();

         if (clientSettings != null) {
            for (RemoteClientSetting s : clientSettings) {
               RemoteClient client = new RemoteClient(s.getClientId());
               client.setClientName(s.getClientName());
               client.setClientDescription(s.getClientDescription());

               GmaJsonWebserviceApi api = new GmaJsonWebserviceApi(s.getRemoteAccessServer(),
                     s.getRemoteAccessPort(), s.getRemoteAccessMac(), s.getRemoteAccessUser(),
                     s.getRemoteAccessPass(), s.isRemoteAccessUseAuth());
               client.setRemoteAccessApi(api);

               Tv4HomeJsonApi tvApi = new Tv4HomeJsonApi(s.getTvServer(), s.getTvPort(),
                     s.getTvMac(), s.getTvUser(), s.getTvPass(), s.isTvUseAuth());
               client.setTvControlApi(tvApi);

               WifiRemoteMpController clientApi = new WifiRemoteMpController(mContext,
                     s.getRemoteControlServer(), s.getRemoteControlPort(), s.getRemoteControlMac(),
                     s.getRemoteControlUser(), s.getRemoteControlPass(), s.isRemoteControlUseAuth());
               client.setClientControlApi(clientApi);
               returnList.add(client);
            }
         }
         return returnList;
      } catch (Exception ex) {
         Log.e("Database", ex.getMessage());
      }
      // something went wrong obviously
      return null;
   }

   public boolean addRemoteClient(RemoteClient _client) {
      if (mDatabase != null) {
         RemoteClientSetting setting = getSettingsFromClient(_client);

         try {
            ContentValues clubValues = SqliteAnnotationsHelper.getContentValuesFromObject(setting,
                  RemoteClientSetting.class);

            long row = mDatabase.insert(REMOTE_CLIENTS_TABLE, null, clubValues);
            if (row > 0)
               return true;
         } catch (Exception ex) {
            Log.e("Database", ex.getMessage());
         }
      }
      return false;
   }

   private RemoteClientSetting getSettingsFromClient(RemoteClient _client) {
      RemoteClientSetting setting = new RemoteClientSetting();
      setting.setClientId(_client.getClientId());
      setting.setClientName(_client.getClientName());
      setting.setClientDescription(_client.getClientDescription());

      setting.setRemoteAccessServer(_client.getRemoteAccessApi().getServer());
      setting.setRemoteAccessPort(_client.getRemoteAccessApi().getPort());
      setting.setRemoteAccessMac(_client.getRemoteAccessApi().getMac());
      setting.setRemoteAccessUser(_client.getRemoteAccessApi().getUserName());
      setting.setRemoteAccessPass(_client.getRemoteAccessApi().getUserPass());
      setting.setRemoteAccessUseAuth(_client.getRemoteAccessApi().getUseAuth());

      setting.setTvServer(_client.getTvControlApi().getServer());
      setting.setTvPort(_client.getTvControlApi().getPort());
      setting.setTvMac(_client.getTvControlApi().getMac());
      setting.setTvUser(_client.getTvControlApi().getUserName());
      setting.setTvPass(_client.getTvControlApi().getUserPass());
      setting.setTvUseAuth(_client.getTvControlApi().getUseAuth());

      setting.setRemoteControlServer(_client.getClientControlApi().getServer());
      setting.setRemoteControlPort(_client.getClientControlApi().getPort());
      setting.setRemoteControlMac(_client.getClientControlApi().getMac());
      setting.setRemoteControlUser(_client.getClientControlApi().getUserName());
      setting.setRemoteControlPass(_client.getClientControlApi().getUserPass());
      setting.setRemoteControlUseAuth(_client.getClientControlApi().getUseAuth());

      return setting;
   }

   public boolean updateRemoteClient(RemoteClient _client) {
      if (mDatabase != null) {
         RemoteClientSetting setting = getSettingsFromClient(_client);
         try {
            ContentValues clubValues = SqliteAnnotationsHelper.getContentValuesFromObject(setting,
                  RemoteClientSetting.class);

            int rows = mDatabase.update(REMOTE_CLIENTS_TABLE, clubValues,
                  "ClientId=" + _client.getClientId(), null);

            if (rows == 1) {
               return true;
            }
         } catch (Exception ex) {
            Log.e("Database", ex.getMessage());
         }
      }
      return false;
   }

   public boolean removeRemoteClient(RemoteClient _client) {
      if (mDatabase != null && _client != null) {
         int numDeleted = mDatabase.delete(REMOTE_CLIENTS_TABLE,
               "ClientId=" + _client.getClientId(), null);
         return numDeleted == 1;
      }
      return false;
   }

}
