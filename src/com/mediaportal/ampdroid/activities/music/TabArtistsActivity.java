package com.mediaportal.ampdroid.activities.music;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseTabActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.activities.media.TabSeriesDetailsActivity;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.MusicArtist;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ILoadingListener;
import com.mediaportal.ampdroid.lists.views.MusicArtistTextViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.MusicArtistThumbViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.ViewTypes;
import com.mediaportal.ampdroid.utils.Util;

public class TabArtistsActivity  extends Activity implements ILoadingListener {
   private ListView mListView;
   private LazyLoadingAdapter mAdapter;
   DataHandler mService;
   private LoadMusicTask mMusicLoaderTask;
   private int mItemsLoaded = 0;
   private BaseTabActivity mBaseActivity;
   private StatusBarActivityHandler mStatusBarHandler;
   private String mActivityGroup;

   private class LoadMusicTask extends AsyncTask<Integer, List<MusicArtist>, Boolean> {
      private Context mContext;
      private LoadMusicTask(Context _context){
         mContext = _context;
      }
      
      @SuppressWarnings("unchecked")
      @Override
      protected Boolean doInBackground(Integer... _params) {
         int albumsCount = mService.getArtistsCount();
         int loadItems = mItemsLoaded + _params[0];

         while (mItemsLoaded < loadItems && mItemsLoaded <= albumsCount) {
            int end = mItemsLoaded + 4;
            if(end >= albumsCount){
               end = albumsCount - 1;
            }
            List<MusicArtist> series = mService.getArtists(mItemsLoaded, end);
            
            publishProgress(series);
            if (series == null) {
               return false;
            }
            
            mItemsLoaded += 5;
         }

         if (mItemsLoaded < albumsCount) {
            return false;// not yet finished;
         } else {
            return true;// finished
         }
      }

      @Override
      protected void onProgressUpdate(List<MusicArtist>... values) {
         if (values != null) {
            List<MusicArtist> series = values[0];
            if (series != null) {
               for (MusicArtist s : series) {
                  mAdapter.addItem(ViewTypes.TextView.ordinal(), new MusicArtistTextViewAdapterItem(s));
                  mAdapter
                        .addItem(ViewTypes.ThumbView.ordinal(), new MusicArtistThumbViewAdapterItem(s));
               }
            } else {
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
      mAdapter.setView(ViewTypes.TextView.ordinal());
      mAdapter.setLoadingListener(this);

      mListView = (ListView) findViewById(R.id.ListViewVideos);
      mListView.setAdapter(mAdapter);

      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> _adapter, View _view, int _position, long _id) {
            ILoadingAdapterItem selectedItem = (ILoadingAdapterItem) mListView
                  .getItemAtPosition(_position);
            MusicArtist selectedArtist = (MusicArtist) selectedItem.getItem();
            if (selectedArtist != null) {
               Intent myIntent = new Intent(_view.getContext(), TabAlbumsActivity.class);
               myIntent.putExtra("artist", selectedArtist.getTitle());
               myIntent.putExtra("activity_group", mActivityGroup);
               myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

               // Create the view using FirstGroup's LocalActivityManager
               View view = TabArtistsActivityGroup.getGroup().getLocalActivityManager()
                     .startActivity("album_artist", myIntent).getDecorView();

               // Again, replace the view
               TabArtistsActivityGroup.getGroup().replaceView(view);
            }
         }
      });

      mAdapter.setLoadingText(getString(R.string.music_artists_loadartists));
      mAdapter.showLoadingItem(true);

      loadFurtherItems();
      
      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mActivityGroup = extras.getString("activity_group");
      }
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

      SubMenu viewItem = _menu.addSubMenu(0, Menu.FIRST + 1, Menu.NONE, getString(R.string.media_views));

      MenuItem textSettingsItem = viewItem.add(0, Menu.FIRST + 1, Menu.NONE, getString(R.string.media_views_text));
      MenuItem thumbsSettingsItem = viewItem.add(0, Menu.FIRST + 3, Menu.NONE, getString(R.string.media_views_thumbs));
      
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
