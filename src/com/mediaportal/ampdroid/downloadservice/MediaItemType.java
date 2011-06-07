package com.mediaportal.ampdroid.downloadservice;

public enum MediaItemType {
   File, Music, Video, Pictures;
   
   public static MediaItemType fromInt(int _state) {
      switch (_state) {
      case 0:
         return File;
      case 1:
         return Music;
      case 2:
         return Video;
      case 3:
         return Pictures;
      default:
         return File;
      }
   }

   public static int toInt(MediaItemType _state) {
      switch (_state) {
      case File:
         return 0;
      case Music:
         return 1;
      case Video:
         return 2;
      case Pictures:
         return 3;
      default:
         return 0;
      }
   }
   
   public static String getIntentMimeType(MediaItemType _state) {
      switch (_state) {
      case File:
         return "media/*";
      case Music:
         return "audio/*";
      case Video:
         return "video/*";
      case Pictures:
         return "image/*";
      default:
         return "media/*";
      }
   }
}
