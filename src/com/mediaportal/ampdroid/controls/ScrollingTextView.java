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
package com.mediaportal.ampdroid.controls;

import android.content.Context;

import android.graphics.Rect;

import android.util.AttributeSet;

import android.widget.TextView;

public class ScrollingTextView extends TextView {

   public ScrollingTextView(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   public ScrollingTextView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public ScrollingTextView(Context context) {
      super(context);
   }

   @Override
   protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
      if (focused)
         super.onFocusChanged(focused, direction, previouslyFocusedRect);
   }

   @Override
   public void onWindowFocusChanged(boolean focused) {
      if (focused)
         super.onWindowFocusChanged(focused);
   }

   @Override
   public boolean isFocused() {
      return true;
   }

}
