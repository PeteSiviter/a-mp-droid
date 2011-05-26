package com.mediaportal.ampdroid.activities.music;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.MusicTrack;
import com.mediaportal.ampdroid.data.VideoShare;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ILoadingListener;
import com.mediaportal.ampdroid.lists.views.MediaListType;
import com.mediaportal.ampdroid.lists.views.ShareTextViewAdapterItem;
import com.mediaportal.ampdroid.lists.views.ViewTypes;
import com.mediaportal.ampdroid.settings.PreferencesManager;

public class TabMusicSharesActivity extends Activity implements ILoadingListener {
   private ListView mListView;
   private LazyLoadingAdapter mAdapter;
   DataHandler mService;
   private LoadMusicTask mMusicLoaderTask;
   private BaseTabActivity mBaseActivity;
   private StatusBarActivityHandler mStatusBarHandler;
   private String mActivityGroup;

   private class LoadMusicTask extends AsyncTask<Integer, List<MusicTrack>, List<VideoShare>> {
      private Context mContext;

      private LoadMusicTask(Context _context) {
         mContext = _context;
      }

      @Override
      protected List<VideoShare> doInBackground(Integer... _params) {
         List<VideoShare> shares = mService.getAllMusicShares();

         return shares;
      }

      @Override
      protected void onPostExecute(List<VideoShare> _result) {
         if (_result != null) {
            for (VideoShare s : _result) {
               mAdapter.addItem(ViewTypes.TextView.ordinal(), new ShareTextViewAdapterItem(s));
            }
         }
         mAdapter.showLoadingItem(false);
         mAdapter.notifyDataSetChanged();
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
      mAdapter.setView(PreferencesManager.getDefaultView(MediaListType.MusicShares));
      mAdapter.setLoadingListener(this);

      mListView = (ListView) findViewById(R.id.ListViewVideos);
      mListView.setAdapter(mAdapter);

      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> _adapter, View _view, int _position, long _id) {
            ILoadingAdapterItem selectedItem = (ILoadingAdapterItem) mListView
                  .getItemAtPosition(_position);
            VideoShare selectedFileInfo = (VideoShare) selectedItem.getItem();
            if (selectedFileInfo != null) {
               Intent myIntent = new Intent(_view.getContext(), TabMusicDirectoryActivity.class);
               myIntent.putExtra("directory", selectedFileInfo.getPath());
               myIntent.putExtra("extensions", selectedFileInfo.getExtensions());
               myIntent.putExtra("activity_group", mActivityGroup);
               myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

               View view = TabMusicSharesActivityGroup.getGroup().getLocalActivityManager()
                     .startActivity("album_details", myIntent).getDecorView();

               TabMusicSharesActivityGroup.getGroup().replaceView(view);
            }
         }
      });

      mAdapter.setLoadingText(getString(R.string.media_shares_loadshares));
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

      SubMenu viewItem = _menu.addSubMenu(0, Menu.FIRST + 1, Menu.NONE,
            getString(R.string.media_views));

      MenuItem textSettingsItem = viewItem.add(0, Menu.FIRST + 1, Menu.NONE,
            getString(R.string.media_views_text));
      // MenuItem thumbsSettingsItem = viewItem.add(0, Menu.FIRST + 3,
      // Menu.NONE,
      // getString(R.string.media_views_thumbs));

      textSettingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            mAdapter.setView(ViewTypes.TextView);
            mAdapter.notifyDataSetInvalidated();
            return true;
         }
      });

//      thumbsSettingsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
//         @Override
//         public boolean onMenuItemClick(MenuItem item) {
//            mAdapter.setView(ViewTypes.ThumbView.ordinal());
//            mAdapter.notifyDataSetInvalidated();
//            return true;
//         }
//      });

      return true;
   }
}
