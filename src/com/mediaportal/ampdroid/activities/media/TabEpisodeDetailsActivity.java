package com.mediaportal.ampdroid.activities.media;

import java.io.File;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseTabActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.api.ApiCredentials;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.SeriesEpisodeDetails;
import com.mediaportal.ampdroid.data.SeriesEpisodeFile;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.downloadservice.DownloadItemType;
import com.mediaportal.ampdroid.downloadservice.DownloadJob;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderHelper;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderService;
import com.mediaportal.ampdroid.downloadservice.MediaItemType;
import com.mediaportal.ampdroid.lists.ImageHandler;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DateTimeHelper;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabEpisodeDetailsActivity extends Activity {
   private BaseTabActivity mBaseActivity;
   private int mSeriesId;
   private String mSeriesName;
   private int mEpisodeId;
   private String mEpisodeBannerUrl;
   private ProgressDialog mLoadingDialog;
   private LoadEpisodeDetailsTask mLoadSeriesTask;
   private SeriesEpisodeDetails mEpisodeDetails;
   private DataHandler mService;
   private ImageHandler mImageHandler;
   private ImageView mImageViewEpisodeImage;
   private TextView mTextViewFirstAiredDate;
   private TextView mTextViewEpisodeActors;
   private RatingBar mRatingBarEpisodeRating;
   private TextView mTextViewEpisodeOverview;
   private TextView mTextViewEpisodename;
   private String mEpisodeName;
   private int mEpisodeSeasonNr;
   private int mEpisodeNr;
   private TextView mTextViewRuntime;
   private Button mButtonPlayPc;
   private Button mButtonPlayMobile;
   private Button mButtonDownload;

   private class LoadEpisodeDetailsTask extends AsyncTask<Integer, List<Movie>, SeriesEpisodeDetails> {
      Activity mContext;

      private LoadEpisodeDetailsTask(Activity _context) {
         mContext = _context;
      }

      @Override
      protected SeriesEpisodeDetails doInBackground(Integer... _params) {
         mEpisodeDetails = mService.getEpisode(_params[0], _params[1]);

         return mEpisodeDetails;
      }

      @Override
      protected void onPostExecute(SeriesEpisodeDetails _result) {
         if (_result != null) {
            String seriesPoster = _result.getBannerUrl();
            if (seriesPoster != null && !seriesPoster.equals("")) {
               String ext = Utils.getExtension(_result.getBannerUrl());

               int width = 400;
               int height = 300;
               String cacheName = "Series" + File.separator + mSeriesId + File.separator
                     + "Season." + _result.getSeasonNumber() + File.separator + "Ep"
                     + _result.getEpisodeNumber() + "_" + width + "x" + height + "." + ext;

               LazyLoadingImage image = new LazyLoadingImage(seriesPoster, cacheName, 500, 300);
               mImageViewEpisodeImage.setTag(seriesPoster);
               mImageHandler.DisplayImage(image, R.drawable.listview_imageloading_thumb, mContext,
                     mImageViewEpisodeImage);

            }

            Date firstAired = _result.getFirstAired();
            if (firstAired != null) {
               String date = DateTimeHelper.getDateString(firstAired, false);
               mTextViewFirstAiredDate.setText(date);
            }

            String[] actors = _result.getGuestStarsString().split("\\|");
            if (actors != null) {
               String actorsString = "";
               for (String a : actors) {
                  if (a != null && !a.equals("")) {
                     actorsString += " - " + a + "\n";
                  }
               }
               mTextViewEpisodeActors.setText(actorsString);
            } else {
               mTextViewEpisodeActors.setText("-");
            }

            int rating = (int) _result.getRating();
            mRatingBarEpisodeRating.setRating(rating);

            SeriesEpisodeFile epFile = _result.getEpisodeFile();
            if (epFile != null) {
               mTextViewRuntime.setText(String.valueOf((int) (epFile.getDuration() / 60000))
                     + getString(R.string.media_episodes_minutes));
            }

            mTextViewEpisodeOverview.setText(_result.getSummary());

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
      }
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tabepisodedetailsactivity);

      mTextViewEpisodeActors = (TextView) findViewById(R.id.TextViewEpisodeActors);
      mRatingBarEpisodeRating = (RatingBar) findViewById(R.id.RatingBarEpisodeRating);
      mTextViewFirstAiredDate = (TextView) findViewById(R.id.TextViewEpisodeFirstAired);
      mTextViewRuntime = (TextView) findViewById(R.id.TextViewEpisodeRuntime);
      mImageViewEpisodeImage = (ImageView) findViewById(R.id.ImageViewEpisodeBanner);
      mTextViewEpisodeOverview = (TextView) findViewById(R.id.TextViewEpisodeOverview);
      mTextViewEpisodename = (TextView) findViewById(R.id.TextViewEpisodeName);

      mButtonPlayPc = (Button) findViewById(R.id.ButtonPlayPc);
      mButtonPlayMobile = (Button) findViewById(R.id.ButtonPlayMobile);
      mButtonDownload = (Button) findViewById(R.id.ButtonDownload);

      mService = DataHandler.getCurrentRemoteInstance();
      mImageHandler = new ImageHandler(this);
      // mStatusBarHandler = new StatusBarActivityHandler(mBaseActivity,
      // mService);
      // mStatusBarHandler.setHome(false);

      mBaseActivity = (BaseTabActivity) getParent().getParent();

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mSeriesId = extras.getInt("series_id");
         mSeriesName = extras.getString("series_name");
         mEpisodeId = extras.getInt("episode_id");
         mEpisodeBannerUrl = extras.getString("episode_banner");
         mEpisodeName = extras.getString("episode_name");
         mEpisodeSeasonNr = extras.getInt("episode_season");
         mEpisodeNr = extras.getInt("episode_nr");

         mTextViewEpisodename.setText(mEpisodeName + " (" + mEpisodeSeasonNr + "x" + mEpisodeNr
               + ")");

         mLoadSeriesTask = new LoadEpisodeDetailsTask(this);
         mLoadSeriesTask.execute(mSeriesId, mEpisodeId);

         mLoadingDialog = ProgressDialog.show(getParent(),
               getString(R.string.media_series_loadepisodedetails),
               getString(R.string.info_loading_title), true);
         mLoadingDialog.setCancelable(true);
      }

      mButtonPlayPc.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (mService.isClientControlConnected()) {
               String epFile = mEpisodeDetails.getEpisodeFile().getFileName();
               mService.playVideoFileOnClient(epFile);
            } else {
               Util.showToast(v.getContext(), getString(R.string.info_remote_notconnected));
            }
         }
      });

      mButtonPlayMobile.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            String epFile = mEpisodeDetails.getEpisodeFile().getFileName();
            String dirName = DownloaderUtils.getTvEpisodePath(mSeriesName, mEpisodeDetails);
            String fileName = dirName + Utils.getFileNameWithExtension(epFile, "\\");
            File localFileName = new File(DownloaderUtils.getBaseDirectory() + "/" + fileName);
            if (localFileName.exists()) {
               Intent playIntent = new Intent(Intent.ACTION_VIEW);
               playIntent.setDataAndType(Uri.parse(localFileName.toString()), "video/*");
               startActivity(playIntent);
            } else {
               Util.showToast(v.getContext(), getString(R.string.info_file_not_found));
            }
         }
      });

      mButtonDownload.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View _view) {
            SeriesEpisodeFile epFile = mEpisodeDetails.getEpisodeFile();
            if (epFile != null) {
               String epFileName = epFile.getFileName();
               if (epFileName != null) {
                  String dirName = DownloaderUtils.getTvEpisodePath(mSeriesName, mEpisodeDetails);
                  String fileName = dirName + Utils.getFileNameWithExtension(epFileName, "\\");
                  File localFileName = new File(DownloaderUtils.getBaseDirectory() + "/" + fileName);
                  if (!localFileName.exists()) {
                     String url = mService.getDownloadUri(String.valueOf(mEpisodeDetails.getId()),
                           DownloadItemType.TvSeriesItem);
                     FileInfo info = mService.getFileInfo(epFileName);
                     ApiCredentials cred = mService.getDownloadCredentials();
                     if (url != null) {
                        DownloadJob job = new DownloadJob();
                        job.setUrl(url);
                        job.setFileName(fileName);
                        job.setDisplayName(epFile.toString());
                        job.setMediaType(MediaItemType.Video);
                        if (info != null) {
                           info.setLength(info.getLength());
                        }
                        if (cred.useAut()) {
                           job.setAuth(cred.getUsername(), cred.getPassword());
                        }

                        Intent download = ItemDownloaderHelper.createDownloadIntent(
                              _view.getContext(), job);
                        startService(download);
                     }
                  }
               } else {
                  Util.showToast(_view.getContext(),
                        getString(R.string.info_file_already_downloaded));
               }
            }
         }
      });
   }
}
