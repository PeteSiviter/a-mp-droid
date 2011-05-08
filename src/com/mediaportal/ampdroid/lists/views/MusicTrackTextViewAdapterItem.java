package com.mediaportal.ampdroid.lists.views;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.data.MusicTrack;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ViewHolder;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.SubtextViewHolder;

public class MusicTrackTextViewAdapterItem implements ILoadingAdapterItem {
   private MusicTrack mTracks;
   private boolean mShowArtist;
   public MusicTrackTextViewAdapterItem(MusicTrack _track, boolean _showArtist) {
      super();
      mTracks = _track;
      mShowArtist = _showArtist;
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
      return mTracks;
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
         String artistString = "";
         if(mShowArtist && mTracks.getArtists() != null){
            if(mTracks.getArtists().length == 0){
               artistString = " - ...";
            }
            else if(mTracks.getArtists().length == 1)
            {
               artistString = " - " + mTracks.getArtists()[0];
            }
            else{
               artistString = " - " + mTracks.getArtists()[0] + ", ...";
            }
         }
         holder.text.setText(mTracks.getTitle() + artistString);
         holder.text.setTextColor(Color.WHITE);
      }
   }

}
