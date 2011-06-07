package com.mediaportal.ampdroid.downloadservice;

public enum DownloadItemType {
   VideoShareItem, VideoDatabaseItem, TvSeriesItem, MovieItem, MusicTrackItem, MusicShareItem;

   public static DownloadItemType fromInt(int _state) {
      switch (_state) {
      case 0:
         return VideoShareItem;
      case 1:
         return VideoDatabaseItem;
      case 2:
         return TvSeriesItem;
      case 3:
         return MovieItem;
      case 4:
         return MusicTrackItem;
      case 5:
         return MusicShareItem;
      default:
         return VideoShareItem;
      }
   }

   public static int toInt(DownloadItemType _state) {
      switch (_state) {
      case VideoShareItem:
         return 0;
      case VideoDatabaseItem:
         return 1;
      case TvSeriesItem:
         return 2;
      case MovieItem:
         return 3;
      case MusicTrackItem:
         return 4;
      case MusicShareItem:
         return 5;
      default:
         return 0;
      }
   }

}
