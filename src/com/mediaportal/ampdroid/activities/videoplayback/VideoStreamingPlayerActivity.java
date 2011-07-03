/***
Copyright (c) 2008-2009 CommonsWare, LLC
Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License. You may obtain
a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.mediaportal.ampdroid.activities.videoplayback;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseActivity;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.MediaInfo;
import com.mediaportal.ampdroid.data.PlaybackSession;
import com.mediaportal.ampdroid.database.PlaybackDatabaseHandler;
import com.mediaportal.ampdroid.downloadservice.DownloadItemType;
import com.mediaportal.ampdroid.settings.PreferencesManager;
import com.mediaportal.ampdroid.utils.Constants;
import com.mediaportal.ampdroid.utils.DateTimeHelper;
import com.mediaportal.ampdroid.utils.Util;
import com.mediaportal.ampdroid.videoplayer.TappableSurfaceView;

public class VideoStreamingPlayerActivity extends BaseActivity implements OnCompletionListener,
      MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
   private class StartStreamingTask extends AsyncTask<Boolean, Void, Boolean> {
      protected Boolean doInBackground(Boolean... _params) {
         boolean init = _params[0];

         if (mIsTv) {
            if (init) {
               Log.i(Constants.LOG_CONST, "Initialising Tv Stream");
               boolean success = mService.initTvStreaming(mIdentifier, mClientName,
                     Integer.parseInt(mStreamingFile), mProfile);
               if (!success) {
                  return false;
               }
            }
            mStreamingUrl = mService.startTvStreaming(mIdentifier, mStartPosition / 1000);

         } else if (mIsRecording) {
            Log.i(Constants.LOG_CONST, "Initialising Recording Stream");
            boolean success = mService.initRecordingStreaming(mIdentifier, mClientName,
                  Integer.parseInt(mStreamingFile), mProfile, (int) (mStartPosition / 1000));
            
            if (!success) {
               return false;
            }
            mStreamingUrl = mService.startRecordingStreaming(mIdentifier);
         } else {
            if (init) {
               Log.i(Constants.LOG_CONST, "Initialising Media Stream");
               mService.initStreaming(mIdentifier, mClientName, mStreamingType, mStreamingFile,
                     mProfile);
            }
            mStreamingUrl = mService.startStreaming(mIdentifier, mStartPosition / 1000);
         }

         Log.i(Constants.LOG_CONST, "New Streaming url: " + mStreamingUrl);
         return true;
      }

      @Override
      protected void onPostExecute(Boolean _result) {
         if (_result) {
            if (mUseInternalPlayer) {
               playVideo(USE_AUTOSTART);
            } else {
               showExternalPlayerStartOverlay(true);
               setSeekingIndicatorEnabled(false);
            }
         } else {
            showInitError();
         }
         super.onPostExecute(_result);
      }

      @Override
      protected void onProgressUpdate(Void... values) {
         // TODO Auto-generated method stub
         super.onProgressUpdate(values);
      }
   }

   private int mVideoWidth = 0;
   private int mVideoHeight = 0;
   private MediaPlayer mMediaPlayer;
   private TappableSurfaceView mSurface;
   private SurfaceHolder mHolder;
   private View mTopPanel = null;
   private View mBottomPanel = null;
   private long mLastActionTime = 0L;
   private boolean mIsPaused = false;
   private SeekBar mTimeline = null;
   private ImageButton mButtonPlayPause = null;
   public boolean mUpdating;
   public boolean mIsPlaying;
   private long mVideoLength = 0;
   private long mStartPosition;
   private Date mStartSeek;
   private ProgressBar mProgressbarSeeking;

   private String mStreamingFile;
   protected int mProgressLoaded;
   private DownloadItemType mStreamingType;
   private String mStreamingUrl;
   private FileInfo mFileInfo;
   private MediaInfo mMediaInfo;
   protected boolean mUserTracking;
   private boolean mSurfaceCreated;
   private String mIdentifier;
   private String mProfile;
   private boolean mPanelsVisible;
   private TextView mTextViewVideoName;
   private StartVideoPlaybackTask mStartVideoTask;
   private boolean mAutoStart;
   private String mDisplayName;
   private boolean mStartMediaPlayerOnSurfaceCreated;
   private boolean mMediaPlayerPrepared = false;
   private TextView mTextViewVideoPosition;
   private ImageView mImageViewOverlay;
   private String mClientName;
   private LinearLayout mStartExternalPlayerOverlay;
   protected boolean mUseInternalPlayer;
   private boolean mIsTv = false;
   private String[] mStreamingProfiles;
   protected StartStreamingTask mStartStreamingTask;
   public boolean mIsRecording = false;

   private static boolean USE_AUTOSTART = true;

   public void onCreate(Bundle _bundle) {
      super.onCreate(_bundle);
      mSurfaceCreated = false;
      Thread.setDefaultUncaughtExceptionHandler(onUncaughtException);

      setContentView(R.layout.activity_videoplayer);

      if (android.os.Build.VERSION.SDK_INT >= 9) {
         // only allow usage of inbuilt video player if at least
         // gingerbread (2.3). If after 2.3, the user can still decide
         // to use an external player
         mUseInternalPlayer = !PreferencesManager.getUseExternalPlayer();
      } else {
         mUseInternalPlayer = false;
      }

      mSurface = (TappableSurfaceView) findViewById(R.id.surface);
      mSurface.addTapListener(onTap);

      mProgressbarSeeking = (ProgressBar) findViewById(R.id.progressBarSeeking);
      mTextViewVideoName = (TextView) findViewById(R.id.TextViewVideoName);
      mTextViewVideoPosition = (TextView) findViewById(R.id.TextViewVideoPosition);
      mImageViewOverlay = (ImageView) findViewById(R.id.ImageViewVideoOverlay);
      mStartExternalPlayerOverlay = (LinearLayout) findViewById(R.id.LinearLayoutStartExternalPlayer);
      showExternalPlayerStartOverlay(false);
      mStartExternalPlayerOverlay.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View arg0) {
            playVideoInExternalPlayer();
         }
      });

      if (mUseInternalPlayer) {
         mHolder = mSurface.getHolder();
         mHolder.addCallback(this);
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

         mMediaPlayer = new MediaPlayer();
         mMediaPlayer.setScreenOnWhilePlaying(true);
         mMediaPlayer.setOnPreparedListener(this);

         mMediaPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer arg0, int _error1, int _error2) {
               switch (_error1) {
               case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                  /*try {
                     Thread.sleep(10000);
                  } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
                  if (mUseInternalPlayer) {
                     mMediaPlayer.stop();
                     mMediaPlayer.reset();
                  }
                  
                  playVideo(USE_AUTOSTART);*/
                  showMediaDiedError();
                  showToast("MEDIA_ERROR_SERVER_DIED", _error2);
                  break;
               case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                  showToast("MEDIA_ERROR_UNKNOWN", _error2);
                  break;
               case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                  showToast("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK", _error2);
                  break;
               default:
                  Log.w(Constants.LOG_CONST, "Media Error: " + String.valueOf(_error1) + " | "
                        + String.valueOf(_error2));
               }

               return false;
            }
         });
         mMediaPlayer.setOnCompletionListener(this);
      }

      mTopPanel = findViewById(R.id.top_panel);
      mBottomPanel = findViewById(R.id.bottom_panel);

      mTimeline = (SeekBar) findViewById(R.id.timeline);
      if (mTimeline != null) {
         mTimeline.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar _seekbar) {
               mStartSeek = new Date();
               mUserTracking = false;

               mStartPosition = mVideoLength * _seekbar.getProgress() / 1000;

               downloadAndShowOverlayImage((int) (mStartPosition / 1000));

               if (mUseInternalPlayer) {
                  mMediaPlayer.stop();
                  mMediaPlayer.reset();
               }

               mStartStreamingTask = new StartStreamingTask();
               mStartStreamingTask.execute(false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar _seekbar) {
               mUserTracking = true;
               setSeekingIndicatorEnabled(true);
               if (mUseInternalPlayer) {
                  mMediaPlayer.pause();
               }
               showExternalPlayerStartOverlay(false);
            }

            @Override
            public void onProgressChanged(SeekBar _seekbar, int _progress, boolean _fromUser) {
               if (_fromUser) {

               }
            }
         });
      }

      mTimeline.setMax(1000);
      setSeekingIndicatorEnabled(true);

      mButtonPlayPause = (ImageButton) findViewById(R.id.media);
      mButtonPlayPause.setOnClickListener(onMedia);

      Bundle extras = getIntent().getExtras();
      final Object retainData = getLastNonConfigurationInstance();

      mClientName = PreferencesManager.getTvClientName();

      if (retainData != null) {
         Log.d(Constants.LOG_CONST, "MediaPlayer: start from retained state");
         // we have retain data (from orientation change)
         PlaybackSession session = (PlaybackSession) retainData;
         mStreamingFile = session.getVideoId();
         mStreamingType = DownloadItemType.fromInt(session.getVideoType());
         mDisplayName = session.getDisplayName();
         mStartPosition = session.getStartPosition();
         mMediaInfo = session.getMetaData();
         mFileInfo = session.getVideoFile();
         mStreamingUrl = session.getVideoUrl();

         if (mMediaInfo != null) {
            mVideoLength = mMediaInfo.getDuration();
            mTimeline.setEnabled(true);
         }

         playVideo(true);
      } else if (extras != null) {
         Log.d(Constants.LOG_CONST, "MediaPlayer: start from fresh state");
         mStreamingFile = extras.getString("video_id");
         int type = extras.getInt("video_type", 0);
         mStreamingType = DownloadItemType.fromInt(type);
         mDisplayName = extras.getString("video_name");
         mProfile = extras.getString("profile_name");
         mIdentifier = createIdentifier();
         mStartPosition = extras.getLong("video_startfrom");

         if (extras.containsKey("video_length")) {
            mVideoLength = extras.getLong("video_length");
         }

         mStreamingProfiles = extras.getStringArray("streaming_profiles");

         mIsTv = (mStreamingType == DownloadItemType.LiveTv);
         mIsRecording = (mStreamingType == DownloadItemType.TvRecording);

         downloadAndShowOverlayImage((int) (mStartPosition / 1000));

         new Thread(new Runnable() {
            public void run() {
               if (mIsTv) {

               } else {
                  mFileInfo = mService.getFileInfo(mStreamingFile, mStreamingType);
                  mMediaInfo = mService.getMediaInfo(mStreamingFile, mStreamingType);
               }

               if (mMediaInfo != null) {
                  mVideoLength = mMediaInfo.getDuration();
               }

               mStartStreamingTask = new StartStreamingTask();
               mStartStreamingTask.execute(true);
            }
         }).start();
      } else {
         // something went wrong (false parameters?)
         Log.e(Constants.LOG_CONST, "Invalid parameters when opening video player");
         Util.showToast(this, "Invalid parameters when opening video player");
      }

      mTextViewVideoName.setText(mDisplayName);

   }

   private String createIdentifier() {
      return "aMPdroid." + new Random().nextInt() + ".mpeg";
   }

   public void showInitError() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.streaming_initfailed_title));
      builder.setMessage(getString(R.string.streaming_initfailed_text));
      builder.setCancelable(false);
      builder.setPositiveButton(getString(R.string.dialog_ok),
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  finish();
               }
            });

      AlertDialog alert = builder.create();
      alert.show();
   }

   public void showMediaDiedError() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle("MEDIA_ERROR_SERVER_DIED");
      builder.setMessage("Media-Died-Error, retry?");
      builder.setCancelable(false);
      builder.setPositiveButton(getString(R.string.dialog_yes),
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  mStartSeek = new Date();
                  stopStreamingAsync(mIdentifier);
                  mIdentifier = createIdentifier();

                  //mStartPosition = mStartPosition + mMediaPlayer.getCurrentPosition();
                  //downloadAndShowOverlayImage((int) (mStartPosition / 1000));

                  setSeekingIndicatorEnabled(true);
                  if (mUseInternalPlayer) {
                     mMediaPlayer.stop();
                     mMediaPlayer.reset();
                  }
                  showExternalPlayerStartOverlay(false);

                  mStartStreamingTask = new StartStreamingTask();
                  mStartStreamingTask.execute(true);
               }
            });
      
      builder.setNegativeButton(getString(R.string.dialog_no),
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  finish();
               }
            });

      AlertDialog alert = builder.create();
      alert.show();
   }

   protected void showExternalPlayerStartOverlay(boolean _visible) {
      if (_visible) {
         mStartExternalPlayerOverlay.setVisibility(View.VISIBLE);
      } else {
         mStartExternalPlayerOverlay.setVisibility(View.INVISIBLE);
      }

   }

   protected void downloadAndShowOverlayImage(final int _position) {
      // not yet implemented for tv
      if (!mIsTv) {
         new Thread(new Runnable() {
            public void run() {
               Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
               final Bitmap bm = mService.getBitmapFromMedia(mStreamingType, mStreamingFile,
                     _position, display.getWidth(), display.getHeight());
               mImageViewOverlay.post(new Runnable() {
                  public void run() {
                     setOverlayImage(new BitmapDrawable(bm));
                  }
               });
            }
         }).start();
      }
   }

   protected void setSeekingIndicatorEnabled(boolean _enabled) {
      if (_enabled) {
         mProgressbarSeeking.setVisibility(View.VISIBLE);
         mTextViewVideoPosition.setVisibility(View.INVISIBLE);
      } else {
         mProgressbarSeeking.setVisibility(View.INVISIBLE);
         mTextViewVideoPosition.setVisibility(View.VISIBLE);
      }
   }

   protected void showToast(String _error1, int _error2) {
      Util.showToast(this, "Error: " + _error1 + ", " + String.valueOf(_error2));
   }

   @Override
   protected void onPause() {
      Log.d(Constants.LOG_CONST, "MediaPlayer onPause");

      mIsPaused = true;

      if (!mIsTv) {
         PlaybackSession session = createPlaybackSession();

         Log.i(Constants.LOG_CONST, "Saving playback session state to db");
         try {
            PlaybackDatabaseHandler db = new PlaybackDatabaseHandler(this);
            db.open();
            db.savePlaybackSession(session);
            db.close();
         } catch (Exception ex) {
            Log.e(Constants.LOG_CONST,
                  "Error saving playback session state to db: " + ex.toString());
         }
      }
      super.onPause();
   }

   @Override
   protected void onStop() {
      Log.d(Constants.LOG_CONST, "MediaPlayer onStop");
      if (mMediaPlayer != null) {
         mMediaPlayer.stop();
      }

      super.onStop();
   }

   @Override
   protected void onResume() {
      Log.d(Constants.LOG_CONST, "MediaPlayer onResume");
      super.onResume();

      mIsPaused = false;
      mSurface.postDelayed(onEverySecond, 1000);
   }

   @Override
   protected void onDestroy() {
      Log.d(Constants.LOG_CONST, "MediaPlayer onDestroy");
      onEverySecond = null;

      if (mMediaPlayer != null) {
         mMediaPlayer.release();
         mMediaPlayer = null;
      }

      mUpdating = false;

      stopStreamingAsync(mIdentifier);

      mSurface.removeTapListener(onTap);
      super.onDestroy();
   }

   private void stopStreamingAsync(final String _identifier) {
      new Thread(new Runnable() {
         public void run() {
            if (mIsTv) {
               mService.stopTvStreaming(_identifier);
            } else {
               mService.stopStreaming(_identifier);
            }
         }
      }).start();
   }

   private PlaybackSession createPlaybackSession() {
      PlaybackSession session = new PlaybackSession();
      session.setMetaData(mMediaInfo);
      session.setVideoFile(mFileInfo);

      if (mUseInternalPlayer) {
         session.setStartPosition(mStartPosition + mMediaPlayer.getCurrentPosition());
      } else {
         session.setStartPosition(mStartPosition);
      }

      session.setVideoId(mStreamingFile);
      session.setVideoType(DownloadItemType.toInt(mStreamingType));
      session.setVideoUrl(mStreamingUrl);
      session.setDisplayName(mDisplayName);
      session.setLastWatched(new Date());
      return session;
   }

   @Override
   public Object onRetainNonConfigurationInstance() {
      Log.d(Constants.LOG_CONST, "MediaPlayer onRetainNonConfigurationInstance");
      // TODO: For orientation changes -> will try again at a later point,
      // proves to be really difficult.
      /*
       * PlaybackSession session = createPlaybackSession();
       * 
       * if (mMediaPlayer != null) { mMediaPlayer.stop(); mMediaPlayer.reset();
       * mMediaPlayer.release(); }
       * 
       * return session;
       */
      return null;
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      mLastActionTime = SystemClock.elapsedRealtime();

      if (keyCode == KeyEvent.KEYCODE_BACK
            && (mTopPanel.getVisibility() == View.VISIBLE || mBottomPanel.getVisibility() == View.VISIBLE)) {
         mLastActionTime = 0;

         setPanelsVisible(false);

         return (true);
      }

      mSurface.setBackgroundDrawable(null);
      return (super.onKeyDown(keyCode, event));
   }

   /*
    * public void onBufferingUpdate(MediaPlayer arg0, int percent) { }
    */

   public void onCompletion(MediaPlayer arg0) {
      Log.d(Constants.LOG_CONST, "MediaPlayer onCompletion");
      // finish();
      // media.setEnabled(false);
   }

   public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
      Log.d(Constants.LOG_CONST, "MediaPlayer surfaceChanged");
   }

   public void surfaceDestroyed(SurfaceHolder surfaceholder) {
      Log.d(Constants.LOG_CONST, "MediaPlayer surfaceDestroyed");
   }

   public void surfaceCreated(SurfaceHolder holder) {
      Log.d(Constants.LOG_CONST, "MediaPlayer surfaceCreated");

      if (mUseInternalPlayer) {
         mMediaPlayer.setDisplay(holder);
         mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

         mStartSeek = new Date();

         mSurfaceCreated = true;

         if (mStartMediaPlayerOnSurfaceCreated) {
            prepareMediaPlayerAsync();
         }
      }
   }

   private void prepareMediaPlayerAsync() {
      try {
         Log.d(Constants.LOG_CONST, "MediaPlayer prepareMediaPlayerAsync");
         mMediaPlayerPrepared = false;
         mMediaPlayer.setDataSource(this, Uri.parse(mStreamingUrl));
         mMediaPlayer.prepareAsync();
      } catch (IllegalArgumentException e) {
         Log.e(Constants.LOG_CONST, e.toString());
      } catch (SecurityException e) {
         Log.e(Constants.LOG_CONST, e.toString());
      } catch (IllegalStateException e) {
         Log.e(Constants.LOG_CONST, e.toString());
      } catch (IOException e) {
         Log.e(Constants.LOG_CONST, e.toString());
      }

   }

   private void startCheckingPlayStatus() {
      Log.d(Constants.LOG_CONST, "MediaPlayer start checking transcoding status");
      if (mStartVideoTask != null) {
         mStartVideoTask.cancelTask();
      }
      mStartVideoTask = new StartVideoPlaybackTask(this, mService);
      mStartVideoTask.execute();

   }

   private void playVideoInExternalPlayer() {
      try {
         Intent i = new Intent(Intent.ACTION_VIEW);
         i.setDataAndType(Uri.parse(mStreamingUrl), "video/*");
         startActivityForResult(i, 1);
      } catch (Exception ex) {
         Log.e(Constants.LOG_CONST, ex.toString());
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 1) {
         if (resultCode < 0) {
            Util.showToast(this, getString(R.string.tvserver_errorplaying) + mStreamingUrl
                  + getString(R.string.tvserver_errorplaying_resultcode) + resultCode);
         } else {
            Util.showToast(this, getString(R.string.tvserver_finishedplaying) + mStreamingUrl);
         }
         mService.stopTimeshift(PreferencesManager.getTvClientName());
      }

      super.onActivityResult(requestCode, resultCode, data);
   }

   private void playVideo(boolean autostart) {
      Log.d(Constants.LOG_CONST, "MediaPlayer playVideo");
      try {
         if (!autostart) {
            startCheckingPlayStatus();
         }
         mAutoStart = autostart;
         if (mSurfaceCreated) {
            prepareMediaPlayerAsync();
         } else {
            mStartMediaPlayerOnSurfaceCreated = true;
         }
      } catch (Throwable t) {
         Log.e(Constants.LOG_CONST, "Exception in media prep", t);
      }
   }

   protected void setOverlayImage(BitmapDrawable _bitmap) {
      if (_bitmap != null) {
         mImageViewOverlay.setImageDrawable(_bitmap);
      } else {
         // TODO: fade out
         mImageViewOverlay.setImageDrawable(null);
      }
   }

   public void onPrepared(MediaPlayer mediaplayer) {
      Log.d(Constants.LOG_CONST, "MediaPlayer onPrepared");
      mVideoWidth = mMediaPlayer.getVideoWidth();
      mVideoHeight = mMediaPlayer.getVideoHeight();

      if (mVideoWidth != 0 && mVideoHeight != 0) {

         // Get the width of the screen
         int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
         int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

         // Get the SurfaceView layout parameters
         android.view.ViewGroup.LayoutParams lp = mSurface.getLayoutParams();

         if (screenWidth > screenHeight) {
            // landscape
            lp.width = screenWidth;
            lp.height = (int) (((float) mVideoHeight / (float) mVideoWidth) * (float) screenWidth);

         } else {
            lp.width = screenWidth;
            lp.height = (int) (((float) mVideoHeight / (float) mVideoWidth) * (float) screenWidth);
         }

         // // Commit the layout parameters
         mSurface.setLayoutParams(lp);

         // holder.setFixedSize(width, height);
         // timeline.setProgress(0);
         // timeline.setSecondaryProgress(0);

         if (mStartSeek != null) {
            Util.showToast(
                  this,
                  "Time for seeking: "
                        + String.valueOf(new Date().getTime() - mStartSeek.getTime()) + " ms");
         }

         if (mAutoStart) {
            mMediaPlayer.start();
            setOverlayImage(null);
         }
         mBottomPanel.setVisibility(View.VISIBLE);
         setSeekingIndicatorEnabled(false);

         mMediaPlayerPrepared = true;
         mSurface.postDelayed(onEverySecond, 1000);
      }

      // media.setEnabled(true);
   }

   private TappableSurfaceView.TapListener onTap = new TappableSurfaceView.TapListener() {
      public void onTap(MotionEvent event) {
         mLastActionTime = SystemClock.elapsedRealtime();

         setPanelsVisible(!mPanelsVisible);
      }
   };

   private Runnable onEverySecond = new Runnable() {
      public void run() {
         try {
            if (mUseInternalPlayer) {
               if (mLastActionTime > 0 && SystemClock.elapsedRealtime() - mLastActionTime > 3000) {
                  mLastActionTime = 0;

                  if (mMediaPlayer.isPlaying()) {
                     setPanelsVisible(false);
                  }
               }

               if (!mUserTracking && mMediaPlayer != null) {
                  if (mMediaPlayerPrepared && mVideoLength > 0) {
                     mTimeline.setEnabled(true);
                     long pos = (mStartPosition + mMediaPlayer.getCurrentPosition()) * 1000
                           / mVideoLength;
                     mTextViewVideoPosition
                           .setText(DateTimeHelper.getTimeStringFromMs(mStartPosition
                                 + mMediaPlayer.getCurrentPosition()));
                     // mTextViewVideoPosition.setText(String.valueOf(mMediaPlayer.getCurrentPosition()));
                     mTimeline.setProgress((int) pos);
                  } else {
                     mTimeline.setEnabled(false);
                  }
               }

               if (!mIsPaused) {
                  mSurface.postDelayed(onEverySecond, 1000);
               }
            }
         } catch (Exception ex) {
            Log.e(Constants.LOG_CONST, "Error on runnable: " + ex.toString());
         }
      }
   };

   private View.OnClickListener onMedia = new View.OnClickListener() {
      public void onClick(View v) {
         mLastActionTime = SystemClock.elapsedRealtime();

         if (mUseInternalPlayer) {
            if (mMediaPlayer != null) {
               if (mMediaPlayer.isPlaying()) {
                  mButtonPlayPause.setImageResource(R.drawable.ic_media_play);
                  mMediaPlayer.pause();
                  // mMediaPlayer.stop();
                  // mMediaPlayer.reset();
               } else {
                  mButtonPlayPause.setImageResource(R.drawable.ic_media_pause);
                  mMediaPlayer.start();
                  // playVideo(mStreamingUrl, true);
               }
            }
         } else {
            playVideoInExternalPlayer();
         }
      }
   };

   private Thread.UncaughtExceptionHandler onUncaughtException = new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread thread, Throwable ex) {
         Log.e(Constants.LOG_CONST, "Uncaught exception", ex);
      }
   };
   private SubMenu mQualityItem;

   protected void setPanelsVisible(boolean _visible) {
      mPanelsVisible = _visible;
      if (_visible) {
         mTopPanel.setVisibility(View.VISIBLE);
         mBottomPanel.setVisibility(View.VISIBLE);
      } else {
         mTopPanel.setVisibility(View.GONE);
         mBottomPanel.setVisibility(View.GONE);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu _menu) {
      if (!mIsTv) {
         // TODO: also enable beaming for tv
         MenuItem settingsItem = _menu.add(0, Menu.FIRST, Menu.NONE,
               getString(R.string.menu_beam_to_pc));
         settingsItem.setIcon(R.drawable.ic_menu_share);
         settingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
               if (mService.isClientControlConnected()) {
                  int pos = (int) ((mStartPosition + mMediaPlayer.getCurrentPosition()) / 1000);
                  mService.playVideoFileOnClient(mFileInfo.getFullPath(), pos);
                  mButtonPlayPause.setImageResource(R.drawable.ic_media_play);
                  mMediaPlayer.pause();
               }
               return true;
            }
         });
      }

      mQualityItem = _menu.addSubMenu(0, Menu.FIRST, Menu.NONE,
            getString(R.string.menu_select_stream_quality));
      mQualityItem.setIcon(R.drawable.ic_menu_equalizer);

      return true;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      if (mStreamingProfiles != null) {
         mQualityItem.clear();
         for (final String p : mStreamingProfiles) {
            MenuItem profile = mQualityItem.add(0, Menu.FIRST, Menu.NONE, p);
            profile.setCheckable(true);
            if (mProfile.equals(p)) {
               profile.setChecked(true);
            }

            profile.setOnMenuItemClickListener(new OnMenuItemClickListener() {
               @Override
               public boolean onMenuItemClick(MenuItem item) {
                  changeStreamingQuality(p);
                  return true;
               }
            });
         }
      }
      return super.onPrepareOptionsMenu(menu);
   }

   protected void changeStreamingQuality(String _profile) {
      if (mProfile != _profile) {
         mProfile = _profile;
         mStartSeek = new Date();
         String oldIdentifier = mIdentifier;
         stopStreamingAsync(oldIdentifier);
         mIdentifier = createIdentifier();

         mStartPosition = mStartPosition + mMediaPlayer.getCurrentPosition();
         downloadAndShowOverlayImage((int) (mStartPosition / 1000));

         setSeekingIndicatorEnabled(true);
         if (mUseInternalPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
         }
         showExternalPlayerStartOverlay(false);

         mStartStreamingTask = new StartStreamingTask();
         mStartStreamingTask.execute(true);
      }
   }

   protected MediaPlayer getMediaPlayer() {
      return mMediaPlayer;
   }

   public void startMediaTaskFinished() {
      mStartVideoTask = null;
   }
}