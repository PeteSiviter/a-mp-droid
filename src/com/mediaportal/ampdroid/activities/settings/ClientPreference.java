package com.mediaportal.ampdroid.activities.settings;

import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.api.gmawebservice.GmaJsonWebserviceApi;
import com.mediaportal.ampdroid.api.tv4home.Tv4HomeJsonApi;
import com.mediaportal.ampdroid.api.wifiremote.WifiRemoteMpController;
import com.mediaportal.ampdroid.data.RemoteClient;
import com.mediaportal.ampdroid.database.RemoteClientsDatabaseHandler;

public class ClientPreference extends DialogPreference {
   private Context mContext;
   private RemoteClient mClient;
   private RemoteClientsDatabaseHandler mDbHandler;

   private EditText mNameView;
   private EditText mHostView;
   private EditText mUserView;
   private EditText mPassView;

   private CheckBox mUseAuthView;

   public ClientPreference(Context context, AttributeSet attrs) {
      super(context, attrs);

      mContext = context;

      setDialogLayoutResource(R.layout.preference_host_simple);
      setDialogTitle(mContext.getString(R.string.dialog_title_addhost));
      setDialogIcon(R.drawable.bubble_add);
   }

   public ClientPreference(Context context) {
      this(context, null);
   }

   public void create(PreferenceManager preferenceManager) {
      onAttachedToHierarchy(preferenceManager);
      showDialog(null);
   }

   public void setClient(RemoteClient _client) {
      mClient = _client;
      setTitle(_client.getClientName());
      setSummary(_client.getClientDescription());
      setDialogTitle(_client.getClientName());
      setDialogIcon(null);
   }

   public RemoteClient getHost() {
      return mClient;
   }

   public void setDbHandler(RemoteClientsDatabaseHandler _dbHandler) {
      mDbHandler = _dbHandler;
   }

   @Override
   protected View onCreateView(final ViewGroup parent) {
      final ViewGroup view = (ViewGroup) super.onCreateView(parent);
      ImageView btn = new ImageView(getContext());
      btn.setImageResource(R.drawable.bubble_del_up);
      btn.setClickable(true);
      btn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Are you sure you want to delete the client \"" + "client_name"
                  + "\"?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                  mDbHandler.removeRemoteClient(mClient);
                  ((PreferenceActivity) view.getContext()).getPreferenceScreen().removePreference(
                        ClientPreference.this);
               }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                  dialog.cancel();
               }
            });
            builder.create().show();
         }
      });
      view.addView(btn);
      return view;
   }

   @Override
   protected View onCreateDialogView() {
      final ViewGroup parent = (ViewGroup) super.onCreateDialogView();
      mNameView = (EditText) parent.findViewById(R.id.pref_name);
      mHostView = (EditText) parent.findViewById(R.id.pref_host);

      mUserView = (EditText) parent.findViewById(R.id.pref_user);
      mPassView = (EditText) parent.findViewById(R.id.pref_pass);
      
      mUseAuthView = (CheckBox) parent.findViewById(R.id.pref_use_auth);
      mUseAuthView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setUseAuth(isChecked);
         }
      });
      return parent;
   }

   @Override
   protected void onBindDialogView(View view) {
      super.onBindDialogView(view);
      if (mClient != null) {
         mNameView.setText(mClient.getClientName());

         mHostView.setText(mClient.getClientAddress());

         mUseAuthView.setChecked(mClient.useAuth());

         setUseAuth(mClient.useAuth());

      } else {
         // set defaults:
      }
   }

   private void setUseAuth(boolean useAuth) {
      if (useAuth) {
         mUserView.setText(mClient.getUserName());
         mPassView.setText(mClient.getUserPassword());
      }
      mUserView.setEnabled(useAuth);
      mPassView.setEnabled(useAuth);
   }

   @Override
   protected void onDialogClosed(boolean positiveResult) {
      super.onDialogClosed(positiveResult);
      if (positiveResult) {
         boolean create = false;
         if (mClient == null) {
            Random rnd = new Random();
            mClient = new RemoteClient(rnd.nextInt());
            create = true;
         }
         mClient.setClientName(mNameView.getText().toString());
         String addr = mHostView.getText().toString();
         
         
         String user = mUserView.getText().toString();
         String pass = mPassView.getText().toString();
         boolean auth = mUseAuthView.isChecked();
         
         GmaJsonWebserviceApi api = new GmaJsonWebserviceApi(addr, 44321, user, pass, auth);
         mClient.setRemoteAccessApi(api);

         Tv4HomeJsonApi tvApi = new Tv4HomeJsonApi(addr, 4321, user, pass, auth);
         mClient.setTvControlApi(tvApi);

         WifiRemoteMpController clientApi = new WifiRemoteMpController(mContext, addr, 8017, user, pass, auth);
         mClient.setClientControlApi(clientApi);

         if (create) {
            mDbHandler.addRemoteClient(mClient);
         } else {
            mDbHandler.updateRemoteClient(mClient);
         }

         DataHandler.updateRemoteClient(mClient);
         // host.user = mUserView.getText().toString();
         // host.pass = mPassView.getText().toString();

         // host.access_point = mAccPointView.getText().toString();
         // host.wifi_only = mWifiOnlyView.isChecked();

         setClient(mClient);

      }
   }

}
