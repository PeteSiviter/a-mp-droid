package com.mediaportal.ampdroid.activities.tvserver;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.data.TvChannel;
import com.mediaportal.ampdroid.data.TvChannelGroup;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.settings.PreferencesManager;
import com.mediaportal.ampdroid.utils.Constants;
import com.mediaportal.ampdroid.utils.Util;

public class TvServerChannelsActivity extends BaseActivity {
   private DataHandler mService;
   private ListView mListView;
   private Spinner mGroupsSpinner;

   ArrayAdapter<TvChannel> mChannelItems;
   ArrayAdapter<TvChannelGroup> mGroupsItems;
   UpdateGroupsTask mGroupsUpdater;
   UpdateChannelsTask mChannelUpdater;
   ProgressDialog mLoadingDialog;
   TvChannel mSelectedChannel;
   String mPlayingUrl;

   boolean mShowAllGroup = false;
   private StatusBarActivityHandler mStatusBarHandler;

   private class UpdateGroupsTask extends AsyncTask<Integer, Integer, List<TvChannelGroup>> {
      @Override
      protected List<TvChannelGroup> doInBackground(Integer... _group) {
         List<TvChannelGroup> groups = mService.getTvChannelGroups();
         return groups;
      }

      @Override
      protected void onPostExecute(List<TvChannelGroup> _result) {
         mGroupsItems.clear();
         if (_result != null) {
            for (TvChannelGroup g : _result) {
               if (mShowAllGroup || g.getIdGroup() != 1) {
                  mGroupsItems.add(g);
               }
            }
         }
      }
   }

   private class UpdateChannelsTask extends AsyncTask<TvChannelGroup, Integer, List<TvChannel>> {
      @Override
      protected List<TvChannel> doInBackground(TvChannelGroup... _group) {
         List<TvChannel> channels = mService.getTvChannelsForGroup(_group[0].getIdGroup()); // get
         return channels;
      }

      @Override
      protected void onPostExecute(List<TvChannel> _result) {
         if (_result != null) {
            mChannelItems.clear();

            for (TvChannel c : _result) {
               mChannelItems.add(c);
            }
         }
         mLoadingDialog.cancel();
      }
   }

   @Override
   public void onCreate(Bundle _savedInstanceState) {
      super.onCreate(_savedInstanceState);
      setTitle(R.string.title_tvserver_channels);

      setContentView(R.layout.tvserverchannelsactivity);
      mListView = (ListView) findViewById(R.id.ListViewChannels);
      mChannelItems = new ArrayAdapter<TvChannel>(this, android.R.layout.simple_list_item_1);
      mListView.setAdapter(mChannelItems);

      mGroupsSpinner = (Spinner) findViewById(R.id.SpinnerGroups);
      mGroupsItems = new ArrayAdapter<TvChannelGroup>(this, android.R.layout.simple_spinner_item);
      mGroupsItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mGroupsSpinner.setAdapter(mGroupsItems);

      mService = DataHandler.getCurrentRemoteInstance();
      mStatusBarHandler = new StatusBarActivityHandler(this, mService);
      mStatusBarHandler.setHome(false);

      mGroupsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> _adapter, View _view, int _position, long _id) {
            TvChannelGroup group = (TvChannelGroup) mGroupsSpinner.getItemAtPosition(_position);

            refreshChannelList(group);
         }

         @Override
         public void onNothingSelected(AdapterView<?> _adapter) {

         }
      });

      mListView.setOnItemClickListener(new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> _adapter, View _view, int _pos, long _id) {
            TvChannel channel = mChannelItems.getItem(_pos);

            Intent myIntent = new Intent(_view.getContext(), TvServerChannelDetailsActivity.class);
            myIntent.putExtra("channel_id", channel.getIdChannel());
            myIntent.putExtra("channel_name", channel.getDisplayName());
            startActivity(myIntent);

         }
      });

      mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> _item, View _view, int _pos, long _id) {
            // ILoadingAdapterItem item = (ILoadingAdapterItem) mListView
            // .getItemAtPosition(_pos);
            final QuickAction qa = new QuickAction(_view);
            mSelectedChannel = (TvChannel) mListView.getItemAtPosition(_pos);

            // start channel on device -> not working yet on plugin-side
            /*
             * ActionItem playOnClientAction = new ActionItem();
             * playOnClientAction.setTitle("Play on Client"); playOnClientAction
             * .
             * setIcon(getResources().getDrawable(R.drawable.quickaction_play));
             * playOnClientAction.setOnClickListener(new OnClickListener() {
             * 
             * @Override public void onClick(View _view) { TvChannel channel =
             * mSelectedChannel;
             * mService.playChannelOnClient(channel.getIdChannel());
             * 
             * qa.dismiss(); } }); qa.addActionItem(playOnClientAction);
             */

            ActionItem playOnDeviceAction = new ActionItem();
            playOnDeviceAction.setTitle(getString(R.string.quickactions_playdevice));
            playOnDeviceAction.setIcon(getResources().getDrawable(R.drawable.quickaction_play));
            playOnDeviceAction.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View _view) {
                  TvChannel channel = mSelectedChannel;

                  mPlayingUrl = mService.startTimeshift(channel.getIdChannel(),
                        PreferencesManager.getTvClientName());
                  mPlayingUrl = mPlayingUrl.replace("bagga-server", "10.1.0.166");

                  try {
                     Intent i = new Intent(Intent.ACTION_VIEW);
                     i.setDataAndType(Uri.parse(mPlayingUrl), "video/*");
                     startActivityForResult(i, 1);
                  } catch (Exception ex) {
                     Log.e(Constants.LOG_CONST, ex.toString());
                  }

                  qa.dismiss();
               }
            });
            qa.addActionItem(playOnDeviceAction);

            qa.show();
            return true;
         }
      });

      refreshGroups();
   }

   private void refreshGroups() {
      mLoadingDialog = ProgressDialog.show(TvServerChannelsActivity.this,
            getString(R.string.tvserver_loadgroups), getString(R.string.info_loading_title), true);
      mLoadingDialog.setCancelable(true);

      mGroupsUpdater = new UpdateGroupsTask();
      mGroupsUpdater.execute(0);
   }

   private void refreshChannelList(TvChannelGroup _group) {
      mLoadingDialog.cancel();
      mLoadingDialog = ProgressDialog
            .show(TvServerChannelsActivity.this, getString(R.string.tvserver_loadchannels),
                  getString(R.string.info_loading_title), true);
      mLoadingDialog.setCancelable(true);

      mChannelUpdater = new UpdateChannelsTask();
      mChannelUpdater.execute(_group);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 1) {
         if (resultCode < 0) {
            Util.showToast(this, getString(R.string.tvserver_errorplaying) + mPlayingUrl
                  + getString(R.string.tvserver_errorplaying_resultcode) + resultCode);
         } else {
            Util.showToast(this, getString(R.string.tvserver_finishedplaying) + mPlayingUrl);
         }
         mService.stopTimeshift(PreferencesManager.getTvClientName());
      }

      super.onActivityResult(requestCode, resultCode, data);
   }

}
