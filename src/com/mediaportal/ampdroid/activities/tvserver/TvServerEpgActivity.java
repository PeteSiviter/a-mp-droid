package com.mediaportal.ampdroid.activities.tvserver;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.activities.BaseActivity;
import com.mediaportal.ampdroid.activities.StatusBarActivityHandler;
import com.mediaportal.ampdroid.api.DataHandler;
import com.mediaportal.ampdroid.asynctasks.AddScheduleTask;
import com.mediaportal.ampdroid.asynctasks.CancelScheduleTask;
import com.mediaportal.ampdroid.data.TvChannel;
import com.mediaportal.ampdroid.data.TvChannelGroup;
import com.mediaportal.ampdroid.data.TvProgramBase;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter;
import com.mediaportal.ampdroid.lists.views.TvServerChannelAdapterItem;
import com.mediaportal.ampdroid.lists.views.TvServerProgramsBaseViewItem;
import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;
import com.mediaportal.ampdroid.utils.Util;

public class TvServerEpgActivity extends BaseActivity {
   private DataHandler mService;
   private ListView mEpgView;
   private LoadEpgTask mEpgLoaderTask;
   private StatusBarActivityHandler mStatusBarHandler;
   private LazyLoadingAdapter mEpgAdapter;
   ProgressDialog mLoadingDialog;

   boolean mShowAllGroup = false;
   ArrayAdapter<TvChannelGroup> mGroupsItems;
   UpdateGroupsTask mGroupsUpdater;
   private Spinner mGroups;
   private int mHoursOffset = -1;
   private Button mLaterButton;
   private Button mEarlierButton;

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

   private class LoadEpgTask extends AsyncTask<Integer, ILoadingAdapterItem, List<TvProgramBase>> {
      private Context mContext;

      private LoadEpgTask(Context _context) {
         mContext = _context;
      }

      @Override
      protected List<TvProgramBase> doInBackground(Integer... _params) {

         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.HOUR, mHoursOffset);
         Date begin = cal.getTime();
         cal.add(Calendar.HOUR, 2);
         Date end = cal.getTime();

         List<TvChannel> channels = mService.getTvChannelsForGroup(_params[0]);
         if (channels != null) {

            for (TvChannel c : channels) {

               List<TvProgramBase> programs = mService.getTvBaseEpgForChannel(c.getIdChannel(),
                     begin, end);
               // ILoadingAdapterItem[] channelList = new
               // ILoadingAdapterItem[programs.size() + 1];
               publishProgress(new TvServerChannelAdapterItem(c));
               for (TvProgramBase p : programs) {
                  publishProgress(new TvServerProgramsBaseViewItem(p));
               }
            }
         }
         return null;
      }

      @Override
      protected void onProgressUpdate(ILoadingAdapterItem... values) {
         super.onProgressUpdate(values);
         if (values != null) {
            for (ILoadingAdapterItem i : values) {
               mEpgAdapter.addItem(i);
            }
         }
         mEpgAdapter.showLoadingItem(false);
         mEpgAdapter.notifyDataSetChanged();
      }

      @Override
      protected void onPostExecute(List<TvProgramBase> _result) {
         Util.showToast(mContext, getString(R.string.tvserver_loadepg_finished));
         mLoadingDialog.cancel();
      }
   }

   @Override
   public void onCreate(Bundle _savedInstanceState) {
      setTitle(R.string.title_tvserver_epg);
      super.onCreate(_savedInstanceState);
      setContentView(R.layout.tvserverepgactivity);
      mEpgView = (ListView) findViewById(R.id.ListViewEpg);
      mGroups = (Spinner) findViewById(R.id.SpinnerGroups);
      mLaterButton = (Button) findViewById(R.id.ButtonLater);
      mEarlierButton = (Button) findViewById(R.id.ButtonEarlier);

      mGroups.setOnItemSelectedListener(new OnItemSelectedListener() {

         @Override
         public void onItemSelected(AdapterView<?> _adapter, View _view, int _position, long _id) {
            Object selected = mGroups.getItemAtPosition(_position);
            refreshEpg((TvChannelGroup) selected);
         }

         @Override
         public void onNothingSelected(AdapterView<?> _adapter) {

         }
      });

      mEpgAdapter = new LazyLoadingAdapter(this);
      mEpgView.setAdapter(mEpgAdapter);

      mEpgView.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView<?> _adapter, View _view, int _pos, long _id) {
            ILoadingAdapterItem item = (ILoadingAdapterItem) mEpgAdapter.getItem(_pos);
            if (_adapter.getItemAtPosition(_pos).getClass()
                  .equals(TvServerChannelAdapterItem.class)) {
               // open channel detail view
               TvChannel channel = (TvChannel) item.getItem();

               Intent myIntent = new Intent(_view.getContext(),
                     TvServerChannelDetailsActivity.class);
               myIntent.putExtra("channel_id", channel.getIdChannel());
               myIntent.putExtra("channel_name", channel.getDisplayName());
               startActivity(myIntent);
            }

            if (_adapter.getItemAtPosition(_pos).getClass()
                  .equals(TvServerProgramsBaseViewItem.class)) {
               // open program detail view
               TvProgramBase prog = (TvProgramBase) item.getItem();

               Intent myIntent = new Intent(_view.getContext(),
                     TvServerProgramDetailsActivity.class);
               myIntent.putExtra("program_id", prog.getIdProgram());
               myIntent.putExtra("program_name", prog.getTitle());
               startActivity(myIntent);
            }
         }
      });

      mEpgView.setOnItemLongClickListener(new OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> _item, View _view, final int _pos, long _id) {
            ILoadingAdapterItem item = (ILoadingAdapterItem) mEpgView.getItemAtPosition(_pos);
            if (item.getClass().equals(TvServerProgramsBaseViewItem.class)) {
               final TvProgramBase program = (TvProgramBase) item.getItem();

               final QuickAction qa = new QuickAction(_view);

               if (program.isIsScheduled()) {
                  ActionItem addScheduleAction = new ActionItem();
                  addScheduleAction.setTitle(getString(R.string.quickactions_cancelrecord));
                  addScheduleAction.setIcon(getResources().getDrawable(R.drawable.bubble_del));
                  addScheduleAction.setOnClickListener(new OnClickListener() {
                     private CancelScheduleTask mCancelScheduleTask;

                     @Override
                     public void onClick(View _view) {
                        mCancelScheduleTask = new CancelScheduleTask(_view.getContext(), mService,
                              mEpgAdapter);
                        mCancelScheduleTask.execute(program);

                        qa.dismiss();
                     }
                  });
                  qa.addActionItem(addScheduleAction);
               } else {
                  ActionItem addScheduleAction = new ActionItem();
                  addScheduleAction.setTitle(getString(R.string.quickactions_record));
                  addScheduleAction.setIcon(getResources().getDrawable(
                        R.drawable.quickaction_recording));
                  addScheduleAction.setOnClickListener(new OnClickListener() {
                     private AddScheduleTask mAddScheduleTask;

                     @Override
                     public void onClick(View _view) {
                        mAddScheduleTask = new AddScheduleTask(_view.getContext(), mService,
                              mEpgAdapter);
                        mAddScheduleTask.execute(program);

                        qa.dismiss();
                     }
                  });
                  qa.addActionItem(addScheduleAction);
               }
               qa.show();
               
            }
            return true;
         }
      });

      mEarlierButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            TvChannelGroup group = (TvChannelGroup) mGroups.getSelectedItem();
            mHoursOffset--;
            refreshEpg(group);
         }
      });

      mLaterButton.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            TvChannelGroup group = (TvChannelGroup) mGroups.getSelectedItem();
            mHoursOffset++;
            refreshEpg(group);
         }
      });

      mGroupsItems = new ArrayAdapter<TvChannelGroup>(this, android.R.layout.simple_spinner_item);
      mGroupsItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mGroups.setAdapter(mGroupsItems);

      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      mShowAllGroup = prefs.getBoolean("tvserver_showall", true);

      mService = DataHandler.getCurrentRemoteInstance();
      mStatusBarHandler = new StatusBarActivityHandler(this, mService);
      mStatusBarHandler.setHome(false);

      refreshGroups();
   }

   private void refreshGroups() {
      mLoadingDialog = ProgressDialog.show(TvServerEpgActivity.this, getString(R.string.tvserver_loadgroups),
            getString(R.string.info_loading_title), true);
      mLoadingDialog.setCancelable(true);

      mGroupsUpdater = new UpdateGroupsTask();
      mGroupsUpdater.execute(0);
   }

   private void refreshEpg(TvChannelGroup _group) {
      mLoadingDialog.cancel();
      mLoadingDialog = ProgressDialog.show(TvServerEpgActivity.this, getString(R.string.tvserver_loadepg),
            getString(R.string.info_loading_title), true);
      mLoadingDialog.setCancelable(true);
      mEpgAdapter.clear();
      mEpgAdapter.showLoadingItem(true);
      mEpgAdapter.notifyDataSetChanged();
      mEpgLoaderTask = new LoadEpgTask(this);
      mEpgLoaderTask.execute(_group.getIdGroup());
   }
}
