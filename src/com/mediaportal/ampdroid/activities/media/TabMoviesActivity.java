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
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderService;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ILoadingListener;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.lists.views.MoviePosterViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.MovieTextViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.MovieThumbViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.ViewTypes;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabMoviesActivity extends Activity implements ILoadingListener {
   private ListView mListView;
   private LazyLoadingAdapter mAdapter;
   DataHandler mService;
   private LoadMoviesTask mMoviesLoaderTask;
   private int mMoviesLoaded = 0;
   private BaseTabActivity mBaseActivity;
   private StatusBarActivityHandler mStatusBarHandler;

   private class LoadMoviesTask extends AsyncTask<Integer, List<Movie>, Boolean> {
      private Context mContext;
      private LoadMoviesTask(Context _context){
         mContext = _context;
      }
      @SuppressWarnings("unchecked")
      @Override
      protected Boolean doInBackground(Integer... _params) {
         int loadItems = mMoviesLoaded + _params[0];
         int moviesCount = mService.getMovieCount();

         while (mMoviesLoaded < loadItems) {
            int end = mMoviesLoaded + 4;
            if(end >= moviesCount){
               end = moviesCount - 1;
            }
            List<Movie> series = mService.getMovies(mMoviesLoaded, end);
            publishProgress(series);
            if (series == null){
               return false;
            }
            mMoviesLoaded += 5;
         }
         
         if (mMoviesLoaded < moviesCount) {
            return false;// not yet finished;
         } else {
            return true;// finished
         }
      }

      @Override
      protected void onProgressUpdate(List<Movie>... values) {
         if (values != null) {
            List<Movie> movies = values[0];
            if (movies != null) {
               for (Movie m : movies) {
                  mAdapter.addItem(ViewTypes.TextView.ordinal(),
                        new MovieTextViewAdapterItem(m));
                  mAdapter.addItem(ViewTypes.PosterView.ordinal(),
                        new MoviePosterViewAdapterItem(m));
                  mAdapter.addItem(ViewTypes.ThumbView.ordinal(), new MovieThumbViewAdapterItem(m));
               }
            }
            else{
               mAdapter.showLoadingItem(false);
               //mAdapter.setLoadingText("Loading failed, check your connection");
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
         mMoviesLoaderTask = null;
      }
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tabmoviesactivity);

      mBaseActivity = (BaseTabActivity) getParent().getParent();

      mService = DataHandler.getCurrentRemoteInstance();
      mStatusBarHandler = new StatusBarActivityHandler(mBaseActivity, mService);
      mStatusBarHandler.setHome(false);

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
            Movie selectedMovie = (Movie) ((ILoadingAdapterItem) mListView
                  .getItemAtPosition(position)).getItem();

            Intent myIntent = new Intent(v.getContext(), TabMovieDetailsActivity.class);
            myIntent.putExtra("movie_id", selectedMovie.getId());
            myIntent.putExtra("movie_name", selectedMovie.getName());

            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Create the view using FirstGroup's LocalActivityManager
            View view = TabMoviesActivityGroup.getGroup().getLocalActivityManager()
                  .startActivity("movie_details", myIntent).getDecorView();

            // Again, replace the view
            TabMoviesActivityGroup.getGroup().replaceView(view);

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
                           if (url != null) {
                              Intent download = new Intent(_view.getContext(),
                                    ItemDownloaderService.class);
                              download.putExtra("url", url);
                              download.putExtra("name", fileName);
                              if (info != null) {
                                 download.putExtra("length", info.getLength());
                              }
                              startService(download);
                              
                              qa.dismiss();
                           }
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

      mAdapter.setLoadingText(getString(R.string.media_movies_loading));
      mAdapter.showLoadingItem(true);
      loadFurtherMovieItems();
   }

   @Override
   public void EndOfListReached() {
      loadFurtherMovieItems();
   }

   private void loadFurtherMovieItems() {
      if (mMoviesLoaderTask == null) {
         mMoviesLoaderTask = new LoadMoviesTask(this);
         mStatusBarHandler.setLoading(true);
         mMoviesLoaderTask.execute(20);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu _menu) {
      super.onCreateOptionsMenu(_menu);
      SubMenu viewItem = _menu.addSubMenu(0, Menu.FIRST + 1, Menu.NONE, getString(R.string.media_views));

      MenuItem textSettingsItem = viewItem.add(0, Menu.FIRST + 1, Menu.NONE, getString(R.string.media_views_text));
      MenuItem posterSettingsItem = viewItem.add(0, Menu.FIRST + 2, Menu.NONE, getString(R.string.media_views_poster));
      MenuItem thumbsSettingsItem = viewItem.add(0, Menu.FIRST + 3, Menu.NONE, getString(R.string.media_views_thumbs));

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
