package com.mediaportal.ampdroid.downloadservice;

import java.util.ArrayList;
import java.util.List;

public class DownloadGroup {
   private int mGroupId;
   private List<DownloadJob> mGroupItems;
   private int mPercentageDone;
   private boolean mContentChanged;
   private long mGroupSize;
   private long mTotalDone;

   public DownloadGroup(int _groupId) {
      mGroupId = _groupId;
      mGroupItems = new ArrayList<DownloadJob>();
   }

   public void setGroupId(int groupId) {
      mGroupId = groupId;
   }

   public int getGroupId() {
      return mGroupId;
   }

   public void setPercentageDone(int _percentage) {
      mPercentageDone = _percentage;
   }

   public int getPercentageDone() {
      return mPercentageDone;
   }

   public void addItem(DownloadJob job) {
      mGroupItems.add(job);
      mContentChanged = true;
   }

   public long getGroupContentSize() {
      if (mContentChanged) {
         mGroupSize = 0;
         for (DownloadJob j : mGroupItems) {
            mGroupSize += j.getLength();
         }
         return mGroupSize;
      }
      else{
         return mGroupSize;
      }
   }

   public void addRead(int _read) {
      mTotalDone += _read;
   }

   public long getTotalDone() {
      return mTotalDone;
   }
}
