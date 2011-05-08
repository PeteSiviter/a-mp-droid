package com.mediaportal.ampdroid.activities.media;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.Movie;
import com.mediaportal.ampdroid.data.MovieFull;
import com.mediaportal.ampdroid.lists.ImageHandler;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.Utils;

public class TabMovieDetailsActivity extends Activity {
   private DataHandler mService;
   MovieFull mMovie;
   int mMovieId;

   private ImageHandler mImageHandler;
   private LoadVideoDetailsTask mLoadMovieTask;
   private ProgressDialog mLoadingDialog;
   private ImageView mImageViewMoviePoster;
   private TextView mTextViewMovieName;
   private TextView mTextViewMovieReleaseDate;
   private RatingBar mRatingBarMovieRating;
   private TextView mTextViewMovieOverview;
   private TextView mTextViewMovieRuntime;
   private TextView mTextViewMovieActors;
   private String mMovieName;

   private class LoadVideoDetailsTask extends AsyncTask<Integer, List<Movie>, MovieFull> {
      Activity mContext;

      private LoadVideoDetailsTask(Activity _context) {
         mContext = _context;
      }

      @Override
      protected MovieFull doInBackground(Integer... _params) {
         mMovie = mService.getMovieDetails(mMovieId);

         return mMovie;
      }

      @Override
      protected void onPostExecute(MovieFull _result) {
         if (_result != null) {
            String seriesPoster = _result.getCoverThumbPath();
            if (seriesPoster != null && !seriesPoster.equals("")) {
               String fileName = Utils.getFileNameWithExtension(_result.getCoverThumbPath(), "\\");
               String cacheName = "Movies" + File.separator + mMovieId + File.separator
                     + "LargePoster" + File.separator + fileName;

               LazyLoadingImage image = new LazyLoadingImage(seriesPoster, cacheName, 200, 400);
               mImageViewMoviePoster.setTag(seriesPoster);
               mImageHandler.DisplayImage(image, R.drawable.listview_imageloading_poster, mContext,
                     mImageViewMoviePoster);

            }

            mTextViewMovieReleaseDate.setText(String.valueOf(_result.getYear()));

            int runtime = _result.getRuntime();
            if (runtime != 0) {
               mTextViewMovieRuntime.setText(String.valueOf(runtime));
            } else {
               mTextViewMovieRuntime.setText("-");
            }

            String actorsString = _result.getActorsString();
            if (actorsString != null) {
               mTextViewMovieActors.setText(actorsString);
            } else {
               mTextViewMovieActors.setText("-");
            }

            int rating = (int) _result.getScore();
            mRatingBarMovieRating.setRating(rating);

            mTextViewMovieOverview.setText(_result.getSummary());
            mLoadingDialog.cancel();
         } else {
            mLoadingDialog.cancel();
            Dialog diag = new Dialog(getParent());
            diag.setTitle(getString(R.string.media_movie_loadingerror));
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

   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tabmoviedetailsactivity);

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         mMovieId = extras.getInt("movie_id");
         mMovieName = extras.getString("movie_name");

         mService = DataHandler.getCurrentRemoteInstance();

         mTextViewMovieName = (TextView) findViewById(R.id.TextViewMovieName);
         mTextViewMovieName.setText(mMovieName);

         mImageViewMoviePoster = (ImageView) findViewById(R.id.ImageViewMoviePoster);
         mTextViewMovieReleaseDate = (TextView) findViewById(R.id.TextViewMovieRelease);
         mRatingBarMovieRating = (RatingBar) findViewById(R.id.RatingBarMovieRating);
         mTextViewMovieOverview = (TextView) findViewById(R.id.TextViewMovieOverview);
         mTextViewMovieRuntime = (TextView) findViewById(R.id.TextViewMovieRuntime);
         mTextViewMovieActors = (TextView) findViewById(R.id.TextViewMovieActors);

         mImageViewMoviePoster.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               mService.playVideoFileOnClient(mMovie.getFilename());
            }
         });

         mImageHandler = new ImageHandler(this);

         mLoadMovieTask = new LoadVideoDetailsTask(this);
         mLoadMovieTask.execute(mMovieId);

         // mPosterGallery.setSpacing(-10);
         // mPosterGallery.setAdapter(mAdapter);

         mLoadingDialog = ProgressDialog.show(getParent(), getString(R.string.media_movie_loadmoviedetails),
               getString(R.string.info_loading_title), true);
         mLoadingDialog.setCancelable(true);
      } else {// activity called without movie id (shouldn't happen ;))

      }
   }
}
