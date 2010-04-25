package com.dantasse.yelicopter;

import android.util.Log;

/**
 * Pokes the view every so often to make it draw with minimal lag.
 */
public class GraphicsUpdaterThread extends Thread {

  // arbitrarily picked; update every 50 ms = no lag maybe?
  private static final long UPDATE_INTERVAL_MS = 50;
  private YelicopterView view;
  
  public GraphicsUpdaterThread(YelicopterView view) {
    this.view = view;
  }
  
  @Override
  public void run() {
    while(true) {
      view.getHandler().post(new Runnable() {
        public void run() {
          view.invalidateCopter();
          
        }
      });
      try {
        Thread.sleep(UPDATE_INTERVAL_MS);
      } catch (InterruptedException e) {
        Log.d(YelicopterActivity.DEBUG_TAG, "failed at thread.sleeping", e);
      }
    }
  }
}
