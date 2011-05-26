package com.mediaportal.ampdroid.lists.views;

import java.io.File;

import android.view.View;

import com.mediaportal.ampdroid.R;
import com.mediaportal.ampdroid.lists.ILoadingAdapterItem;
import com.mediaportal.ampdroid.lists.LazyLoadingImage;
import com.mediaportal.ampdroid.lists.LazyLoadingAdapter.ViewHolder;
import com.mediaportal.ampdroid.lists.Utils;

public class PosterGalleryViewAdapterItem implements ILoadingAdapterItem {
   private LazyLoadingImage mImage;
   private String mPosterUrl;
   public PosterGalleryViewAdapterItem(String _posterUrl){
      mPosterUrl = _posterUrl;
      
      String fileName = Utils.getFileNameWithExtension(mPosterUrl, "\\");
      String cacheName =   "MoviePosters" + File.separator + fileName;
      
      
      mImage = new LazyLoadingImage(mPosterUrl, cacheName, 100, 150);
   }

   @Override
   public LazyLoadingImage getImage() {
      return mImage;
   }
   
   @Override
   public Object getItem() {
      return mPosterUrl;
   }

   @Override
   public int getType() {
      return 0;
   }

   @Override
   public int getXml() {
      return 0;
   }
   @Override
   public ViewHolder createViewHolder(View _view) {
      // TODO Auto-generated method stub
      return null;
   }
   @Override
   public void fillViewFromViewHolder(ViewHolder _holder) {
      // TODO Auto-generated method stub
      
   }
   @Override
   public int getLoadingImageResource() {
      return R.drawable.listview_imageloading_poster;
   }
   @Override
   public int getDefaultImageResource() {
      return R.drawable.listview_imageloading_poster;
   }

   @Override
   public String getSection() {
      return null;
   }
}
