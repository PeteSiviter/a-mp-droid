package com.mediaportal.ampdroid.downloadservice;

import com.mediaportal.ampdroid.R;

import android.content.Context;

public enum DownloadState {
   Queued, Running, Paused, Stopped, Finished, Error;

   public String toStringLocalised(Context _context) {
      switch (this) {
      case Queued:
         return _context.getString(R.string.downloads_state_queued);
      case Running:
         return _context.getString(R.string.downloads_state_running);
      case Paused:
         return _context.getString(R.string.downloads_state_paused);
      case Stopped:
         return _context.getString(R.string.downloads_state_stopped);
      case Finished:
         return _context.getString(R.string.downloads_state_finished);
      case Error:
         return _context.getString(R.string.downloads_state_failed);
      default:
         return _context.getString(R.string.downloads_state_queued);
      }
   }

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
