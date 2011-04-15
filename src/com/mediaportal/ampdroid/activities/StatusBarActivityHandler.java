package com.mediaportal.ampdroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.actionbar.ActionBar;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.api.RemoteCommands;
import com.mediaportal.ampdroid.data.commands.RemoteKey;
import com.mediaportal.ampdroid.remote.RemoteNowPlaying;
import com.mediaportal.ampdroid.remote.RemoteNowPlayingUpdate;
import com.mediaportal.ampdroid.remote.RemotePropertiesUpdate;
import com.mediaportal.ampdroid.remote.RemoteStatusMessage;
import com.mediaportal.ampdroid.remote.RemoteVolumeMessage;
import com.mediaportal.ampdroid.utils.DpiUtils;
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
   private SeekBar mPositionSlider;
   private boolean mProgressSeekBarChanging;
   private int mIgnoreProgressChangedCounter = 0;
   private boolean mVolumeSeekBarChanging;
   private Button mInfoButton;
   private TextView mSliderText;
   private TextView mSliderTextDetails;
   private RelativeLayout mStatusBar;
   private View mBottomMarginLayout;

   private static String currentStatusString;
   private static RemoteNowPlaying currentNowPlayingMessage;
   private static RemoteVolumeMessage currentVolumeMessage;
   private static boolean currentIsTvPlaying;
   private static String currentTitle;
   private static String currentDesc;
   private static int currentProgress;

   public StatusBarActivityHandler(Activity _parent, DataHandler _remote) {
      this(_parent, _remote, false);
   }

   public StatusBarActivityHandler(Activity _parent, DataHandler _remote, boolean _isHome) {
      mParent = _parent;
      mRemote = _remote;
      isHome = _isHome;

      mSlider = (SlidingDrawer) mParent.findViewById(R.id.SlidingDrawerStatus);
      mStatusBar = (RelativeLayout) mParent.findViewById(R.id.BottomBarLayout);
      mBottomMarginLayout = (View) mParent.findViewById(R.id.BottomMarginLayout);

      mPauseButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomPause);
      mPrevButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomRewind);
      mNextButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomNext);
      mRemoteButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomRemote);
      mVolumeButton = (ImageButton) mParent.findViewById(R.id.ImageButtonBottomVolume);
      mInfoButton = (Button) mParent.findViewById(R.id.ButtonInfo);

      mPositionSlider = (SeekBar) mParent.findViewById(R.id.SeekBarSliderPosition);
      mPositionSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
         @Override
         public void onStopTrackingTouch(SeekBar _seekBar) {
            int seekValue = _seekBar.getProgress();// between 0 - 100
            if (mRemote.isClientControlConnected()) {
               mRemote.sendClientPosition(seekValue);
               mIgnoreProgressChangedCounter = 3;
            }

            mProgressSeekBarChanging = false;
         }

         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {
            mProgressSeekBarChanging = true;
         }

         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         }
      });

      if (mPauseButton != null) {
         mPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               sendRemoteKey(RemoteCommands.pauseButton);
            }

         });

         mPauseButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
               sendRemoteKey(RemoteCommands.stopButton);
               return true;
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
               // Util.Vibrate(_view.getContext(), 50);
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

         mVolumeButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
               // send mute button
               mRemote.sendRemoteButton(RemoteCommands.muteButton);

               return true;
            }
         });
      }

      if (mInfoButton != null) {
         mInfoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               mRemote.sendRemoteButton(RemoteCommands.infoButton);
            }
         });
      }

      mStatusText = (TextView) mParent.findViewById(R.id.TextViewSliderSatusText);
      mSliderTitleText = (TextView) mParent.findViewById(R.id.TextViewSliderTitle);
      mSliderText = (TextView) mParent.findViewById(R.id.TextViewSliderText);
      mSliderTextDetails = (TextView) mParent.findViewById(R.id.TextViewSliderTextDetail);

      mSeekBar = (SeekBar) mParent.findViewById(R.id.SeekBarBottomVolume);
      mVolumeSeekBarChanging = false;

      if (mSeekBar != null) {
         mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar _seekBar) {
               mVolumeSeekBarChanging = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar _seekBar) {
               mVolumeSeekBarChanging = true;
               if (!mRemote.isClientControlConnected()) {
                  Util.showToast(mParent, mParent.getString(R.string.info_remote_notconnected));
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

            ImageButton homeButton = (ImageButton) actionBar.getHomeButton();
            homeButton.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                  Intent intent = new Intent(mParent, HomeActivity.class);
                  intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  mParent.startActivity(intent);
               }
            });

            actionBar.setInitialised(true);
         }
      }

      fillDefaults();
   }

   private void fillDefaults() {
      if (currentTitle != null) {
         mStatusText.setText(currentTitle);
         mSliderTitleText.setText(currentTitle);
      }

      if (currentDesc != null) {
         mSliderTextDetails.setText(currentDesc);
      }

      setProgress(currentProgress);
      setVolume(currentVolumeMessage);
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

   private void sendRemoteKey(RemoteKey _button) {
      Util.Vibrate(mParent, 50);
      if (mRemote.isClientControlConnected()) {
         mRemote.sendRemoteButton(_button);
      } else {
         Util.showToast(mParent, mParent.getString(R.string.info_remote_notconnected));
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

   public void setNowPlaying(RemoteNowPlaying _nowPlayingMessage) {
      StatusBarActivityHandler.currentNowPlayingMessage = _nowPlayingMessage;
      StatusBarActivityHandler.currentIsTvPlaying = _nowPlayingMessage.isTv();

      if (mSliderTitleText != null && StatusBarActivityHandler.currentNowPlayingMessage != null) {
         int duration = StatusBarActivityHandler.currentNowPlayingMessage.getDuration();
         int position = StatusBarActivityHandler.currentNowPlayingMessage.getPosition();

         if (duration == 0) {
            mPositionSlider.setProgress(100);
         } else if (position == 0) {
            mPositionSlider.setProgress(0);
         } else {
            mPositionSlider.setProgress(duration * 100 / position);
         }
      }
   }

   public void setNowPlaying(RemoteNowPlayingUpdate _nowPlayingMessage) {
      if (_nowPlayingMessage != null && mPositionSlider != null && !mProgressSeekBarChanging) {
         StatusBarActivityHandler.currentIsTvPlaying = _nowPlayingMessage.isTv();
         if (mIgnoreProgressChangedCounter <= 0) {
            int pos = _nowPlayingMessage.getPosition();
            int duration = _nowPlayingMessage.getDuration();

            int progress = 0;
            if (pos != 0 && duration != 0) {
               progress = pos * 100 / duration;
            }
            setProgress(progress);
         } else {
            mIgnoreProgressChangedCounter--;
         }
      }
   }

   private void setProgress(int _progress) {
      currentProgress = _progress;
      if (mPositionSlider != null) {
         mPositionSlider.setProgress(_progress);
      }
   }

   public void setNowPlaying(final RemotePropertiesUpdate _property) {
      if (_property != null) {
         mParent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
               if (StatusBarActivityHandler.currentIsTvPlaying) {
                  if (_property.getTag().equals("#TV.View.description")) {
                     setTextDetails(_property.getValue());
                  } else if (_property.getTag().equals("#TV.View.title")) {
                     setStatusText(_property.getValue());
                     setTextTitle(_property.getValue());
                  }
               } else {
                  if (_property.getTag().equals("#Play.Current.Title")) {
                     setStatusText(_property.getValue());
                     setTextTitle(_property.getValue());
                  } else if (_property.getTag().equals("#Play.Current.Plot")) {
                     setTextDetails(_property.getValue());
                  }
               }
            }
         });
      }
   }

   protected void setTextTitle(String value) {
      StatusBarActivityHandler.currentTitle = value;
      if (mSliderTitleText != null) {
         mSliderTitleText.setText(value);
      }
   }

   protected void setTextDetails(String value) {
      StatusBarActivityHandler.currentDesc = value;
      if (mSliderTextDetails != null) {
         mSliderTextDetails.setText(value);
      }
   }

   protected void setStatusText(String _text) {
      StatusBarActivityHandler.currentStatusString = _text;
      if (mStatusText != null) {
         mStatusText.setText(StatusBarActivityHandler.currentStatusString);
      }
   }

   public void setStatus(RemoteStatusMessage _statusMessage) {
      if (_statusMessage != null) {
         if (!_statusMessage.isIsPlaying()) {
            mParent.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  setStatusText("");
                  setTextDetails("");
                  setTextTitle("");
               }
            });

         }
      }
   }

   public void setVolume(final RemoteVolumeMessage _statusMessage) {
      StatusBarActivityHandler.currentVolumeMessage = _statusMessage;
      if (_statusMessage != null) {
         if (mSeekBar != null) {
            mParent.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  if (!mVolumeSeekBarChanging) {
                     int volume = _statusMessage.getVolume();
                     mSeekBar.setProgress((int) (volume * 0.2));
                  }
                  if (_statusMessage.isIsMuted()) {
                     mVolumeButton.setImageResource(R.drawable.button_volume_muted);
                  } else {
                     mVolumeButton.setImageResource(R.drawable.button_volume);
                  }
               }
            });
         }
      }
   }

   public void hide() {
      mSlider.setVisibility(View.GONE);
      mStatusBar.setVisibility(View.GONE);
      if (mBottomMarginLayout != null) {
         FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
               LayoutParams.FILL_PARENT);
         lp.gravity = Gravity.TOP;
         lp.setMargins(0, DpiUtils.getPxFromDpi(mParent, 50), 0, 0);
         mBottomMarginLayout.setLayoutParams(lp);
      }
   }

   public void show() {
      mSlider.setVisibility(View.VISIBLE);
      mStatusBar.setVisibility(View.VISIBLE);

      if (mBottomMarginLayout != null) {
         FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
               LayoutParams.FILL_PARENT);
         lp.gravity = Gravity.TOP;
         lp.setMargins(0, DpiUtils.getPxFromDpi(mParent, 50), 0, DpiUtils.getPxFromDpi(mParent, 90));
         mBottomMarginLayout.setLayoutParams(lp);
      }
   }
}
