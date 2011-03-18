package com.mediaportal.ampdroid.activities.tvserver;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.TvChannel;
import com.mediaportal.ampdroid.data.TvRecording;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.views.TvServerRecordingsThumbsViewItem;

public class TvServerRecordingsActivity extends BaseActivity {
   private DataHandler mService;
   private ListView mListView;
   private LazyLoadingAdapter mAdapter;
   private UpdateRecordingsTask mRecordingsUpdater;
   private StatusBarActivityHandler mStatusBarHandler;

   private class UpdateRecordingsTask extends AsyncTask<Integer, Integer, List<TvRecording>> {
      private HashMap<Integer, TvChannel> mChannels;
      private Context mContext;
      private UpdateRecordingsTask(Context _context){
         mContext = _context;
      }
      @Override
      protected List<TvRecording> doInBackground(Integer... _params) {
         List<TvRecording> recordings = mService.getTvRecordings();
         if (recordings != null) {
            mChannels = new HashMap<Integer, TvChannel>();
            for (TvRecording s : recordings) {
               if (!mChannels.containsKey(s.getIdChannel())) {
                  TvChannel channel = mService.getTvChannel(s.getIdChannel());
                  if(channel != null){
                     mChannels.put(s.getIdChannel(), channel);
                  }
               }
            }
         }
         return recordings;
      }

      @Override
      protected void onPostExecute(List<TvRecording> _result) {
         if (_result != null) {
            for (TvRecording r : _result) {
               TvChannel channel = mChannels.get(r.getIdChannel());
               mAdapter.addItem(new TvServerRecordingsThumbsViewItem(mContext, r, channel));
            }

            mListView.setAdapter(mAdapter);
         }
         mAdapter.showLoadingItem(false);
      }
   }

   @Override
   public void onCreate(Bundle _savedInstanceState) {
      setTitle(R.string.title_tvserver_recordings);
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tvserverrecordingsactivity);
      mListView = (ListView) findViewById(R.id.ListViewRecordings);
      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mAdapter.showLoadingItem(false);
            mAdapter.notifyDataSetChanged();

         }
      });
      mAdapter = new LazyLoadingAdapter(this);
      mListView.setAdapter(mAdapter);
      mService = DataHandler.getCurrentRemoteInstance();
      mStatusBarHandler = new StatusBarActivityHandler(this, mService);
      mStatusBarHandler.setHome(false);

      refreshRecordings();
   }

   private void refreshRecordings() {
      mAdapter.showLoadingItem(true);
      mAdapter.setLoadingText(getString(R.string.tvserver_loadrecordings));
      mRecordingsUpdater = new UpdateRecordingsTask(this);
      mRecordingsUpdater.execute(0);
   }
}
