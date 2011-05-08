package com.mediaportal.ampdroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.barcodes.IntentIntegrator;
import com.mediaportal.ampdroid.barcodes.IntentResult;
import com.mediaportal.ampdroid.utils.Util;

public class HomeActivity extends BaseActivity {
   private StatusBarActivityHandler statusBarHandler = null;
   private DataHandler mService;

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
            Util.showToast(_view.getContext(), getString(R.string.info_not_implemented));
            
            Intent myIntent = new Intent(_view.getContext(), MusicActivity.class);
            startActivity(myIntent);
         }
      });

      final ImageButton buttonTv = (ImageButton) findViewById(R.id.ImageButtonTv);
      buttonTv.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            Intent myIntent = new Intent(_view.getContext(), TvServerOverviewActivity.class);
            startActivity(myIntent);
         }
      });

      final ImageButton buttonVideos = (ImageButton) findViewById(R.id.ImageButtonVideos);
      buttonVideos.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            Intent myIntent = new Intent(_view.getContext(), MediaActivity.class);
            startActivity(myIntent);
         }
      });

      final ImageButton buttonPictures = (ImageButton) findViewById(R.id.ImageButtonPictures);
      buttonPictures.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            Util.showToast(_view.getContext(), getString(R.string.info_not_implemented));
//            Intent myIntent = new Intent(_view.getContext(), PicturesActivity.class);
//            startActivity(myIntent);
         }
      });

      final ImageButton buttonPlugins = (ImageButton) findViewById(R.id.ImageButtonPlugins);
      buttonPlugins.setOnClickListener(new View.OnClickListener() {
         public void onClick(View _view) {
            Util.Vibrate(_view.getContext(), 50);
            Util.showToast(_view.getContext(), getString(R.string.info_not_implemented));
            //
           
         }
      });
   }

   @Override
   protected void onStart() {
      super.onStart();
   }
   

}
