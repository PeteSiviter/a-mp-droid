package com.mediaportal.ampdroid.activities;

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.api.ApiCredentials;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.database.DownloadsDatabaseHandler;
import com.mediaportal.ampdroid.downloadservice.DownloadItemType;
import com.mediaportal.ampdroid.downloadservice.DownloadJob;
import com.mediaportal.ampdroid.downloadservice.DownloadState;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderHelper;
import com.mediaportal.ampdroid.downloadservice.MediaItemType;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.views.DownloadsDetailsAdapterItem;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.Constants;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class DownloadsActivity extends BaseActivity {

   private ListView mListView;
   private DownloadsDatabaseHandler mDatabase;
   private LazyLoadingAdapter mAdapter;
   List<DownloadJob> mDownloads;

   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tvserveractivity);

      mService = DataHandler.getCurrentRemoteInstance();
      mListView = (ListView) findViewById(R.id.ListViewItems);
      mDatabase = new DownloadsDatabaseHandler(this);
      
      mAdapter = new LazyLoadingAdapter(this);
      mListView.setAdapter(mAdapter);
      
      fillListView();

      mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> _item, View _view, int _pos, long _id) {
            final ILoadingAdapterItem item = (ILoadingAdapterItem) mListView
                  .getItemAtPosition(_pos);

            if (item != null) {
               final QuickAction qa = new QuickAction(_view);
               final DownloadJob job = (DownloadJob) item.getItem();
               final String file = DownloaderUtils.getBaseDirectory() + "/" + job.getFileName();
               final File downloadFile = new File(file);

               if (job.getState() == DownloadState.Running
                     || job.getState() == DownloadState.Paused) {
                  ActionItem cancelDownloadAction = new ActionItem();
                  cancelDownloadAction.setTitle(getString(R.string.quickactions_canceldownload));
                  cancelDownloadAction.setIcon(getResources().getDrawable(
                        R.drawable.quickaction_remove));
                  cancelDownloadAction.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View _view) {
                        job.setRequestCancelation(true);
                        mDatabase.open();
                        mDatabase.updateDownloads(job);
                        mDatabase.close();
                        qa.dismiss();
                     }
                  });
                  qa.addActionItem(cancelDownloadAction);
               } else {
                  if (downloadFile.exists()) {
                     ActionItem deleteDownloadAction = new ActionItem();
                     deleteDownloadAction.setTitle(getString(R.string.quickactions_deletedownload));
                     deleteDownloadAction.setIcon(getResources().getDrawable(
                           R.drawable.quickaction_delete));
                     deleteDownloadAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           try {
                              Log.i(Constants.LOG_CONST, "Deleting file " + file);
                              if (downloadFile.delete()) {
                                 Util.showToast(_view.getContext(),
                                       getString(R.string.downloads_delete_file_success));
                              } else {
                                 Util.showToast(_view.getContext(),
                                       getString(R.string.downloads_delete_file_failed));
                              }
                           } catch (Exception ex) {
                              Util.showToast(_view.getContext(),
                                    getString(R.string.downloads_delete_file_failed));
                              Log.e(Constants.LOG_CONST, ex.toString());
                           }
                           qa.dismiss();
                        }
                     });
                     qa.addActionItem(deleteDownloadAction);
                  }
               }

               if (downloadFile.exists()) {
                  ActionItem playDownloadAction = new ActionItem();
                  playDownloadAction.setTitle(getString(R.string.quickactions_playdevice));
                  playDownloadAction.setIcon(getResources().getDrawable(
                        R.drawable.quickaction_play_device));
                  playDownloadAction.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View _view) {
                        String file = DownloaderUtils.getBaseDirectory() + "/" + job.getFileName();

                        String mimeString = MediaItemType.getIntentMimeType(job.getMediaType());
                        Intent playIntent = new Intent(Intent.ACTION_VIEW);
                        playIntent.setDataAndType(Uri.parse(file), mimeString);

                        startActivity(playIntent);
                     }
                  });
                  qa.addActionItem(playDownloadAction);
               } else {
                  ActionItem playDownloadAction = new ActionItem();
                  playDownloadAction.setTitle(getString(R.string.quickactions_redownload));
                  playDownloadAction.setIcon(getResources().getDrawable(
                        R.drawable.quickaction_sdcard));
                  playDownloadAction.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View _view) {
                        Intent download = ItemDownloaderHelper.createDownloadIntent(
                              _view.getContext(), job);
                        startService(download);
                     }
                  });
                  qa.addActionItem(playDownloadAction);
               }
               qa.show();
            }
            return true;
         }
      });
   }

   private void fillListView() {
      mDatabase.open();
      mDownloads = mDatabase.getDownloads();
      mDatabase.close();
      
      if (mDownloads != null) {
         for (DownloadJob j : mDownloads) {
            mAdapter.addItem(new DownloadsDetailsAdapterItem(j, this));
         }
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu _menu) {
      super.onCreateOptionsMenu(_menu);

      MenuItem clearDownloadsItem = _menu.add(0, Menu.FIRST, Menu.NONE,
            getString(R.string.menu_clear_downloads));
      clearDownloadsItem.setIcon(R.drawable.ic_menu_close_clear_cancel);
      clearDownloadsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            mDatabase.open();
            if (mDownloads != null) {
               for (DownloadJob j : mDownloads) {
                  if (j.getState() != DownloadState.Running || j.getState() != DownloadState.Paused) {
                     mDatabase.removeDownload(j);
                  }
               }
            }
            mDatabase.close();
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            return true;
         }
      });
      
      MenuItem refreshDownloadsItem = _menu.add(0, Menu.FIRST + 1, Menu.NONE,
            getString(R.string.menu_refresh));
      refreshDownloadsItem.setIcon(R.drawable.ic_menu_refresh);
      refreshDownloadsItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {
            mAdapter.clear();
            fillListView();
            mAdapter.notifyDataSetChanged();
            return true;
         }
      });

      return true;
   }
}
