package com.mediaportal.ampdroid.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mediaportal.ampdroid.data.SeriesEpisodeDetails;
import com.mediaportal.ampdroid.downloadservice.DownloadJob;

public class DownloadsDatabaseHandler {
   Context mContext;
   private DownloadsDatabaseHelper mDbHelper;
   private SQLiteDatabase mDatabase;

   public DownloadsDatabaseHandler(Context _context) {
      mContext = _context;
      mDbHelper = new DownloadsDatabaseHelper(mContext, "ampdroid_downloads", null, 17);
   }

   public void open() {
      mDatabase = mDbHelper.getWritableDatabase();
   }

   public void close() {
      mDatabase.close();
   }

   public List<DownloadJob> getDownloads() {
      try {
         Cursor result = mDatabase
               .query(DownloadJob.TABLE_NAME, null, null, null, null, null, null);
         ArrayList<DownloadJob> downloads = null;
         if (result.moveToFirst()) {
            downloads = (ArrayList<DownloadJob>) SqliteAnnotationsHelper.getObjectsFromCursor(
                  result, DownloadJob.class, 0);
         }

         result.close();

         return downloads;
      } catch (Exception ex) {
         Log.e("Database", ex.getMessage());
      }
      // something went wrong obviously
      return null;
   }

   public boolean addDownload(DownloadJob _download) {
      if (mDatabase != null) {
         try {
            ContentValues downloadValues = SqliteAnnotationsHelper.getContentValuesFromObject(
                  _download, DownloadJob.class);

            long row = mDatabase.insert(DownloadJob.TABLE_NAME, null, downloadValues);
            if (row > 0)
               return true;
         } catch (Exception ex) {
            Log.e("Database", ex.getMessage());
         }
      }
      return false;
   }

   public boolean updateDownloads(DownloadJob _download) {
      if (mDatabase != null) {
         try {
            ContentValues clubValues = SqliteAnnotationsHelper.getContentValuesFromObject(
                  _download, DownloadJob.class);

            int rows = mDatabase.update(DownloadJob.TABLE_NAME, clubValues,
                  "Id=" + _download.getId(), null);

            if (rows == 1) {
               return true;
            }
         } catch (Exception ex) {
            Log.e("Database", ex.getMessage());
         }
      }
      return false;
   }

   public boolean removeDownload(DownloadJob _download) {
      if (mDatabase != null && _download != null) {
         int numDeleted = mDatabase.delete(DownloadJob.TABLE_NAME, "Id=" + _download.getId(), null);
         return numDeleted == 1;
      }
      return false;
   }

   public Boolean getCancelRequest(DownloadJob _job) {
      if (mDatabase != null) {
         try {
            Cursor result = mDatabase.query(DownloadJob.TABLE_NAME, null, "Id=" + _job.getId(),
                  null, null, null, null);
            DownloadJob cancelRequest = null;
            if (result.getCount() == 1) {
               result.moveToFirst();
               cancelRequest = (DownloadJob) SqliteAnnotationsHelper.getObjectFromCursor(result,
                     DownloadJob.class);
            }
            result.close();
            if (cancelRequest != null) {
               return cancelRequest.isRequestCancelation();
            }
         } catch (Exception ex) {
            Log.e("Database", ex.getMessage());
         }
      }
      return false;
   }
}
