package com.mediaportal.ampdroid.activities;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.SeriesEpisode;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.TvServerFeaturesAdapter;
import com.mediaportal.ampdroid.lists.views.EpisodePosterViewAdapterItem;

public class DownloadsActivity extends BaseActivity {
   private ListView mListView;
   private TvServerFeaturesAdapter mFeaturesAdapter;
   private DataHandler mService;
   private StatusBarActivityHandler statusBarHandler;
   private LazyLoadingAdapter mAdapter;

   private class LoadEpisodesTask extends AsyncTask<Integer, List<SeriesEpisode>, Boolean> {
      private Context mContext;

      private LoadEpisodesTask(Context _context) {
         mContext = _context;
      }

      @SuppressWarnings("unchecked")
      @Override
      protected Boolean doInBackground(Integer... _params) {
         /*
          * int seriesCount = mService.getEpisodesCountForSeason(mSeriesId,
          * mSeasonNumber);
          * 
          * int cursor = 0; while (cursor < seriesCount) { List<SeriesEpisode>
          * episodes = mService.getEpisodesForSeason(mSeriesId, mSeasonNumber,
          * cursor, cursor + 4); publishProgress(episodes);
          * 
          * cursor += 5; }
          */

         List<SeriesEpisode> episodes;// =
                                      // mService.getAllEpisodesForSeason(mSeriesId,
                                      // mSeasonNumber);
         // publishProgress(episodes);
         return true;
      }

      @Override
      protected void onProgressUpdate(List<SeriesEpisode>... values) {
         if (values != null) {
            List<SeriesEpisode> episodes = values[0];
            if (episodes != null) {
               for (SeriesEpisode e : episodes) {
                  // EpisodeDetails details = mService.getEpisode(e.getId());
                  //
               }
            }
         }
         mAdapter.notifyDataSetChanged();
         super.onProgressUpdate(values);
      }

      @Override
      protected void onPostExecute(Boolean _result) {
         mAdapter.showLoadingItem(false);
      }
   }

   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tvserveractivity);

      mService = DataHandler.getCurrentRemoteInstance();
      statusBarHandler = new StatusBarActivityHandler(this, mService);
      statusBarHandler.setHome(false);

      mListView = (ListView) findViewById(R.id.ListViewItems);
   }
}
