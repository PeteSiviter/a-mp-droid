package com.mediaportal.ampdroid.activities.music;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseTabActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.api.ApiCredentials;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.data.MusicTrack;
import com.mediaportal.ampdroid.downloadservice.DownloadItemType;
import com.mediaportal.ampdroid.downloadservice.DownloadJob;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderHelper;
import com.mediaportal.ampdroid.downloadservice.MediaItemType;
import com.mediaportal.ampdroid.lists.ImageHandler;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ILoadingListener;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.lists.views.ViewTypes;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabAlbumDetailsActivity extends Activity implements ILoadingListener {
   private LinearLayout mTracksListView;
   private LazyLoadingAdapter mAdapter;
   private DataHandler mService;
   private LoadAlbumTracksTask mMusicLoaderTask;
   private BaseTabActivity mBaseActivity;
   private StatusBarActivityHandler mStatusBarHandler;
   private String mAlbumArtistsString;
   private String mAlbumName;
   private String mActivityGroup;
   private String mAlbumCover;
   private String[] mAlbumArtists;
   private ImageView mAlbumCoverImage;
   private ImageHandler mImageHandler;
   private TextView mTextViewAlbumName;
   private TextView mTextViewAlbumYear;
   private TextView mTextViewAlbumTracks;
   private TextView mTextViewAlbumLength;

   private class LoadAlbumTracksTask extends AsyncTask<Integer, List<Movie>, List<MusicTrack>> {
      Activity mContext;

      private LoadAlbumTracksTask(Activity _context) {
         mContext = _context;
      }

      @Override
      protected List<MusicTrack> doInBackground(Integer... _params) {
         List<MusicTrack> items = mService.getSongsOfAlbum(mAlbumName, mAlbumArtistsString);
         return items;
      }

      @Override
      protected void onPostExecute(List<MusicTrack> _result) {
         if (_result != null) {
            int trackLength = 0;
            int yearMin = Integer.MAX_VALUE;
            int yearMax = 0;
            for (int i = 0; i < _result.size(); i++) {
               View view = Button.inflate(mContext, R.layout.listitem_track, null);
               TextView text = (TextView) view.findViewById(R.id.TextViewTitle);
               ImageView image = (ImageView) view.findViewById(R.id.ImageViewEventImage);
               TextView subtext = (TextView) view.findViewById(R.id.TextViewText);

               MusicTrack s = _result.get(i);
               // String seasonBanner = null;
               // if (seasonBanner != null && !seasonBanner.equals("")) {
               // String fileName = Utils.getFileNameWithExtension(seasonBanner,
               // "\\");
               // String cacheName = "Series" + File.separator + "" +
               // File.separator
               // + "LargePoster" + File.separator + fileName;
               //
               // LazyLoadingImage bannerImage = new
               // LazyLoadingImage(seasonBanner, cacheName, 75,
               // 100);
               // image.setTag(seasonBanner);
               // //mImageHandler.DisplayImage(bannerImage,
               // R.drawable.listview_imageloading_poster,
               // // mContext, image);
               // }

               view.setOnTouchListener(new OnTouchListener() {

                  @Override
                  public boolean onTouch(View v, MotionEvent event) {
                     if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setBackgroundResource(android.R.drawable.list_selector_background);
                     } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        v.setBackgroundColor(Color.TRANSPARENT);
                     } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                     } else {
                        v.setBackgroundColor(Color.TRANSPARENT);
                     }

                     return false;
                  }
               });

               view.setOnLongClickListener(new OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View _view) {
                     try {
                        MusicTrack selected = (MusicTrack) _view.getTag();
                        final String trackTitle = selected.getTitle();
                        final String trackPath = selected.getFilePath();
                        final String trackId = String.valueOf(selected.getTrackId());
                        if (trackTitle != null) {
                           String dirName = DownloaderUtils.getMusicTrackPath();
                           final String fileName = dirName
                                 + Utils.getFileNameWithExtension(trackPath, "\\");
                           final String displayName = selected.toString();
                           final QuickAction qa = new QuickAction(_view);

                           final File localFileName = new File(DownloaderUtils.getBaseDirectory()
                                 + "/" + fileName);

                           if (localFileName.exists()) {
                              ActionItem playItemAction = new ActionItem();

                              playItemAction.setTitle(getString(R.string.quickactions_playdevice));
                              playItemAction.setIcon(getResources().getDrawable(
                                    R.drawable.quickaction_play));
                              playItemAction.setOnClickListener(new OnClickListener() {
                                 @Override
                                 public void onClick(View _view) {
                                    Intent playIntent = new Intent(Intent.ACTION_VIEW);
                                    playIntent.setDataAndType(Uri.parse(localFileName.toString()),
                                          "audio/mp3");
                                    startActivity(playIntent);

                                    qa.dismiss();
                                 }
                              });

                              qa.addActionItem(playItemAction);
                           } else {
                              ActionItem sdCardAction = new ActionItem();
                              sdCardAction.setTitle(getString(R.string.quickactions_downloadsd));
                              sdCardAction.setIcon(getResources().getDrawable(
                                    R.drawable.quickaction_sdcard));
                              sdCardAction.setOnClickListener(new OnClickListener() {
                                 @Override
                                 public void onClick(View _view) {
                                    String url = mService.getDownloadUri(trackId,
                                          DownloadItemType.MusicTrackItem);
                                    FileInfo info = mService.getFileInfo(trackPath);
                                    ApiCredentials cred = mService.getDownloadCredentials();
                                    if (url != null) {
                                       DownloadJob job = new DownloadJob();
                                       job.setUrl(url);
                                       job.setFileName(fileName);
                                       job.setDisplayName(displayName);
                                       job.setMediaType(MediaItemType.Music);
                                       if (info != null) {
                                          job.setLength(info.getLength());
                                       }
                                       if (cred.useAut()) {
                                          job.setAuth(cred.getUsername(), cred.getPassword());
                                       }

                                       Intent download = ItemDownloaderHelper.createDownloadIntent(
                                             _view.getContext(), job);
                                       startService(download);
                                    }
                                    qa.dismiss();
                                 }
                              });
                              qa.addActionItem(sdCardAction);
                           }

                           if (mService.isClientControlConnected()) {
                              ActionItem playOnClientAction = new ActionItem();

                              playOnClientAction
                                    .setTitle(getString(R.string.quickactions_playclient));
                              playOnClientAction.setIcon(getResources().getDrawable(
                                    R.drawable.quickaction_play_device));
                              playOnClientAction.setOnClickListener(new OnClickListener() {
                                 @Override
                                 public void onClick(View _view) {
                                    mService.playAudioFileOnClient(trackPath);

                                    qa.dismiss();
                                 }
                              });
                              qa.addActionItem(playOnClientAction);
                           }

                           qa.setAnimStyle(QuickAction.ANIM_AUTO);

                           qa.show();
                        } else {
                           Util.showToast(_view.getContext(), getString(R.string.media_nofile));
                        }
                        return true;
                     } catch (Exception ex) {
                        return false;
                     }
                  }
               });

               text.setText(s.getTitle());
               subtext.setText(s.getArtistsString());
               view.setTag(s);

               trackLength += s.getDuration();

               if (s.getYear() > yearMax) {
                  yearMax = s.getYear();
               }

               if (s.getYear() < yearMin) {
                  yearMin = s.getYear();
               }

               mTracksListView.addView(view);
            }

            if (yearMin == yearMax) {
               mTextViewAlbumYear.setText(String.valueOf(yearMin));
            } else {
               mTextViewAlbumYear.setText(String.valueOf(yearMin)
                     + getText(R.string.music_album_details_year_seperator)
                     + String.valueOf(yearMax));
            }

            if (trackLength > 0) {
               mTextViewAlbumLength.setText(String.valueOf(trackLength / 60));
            }
            mTextViewAlbumTracks.setText(String.valueOf(_result.size()));
         }
      }
   }

   @Override
   public void EndOfListReached() {
      loadFurtherItems();
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tabalbumdetailsactivity);

      mBaseActivity = (BaseTabActivity) getParent().getParent();

      mService = DataHandler.getCurrentRemoteInstance();
      mStatusBarHandler = new StatusBarActivityHandler(mBaseActivity, mService);
      mStatusBarHandler.setHome(false);

      mAdapter = new LazyLoadingAdapter(this);
      mAdapter.addView(ViewTypes.TextView.ordinal());
      mAdapter.setLoadingListener(this);

      mTracksListView = (LinearLayout) findViewById(R.id.LinearLayoutTracks);

      mTextViewAlbumName = (TextView) findViewById(R.id.TextViewAlbumName);
      mTextViewAlbumYear = (TextView) findViewById(R.id.TextViewAlbumYear);
      mTextViewAlbumTracks = (TextView) findViewById(R.id.TextViewAlbumNumTracks);
      mTextViewAlbumLength = (TextView) findViewById(R.id.TextViewAlbumLength);

      mAlbumCoverImage = (ImageView) findViewById(R.id.ImageViewAlbumCover);
      mImageHandler = new ImageHandler(this);
      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mAlbumArtistsString = extras.getString("album_artists_string");
         mAlbumName = extras.getString("album_name");
         mAlbumCover = extras.getString("album_cover");
         mActivityGroup = extras.getString("activity_group");
         mAlbumArtists = extras.getStringArray("album_artists");
      }

      if (mAlbumCover != null && !mAlbumCover.equals("")) {
         String fileName = Utils.getFileNameWithExtension(mAlbumCover, "\\");
         String cacheName = "Music" + File.separator + mAlbumArtists[0] + File.separator + "Thumbs"
               + File.separator + fileName;
         LazyLoadingImage image = new LazyLoadingImage(mAlbumCover, cacheName, 150, 200);
         mAlbumCoverImage.setTag(mAlbumCover);
         mImageHandler.DisplayImage(image, R.drawable.listview_imageloading_poster, this,
               mAlbumCoverImage);
      }

      mTextViewAlbumName.setText(mAlbumName);

      loadFurtherItems();
   }

   private void loadFurtherItems() {
      if (mMusicLoaderTask == null) {
         mMusicLoaderTask = new LoadAlbumTracksTask(this);
         mMusicLoaderTask.execute(20);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu _menu) {
      super.onCreateOptionsMenu(_menu);

      return true;
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
   }

   @Override
   public void onBackPressed() {
      if (mActivityGroup.equals("artists")) {
         TabArtistsActivityGroup.getGroup().back();
      } else if (mActivityGroup.equals("albums")) {
         TabAlbumsActivityGroup.getGroup().back();
      }
      return;
   }

   @Override
   public boolean onKeyDown(int _keyCode, KeyEvent _event) {
      if (_keyCode == KeyEvent.KEYCODE_BACK) {
         if (mActivityGroup.equals("artists")) {
            TabArtistsActivityGroup.getGroup().back();
         } else if (mActivityGroup.equals("albums")) {
            TabAlbumsActivityGroup.getGroup().back();
         }
         return true;
      }
      return super.onKeyDown(_keyCode, _event);
   }
}
