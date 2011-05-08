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
import java.util.List;
import java.util.Map;

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
import com.mediaportal.ampdroid.activities.HomeActivity;
import com.mediaportal.ampdroid.lists.Utils;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class ItemDownloaderService extends Service {
   public static final String ITEM_DOWNLOAD_STARTED = "download_started";
   public static final String ITEM_DOWNLOAD_PROGRESSED = "download_progressed";
   public static final String ITEM_DOWNLOAD_FINISHED = "download_finished";
   public static final int NOTIFICATION_ID = 44;

   private Intent mIntent;
   private ArrayList<DownloadJob> mDownloadJobs;
   private AsyncTask<String, Integer, Boolean> mDownloader;

   private class DownloadJob {
      private int mId;
      private String mName;
      private String mUrl;
      private long mLength;

      public int getId() {
         return mId;
      }

      public void setId(int _id) {
         this.mId = _id;
      }

      public String getName() {
         return mName;
      }

      public String getShortenedName() {
         String name = mName;
         if (name.length() > 30) {
            name = name.substring(name.length() - 30);
         }
         return name;
      }

      public void setName(String _name) {
         this.mName = _name;
      }

      public String getUrl() {
         return mUrl;
      }

      public void setUrl(String _url) {
         this.mUrl = _url;
      }

      private DownloadJob(int _id, String _name, String _url, long _length) {
         mId = _id;
         mName = _name;
         mUrl = _url;
         setLength(_length);
      }

      public void setLength(long length) {
         mLength = length;
      }

      public long getLength() {
         return mLength;
      }
   }

   private class DownloaderTask extends AsyncTask<String, Integer, Boolean> {
      private Notification mNotification;
      private NotificationManager mNotificationManager;
      private DownloadJob mCurrentJob;
      private int mNumberOfJobs;
      private Context mContext;

      private DownloaderTask(Context _context) {
         mNumberOfJobs = 0;
         mContext = _context;
      }

      @Override
      protected Boolean doInBackground(String... params) {
         while (mDownloadJobs != null && mDownloadJobs.size() > 0) {
            DownloadJob topmostTask = null;
            synchronized (mDownloadJobs) {
               topmostTask = mDownloadJobs.get(0);
               mDownloadJobs.remove(0);
            }

            if (downloadFile(topmostTask)) {
               // download succeeded -> next file

            } else {
               // TODO: download failed -> retry?
               return false;
            }
         }

         return true;
      }

      private boolean downloadFile(DownloadJob _job) {
         try {
            mNumberOfJobs++;
            mCurrentJob = _job;
            URL myFileUrl = new URL(_job.getUrl());
            String myFileName = _job.getName();

            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);

            // configure the intent
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                  homeIntent, 0);

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
            Map<String, List<String>> headers2 = conn.getRequestProperties();

            Authenticator.setDefault(new Authenticator() {
               protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication("admin", "admin".toCharArray());
               }
            });

            conn.setDoInput(true);
            conn.connect();

            InputStream inputStream = conn.getInputStream();
            Map<String, List<String>> headers = conn.getHeaderFields();

            File downloadFile = new File(DownloaderUtils.getBaseDirectory() + "/" + myFileName);
            File donwloadDir = new File(Utils.getFolder(downloadFile.toString(), "/"));

            if (!donwloadDir.exists()) {
               if (donwloadDir.mkdirs()) {
                  Log.d("ItemDownloaderService", "created directory on sd card");
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
            while ((len = inputStream.read(buf)) > 0) {
               out.write(buf, 0, len);

               read += len;
               if (fileSize > 0) {
                  int progress = (int) (read * 100 / fileSize);
                  if (progress > currentProgress + 4) {
                     currentProgress = progress;

                     publishProgress(progress);
                  }
               }
            }
            out.close();
            inputStream.close();

            return true;
         } catch (MalformedURLException e) {
            e.printStackTrace();
            // Util.showToast(mContext, e.toString());
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // Util.showToast(mContext, e.toString());
         } catch (IOException e) {
            e.printStackTrace();
            // Util.showToast(mContext, e.toString());
         } catch (Exception e) {
            if (e != null) {
               e.printStackTrace();
            }
            // Util.showToast(mContext, e.toString());
         }

         return false;
      }

      private void createNotificationText(int _progress) {
         int totalDownloads = mNumberOfJobs + mDownloadJobs.size();
         String filename = Utils.getFileNameWithExtension(mCurrentJob.getName(), "/");
         String overview = (mDownloadJobs.size() > 0 ? mNumberOfJobs + "/" + totalDownloads
               : "1 File");
         String progressText = _progress + " %";

         mNotification.contentView.setTextViewText(R.id.TextViewNotificationFileName, filename);
         mNotification.contentView.setTextViewText(R.id.TextViewNotificationOverview, overview);

         mNotification.contentView.setTextViewText(R.id.TextViewNotificationProgressText,
               progressText);

         if (_progress != -1) {
            mNotification.contentView.setProgressBar(R.id.ProgressBarNotificationTransferStatus,
                  100, _progress, false);
         } else {
            mNotification.contentView.setProgressBar(R.id.ProgressBarNotificationTransferStatus,
                  100, 0, true);
         }

      }

      @Override
      protected void onPostExecute(Boolean result) {
         if (mNotificationManager != null) {
            stopSelf();
            mNotificationManager.cancel(NOTIFICATION_ID);
            Notification notification = new Notification(R.drawable.mp_logo_2, "Notify",
                  System.currentTimeMillis());
            if (result) {
               notification.setLatestEventInfo(getApplicationContext(), "aMPdroid",
                     "Downloads finished", PendingIntent.getActivity(getApplicationContext(), 0,
                           mIntent, PendingIntent.FLAG_CANCEL_CURRENT));
            } else {
               notification.setLatestEventInfo(getApplicationContext(), "aMPdroid",
                     "Downloads failed", PendingIntent.getActivity(getApplicationContext(), 0,
                           mIntent, PendingIntent.FLAG_CANCEL_CURRENT));
            }

            mNotificationManager.notify(49, notification);
         }
         super.onPostExecute(result);
      }

      @Override
      protected void onProgressUpdate(Integer... values) {
         int progress = values[0];
         createNotificationText(progress);
         // inform the progress bar of updates in progress
         mNotificationManager.notify(NOTIFICATION_ID, mNotification);
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
      if ((flags & START_FLAG_RETRY) == 0) {

      } else {

      }

      String url = intent.getStringExtra("url");
      String fName = intent.getStringExtra("name");
      String displayName = intent.getStringExtra("display_name");
      long length = intent.getLongExtra("length", 0);

      synchronized (mDownloadJobs) {
         mDownloadJobs.add(new DownloadJob(0, fName, url, length));
         if (displayName != null) {
            Util.showToast(this, "Added " + displayName + " to download list");
         } else {
            Util.showToast(this, "Added " + fName + " to download list");
         }
      }

      if (mDownloader == null || mDownloader.isCancelled()) {
         mDownloader = new DownloaderTask(this).execute();
      }

      return Service.START_STICKY;
   }

   private void broadcastProgress(String _url, int _progress) {

   }

   private void broadcastStarted(String _url) {

   }

   private void broadcastFinished(String _url) {

   }

}
