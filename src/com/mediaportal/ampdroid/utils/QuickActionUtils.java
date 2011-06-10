package com.mediaportal.ampdroid.utils;

import android.content.Context;
import android.view.View.OnClickListener;

import com.mediaportal.ampdroid.quickactions.ActionItem;
import com.mediaportal.ampdroid.quickactions.QuickAction;

public class QuickActionUtils {

   public static void createQuickAction(Context _context, QuickAction _parent, int _titleStringId, int _iconResId,
         OnClickListener _onClickListener) {
      ActionItem cancelDownloadAction = new ActionItem();
      cancelDownloadAction.setTitle(_context.getString(_titleStringId));
      cancelDownloadAction.setIcon(_context.getResources().getDrawable(_iconResId));
      cancelDownloadAction.setOnClickListener(_onClickListener);
      _parent.addActionItem(cancelDownloadAction);
   }

}
