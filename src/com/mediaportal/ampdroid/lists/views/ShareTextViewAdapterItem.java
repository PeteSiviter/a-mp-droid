package com.mediaportal.ampdroid.lists.views;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.data.VideoShare;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ViewHolder;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.SubtextViewHolder;

public class ShareTextViewAdapterItem implements ILoadingAdapterItem {
   private VideoShare mShare;
   public ShareTextViewAdapterItem(VideoShare _share) {
      super();
      this.mShare = _share;
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
      return R.layout.listitem_text;
   }

   @Override
   public Object getItem() {
      return mShare;
   }

   @Override
   public ViewHolder createViewHolder(View _view) {
      SubtextViewHolder holder = new SubtextViewHolder();
      holder.text = (TextView) _view.findViewById(R.id.TextViewText);
      return holder;
   }

   @Override
   public void fillViewFromViewHolder(ViewHolder _holder) {
      SubtextViewHolder holder = (SubtextViewHolder)_holder;

      if (holder.text != null) {
         holder.text.setText(mShare.getName());
         holder.text.setTextColor(Color.WHITE);
      }
   }

}
