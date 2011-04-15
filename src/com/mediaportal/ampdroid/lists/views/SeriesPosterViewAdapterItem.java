package com.mediaportal.ampdroid.lists.views;

import java.io.File;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.data.Series;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ViewHolder;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.SubtextViewHolder;
import com.mediaportal.ampdroid.lists.Utils;

public class SeriesPosterViewAdapterItem implements ILoadingAdapterItem {
   private Series mSeries;
   private LazyLoadingImage mImage;
   public SeriesPosterViewAdapterItem(Series _series) {
      super();
      this.mSeries = _series;
      
      String fileName = Utils.getFileNameWithExtension(mSeries.getCurrentPosterUrl(), "\\");
      String cacheName =   "Series" + File.separator + mSeries.getId() + File.separator + "Poster" + File.separator + fileName;

      mImage = new LazyLoadingImage(mSeries.getCurrentPosterUrl(), cacheName, 75, 100);
   }

   @Override
   public LazyLoadingImage getImage() {
      return mImage;
   }
   
   @Override
   public int getType() {
      return ViewTypes.PosterView.ordinal();
   }

   @Override
   public int getXml() {
      return R.layout.listitem_poster;
   }
   
   @Override
   public Object getItem() {
      return mSeries;
   }

   @Override
   public ViewHolder createViewHolder(View _view) {
      SubtextViewHolder holder = new SubtextViewHolder();
      holder.text = (TextView) _view.findViewById(R.id.TextViewText);
      holder.image = (ImageView) _view.findViewById(R.id.ImageViewEventImage);
      holder.subtext = (TextView) _view.findViewById(R.id.TextViewSubtext);
      holder.title = (TextView) _view.findViewById(R.id.TextViewTitle);
      return holder;
   }

   @Override
   public void fillViewFromViewHolder(ViewHolder _holder) {
      SubtextViewHolder holder = (SubtextViewHolder)_holder;
      if (holder.title != null) {
         holder.title.setText(mSeries.getPrettyName());// + " (" + "2010" + ")");
      }

      if (holder.text != null) {
         holder.text.setText(mSeries.getGenreString());
      }

      if (holder.subtext != null) {
         holder.subtext.setText(mSeries.getGenreString());
      }
   }

   @Override
   public int getLoadingImageResource() {
      return R.drawable.listview_imageloading_poster_2;
   }
   
   @Override
   public int getDefaultImageResource() {
      return R.drawable.listview_imageloading_poster_2;
   }



}
