package com.mediaportal.ampdroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.actionbar.ActionBar;
import com.mediaportal.ampdroid.activities.settings.SettingsActivity;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.api.RemoteCommands;
import com.mediaportal.ampdroid.data.NowPlaying;
import com.mediaportal.ampdroid.data.commands.RemoteKey;
import com.mediaportal.ampdroid.utils.Util;

public class StatusBarActivityHandler {
   Activity mParent;
   DataHandler mRemote;
   TextView mStatusText;
   ImageButton mPauseButton;
   ImageButton mPrevButton;
   ImageButton mNextButton;
   ImageButton mVolumeButton;
   ImageButton mRemoteButton;
   SeekBar mSeekBar;
   SlidingDrawer mSlider;

   private boolean isHome = false;
   private ActionBar actionBar;
   private TextView mSliderTitleText;

   private static String statusString;
   private static NowPlaying nowPlayingMessage;

   public StatusBarActivityHandler(Activity _parent, DataHandler _remote){
      this(_parent, _remote, false);
   }
   
   public StatusBarActivityHandler(Activity _parent, DataHandler _remote, boolean _isHome) {
      mParent = _parent;
      mRemote = _remote;
      isHome = _isHome;

      mSlider = (SlidingDrawer) mParent.findViewById(R.id.SlidingDrawerStatus);
      mPauseButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomPause);
      mPrevButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomRewind);
      mNextButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomNext);
      mRemoteButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomRemote);
      mVolumeButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomVolume);

      if (mPauseButton != null) {
         mPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               sendRemoteKey(RemoteCommands.pauseButton);
            }

         });
      }

      if (mPrevButton != null) {
         mPrevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View _view) {
               sendRemoteKey(RemoteCommands.prevButton);
            }
         });
      }

      if (mNextButton != null) {
         mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View _view) {
               sendRemoteKey(RemoteCommands.nextButton);
            }
         });
      }

      if (mRemoteButton != null) {
         mRemoteButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View _view, MotionEvent _event) {
               if (_event.getAction() == MotionEvent.ACTION_DOWN) {
                  Util.Vibrate(mParent, 70);
               }
               return false;
            }
         });
         mRemoteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View _view) {
               Util.Vibrate(_view.getContext(), 50);
               if (!mParent.getClass().equals(RemoteControlActivity.class)) {
                  Intent myIntent = new Intent(_view.getContext(), RemoteControlActivity.class);
                  mParent.startActivity(myIntent);
               }

               /*
                * if (!mParent.getClass().equals(HomeActivity.class)) {
                * mSlider.close(); mParent.finish(); } else {
                * mSlider.animateClose(); }
                */
            }
         });
      }

      if (mVolumeButton != null) {
         mVolumeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View _view) {
               if (mSeekBar.getVisibility() == View.VISIBLE) {
                  setControlsVisibility(true);
               } else {
                  setControlsVisibility(false);
               }
            }

         });
      }

      mStatusText = (TextView) mParent.findViewById(R.id.TextViewSliderSatusText);
      mSliderTitleText = (TextView) mParent.findViewById(R.id.TextViewSliderTitle);

      mSeekBar = (SeekBar) mParent.findViewById(R.id.SeekBarBottomVolume);

      if (mSeekBar != null) {
         mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar _seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar _seekBar) {
               if (!mRemote.isClientControlConnected()) {
                  Util.showToast(mParent, "Remote not connected");
               }
            }

            @Override
            public void onProgressChanged(SeekBar _seekBar, int _progress, boolean _fromUser) {
               if (_fromUser) {
                  int seekValue = _seekBar.getProgress();// between 0 - 20
                  Util.Vibrate(mParent, seekValue * 2);
                  if (mRemote.isClientControlConnected()) {
                     mRemote.sendClientVolume((int) seekValue * 5);
                  }
               }
            }
         });
      }

      actionBar = (ActionBar) mParent.findViewById(R.id.actionbar);

      if (actionBar != null) {
         actionBar.setTitle(mParent.getTitle());
         actionBar.setHome(isHome);

         if (!actionBar.isInitialised()) {
            final ImageButton switchClientButton = (ImageButton) actionBar.getChangeClientButton();
            mParent.registerForContextMenu(switchClientButton);
            switchClientButton.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                  switchClientButton.showContextMenu();
               }
            });
            final ProgressBar progress = (ProgressBar) actionBar.getProgressBar();
            final ImageButton searchButton = (ImageButton) actionBar.getSearchButton();
            searchButton.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                  progress.setVisibility(View.INVISIBLE);
               }
            });
            
            ImageButton homeButton = (ImageButton)actionBar.getHomeButton();
            homeButton.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                  Intent intent = new Intent(mParent, HomeActivity.class );
                  intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                  mParent.startActivity( intent );
               }
            });

            actionBar.setInitialised(true);
         }
      }
   }

   public void setHome(boolean isHome) {
      this.isHome = isHome;
   }

   public boolean isHome() {
      return isHome;
   }

   public boolean getLoading() {
      return actionBar.getLoading();
   }

   public void setLoading(boolean _loading) {
      actionBar.setLoading(_loading);
   }

   private void startSettings() {
      Intent settingsIntent = new Intent(mParent, SettingsActivity.class);
      mParent.startActivity(settingsIntent);
      // startActivityForResult(settingsIntent, 0);
   }

   private void sendRemoteKey(RemoteKey _button) {
      Util.Vibrate(mParent, 50);
      if (mRemote.isClientControlConnected()) {
         mRemote.sendRemoteButton(_button);
      } else {
         Util.showToast(mParent, "Remote not connected");
      }

   }

   private void setControlsVisibility(boolean _visibility) {
      if (_visibility) {
         if (mPrevButton != null)
            mPrevButton.setVisibility(View.VISIBLE);
         if (mNextButton != null)
            mNextButton.setVisibility(View.VISIBLE);
         if (mPauseButton != null)
            mPauseButton.setVisibility(View.VISIBLE);
         if (mRemoteButton != null)
            mRemoteButton.setVisibility(View.VISIBLE);

         if (mSeekBar != null)
            mSeekBar.setVisibility(View.INVISIBLE);
      } else {
         if (mPrevButton != null)
            mPrevButton.setVisibility(View.INVISIBLE);
         if (mNextButton != null)
            mNextButton.setVisibility(View.INVISIBLE);
         if (mPauseButton != null)
            mPauseButton.setVisibility(View.INVISIBLE);
         if (mRemoteButton != null)
            mRemoteButton.setVisibility(View.INVISIBLE);

         if (mSeekBar != null)
            mSeekBar.setVisibility(View.VISIBLE);
      }
   }

   protected void setStatusText(String _text) {
      StatusBarActivityHandler.statusString = _text;
      if (mStatusText != null) {
         mStatusText.setText(StatusBarActivityHandler.statusString);
      }
   }

   public void setupRemoteStatus() {
      if (mStatusText != null) {
         // TODO: use async-task for this, also handle nowplaying here
         if (mRemote.isClientControlConnected() || mRemote.connectClientControl()) {
            // statusText.setText("Remote connected...");
         } else {
            Util.showToast(mParent, "Remote not connected");
            StatusBarActivityHandler.statusString = "Remote not connected...";
         }
         mStatusText.setText(StatusBarActivityHandler.statusString);
      }
   }

   public void setNowPlaying(NowPlaying _nowPlayingMessage) {
      StatusBarActivityHandler.nowPlayingMessage = _nowPlayingMessage;
      if (mSliderTitleText != null) {
         mSliderTitleText.setText(StatusBarActivityHandler.nowPlayingMessage.getTitle());
      }
   }
}