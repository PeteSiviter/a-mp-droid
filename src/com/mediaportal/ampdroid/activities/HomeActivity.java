package com.mediaportal.ampdroid.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ImageButton;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.settings.ClientPreference;
import com.mediaportal.ampdroid.data.SupportedFunctions;
import com.mediaportal.ampdroid.settings.PreferencesManager;
import com.mediaportal.ampdroid.utils.Constants;
import com.mediaportal.ampdroid.utils.Util;
import com.mediaportal.ampdroid.utils.WakeOnLan;

public class HomeActivity extends BaseActivity {
   private LoadServiceTask mServiceLoaderTask = null;

   private class LoadServiceTask extends AsyncTask<String, String, Intent> {
      Context mContext;

      private LoadServiceTask(Context _context) {
         mContext = _context;
      }

      @Override
      protected Intent doInBackground(String... _params) {
         String loader = _params[0];

         if (loader.equals("media")) {
            SupportedFunctions functions = mService.getSupportedFunctions();
            if (functions != null) {

               Intent myIntent = new Intent(mContext, MediaActivity.class);

               myIntent.putExtra("service_connected", true);

               myIntent.putExtra("supports_video", functions.supportsVideo());
               myIntent.putExtra("supports_series", functions.supportsTvSeries());
               myIntent.putExtra("supports_movies", functions.supportsMovies());
               return myIntent;
            }
            else{
               publishProgress(loader);
            }

         } else if (loader.equals("music")) {
            SupportedFunctions functions = mService.getSupportedFunctions();
            if (functions != null) {
               Intent myIntent = new Intent(mContext, MusicActivity.class);
               return myIntent;
            }
            else{
               publishProgress(loader);
            }
         } else if (loader.equals("tvservice")) {
            if (mService.isTvServiceActive()) {
               Intent myIntent = new Intent(mContext, TvServerOverviewActivity.class);
               return myIntent;
            }
            else{
               publishProgress(loader);
            }
         }

         return null;
      }



      @Override
      protected void onProgressUpdate(String... _params) {
         String loader = _params[0];
         String mac = null;
         String ip = null;
         int port = 0;
         if (loader.equals("media") || loader.equals("music")) {
            mac = mService.getRemoteAccessApi().getMac();
            ip = mService.getRemoteAccessApi().getServer();
            port = mService.getRemoteAccessApi().getPort();
         } else if (loader.equals("tvservice")) {
            mac = mService.getTvApi().getMac();
            ip = mService.getTvApi().getServer();
            port = mService.getTvApi().getPort();
         }
         
         final String macString = mac;//we need a final for the dialog
         
         boolean autoConnect = PreferencesManager.isWolAutoConnect();
         

         if (autoConnect) {
            if (mac != null && !mac.equals("")) {
               // we have a valid mac and user wants autoWOL -> start WOL
               Util.showToast(mContext, getString(R.string.home_wol_running));
               WakeOnLan.sendMagicPacket(mac);

            } else {
               // user wants wol but hasn't entered a valid mac -> ask for it
               Util.showToast(mContext, getString(R.string.home_wol_nomac));
            }
         }
         if (!autoConnect) {
            if (mac != null & !mac.equals("")) {
               // no autoconnect, but valid mac -> ask user if WOL
               AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
               builder.setTitle(mContext.getString(R.string.home_wol_confirmation_title));
               builder.setMessage(mContext.getString(R.string.home_wol_confirmation_text));
               builder.setPositiveButton(mContext.getString(R.string.dialog_yes),
                     new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                           //user wants to send WOL
                           Util.showToast(mContext, getString(R.string.home_wol_running));
                           WakeOnLan.sendMagicPacket(macString);
                        }
                     });
               builder.setNegativeButton(mContext.getString(R.string.dialog_no),
                     new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                           Util.showToast(mContext, getString(R.string.info_no_connection));
                           dialog.cancel();
                        }
                     });
               builder.create().show();
               
            }
            else{
               Util.showToast(mContext, getString(R.string.info_no_connection));
            }
         }
         
         
         super.onProgressUpdate(_params);
      }

      @Override
      protected void onPostExecute(Intent _intent) {
         if (_intent != null) {
            startActivity(_intent);
         } else {
            
         }
         mStatusBarHandler.setLoading(false);
         mServiceLoaderTask = null;
      }
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.homescreen);

      final ImageButton buttonRemote = (ImageButton) findViewById(R.id.ImageButtonRemote);
      buttonRemote.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            Intent myIntent = new Intent(_view.getContext(), RemoteControlActivity.class);
            startActivity(myIntent);
         }
      });

      final ImageButton buttonMusic = (ImageButton) findViewById(R.id.ImageButtonMusic);
      buttonMusic.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            if (mServiceLoaderTask == null) {
               mServiceLoaderTask = new LoadServiceTask(_view.getContext());
               mServiceLoaderTask.execute("music");
               mStatusBarHandler.setLoading(true);
            }
         }
      });

      final ImageButton buttonTv = (ImageButton) findViewById(R.id.ImageButtonTv);
      buttonTv.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            if (mServiceLoaderTask == null) {
               mServiceLoaderTask = new LoadServiceTask(_view.getContext());
               mServiceLoaderTask.execute("tvservice");
               mStatusBarHandler.setLoading(true);
            }
         }
      });

      final ImageButton buttonVideos = (ImageButton) findViewById(R.id.ImageButtonVideos);
      buttonVideos.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            if (mServiceLoaderTask == null) {
               mServiceLoaderTask = new LoadServiceTask(_view.getContext());
               mServiceLoaderTask.execute("media");
               mStatusBarHandler.setLoading(true);
            }
         }
      });

      final ImageButton buttonPictures = (ImageButton) findViewById(R.id.ImageButtonPictures);
      buttonPictures.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            Util.showToast(_view.getContext(), getString(R.string.info_not_implemented));
         }
      });

      final ImageButton buttonPlugins = (ImageButton) findViewById(R.id.ImageButtonPlugins);
      buttonPlugins.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);

            Intent myIntent = new Intent(_view.getContext(), DownloadsActivity.class);
            startActivity(myIntent);
            // Util.showToast(_view.getContext(),
            // getString(R.string.info_not_implemented));
         }
      });
   }

   @Override
   protected void onStart() {
      super.onStart();
   }

}
