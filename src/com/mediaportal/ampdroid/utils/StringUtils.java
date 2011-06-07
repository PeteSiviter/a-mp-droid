package com.mediaportal.ampdroid.utils;

public class StringUtils {

   public static String createStringArray(String[] _array) {
      if(_array != null){
         String retString = "|";
         for(String s : _array){
            retString += " " + s + " |";
         }
         return retString;
      }
      return "";
   }

}
