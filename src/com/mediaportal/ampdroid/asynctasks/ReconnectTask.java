package com.mediaportal.ampdroid.asynctasks;

import android.os.AsyncTask;

import com.mediaportal.ampdroid.api.DataHandler;

public class ReconnectTask extends AsyncTask<DataHandler, Void, Void> {
   private boolean mTryReconnecting;

   @Override
   protected Void doInBackground(DataHandler... _service) {
      mTryReconnecting = true;
      while(mTryReconnecting){
         if(_service[0].connectClientControl()){
            return null;
         }
         else{
            try {
               Thread.sleep(5000);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }
      return null;
   }
   
   public void cancelReConnect(){
      mTryReconnecting = false;
   }

}
