package com.mediaportal.ampdroid.activities.media;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.api.ItemDownloaderService;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.SeriesEpisode;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.lists.views.EpisodePosterViewAdapterItem;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabEpisodesActivity extends Activity {
   private ListView mListView;
   private LazyLoadingAdapter mAdapter;
   MediaPlayer mMediaPlayer;
   DataHandler mService;

   private String mSeriesName;
   private int mSeriesId;
   private int mSeasonNumber;

   private LoadEpisodesTask mEpisodesLoaderTask;
   private TextView mTextViewSeriesName;
   private TextView mTextViewSeason;

   private class LoadEpisodesTask extends AsyncTask<Integer, List<SeriesEpisode>, Boolean> {
      private Context mContext;

      private LoadEpisodesTask(Context _context){
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

         List<SeriesEpisode> episodes = mService.getAllEpisodesForSeason(mSeriesId, mSeasonNumber);
         publishProgress(episodes);
         return true;
      }

      @Override
      protected void onProgressUpdate(List<SeriesEpisode>... values) {
         if (values != null) {
            List<SeriesEpisode> episodes = values[0];
            if (episodes != null) {
               for (SeriesEpisode e : episodes) {
                  // EpisodeDetails details = mService.getEpisode(e.getId());
                  mAdapter.addItem(new EpisodePosterViewAdapterItem(mContext, mSeriesId, e));
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
      setContentView(R.layout.tabepisodesactivity);
      mListView = (ListView) findViewById(R.id.ListView);
      mTextViewSeriesName = (TextView) findViewById(R.id.TextViewSeriesName);
      mTextViewSeason = (TextView) findViewById(R.id.TextViewSeason);
      mMediaPlayer = new MediaPlayer();

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mSeriesId = extras.getInt("series_id");
         mSeriesName = extras.getString("series_name");
         mSeasonNumber = extras.getInt("season_number");

         mTextViewSeriesName.setText(mSeriesName);
         mTextViewSeason.setText(getString(R.string.media_series_season) + " " + mSeasonNumber);
         mService = DataHandler.getCurrentRemoteInstance();

         mAdapter = new LazyLoadingAdapter(this);
         mListView.setAdapter(mAdapter);

         refreshEpisodes();

      } else {// activity called without bundle infos (shouldn't happen ;))

      }

      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> _item, View _view, int _position, long _id) {
            Object obj = _item.getItemAtPosition(_position);
            ILoadingAdapterItem selectedItem = (ILoadingAdapterItem) obj;
            SeriesEpisode selectedEp = (SeriesEpisode) selectedItem.getItem();

            Intent myIntent = new Intent(_view.getContext(), TabEpisodeDetailsActivity.class);
            myIntent.putExtra("series_id", mSeriesId);
            myIntent.putExtra("series_name", mSeriesName);
            myIntent.putExtra("episode_id", selectedEp.getId());
            myIntent.putExtra("episode_name", selectedEp.getName());
            myIntent.putExtra("episode_season", selectedEp.getSeasonNumber());
            myIntent.putExtra("episode_nr", selectedEp.getEpisodeNumber());
            myIntent.putExtra("episode_banner", selectedEp.getBannerUrl());

            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Create the view using FirstGroup's LocalActivityManager
            View view = TabSeriesActivityGroup.getGroup().getLocalActivityManager()
                  .startActivity("episode_details", myIntent).getDecorView();

            // Again, replace the view
            TabSeriesActivityGroup.getGroup().replaceView(view);
         }
      });

      mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> _item, View _view, final int _position,
               long _id) {
            try {
               SeriesEpisode selected = (SeriesEpisode) ((ILoadingAdapterItem) _item
                     .getItemAtPosition(_position)).getItem();
               // EpisodeDetails details = mService.getEpisode(mSeriesId,
               // selected.getId());
               final String epFile = selected.getFileName();
               if (epFile != null) {
                  String dirName = DownloaderUtils.getTvEpisodePath(mSeriesName, selected);
                  final String fileName = dirName + Utils.getFileNameWithExtension(epFile, "\\");

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
                           String url = mService.getDownloadUri(epFile);
                           FileInfo info = mService.getFileInfo(epFile);
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
                           mService.playFileOnClient(epFile);
                           
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
   }

   private void refreshEpisodes() {
      mAdapter.setLoadingText(getString(R.string.media_episodes_loading));
      mAdapter.showLoadingItem(true);
      mEpisodesLoaderTask = new LoadEpisodesTask(this);
      mEpisodesLoaderTask.execute(0);

   }

   @Override
   public void onDestroy() {
      mAdapter.mImageLoader.stopThread();
      mListView.setAdapter(null);
      super.onDestroy();
   }

   @Override
   public void onBackPressed() {
      TabSeriesActivityGroup.getGroup().back();
      return;
   }

   @Override
   public boolean onKeyDown(int _keyCode, KeyEvent _event) {
      if (_keyCode == KeyEvent.KEYCODE_BACK) {
         TabSeriesActivityGroup.getGroup().back();
         return true;
      }
      return super.onKeyDown(_keyCode, _event);
   }
}
