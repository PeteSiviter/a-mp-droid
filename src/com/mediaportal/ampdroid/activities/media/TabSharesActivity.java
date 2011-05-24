package com.mediaportal.ampdroid.activities.media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseTabActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.FileInfo;
import com.mediaportal.ampdroid.data.VideoShare;
import com.mediaportal.ampdroid.downloadservice.ItemDownloaderService;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.DownloaderUtils;
import com.mediaportal.ampdroid.utils.Util;

public class TabSharesActivity extends Activity {
   private ListView mListView;
   private ArrayAdapter<VideoShare> mListItems;
   private ArrayAdapter<FileInfo> mFileItems;
   private List<String> mBreadCrumb;
   private VideoShare mCurrentShare;
   private LoadSharesTask mSeriesLoaderTask;
   private DataHandler mService;
   private LoadFolderTask mFilesLoaderTask;
   private ProgressDialog mLoadingDialog;
   private BaseTabActivity mBaseActivity;
   private StatusBarActivityHandler mStatusBarHandler;

   private class LoadSharesTask extends AsyncTask<Integer, Integer, List<VideoShare>> {
      @Override
      protected List<VideoShare> doInBackground(Integer... _params) {
         List<VideoShare> shares = mService.getAllVideoShares();

         return shares;
      }
 
      @Override
      protected void onPostExecute(List<VideoShare> _result) {
         mListItems.clear();

         if (_result != null) {
            for (VideoShare s : _result) {
               mListItems.add(s);
            }
         }

         mListView.setAdapter(mListItems);
         mListItems.notifyDataSetChanged();
         mLoadingDialog.dismiss();
      }
   }

   private class LoadFolderTask extends AsyncTask<String, Integer, List<FileInfo>> {
      @Override
      protected List<FileInfo> doInBackground(String... _params) {
         String path = _params[0];
         List<FileInfo> retList = new ArrayList<FileInfo>();

         List<FileInfo> folders = mService.getFoldersForFolder(path);
         List<FileInfo> files = mService.getFilesForFolder(path);

         if (folders != null) {
            retList.addAll(folders);
         }

         if (files != null) {
            retList.addAll(files);
         }
         return retList;
      }

      @Override
      protected void onPostExecute(List<FileInfo> _result) {
         mFileItems.clear();

         if (_result != null) {
            for (FileInfo f : _result) {
               if (f.isFolder() || checkForValidExt(f)) {
                  mFileItems.add(f);
               }
            }
         }

         mListView.setAdapter(mFileItems);
         mFileItems.notifyDataSetChanged();
         mLoadingDialog.dismiss();
      }

      private boolean checkForValidExt(FileInfo f) {
         for(String e : mCurrentShare.Extensions){
            if(f.getFullPath() != null && f.getFullPath().endsWith(e)) return true;
         }
         return false;
      }
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tabsharesactivity);

      mListView = (ListView) findViewById(R.id.ListViewShares);
      mListView.setFastScrollEnabled(true);
      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> _adapter, View _view, int _pos, long _id) {
            VideoShare selected = (VideoShare) mListView.getItemAtPosition(_pos);
            Util.showToast(_view.getContext(), selected.Path);
            // handleListClick(v, position, selected);
         }
      });

      mFileItems = new ArrayAdapter<FileInfo>(this, android.R.layout.simple_list_item_1);

      mListItems = new ArrayAdapter<VideoShare>(this, android.R.layout.simple_list_item_1);
      mListView.setAdapter(mListItems);
      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> _adapter, View _view, int _pos, long _id) {
            if (_adapter.getAdapter().equals(mListItems)) {
               VideoShare selected = (VideoShare) mListView.getItemAtPosition(_pos);
               mCurrentShare = selected;
               loadFiles(selected.Path);
            } else if (_adapter.getAdapter().equals(mFileItems)) {
               FileInfo selected = (FileInfo) mListView.getItemAtPosition(_pos);
               if (selected.isFolder()) {
                  loadFiles(selected.getFullPath());
               }
            }
         }
      });
      
      mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> _adapter, View _view, final int _pos,
               long _id) {
            try {
               if (_adapter.getAdapter().equals(mFileItems)) {
                  final FileInfo selected = (FileInfo) mListView.getItemAtPosition(_pos);
                                
                  final String fileName = DownloaderUtils.getVideoPath(mCurrentShare, selected);

                  final QuickAction qa = new QuickAction(_view);
                  
                  final File localFileName = new File(DownloaderUtils.getBaseDirectory() + "/"
                        + fileName);

                  if (localFileName.exists()) {
                     ActionItem playItemAction = new ActionItem();

                     playItemAction.setTitle(getString(R.string.quickactions_playdevice));
                     playItemAction.setIcon(getResources().getDrawable(
                           R.drawable.quickaction_play));
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
                  }
                  else{
                     ActionItem sdCardAction = new ActionItem();
                     sdCardAction.setTitle(getString(R.string.quickactions_downloadsd));
                     sdCardAction.setIcon(getResources().getDrawable(R.drawable.quickaction_sdcard));
                     sdCardAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           String url = mService.getDownloadUri(selected.getFullPath());
                           FileInfo info = mService.getFileInfo(selected.getFullPath());
                           if (url != null) {
                              Intent download = new Intent(_view.getContext(),
                                    ItemDownloaderService.class);
                              download.putExtra("url", url);
                              download.putExtra("name", fileName);
                              if (info != null) {
                                 download.putExtra("length", info.getLength());
                              }
                              startService(download);
                           }
                           
                           qa.dismiss();
                        }
                     });
                     qa.addActionItem(sdCardAction);
                  }
                  
                  if(mService.isClientControlConnected()){
                     ActionItem playOnClientAction = new ActionItem();

                     playOnClientAction.setTitle(getString(R.string.quickactions_playclient));
                     playOnClientAction.setIcon(getResources().getDrawable(
                           R.drawable.quickaction_play_device));
                     playOnClientAction.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View _view) {
                           mService.playVideoFileOnClient(selected.getFullPath());
                           
                           qa.dismiss();
                        }
                     });
                     qa.addActionItem(playOnClientAction);
                  }

                  qa.setAnimStyle(QuickAction.ANIM_AUTO);

                  qa.show();
               }
            } catch (Exception ex) {
               return false;
            }
            return false;
         }
      });

      mBreadCrumb = new ArrayList<String>();
      
      mBaseActivity = (BaseTabActivity) getParent();
      mService = DataHandler.getCurrentRemoteInstance();
      
      if (mBaseActivity != null && mService != null) {
         mStatusBarHandler = new StatusBarActivityHandler(mBaseActivity, mService);
         mStatusBarHandler.setHome(false);
      }

      loadShares();
   }

   private void loadShares() {
      mLoadingDialog = ProgressDialog.show(getParent(), getString(R.string.media_shares_loadshares),
            getString(R.string.info_loading_title), true);
      mLoadingDialog.setCancelable(true);

      mSeriesLoaderTask = new LoadSharesTask();
      mSeriesLoaderTask.execute(0);
   }

   protected void loadFiles(String _path) {
      mLoadingDialog = ProgressDialog.show(getParent(), getString(R.string.media_shares_loadfiles),
            getString(R.string.info_loading_title), true);
      mLoadingDialog.setCancelable(true);
      mBreadCrumb.add(_path);

      mFilesLoaderTask = new LoadFolderTask();
      mFilesLoaderTask.execute(_path);
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
         if (!mListView.getAdapter().equals(mListItems)) {
            if (mBreadCrumb.size() == 1) {
               mBreadCrumb.clear();

               loadShares();
            } else {
               mBreadCrumb.remove(mBreadCrumb.size() - 1);// remove current
                                                          // directory
               String lastFolder = mBreadCrumb.get(mBreadCrumb.size() - 1); // get
                                                                            // the
                                                                            // previous
                                                                            // directory
               mBreadCrumb.remove(mBreadCrumb.size() - 1); // remove prev
                                                           // directory since it
                                                           // will be readded on
                                                           // loading
               loadFiles(lastFolder);

            }
            return true;
         }

      }
      return super.onKeyDown(keyCode, event);
   }

}
