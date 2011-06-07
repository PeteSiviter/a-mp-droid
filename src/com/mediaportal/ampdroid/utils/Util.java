package com.mediaportal.ampdroid.utils;

import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

import com.mediaportal.ampdroid.settings.PreferencesManager;

public class Util {
   public static void Vibrate(Context _context, int _time) {
      if(PreferencesManager.isVibrating()){
         Vibrator v = (Vibrator) _context.getSystemService(Context.VIBRATOR_SERVICE);
         v.vibrate(_time);
      }
   }

   public static Toast toast;
   private static String lastToastText;

   public static void showToast(Context _context, String _text) {
      if (toast != null) {
         toast.cancel();
      }
      if (_text != null && _text.equals(lastToastText)) {
         toast.show();
      } else {
         lastToastText = _text;
         toast = Toast.makeText(_context, _text, Toast.LENGTH_SHORT);
         toast.show();
      }
   }
   
   public static boolean compare(Object... compare) {
      for (Object o : compare) {
         if (!compare[0].equals(o)) {
            return false;
         }
      }
      return true;
   }
}
