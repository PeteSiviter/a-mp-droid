package com.mediaportal.ampdroid.lists;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediaportal.ampdroid.lists.views.LoadingAdapterItem;

public class LazyLoadingAdapter extends BaseAdapter {
   public interface ILoadingListener {
      void EndOfListReached();
   }

   private Activity mActivity;
   private ArrayList<ILoadingAdapterItem> mCurrentViewData;
   private HashMap<Integer, ArrayList<ILoadingAdapterItem>> mViews;
   private static LayoutInflater mInflater = null;
   private boolean mShowLoadingItem;
   public ImageHandler mImageLoader;
   private ILoadingAdapterItem mIoadingIndicator;
   private ILoadingListener mLoadingListener;

   public void setLoadingListener(ILoadingListener _listener) {
      mLoadingListener = _listener;
   }

   public LazyLoadingAdapter(Activity _activity) {
      mActivity = _activity;
      mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      mImageLoader = new ImageHandler(mActivity.getApplicationContext());
      mIoadingIndicator = new LoadingAdapterItem("");
      mViews = new HashMap<Integer, ArrayList<ILoadingAdapterItem>>();
      mCurrentViewData = new ArrayList<ILoadingAdapterItem>();
   }

   public LazyLoadingAdapter(Activity _activity, ArrayList<ILoadingAdapterItem> _items) {
      this(_activity);
      mCurrentViewData = _items;
   }

   public void addItem(ILoadingAdapterItem _item) {
      mCurrentViewData.add(_item);
   }

   public boolean addItem(int _viewId, ILoadingAdapterItem _item) {
      if (mViews.containsKey(_viewId)) {
         mViews.get(_viewId).add(_item);
         return true;
      } else {
         return false;
      }
   }

   public boolean setView(int _viewId) {
      if (mViews.containsKey(_viewId)) {
         mCurrentViewData = mViews.get(_viewId);
         return true;
      } else {
         return false;
      }
   }

   public boolean addView(int _viewId) {
      if (!mViews.containsKey(_viewId)) {
         mViews.put(_viewId, new ArrayList<ILoadingAdapterItem>());
         return true;
      } else {
         return false;
      }
   }

   public int getCount() {
      return (mShowLoadingItem ? mCurrentViewData.size() + 1 : mCurrentViewData.size());
   }

   public Object getItem(int position) {
      if (position == mCurrentViewData.size()) {
         return mIoadingIndicator;
      } else {
         return mCurrentViewData.get(position);
      }

   }

   public long getItemId(int position) {
      return position;
   }

   public static class ViewHolder {
      public TextView text;
      public ImageView image;
   }

   @Override
   public int getItemViewType(int position) {
      if (position == mCurrentViewData.size()) {
         return mIoadingIndicator.getType();
      } else {
         return mCurrentViewData.get(position).getType();
      }
   }

   @Override
   public int getViewTypeCount() {
      return 10;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      View vi = convertView;
      ViewHolder holder = null;
      if (mCurrentViewData != null) {
         ILoadingAdapterItem item = null;
         if (position >= mCurrentViewData.size()) {
            item = mIoadingIndicator;
            if (mLoadingListener != null) {
               mLoadingListener.EndOfListReached();
            }
         } else {
            item = mCurrentViewData.get(position);
         }

         if (convertView == null) {
            try {
               vi = mInflater.inflate(item.getXml(), null);
               holder = item.createViewHolder(vi);
               vi.setTag(holder);
            } catch (Exception ex) {
               Log.d("Adapter", ex.getMessage());
            }
         } else {
            holder = (SubtextViewHolder) vi.getTag();
         }

         if (holder != null) {
            item.fillViewFromViewHolder(holder);

            if (holder.image != null) {
               LazyLoadingImage image = item.getImage();
               int defaultImage = item.getDefaultImageResource();
               int loadingImage = item.getLoadingImageResource();

               if (image != null && !image.equals("")) {
                  holder.image.setTag(image.getImageUrl());
                  mImageLoader.DisplayImage(image, loadingImage, mActivity, holder.image);
               } else {
                  if (defaultImage == 0) {// show nothing as default image
                     holder.image.setImageBitmap(null);
                  } else {
                     holder.image.setImageResource(defaultImage);
                  }
               }
            }
         }
      }

      return vi;
   }

   public void showLoadingItem(boolean showLoadingItem) {
      this.mShowLoadingItem = showLoadingItem;
   }

   public boolean isLoadingItemShown() {
      return mShowLoadingItem;
   }

   public void setLoadingText(String _text) {
      setLoadingText(_text, true);
   }

   public void setLoadingText(String _text, boolean _loading) {
      ((LoadingAdapterItem) mIoadingIndicator).setLoadingText(_text);
      ((LoadingAdapterItem) mIoadingIndicator).setLoadingVisible(_loading);
   }

   public void clear() {
      mCurrentViewData.clear();
   }

   public void removeItem(ILoadingAdapterItem _item) {
      mCurrentViewData.remove(_item);
   }
}
