package com.mediaportal.ampdroid.utils;

import android.content.Context;
import android.util.TypedValue;

public class DpiUtils {
   public static int getPxFromDpi(Context _context, int _px){
      int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
            (float) _px, _context.getResources().getDisplayMetrics());
      return value;

   }
}
