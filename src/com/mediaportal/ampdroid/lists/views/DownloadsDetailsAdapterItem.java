package com.mediaportal.ampdroid.lists.views;

import android.view.View;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.downloadservice.DownloadJob;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ViewHolder;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.SubtextViewHolder;

public class DownloadsDetailsAdapterItem  implements ILoadingAdapterItem{
   private DownloadJob mDownload;
   public DownloadsDetailsAdapterItem(DownloadJob _job){
      mDownload = _job;
   }
   
   @Override
   public LazyLoadingImage getImage() {
      return null;
   }

   @Override
   public int getLoadingImageResource() {
      return 0;
   }

   @Override
   public int getDefaultImageResource() {
      return 0;
   }

   @Override
   public int getType() {
      return 0;
   }

   @Override
   public int getXml() {
      return R.layout.listitem_download_details;
   }

   @Override
   public Object getItem() {
      return mDownload;
   }

   @Override
   public ViewHolder createViewHolder(View _view) {
      SubtextViewHolder holder = new SubtextViewHolder();
      holder.text = (TextView) _view.findViewById(R.id.TextViewState);
      holder.subtext = (TextView) _view.findViewById(R.id.TextViewProgress);
      holder.title = (TextView) _view.findViewById(R.id.TextViewName);
      return holder;
   }

   @Override
   public void fillViewFromViewHolder(ViewHolder _holder) {
      SubtextViewHolder holder = (SubtextViewHolder) _holder;
      holder.text.setText(mDownload.getState().toString());
      holder.subtext.setText(String.valueOf(mDownload.getProgress()));
      holder.title.setText(mDownload.getDisplayName());
      
   }

   @Override
   public String getSection() {
      return null;
   }

}
