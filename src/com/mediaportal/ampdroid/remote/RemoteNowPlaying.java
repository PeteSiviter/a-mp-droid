/*******************************************************************************
 * Copyright (c) 2011 Benjamin Gmeiner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Benjamin Gmeiner - Project Owner
 ******************************************************************************/
package com.mediaportal.ampdroid.remote;

import org.codehaus.jackson.annotate.JsonProperty;

public class RemoteNowPlaying {
   private int mDuration;
   private String mFile;
   private int mPosition;
   private boolean mIsTv;

   @JsonProperty("Duration")
   public int getDuration() {
      return mDuration;
   }

   @JsonProperty("Duration")
   public void setDuration(int duration) {
      mDuration = duration;
   }

   @JsonProperty("File")
   public String getFile() {
      return mFile;
   }

   @JsonProperty("File")
   public void setFile(String file) {
      mFile = file;
   }

   @JsonProperty("Position")
   public int getPosition() {
      return mPosition;
   }

   @JsonProperty("Position")
   public void setPosition(int position) {
      mPosition = position;
   }
   
   @JsonProperty("IsTv")
   public void setIsTv(boolean isTv) {
      mIsTv = isTv;
   }

   @JsonProperty("IsTv")
   public boolean isTv() {
      return mIsTv;
   }

}
