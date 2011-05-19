package com.mediaportal.ampdroid.activities.media;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.downloadservice.DownloadJob;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderHelper;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderService;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ILoadingListener;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.lists.views.LoadingAdapterItem;
import com.mediaportal.ampdroid.lists.views.MoviePosterViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.MovieTextViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.MovieThumbViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.ViewTypes;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabVideosActivity extends Activity implements ILoadingListener {
   private ListView mListView;
   private LazyLoadingAdapter mAdapter;
   DataHandler mService;
   private LoadVideosTask mVideosLoaderTask;
   private int mVideosLoaded = 0;
   private BaseTabActivity mBaseActivity;
   private StatusBarActivityHandler mStatusBarHandler;

   private class LoadVideosTask extends AsyncTask<Integer, List<Movie>, Boolean> {
      private Context mContext;

      private LoadVideosTask(Context _context) {
         mContext = _context;
      }

      @SuppressWarnings("unchecked")
      @Override
      protected Boolean doInBackground(Integer... _params) {
         int loadItems = mVideosLoaded + _params[0];
         int videosCount = mService.getVideosCount();

         if (videosCount == -99) {
            publishProgress(null, null);
            return false;
         } else {
            while (mVideosLoaded < loadItems && mVideosLoaded < videosCount) {
               int end = mVideosLoaded + 4;
               if (end >= videosCount) {
                  end = videosCount - 1;
               }
               List<Movie> videos = mService.getVideos(mVideosLoaded, end);

               publishProgress(videos);
               if (videos == null) {
                  return false;
               } else {
                  mVideosLoaded += 5;
               }
            }
            if (mVideosLoaded < videosCount) {
               return false;// not yet finished;
            } else {
               return true;// finished
            }
         }
      }

      @Override
      protected void onProgressUpdate(List<Movie>... values) {
         if (values != null) {
            List<Movie> movies = values[0];
            if (movies != null) {
               for (Movie m : movies) {
                  mAdapter.addItem(ViewTypes.TextView.ordinal(), new MovieTextViewAdapterItem(m));
                  mAdapter.addItem(ViewTypes.PosterView.ordinal(),
                        new MoviePosterViewAdapterItem(m));
                  mAdapter.addItem(ViewTypes.ThumbView.ordinal(), new MovieThumbViewAdapterItem(m));
               }
            } else {
               mAdapter.setLoadingText(getString(R.string.info_loading_failed), false);
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
         mVideosLoaderTask = null;
      }
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tabmoviesactivity);

      mBaseActivity = (BaseTabActivity) getParent().getParent();

      mService = DataHandler.getCurrentRemoteInstance();

      if (mBaseActivity != null && mService != null) {
         mStatusBarHandler = new StatusBarActivityHandler(mBaseActivity, mService);
         mStatusBarHandler.setHome(false);
      }

      mAdapter = new LazyLoadingAdapter(this);
      mAdapter.addView(ViewTypes.TextView.ordinal());
      mAdapter.addView(ViewTypes.PosterView.ordinal());
      mAdapter.addView(ViewTypes.ThumbView.ordinal());
      mAdapter.setView(ViewTypes.PosterView.ordinal());

      mAdapter.setLoadingListener(this);

      mListView = (ListView) findViewById(R.id.ListViewVideos);
      mListView.setAdapter(mAdapter);

      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> a, View v, int position, long id) {
            ILoadingAdapterItem item = (ILoadingAdapterItem) mListView.getItemAtPosition(position);

            if (item.getClass().equals(LoadingAdapterItem.class)) {
               loadFurtherMovieItems();
            } else {
               Movie selectedMovie = (Movie) item.getItem();
               Intent myIntent = new Intent(v.getContext(), TabVideoDetailsActivity.class);
               myIntent.putExtra("video_id", selectedMovie.getId());
               myIntent.putExtra("video_name", selectedMovie.getName());

               myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

               // Create the view using FirstGroup's LocalActivityManager
               View view = TabVideosActivityGroup.getGroup().getLocalActivityManager()
                     .startActivity("video_details", myIntent).getDecorView();

               // Again, replace the view
               TabVideosActivityGroup.getGroup().replaceView(view);
            }
         }
      });

      mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> _item, View _view, final int _position,
               long _id) {
            try {
               Movie selected = (Movie) ((ILoadingAdapterItem) _item.getItemAtPosition(_position))
                     .getItem();
               // EpisodeDetails details = mService.getEpisode(mSeriesId,
               // selected.getId());
               final String movieFile = selected.getFilename();
               if (movieFile != null) {
                  String dirName = DownloaderUtils.getMoviePath(selected);
                  final String fileName = dirName + Utils.getFileNameWithExtension(movieFile, "\\");
                  final String displayName = selected.toString();
                  final QuickAction qa = new QuickAction(_view);

                  final File localFileName = new File(DownloaderUtils.getBaseDirectory() + "/"
                        + fileName);

                  if (localFileName.exists()) {
                     ActionItem playItemAction = new ActionItem();

                     playItemAction.setTitle(getString(R.string.quickactions_playdevice));
                     playItemAction
                           .setIcon(getResources().getDrawable(R.drawable.quickaction_play));
                     playItemAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           Intent playIntent = new Intent(Intent.ACTION_VIEW);
                           playIntent.setDataAndType(Uri.parse(localFileName.toString()), "video/*");
                           startActivity(playIntent);

                           qa.dismiss();
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
                           String url = mService.getDownloadUri(movieFile);
                           FileInfo info = mService.getFileInfo(movieFile);
                           ApiCredentials cred = mService.getDownloadCredentials();
                           if (url != null) {
                              DownloadJob job = new DownloadJob();
                              job.setUrl(url);
                              job.setFileName(fileName);
                              job.setDisplayName(displayName);
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

                     playOnClientAction.setTitle(getString(R.string.quickactions_playclient));
                     playOnClientAction.setIcon(getResources().getDrawable(
                           R.drawable.quickaction_play_device));
                     playOnClientAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           mService.playVideoFileOnClient(movieFile);

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

      loadFurtherMovieItems();
   }

   @Override
   public void EndOfListReached() {
      loadFurtherMovieItems();
   }

   private void loadFurtherMovieItems() {
      if (mVideosLoaderTask == null) {
         mAdapter.setLoadingText(getString(R.string.media_videos_loadvideos));
         mAdapter.showLoadingItem(true);

         mVideosLoaderTask = new LoadVideosTask(this);
         mStatusBarHandler.setLoading(true);
         mVideosLoaderTask.execute(20);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu _menu) {
      super.onCreateOptionsMenu(_menu);
      SubMenu viewItem = _menu.addSubMenu(0, Menu.FIRST + 1, Menu.NONE,
            getString(R.string.media_views));

      MenuItem textSettingsItem = viewItem.add(0, Menu.FIRST + 1, Menu.NONE,
            getString(R.string.media_views_text));
      MenuItem posterSettingsItem = viewItem.add(0, Menu.FIRST + 2, Menu.NONE,
            getString(R.string.media_views_poster));
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

      posterSettingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            mAdapter.setView(ViewTypes.PosterView.ordinal());
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
}
