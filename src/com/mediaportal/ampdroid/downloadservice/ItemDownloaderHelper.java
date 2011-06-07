package com.mediaportal.ampdroid.downloadservice;

import android.content.Context;
import android.content.Intent;

public class ItemDownloaderHelper {

   public static Intent createDownloadIntent(Context _context, DownloadJob _job) {
      Intent download = new Intent(_context,
            ItemDownloaderService.class);
      download.putExtra("url", _job.getUrl());
      download.putExtra("file_name", _job.getFileName());
      download.putExtra("display_name", _job.getDisplayName());
      download.putExtra("length", _job.getLength());
      download.putExtra("item_type", _job.getMediaTypeInt());
      
      download.putExtra("useAuth", _job.isUseAut());
      download.putExtra("username", _job.getUsername());
      download.putExtra("password", _job.getPassword());
      download.putExtra("job_id", _job.getId());
      
      return download;
   }

   public static DownloadJob getDownloadJobFromIntent(Intent intent) {
      String url = intent.getStringExtra("url");
      String fileName = intent.getStringExtra("file_name");
      String displayName = intent.getStringExtra("display_name");
      long length = intent.getLongExtra("length", 0);
      boolean useAuth = intent.getBooleanExtra("useAuth", false);
      String username = intent.getStringExtra("username");
      String password = intent.getStringExtra("password");
      int itemType = intent.getIntExtra("item_type", 0);
      int id = intent.getIntExtra("job_id", 0);
      
      DownloadJob job = new DownloadJob();
      job.setId(id);
      job.setUrl(url);
      job.setFileName(fileName);
      job.setDisplayName(displayName);
      job.setLength(length);
      job.setMediaTypeInt(itemType);
      
      if(useAuth)
      {
         job.setAuth(username, password);
      }
      return job;
   }

}
