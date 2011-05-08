package com.mediaportal.ampdroid.activities.media;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseTabActivity;
import com.mediaportal.ampdroid.activities.TvServerOverviewActivity;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.data.SeriesEpisode;
import com.mediaportal.ampdroid.data.SeriesFull;
import com.mediaportal.ampdroid.data.SeriesSeason;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderService;
import com.mediaportal.ampdroid.lists.ImageHandler;
import com.mediaportal.ampdroid.lists.LazyLoadingGalleryAdapter;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DateTimeHelper;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabSeriesDetailsActivity extends Activity {
   private LazyLoadingGalleryAdapter mAdapter;
   private LinearLayout mSeasonLayout;
   private ImageView mSeriesPoster;
   private TextView mSeriesName;
   private TextView mSeriesOverview;
   private TextView mSeriesReleaseDate;
   private TextView mSeriesRuntime;
   private TextView mSeriesCertification;
   private TextView mSeriesActors;
   private RatingBar mSeriesRating;

   private Gallery mPosterGallery;
   private int mSeriesId;
   private SeriesFull mSeries;
   private ImageHandler mImageHandler;
   private LoadSeriesDetailsTask mLoadSeriesTask;
   private LoadSeasonsDetailsTask mLoadSeasonTask;
   private DownloadSeasonTask mSeasonDownloaderTask;
   private DataHandler mService;
   private ProgressDialog mLoadingDialog;
   private BaseTabActivity mBaseActivity;

   private class DownloadSeasonTask extends AsyncTask<SeriesSeason, Intent, Boolean> {
      private Context mContext;

      private DownloadSeasonTask(Context _context) {
         mContext = _context;
      }

      @Override
      protected Boolean doInBackground(SeriesSeason... _params) {
         SeriesSeason season = _params[0];

         for (int i = 0; i < season.getEpisodesCount(); i++) {
            List<SeriesEpisode> episodes = mService.getEpisodesForSeason(mSeriesId,
                  season.getSeasonNumber(), i, i + 1);
            SeriesEpisode ep = episodes.get(0);
            String epFile = ep.getFileName();

            String url = mService.getDownloadUri(epFile);
            FileInfo info = mService.getFileInfo(epFile);
            String dirName = DownloaderUtils.getTvEpisodePath(mSeries.getPrettyName(), ep);
            final String fileName = dirName + Utils.getFileNameWithExtension(epFile, "\\");

            Intent download = new Intent(mContext, ItemDownloaderService.class);
            download.putExtra("url", url);
            download.putExtra("name", fileName);
            if (info != null) {
               download.putExtra("length", info.getLength());
            }

            publishProgress(download);
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

   private class LoadSeriesDetailsTask extends AsyncTask<Integer, List<Movie>, SeriesFull> {
      Activity mContext;

      private LoadSeriesDetailsTask(Activity _context) {
         mContext = _context;
      }

      @Override
      protected SeriesFull doInBackground(Integer... _params) {
         mSeries = mService.getFullSeries(mSeriesId);

         return mSeries;
      }

      @Override
      protected void onPostExecute(SeriesFull _result) {
         if (_result != null) {
            String seriesPoster = _result.getCurrentPosterUrl();
            if (seriesPoster != null && !seriesPoster.equals("")) {
               String fileName = Utils
                     .getFileNameWithExtension(_result.getCurrentPosterUrl(), "\\");
               String cacheName = "Series" + File.separator + mSeriesId + File.separator
                     + "LargePoster" + File.separator + fileName;

               LazyLoadingImage image = new LazyLoadingImage(seriesPoster, cacheName, 150, 200);
               mSeriesPoster.setTag(seriesPoster);
               mImageHandler.DisplayImage(image, R.drawable.listview_imageloading_poster, mContext,
                     mSeriesPoster);

            }

            Date firstAired = _result.getFirstAired();

            if (firstAired != null) {
               String date = DateTimeHelper.getDateString(firstAired, false);
               mSeriesReleaseDate.setText(date);
            }

            int runtime = _result.getRuntime();
            if (runtime != 0) {
               mSeriesRuntime.setText(String.valueOf(runtime));
            } else {
               mSeriesRuntime.setText("-");
            }

            String[] actors = _result.getActors();
            if (actors != null) {
               String actorsString = "| ";
               for (String a : actors) {
                  actorsString += a + " | ";
               }
               mSeriesActors.setText(actorsString);
            } else {
               mSeriesActors.setText("-");
            }

            int rating = (int) _result.getRating();
            mSeriesRating.setRating(rating);

            mSeriesOverview.setText(_result.getSummary());
            mLoadingDialog.cancel();
         } else {
            mLoadingDialog.cancel();
            Dialog diag = new Dialog(getParent());
            diag.setTitle(getString(R.string.media_series_loadingerror));
            diag.setCancelable(true);

            diag.show();
            diag.setOnDismissListener(new OnDismissListener() {
               @Override
               public void onDismiss(DialogInterface dialog) {
                  mContext.finish();

               }
            });
         }

         mLoadSeasonTask = new LoadSeasonsDetailsTask(mContext);
         mLoadSeasonTask.execute(mSeriesId);
      }
   }

   private class LoadSeasonsDetailsTask extends AsyncTask<Integer, List<Movie>, List<SeriesSeason>> {
      Activity mContext;

      private LoadSeasonsDetailsTask(Activity _context) {
         mContext = _context;
      }

      @Override
      protected List<SeriesSeason> doInBackground(Integer... _params) {
         List<SeriesSeason> seasons = mService.getAllSeasons(mSeriesId);

         return seasons;
      }

      @Override
      protected void onPostExecute(List<SeriesSeason> _result) {
         if (_result != null) {
            for (int i = 0; i < _result.size(); i++) {
               View view = Button.inflate(mContext, R.layout.listitem_poster, null);
               TextView text = (TextView) view.findViewById(R.id.TextViewTitle);
               ImageView image = (ImageView) view.findViewById(R.id.ImageViewEventImage);
               TextView subtext = (TextView) view.findViewById(R.id.TextViewText);

               SeriesSeason s = _result.get(i);
               String seasonBanner = s.getSeasonBanner();
               if (seasonBanner != null && !seasonBanner.equals("")) {
                  String fileName = Utils.getFileNameWithExtension(seasonBanner, "\\");
                  String cacheName = "Series" + File.separator + mSeriesId + File.separator
                        + "LargePoster" + File.separator + fileName;

                  LazyLoadingImage bannerImage = new LazyLoadingImage(seasonBanner, cacheName, 75,
                        100);
                  image.setTag(seasonBanner);
                  mImageHandler.DisplayImage(bannerImage, R.drawable.listview_imageloading_poster,
                        mContext, image);
               }

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

               view.setOnClickListener(new OnClickListener() {

                  @Override
                  public void onClick(View _view) {
                     Intent myIntent = new Intent(_view.getContext(), TabEpisodesActivity.class);
                     SeriesSeason s = (SeriesSeason) _view.getTag();
                     myIntent.putExtra("series_id", s.getSeriesId());
                     myIntent.putExtra("season_number", s.getSeasonNumber());
                     myIntent.putExtra("series_name", mSeries.getPrettyName());

                     myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                     // Create the view using FirstGroup's LocalActivityManager
                     View view = TabSeriesActivityGroup.getGroup().getLocalActivityManager()
                           .startActivity("season_episodes", myIntent).getDecorView();

                     // Again, replace the view
                     TabSeriesActivityGroup.getGroup().replaceView(view);
                  }
               });

               view.setOnLongClickListener(new OnLongClickListener() {
                  @Override
                  public boolean onLongClick(View _view) {
                     try {
                        final SeriesSeason s = (SeriesSeason) _view.getTag();

                        if (s != null) {
                           final QuickAction qa = new QuickAction(_view);

                           ActionItem sdCardAction = new ActionItem();
                           sdCardAction.setTitle(getString(R.string.quickactions_downloadsd));
                           sdCardAction.setIcon(getResources().getDrawable(
                                 R.drawable.quickaction_sdcard));
                           sdCardAction.setOnClickListener(new OnClickListener() {
                              @Override
                              public void onClick(final View _view) {
                                 AlertDialog.Builder builder = new AlertDialog.Builder(
                                       mBaseActivity);
                                 builder
                                       .setTitle(getString(R.string.media_series_loadmultiplewarning_title));
                                 builder
                                       .setMessage(getString(R.string.media_series_loadmultiplewarning_text_begin)
                                             + s.getEpisodesCount()
                                             + getString(R.string.media_series_loadmultiplewarning_text_end));
                                 builder.setCancelable(false);
                                 builder.setPositiveButton(getString(R.string.dialog_yes),
                                       new DialogInterface.OnClickListener() {
                                          public void onClick(DialogInterface dialog, int id) {
                                             mSeasonDownloaderTask = new DownloadSeasonTask(_view
                                                   .getContext());
                                             mSeasonDownloaderTask.execute(s);
                                          }
                                       });

                                 builder.setNegativeButton(getString(R.string.dialog_no),
                                       new DialogInterface.OnClickListener() {
                                          public void onClick(DialogInterface dialog, int id) {
                                             dialog.dismiss();
                                          }
                                       });
                                 AlertDialog alert = builder.create();
                                 alert.show();

                                 qa.dismiss();
                              }
                           });
                           qa.addActionItem(sdCardAction);

                           if (mService.isClientControlConnected()) {
                              ActionItem playOnClientAction = new ActionItem();

                              playOnClientAction.setTitle(getString(R.string.quickactions_playclient));
                              playOnClientAction.setIcon(getResources().getDrawable(
                                    R.drawable.quickaction_play_device));
                              playOnClientAction.setOnClickListener(new OnClickListener() {
                                 @Override
                                 public void onClick(View _view) {
                                    // TODO: Add all files to playlist and start
                                    // playback
                                    Util.showToast(_view.getContext(), getString(R.string.info_not_implemented));
                                    // mService.playFileOnClient(epFile);

                                    qa.dismiss();
                                 }
                              });
                              qa.addActionItem(playOnClientAction);
                           }

                           qa.setAnimStyle(QuickAction.ANIM_AUTO);

                           qa.show();
                        } else {
                           Util.showToast(_view.getContext(),
                                 getString(R.string.media_nofile));
                        }
                        return true;
                     } catch (Exception ex) {
                        return false;
                     }
                  }
               });

               text.setText(getString(R.string.media_series_season) + " " + s.getSeasonNumber());
               subtext.setText(s.getEpisodesCount() + " " + getString(R.string.media_episodes));
               view.setTag(s);

               mSeasonLayout.addView(view);
            }
         }
      }
   }

   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tabseriesdetailsactivity);
      mSeasonLayout = (LinearLayout) findViewById(R.id.LinearLayoutSeasons);
      mSeriesPoster = (ImageView) findViewById(R.id.ImageViewSeriesPoster);
      mSeriesName = (TextView) findViewById(R.id.TextViewSeriesName);
      mSeriesOverview = (TextView) findViewById(R.id.TextViewOverview);
      mPosterGallery = (Gallery) findViewById(R.id.GalleryAllMoviePosters);
      mSeriesReleaseDate = (TextView) findViewById(R.id.TextViewSeriesRelease);
      mSeriesRuntime = (TextView) findViewById(R.id.TextViewSeriesRuntime);
      mSeriesCertification = (TextView) findViewById(R.id.TextViewSeriesCertification);
      mSeriesActors = (TextView) findViewById(R.id.TextViewSeriesActors);
      mSeriesRating = (RatingBar) findViewById(R.id.RatingBarSeriesRating);
      mSeriesRating.setNumStars(10);

      mBaseActivity = (BaseTabActivity) getParent().getParent();

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mSeriesId = extras.getInt("series_id");
         String seriesName = extras.getString("series_name");
         mSeriesName.setText(seriesName);

         mService = DataHandler.getCurrentRemoteInstance();
         mImageHandler = new ImageHandler(this);

         mLoadSeriesTask = new LoadSeriesDetailsTask(this);
         mLoadSeriesTask.execute(mSeriesId);

         // mPosterGallery.setSpacing(-10);
         // mPosterGallery.setAdapter(mAdapter);

         mLoadingDialog = ProgressDialog.show(getParent(), getString(R.string.media_series_loadseriesdetails),
               getString(R.string.info_loading_title), true);
         mLoadingDialog.setCancelable(true);
      } else {// activity called without movie id (shouldn't happen ;))

      }
   }

   @Override
   public void onDestroy() {
      // adapter.imageLoader.stopThread();
      // m_listView.setAdapter(null);
      super.onDestroy();
   }
}
