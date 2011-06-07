package com.mediaportal.ampdroid.downloadservice;

public enum DownloadState {
   Queued, Running, Paused, Stopped, Finished, Error;

   public static DownloadState fromInt(int _state) {
      switch (_state) {
      case 0:
         return Queued;
      case 1:
         return Running;
      case 2:
         return Paused;
      case 3:
         return Stopped;
      case 4:
         return Finished;
      case 5:
         return Error;
      default:
         return Queued;
      }
   }

   public static int toInt(DownloadState _state) {
      switch (_state) {
      case Queued:
         return 0;
      case Running:
         return 1;
      case Paused:
         return 2;
      case Stopped:
         return 3;
      case Finished:
         return 4;
      case Error:
         return 5;
      default:
         return 0;
      }
   }

}
