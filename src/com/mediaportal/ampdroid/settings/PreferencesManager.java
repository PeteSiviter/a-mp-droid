package com.mediaportal.ampdroid.settings;

import com.mediaportal.ampdroid.utils.LogUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferencesManager {
   public enum StatusbarAutohide {
      AlwaysShow, AlwaysHide, ShowWhenConnected, ShowManually
   }

   static SharedPreferences Preferences;

   public static void intitialisePreferencesManager(Context _context) {
      Preferences = PreferenceManager.getDefaultSharedPreferences(_context);
   }

   public static boolean isFullscreen() {
      if (Preferences != null) {
         return Preferences.getBoolean("general_fullscreen", true);
      } else {
         Log.w(LogUtils.LOG_CONST, "PreferencesManager not initialised");
         return false;
      }
   }

   public static boolean isVibrating() {
      if (Preferences != null) {
         return Preferences.getBoolean("general_vibrate", false);
      } else {
         Log.w(LogUtils.LOG_CONST, "PreferencesManager not initialised");
         return false;
      }
   }

   public static String getTvClientName() {
      if (Preferences != null) {
         return Preferences.getString("tvserver_clientname", "unknown");
      } else {
         Log.w(LogUtils.LOG_CONST, "PreferencesManager not initialised");
         return "unknown";
      }
   }

   public static StatusbarAutohide getStatusbarAutohide() {
      if (Preferences != null) {
         String value = Preferences.getString("statusbar_autohide", "0");
         if (value.equals("0")) {
            return StatusbarAutohide.AlwaysShow;
         } else if (value.equals("1")) {
            return StatusbarAutohide.AlwaysHide;
         } else if (value.equals("2")) {
            return StatusbarAutohide.ShowWhenConnected;
         } else if (value.equals("3")) {
            return StatusbarAutohide.ShowManually;
         }
      } else {
         Log.w(LogUtils.LOG_CONST, "PreferencesManager not initialised");
      }
      return StatusbarAutohide.AlwaysShow;
   }

   public static boolean connectOnStartup() {
      if (Preferences != null) {
         return Preferences.getBoolean("auto_connect", false);
      } else {
         Log.w(LogUtils.LOG_CONST, "PreferencesManager not initialised");
         return false;
      }
   }

}
