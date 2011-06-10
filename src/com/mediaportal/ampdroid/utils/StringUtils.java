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

   public static boolean containedInArray(String _string, String[] _array) {
      if(_array == null)return false;
      for(String s : _array){
         if(s != null && s.equals(_string)){
            return true;
         }
      }
      return false;
   }

}
