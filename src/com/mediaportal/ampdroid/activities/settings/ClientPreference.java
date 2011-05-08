package com.mediaportal.ampdroid.activities.settings;

import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
   private EditText mEditTextGmaHost;
   private EditText mEditTextGmaPort;
   private EditText mEditTextTv4HomeHost;
   private EditText mEditTextTv4HomePort;
   private EditText mEditTextWifiRemoteHost;
   private EditText mEditTextWifiRemotePort;
   private CheckBox mUseGma;
   private CheckBox mUseTv4Home;
   private CheckBox mUseWifiRemote;
   private boolean mUseSimple = true;
   private LinearLayout mLinearLayoutSimple;
   private LinearLayout mLinearLayoutAdvanced;
   private CheckBox mCheckBoxUseSimple;
   private EditText mUserViewGma;
   private EditText mPassViewGma;
   private CheckBox mUseGmaAuth;
   private CheckBox mUseTv4HomeAuth;
   private EditText mUserViewTv4Home;
   private EditText mPassViewTv4Home;
   private CheckBox mUseWifiRemoteAuth;
   private EditText mUserViewWifiRemote;
   private EditText mPassViewWifiRemote;
   private QRClientDescription mQrDescription;
   private PreferenceScreen mRoot;

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

   public void fillFromQRCode(QRClientDescription _desc) {
      mQrDescription = _desc;
      // set defaults:
      if (mQrDescription != null) {
         if (mQrDescription.getAuthOptions() != 0) {
            mUseAuthView.setChecked(true);
         } else {
            mUseAuthView.setChecked(false);
         }

         if (mQrDescription.getName() != null) {
            mNameView.setText(mQrDescription.getName());
         }

         if (mQrDescription.getUser() != null) {
            mUserView.setText(mQrDescription.getUser());
         }

         if (mQrDescription.getPassword() != null) {
            mPassView.setText(mQrDescription.getPassword());
         }

         if (mQrDescription.getAddress() != null) {
            mHostView.setText(mQrDescription.getAddress());
         }
      }
   }

   public void create(PreferenceManager preferenceManager, PreferenceScreen _root) {
      onAttachedToHierarchy(preferenceManager);
      mRoot = _root;
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
            builder.setTitle(mContext.getString(R.string.settings_delete_client_title));
            builder.setMessage(mContext.getString(R.string.settings_delete_client_start)
                  + mClient.getClientName()
                  + mContext.getString(R.string.settings_delete_client_end));
            builder.setPositiveButton(mContext.getString(R.string.dialog_yes),
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                        mDbHandler.removeRemoteClient(mClient);
                        ((PreferenceActivity) view.getContext()).getPreferenceScreen()
                              .removePreference(ClientPreference.this);
                     }
                  });
            builder.setNegativeButton(mContext.getString(R.string.dialog_no),
                  new DialogInterface.OnClickListener() {
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

      // simple layout
      mNameView = (EditText) parent.findViewById(R.id.pref_name);
      mHostView = (EditText) parent.findViewById(R.id.pref_host);
      mUserView = (EditText) parent.findViewById(R.id.pref_user);
      mPassView = (EditText) parent.findViewById(R.id.pref_pass);
      mUseAuthView = (CheckBox) parent.findViewById(R.id.pref_use_auth);
      mUseAuthView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setUseAuth(isChecked, mUseAuthView, mUserView, mPassView);
         }
      });

      // advanced layout
      mEditTextGmaHost = (EditText) parent.findViewById(R.id.pref_host_gma_addr);
      mEditTextGmaPort = (EditText) parent.findViewById(R.id.pref_host_gma_port);
      mUseGma = (CheckBox) parent.findViewById(R.id.pref_gma_use_service);
      mUseGmaAuth = (CheckBox) parent.findViewById(R.id.pref_gma_use_auth);
      mUserViewGma = (EditText) parent.findViewById(R.id.pref_gma_user);
      mPassViewGma = (EditText) parent.findViewById(R.id.pref_gma_pass);
      mUseGmaAuth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setUseAuth(isChecked, mUseGmaAuth, mUserViewGma, mPassViewGma);
         }
      });

      mEditTextTv4HomeHost = (EditText) parent.findViewById(R.id.pref_host_tv4home_addr);
      mEditTextTv4HomePort = (EditText) parent.findViewById(R.id.pref_host_tv4home_port);
      mUseTv4Home = (CheckBox) parent.findViewById(R.id.pref_tv4home_use_service);
      mUseTv4HomeAuth = (CheckBox) parent.findViewById(R.id.pref_tv4home_use_auth);
      mUserViewTv4Home = (EditText) parent.findViewById(R.id.pref_tv4home_user);
      mPassViewTv4Home = (EditText) parent.findViewById(R.id.pref_tv4home_pass);
      mUseTv4HomeAuth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setUseAuth(isChecked, mUseTv4HomeAuth, mUserViewTv4Home, mPassViewTv4Home);
         }
      });

      mEditTextWifiRemoteHost = (EditText) parent.findViewById(R.id.pref_host_wifiremote_addr);
      mEditTextWifiRemotePort = (EditText) parent.findViewById(R.id.pref_host_wifiremote_port);
      mUseWifiRemote = (CheckBox) parent.findViewById(R.id.pref_wifiremote_use_service);
      mUseWifiRemoteAuth = (CheckBox) parent.findViewById(R.id.pref_wifiremote_use_auth);
      mUserViewWifiRemote = (EditText) parent.findViewById(R.id.pref_wifiremote_user);
      mPassViewWifiRemote = (EditText) parent.findViewById(R.id.pref_wifiremote_pass);
      mUseWifiRemoteAuth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setUseAuth(isChecked, mUseWifiRemoteAuth, mUserViewWifiRemote, mPassViewWifiRemote);
         }
      });

      mLinearLayoutSimple = (LinearLayout) parent.findViewById(R.id.LinearLayoutSimpleHost);
      mLinearLayoutAdvanced = (LinearLayout) parent.findViewById(R.id.LinearLayoutAdvancedHost);
      mCheckBoxUseSimple = (CheckBox) parent.findViewById(R.id.CheckBoxUseSimpleSetting);
      mCheckBoxUseSimple.setOnCheckedChangeListener(new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton arg0, boolean _state) {
            handleUseSimpleSetting(_state);
         }
      });

      handleUseSimpleSetting(mUseSimple);

      return parent;
   }

   private void handleUseSimpleSetting(boolean _state) {
      if (_state) {
         mLinearLayoutSimple.setVisibility(View.VISIBLE);
         mLinearLayoutAdvanced.setVisibility(View.GONE);
      } else {
         mLinearLayoutSimple.setVisibility(View.GONE);
         mLinearLayoutAdvanced.setVisibility(View.VISIBLE);
      }
      mUseSimple = _state;
   }

   @Override
   protected void onBindDialogView(View view) {
      super.onBindDialogView(view);
      if (mClient != null) {
         // general
         mNameView.setText(mClient.getClientName());

         // simple
         mHostView.setText(mClient.getClientAddress());
         mUserView.setText(mClient.getUserName());
         mPassView.setText(mClient.getUserPassword());
         setUseAuth(mClient.useAuth(), mUseAuthView, mUserView, mPassView);

         // advanced
         mEditTextGmaHost.setText(mClient.getRemoteAccessApi().getServer());
         mEditTextGmaPort.setText(String.valueOf(mClient.getRemoteAccessApi().getPort()));
         mUserViewGma.setText(mClient.getRemoteAccessApi().getUserName());
         mPassViewGma.setText(mClient.getRemoteAccessApi().getUserPass());
         setUseAuth(mClient.getRemoteAccessApi().getUseAuth(), mUseGmaAuth, mUserViewGma,
               mPassViewGma);

         mEditTextTv4HomeHost.setText(mClient.getTvControlApi().getServer());
         mEditTextTv4HomePort.setText(String.valueOf(mClient.getTvControlApi().getPort()));
         mUserViewTv4Home.setText(mClient.getTvControlApi().getUserName());
         mPassViewTv4Home.setText(mClient.getTvControlApi().getUserPass());
         setUseAuth(mClient.getTvControlApi().getUseAuth(), mUseTv4HomeAuth, mUserViewTv4Home,
               mPassViewTv4Home);

         mEditTextWifiRemoteHost.setText(mClient.getClientControlApi().getServer());
         mEditTextWifiRemotePort.setText(String.valueOf(mClient.getClientControlApi().getPort()));
         mUserViewWifiRemote.setText(mClient.getClientControlApi().getUserName());
         mPassViewWifiRemote.setText(mClient.getClientControlApi().getUserPass());
         setUseAuth(mClient.getClientControlApi().getUseAuth(), mUseWifiRemoteAuth,
               mUserViewWifiRemote, mPassViewWifiRemote);

      }
   }

   private void setUseAuth(boolean useAuth, CheckBox _checkBox, EditText _userEdit,
         EditText _passEdit) {
      // if (useAuth) {
      // mUserView.setText(mClient == null ? "" : mClient.getUserName());
      // mPassView.setText(mClient == null ? "" : mClient.getUserPassword());
      // }
      _checkBox.setChecked(useAuth);
      _userEdit.setEnabled(useAuth);
      _passEdit.setEnabled(useAuth);
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

         String user = mUserView.getText().toString();
         String pass = mPassView.getText().toString();
         boolean auth = mUseAuthView.isChecked();

         if (mUseSimple) {
            String addr = mHostView.getText().toString();
            GmaJsonWebserviceApi api = new GmaJsonWebserviceApi(addr, 44321, user, pass, auth);
            mClient.setRemoteAccessApi(api);

            Tv4HomeJsonApi tvApi = new Tv4HomeJsonApi(addr, 4321, user, pass, auth);
            mClient.setTvControlApi(tvApi);

            WifiRemoteMpController clientApi = new WifiRemoteMpController(mContext, addr, 8017,
                  user, pass, auth);
            mClient.setClientControlApi(clientApi);
         } else {
            String gmaAddr = mEditTextGmaHost.getText().toString();
            String gmaPort = mEditTextGmaPort.getText().toString();
            GmaJsonWebserviceApi api = new GmaJsonWebserviceApi(gmaAddr, Integer.valueOf(gmaPort),
                  user, pass, auth);
            mClient.setRemoteAccessApi(api);

            String tv4homeAddr = mEditTextTv4HomeHost.getText().toString();
            String tv4homePort = mEditTextTv4HomePort.getText().toString();
            Tv4HomeJsonApi tvApi = new Tv4HomeJsonApi(tv4homeAddr, Integer.valueOf(tv4homePort),
                  user, pass, auth);
            mClient.setTvControlApi(tvApi);

            String wifiAddr = mEditTextWifiRemoteHost.getText().toString();
            String wifiPort = mEditTextWifiRemotePort.getText().toString();
            WifiRemoteMpController clientApi = new WifiRemoteMpController(mContext, wifiAddr,
                  Integer.valueOf(wifiPort), user, pass, auth);
            mClient.setClientControlApi(clientApi);
         }

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

      } else {
         if (mClient == null) {
            mRoot.removePreference(ClientPreference.this);
         }
      }
   }

}
