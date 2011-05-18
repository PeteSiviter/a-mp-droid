package com.mediaportal.ampdroid.activities.music;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseTabActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.api.ApiCredentials;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.MusicAlbum;
import com.mediaportal.ampdroid.data.MusicTrack;
import com.mediaportal.ampdroid.downloadservice.DownloadJob;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderHelper;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderService;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ILoadingListener;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.lists.views.MusicAlbumTextViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.MusicAlbumThumbViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.ViewTypes;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabAlbumsActivity extends Activity implements ILoadingListener {
   private ListView mListView;
   private LazyLoadingAdapter mAdapter;
   DataHandler mService;
   private LoadMusicTask mMusicLoaderTask;
   private int mItemsLoaded = 0;
   private BaseTabActivity mBaseActivity;
   private StatusBarActivityHandler mStatusBarHandler;
   private String mArtist;
   private String mActivityGroup;

   private class DownloadAlbumTask extends AsyncTask<MusicAlbum, Intent, Boolean> {
      private Context mContext;

      private DownloadAlbumTask(Context _context) {
         mContext = _context;
      }

      @Override
      protected Boolean doInBackground(MusicAlbum... _params) {
         MusicAlbum album = _params[0];
         List<MusicTrack> tracks = mService.getSongsOfAlbum(album.getTitle(),
               album.getAlbumArtistString());

         for (int i = 0; i < tracks.size(); i++) {
            MusicTrack track = tracks.get(i);

            String url = mService.getDownloadUri(track.getFilePath());
            FileInfo info = mService.getFileInfo(track.getFilePath());
            String dirName = DownloaderUtils.getMusicTrackPath(album.getAlbumArtists()[0],
                  album.getTitle());
            final String fileName = dirName
                  + Utils.getFileNameWithExtension(track.getFilePath(), "\\");

            ApiCredentials cred = mService.getDownloadCredentials();
            if (url != null) {
               DownloadJob job = new DownloadJob();
               job.setUrl(url);
               job.setFileName(fileName);
               job.setDisplayName(track.toString());
               if (info != null) {
                  job.setLength(info.getLength());
               }
               if (cred.useAut()) {
                  job.setAuth(cred.getUsername(), cred.getPassword());
               }

               Intent download = ItemDownloaderHelper.createDownloadIntent(
                     mContext, job);
               publishProgress(download);
            }
         }

         return true;
      }

      @Override
      protected void onProgressUpdate(Intent... values) {
         Intent donwloadIntent = values[0];
         if (donwloadIntent != null) {
            startService(donwloadIntent);
         }
         super.onProgressUpdate(values);
      }

      @Override
      protected void onPostExecute(Boolean _result) {

      }
   }

   private class LoadMusicTask extends AsyncTask<Integer, List<MusicAlbum>, Boolean> {
      private Context mContext;

      private LoadMusicTask(Context _context) {
         mContext = _context;
      }

      @SuppressWarnings("unchecked")
      @Override
      protected Boolean doInBackground(Integer... _params) {
         if (mArtist == null) {
            int albumsCount = mService.getAlbumsCount();
            int loadItems = mItemsLoaded + _params[0];

            while (mItemsLoaded < loadItems) {
               int end = mItemsLoaded + 4;
               if (end >= albumsCount) {
                  end = albumsCount - 1;
               }
               List<MusicAlbum> items = mService.getAlbums(mItemsLoaded, end);

               publishProgress(items);
               if (items == null) {
                  return false;
               }

               mItemsLoaded += 5;
            }

            if (mItemsLoaded < albumsCount) {
               return false;// not yet finished;
            } else {
               return true;// finished
            }
         } else {
            List<MusicAlbum> items = mService.getMusicAlbumsByArtist(mArtist);

            if (items != null) {
               publishProgress(items);
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      protected void onProgressUpdate(List<MusicAlbum>... values) {
         if (values != null) {
            List<MusicAlbum> series = values[0];
            if (series != null) {
               boolean showArtist = mArtist == null;
               for (MusicAlbum s : series) {
                  mAdapter.addItem(ViewTypes.TextView.ordinal(), new MusicAlbumTextViewAdapterItem(
                        s, showArtist));
                  mAdapter.addItem(ViewTypes.ThumbView.ordinal(),
                        new MusicAlbumThumbViewAdapterItem(s, showArtist));
               }
            } else {
               mAdapter.showLoadingItem(false);
               // mAdapter.setLoadingText("Loading failed, check your connection");
               Util.showToast(mContext, getString(R.string.info_loading_failed));
            }
         }

         mAdapter.notifyDataSetChanged();
         super.onProgressUpdate(values);
      }

      @Override
      protected void onPostExecute(Boolean _result) {
         if (_result) {
            mAdapter.showLoadingItem(false);
            mAdapter.notifyDataSetChanged();
         }
         mStatusBarHandler.setLoading(false);
         mMusicLoaderTask = null;
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
      setContentView(R.layout.tabseriesactivity);

      mBaseActivity = (BaseTabActivity) getParent().getParent();

      mService = DataHandler.getCurrentRemoteInstance();
      mStatusBarHandler = new StatusBarActivityHandler(mBaseActivity, mService);
      mStatusBarHandler.setHome(false);

      mAdapter = new LazyLoadingAdapter(this);
      mAdapter.addView(ViewTypes.TextView.ordinal());
      mAdapter.addView(ViewTypes.ThumbView.ordinal());
      mAdapter.setView(ViewTypes.ThumbView.ordinal());
      mAdapter.setLoadingListener(this);

      mListView = (ListView) findViewById(R.id.ListViewVideos);
      mListView.setAdapter(mAdapter);

      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> _adapter, View _view, int _position, long _id) {
            ILoadingAdapterItem selectedItem = (ILoadingAdapterItem) mListView
                  .getItemAtPosition(_position);
            MusicAlbum selectedSeries = (MusicAlbum) selectedItem.getItem();
            if (selectedSeries != null) {
               Intent myIntent = new Intent(_view.getContext(), TabAlbumDetailsActivity.class);
               myIntent.putExtra("album_artists_string", selectedSeries.getAlbumArtistString());
               myIntent.putExtra("album_name", selectedSeries.getTitle());
               myIntent.putExtra("album_artists", selectedSeries.getAlbumArtists());
               myIntent.putExtra("album_cover", selectedSeries.getCoverPathLarge());
               myIntent.putExtra("activity_group", mActivityGroup);
               myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
               
               if (mArtist != null) {
                  View view = TabArtistsActivityGroup.getGroup().getLocalActivityManager()
                        .startActivity("album_details", myIntent).getDecorView();

                  TabArtistsActivityGroup.getGroup().replaceView(view);
               } else {
                  View view = TabAlbumsActivityGroup.getGroup().getLocalActivityManager()
                        .startActivity("album_details", myIntent).getDecorView();

                  TabAlbumsActivityGroup.getGroup().replaceView(view);
               }
            }
         }
      });

      mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> _item, View _view, final int _position,
               long _id) {
            try {
               final MusicAlbum selected = (MusicAlbum) ((ILoadingAdapterItem) _item
                     .getItemAtPosition(_position)).getItem();
               final String trackTitle = selected.getTitle();
               if (trackTitle != null) {
                  String dirName = DownloaderUtils.getMusicTrackPath(selected.getAlbumArtists()[0],
                        selected.getTitle());
                  final QuickAction qa = new QuickAction(_view);

                  final File localFileName = new File(DownloaderUtils.getBaseDirectory() + "/"
                        + dirName);

                  if (localFileName.exists()) {
                     ActionItem playItemAction = new ActionItem();

                     playItemAction.setTitle(getString(R.string.quickactions_playdevice));
                     playItemAction
                           .setIcon(getResources().getDrawable(R.drawable.quickaction_play));
                     playItemAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           // TODO: play complete album with music player
                           Util.showToast(_view.getContext(),
                                 getString(R.string.info_not_implemented));
                        }
                     });

                     qa.addActionItem(playItemAction);
                  } else {
                     ActionItem sdCardAction = new ActionItem();
                     sdCardAction.setTitle(getString(R.string.quickactions_downloadsd));
                     sdCardAction
                           .setIcon(getResources().getDrawable(R.drawable.quickaction_sdcard));
                     sdCardAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           DownloadAlbumTask task = new DownloadAlbumTask(_view.getContext());
                           task.execute(selected);

                           qa.dismiss();
                        }
                     });
                     qa.addActionItem(sdCardAction);
                  }
                  if (mService.isClientControlConnected()) {
                     ActionItem playOnClientAction = new ActionItem();

                     playOnClientAction.setTitle(getString(R.string.quickactions_playclient));
                     playOnClientAction.setIcon(getResources().getDrawable(
                           R.drawable.quickaction_play_device));
                     playOnClientAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           // TODO: Play complete album on client
                           Util.showToast(_view.getContext(),
                                 getString(R.string.info_not_implemented));
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

      mAdapter.setLoadingText(getString(R.string.music_albums_loadalbums));
      mAdapter.showLoadingItem(true);

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mArtist = extras.getString("artist");
         mActivityGroup = extras.getString("activity_group");
      }

      loadFurtherItems();
   }

   private void loadFurtherItems() {
      if (mMusicLoaderTask == null) {
         mMusicLoaderTask = new LoadMusicTask(this);
         mStatusBarHandler.setLoading(true);
         mMusicLoaderTask.execute(20);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu _menu) {
      super.onCreateOptionsMenu(_menu);

      SubMenu viewItem = _menu.addSubMenu(0, Menu.FIRST + 1, Menu.NONE,
            getString(R.string.media_views));

      MenuItem textSettingsItem = viewItem.add(0, Menu.FIRST + 1, Menu.NONE,
            getString(R.string.media_views_text));
      MenuItem thumbsSettingsItem = viewItem.add(0, Menu.FIRST + 3, Menu.NONE,
            getString(R.string.media_views_thumbs));

      textSettingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            mAdapter.setView(ViewTypes.TextView.ordinal());
            mAdapter.notifyDataSetInvalidated();
            return true;
         }
      });

      thumbsSettingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            mAdapter.setView(ViewTypes.ThumbView.ordinal());
            mAdapter.notifyDataSetInvalidated();
            return true;
         }
      });

      return true;
   }

   @Override
   public void onDestroy() {
      mAdapter.mImageLoader.stopThread();
      mListView.setAdapter(null);
      super.onDestroy();
   }

   @Override
   public void onBackPressed() {
      if (mActivityGroup.equals("artists")) {
         TabArtistsActivityGroup.getGroup().back();
      } else if(mActivityGroup.equals("albums")){
         TabAlbumsActivityGroup.getGroup().back();
      }
      return;
   }

   @Override
   public boolean onKeyDown(int _keyCode, KeyEvent _event) {
      if (_keyCode == KeyEvent.KEYCODE_BACK) {
         if (mActivityGroup.equals("artists")) {
            TabArtistsActivityGroup.getGroup().back();
         } else if(mActivityGroup.equals("albums")){
            TabAlbumsActivityGroup.getGroup().back();
         }
         return true;
      }
      return super.onKeyDown(_keyCode, _event);
   }
}
