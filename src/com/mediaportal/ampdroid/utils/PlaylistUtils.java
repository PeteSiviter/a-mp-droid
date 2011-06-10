package com.mediaportal.ampdroid.utils;

import java.io.File;

import com.mediaportal.ampdroid.lists.Utils;

public class PlaylistUtils {

   public static String createM3UPlaylistFromFolder(File _folder, String[] _extension) {
      String retString = null;
      if(_folder.isDirectory()){
         File[] files = _folder.listFiles();
         StringBuilder builder = new StringBuilder();
         
         for(File f : files){
            if(f.isFile()){
               if(StringUtils.containedInArray(Utils.getExtension(f.getName()), _extension)){
                  builder.append(f.getName());
                  builder.append("\n");
               }
            }
         }
         retString = builder.toString();
      }
      return retString;
   }

}
