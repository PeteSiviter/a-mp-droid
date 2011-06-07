package com.mediaportal.ampdroid.downloadservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.DownloadsActivity;
import com.mediaportal.ampdroid.database.DownloadsDatabaseHandler;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.utils.Constants;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class ItemDownloaderService extends Service {
   public static final String ITEM_DOWNLOAD_STARTED = "download_started";
   public static final String ITEM_DOWNLOAD_PROGRESSED = "download_progressed";
   public static final String ITEM_DOWNLOAD_FINISHED = "download_finished";
   public static final int NOTIFICATION_ID = 44;
   public static final int UPDATE_INTERVAL = 2000;

   private Intent mIntent;
   private ArrayList<DownloadJob> mDownloadJobs;
   private AsyncTask<String, Integer, HashMap<DownloadState, Integer>> mDownloader;
   private DownloadsDatabaseHandler mDownloadDatabase;

   private class DownloaderTask extends AsyncTask<String, Integer, HashMap<DownloadState, Integer>> {
      private Notification mNotification;
      private NotificationManager mNotificationManager;
      private DownloadJob mCurrentJob;
      private int mNumberOfJobs;
      private Context mContext;
      private String mToastMessage;

      private DownloaderTask(Context _context) {
         mNumberOfJobs = 0;
         mContext = _context;
      }

      @Override
      protected HashMap<DownloadState, Integer> doInBackground(String... params) {
         HashMap<DownloadState, Integer> downloadResult = new HashMap<DownloadState, Integer>();
         while (mDownloadJobs != null && mDownloadJobs.size() > 0) {
            DownloadJob topmostTask = null;
            synchronized (mDownloadJobs) {
               topmostTask = mDownloadJobs.get(0);
               mDownloadJobs.remove(0);
            }

            DownloadState state = downloadFile(topmostTask);

            if (downloadResult.containsKey(state)) {
               int num = downloadResult.get(state);
               downloadResult.put(state, num++);
            } else {
               downloadResult.put(state, 1);
            }
            // download succeeded -> next file
            // topmostTask.setState(DownloadState.Finished);

            // TODO: download failed -> retry?
            // return false;
         }

         return downloadResult;
      }

      private DownloadState downloadFile(DownloadJob _job) {
         File downloadFile = null;
         mToastMessage = null;
         long lastUpdated = new Date().getTime();
         boolean cancelRequested = false;
         try {
            mNumberOfJobs++;
            mCurrentJob = _job;
            URL myFileUrl = new URL(_job.getUrl());
            String myFileName = _job.getFileName();

            Intent onClickIntent = new Intent(getApplicationContext(), DownloadsActivity.class);

            _job.setState(DownloadState.Running);
            _job.setDateStarted(new Date());
            mDownloadDatabase.open();
            mDownloadDatabase.updateDownloads(_job);
            mDownloadDatabase.close();

            // configure the intent
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                  onClickIntent, 0);

            // This is who should be launched if the user selects the app icon
            // in the notification.
            // Intent appIntent = new Intent(getApplicationContext(),
            // HomeActivity.class);

            // configure the notification
            mNotification = new Notification(R.drawable.quickaction_sdcard, "Download",
                  System.currentTimeMillis());
            mNotification.flags = mNotification.flags | Notification.FLAG_ONGOING_EVENT;
            mNotification.contentView = new RemoteViews(getApplicationContext().getPackageName(),
                  R.layout.download_progress);
            mNotification.contentIntent = pendingIntent;
            mNotification.contentView.setImageViewResource(R.id.status_icon, R.drawable.icon);

            createNotificationText(0);

            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(
                  getApplicationContext().NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);

            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();

            if (_job.isUseAut()) {
               final String username = _job.getUsername();
               final String password = _job.getPassword();
               Authenticator.setDefault(new Authenticator() {
                  protected PasswordAuthentication getPasswordAuthentication() {
                     return new PasswordAuthentication(username, password.toCharArray());
                  }
               });
            } else {
               Authenticator.setDefault(null);
            }

            conn.setDoInput(true);
            conn.connect();

            InputStream inputStream = conn.getInputStream();

            downloadFile = new File(DownloaderUtils.getBaseDirectory() + "/" + myFileName);
            File donwloadDir = new File(Utils.getFolder(downloadFile.toString(), "/"));

            if (!donwloadDir.exists()) {
               if (donwloadDir.mkdirs()) {
                  Log.d(Constants.LOG_CONST_ITEMDOWNLOADER, "created directory on sd card");
               }
            }

            OutputStream out = new FileOutputStream(downloadFile);
            byte buf[] = new byte[1024];
            long fileSize = conn.getContentLength();
            if (fileSize == -1) {
               fileSize = _job.getLength();
            }
            long read = 0;
            int currentProgress = 0;
            int len;
            while ((len = inputStream.read(buf)) > 0 && !cancelRequested) {
               out.write(buf, 0, len);

               read += len;
               long curTime = new Date().getTime();
               if (curTime > lastUpdated + UPDATE_INTERVAL) {
                  int progress = 0;
                  if (fileSize == 0) {
                     progress = -99;
                  } else {
                     progress = (int) (read * 100 / fileSize);
                  }

                  currentProgress = progress;

                  _job.setProgress(currentProgress);
                  mDownloadDatabase.open();

                  cancelRequested = mDownloadDatabase.getCancelRequest(_job);

                  mDownloadDatabase.updateDownloads(_job);
                  mDownloadDatabase.close();

                  publishProgress(progress);

                  lastUpdated = curTime;
               }
            }
            out.close();
            inputStream.close();

            mDownloadDatabase.open();
            if (cancelRequested) {
               if (downloadFile != null) {
                  deleteFile(downloadFile);
               }
               _job.setState(DownloadState.Stopped);
               _job.setErrorMessage("Cancelled by User");
            } else {
               _job.setState(DownloadState.Finished);
               _job.setProgress(100);
            }
            _job.setDateFinished(new Date());

            mDownloadDatabase.updateDownloads(_job);
            mDownloadDatabase.close();

            // return true only if download finished
            return _job.getState();
         } catch (MalformedURLException e) {
            if (e != null) {
               mToastMessage = e.getMessage();
               publishProgress(-1);
            }
         } catch (UnsupportedEncodingException e) {
            if (e != null) {
               mToastMessage = e.getMessage();
               publishProgress(-1);
            }
         } catch (IOException e) {
            if (e != null) {
               mToastMessage = e.getMessage();
               publishProgress(-1);
            }
         } catch (Exception e) {
            if (e != null) {
               mToastMessage = e.getMessage();
               publishProgress(-1);
            }
         }

         mDownloadDatabase.open();

         // delete unfinished files
         if (downloadFile != null) {
            deleteFile(downloadFile);
         }
         _job.setState(DownloadState.Error);
         _job.setErrorMessage(mToastMessage);
         _job.setDateFinished(new Date());
         mDownloadDatabase.updateDownloads(_job);
         mDownloadDatabase.close();

         return _job.getState();
      }

      private void deleteFile(File _file) {
         try {
            _file.delete();
         } catch (Exception ex) {
            Log.e(Constants.LOG_CONST_ITEMDOWNLOADER, ex.toString());
         }

      }

      private void createNotificationText(int _progress) {
         int totalDownloads = mNumberOfJobs + mDownloadJobs.size();
         String filename = Utils.getFileNameWithExtension(mCurrentJob.getFileName(), "/");
         String overview = (mDownloadJobs.size() > 0 ? mNumberOfJobs + "/" + totalDownloads
               : "1 File");
         String progressText = _progress + " %";

         mNotification.contentView.setTextViewText(R.id.TextViewNotificationFileName, filename);
         mNotification.contentView.setTextViewText(R.id.TextViewNotificationOverview, overview);

         mNotification.contentView.setTextViewText(R.id.TextViewNotificationProgressText,
               progressText);

         if (_progress != -99) {
            mNotification.contentView.setProgressBar(R.id.ProgressBarNotificationTransferStatus,
                  100, _progress, false);
         } else {
            mNotification.contentView.setProgressBar(R.id.ProgressBarNotificationTransferStatus,
                  100, 0, true);
         }

      }

      @Override
      protected void onPostExecute(HashMap<DownloadState, Integer> _result) {
         if (mNotificationManager != null) {
            stopSelf();
            mNotificationManager.cancel(NOTIFICATION_ID);

            Intent onClickIntent = new Intent(getApplicationContext(), DownloadsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                  onClickIntent, PendingIntent.FLAG_CANCEL_CURRENT);// PendingIntent.FLAG_CANCEL_CURRENT
            Notification notification = new Notification(R.drawable.mp_logo_2,
                  mContext.getString(R.string.notification_title), System.currentTimeMillis());
            if (_result != null) {
               notification.setLatestEventInfo(getApplicationContext(),
                     mContext.getString(R.string.notification_title),
                     createFinishedNotificationText(_result), pendingIntent);
            } else {
               notification.setLatestEventInfo(getApplicationContext(),
                     mContext.getString(R.string.notification_title),
                     mContext.getString(R.string.notification_download_failed), pendingIntent);
            }

            mNotificationManager.notify(49, notification);
         }
         super.onPostExecute(_result);
      }

      private String createFinishedNotificationText(HashMap<DownloadState, Integer> _result) {
         int numFinished = 0;
         if (_result.containsKey(DownloadState.Finished)) {
            numFinished += _result.get(DownloadState.Finished);
         }

         int numFailed = 0;
         if (_result.containsKey(DownloadState.Stopped)) {
            numFailed += _result.get(DownloadState.Stopped);
         }
         if (_result.containsKey(DownloadState.Error)) {
            numFailed += _result.get(DownloadState.Error);
         }

         String retString = mContext.getString(R.string.notification_download_succeeded);
         if (numFinished > 0) {
            retString += "Succeeded: " + numFinished;
         }
         if (numFailed > 0) {
            retString += " Failed: " + numFailed;
         }

         return retString;
      }

      @Override
      protected void onProgressUpdate(Integer... values) {
         int progress = values[0];
         if (progress != -1) {
            createNotificationText(progress);
            // inform the progress bar of updates in progress
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
         } else {
            Util.showToast(mContext, mToastMessage);
         }
      }

   }

   @Override
   public IBinder onBind(Intent intent) {
      mIntent = intent;
      return null;
   }

   @Override
   public void onCreate() {
      mDownloadJobs = new ArrayList<DownloadJob>();
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      if (mDownloadDatabase == null) {
         mDownloadDatabase = new DownloadsDatabaseHandler(this);
      }

      if ((flags & START_FLAG_RETRY) == 0) {

      } else {

      }

      synchronized (mDownloadJobs) {
         DownloadJob job = ItemDownloaderHelper.getDownloadJobFromIntent(intent);

         mDownloadDatabase.open();
         if (!mDownloadDatabase.updateDownloads(job)) {
            mDownloadDatabase.addDownload(job);
         }
         mDownloadDatabase.close();
         mDownloadJobs.add(job);

         if (job.getDisplayName() != null) {
            Util.showToast(this, "Added " + job.getDisplayName() + " to download list");
         } else {
            Util.showToast(this, "Added " + job.getFileName() + " to download list");
         }
      }

      if (mDownloader == null || mDownloader.isCancelled()) {
         mDownloader = new DownloaderTask(this).execute();
      }

      return Service.START_STICKY;
   }
}
