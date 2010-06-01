package com.dantasse.whistleweasel;

import android.util.Log;

/**
 * Pokes the view every so often to make it draw with minimal lag.
 */
public class GraphicsUpdaterThread extends Thread {

  // arbitrarily picked; update every 50 ms = no lag maybe?
  private static final long UPDATE_INTERVAL_MS = 50;
  private WWView view;

  public GraphicsUpdaterThread(WWView view) {
    this.view = view;
  }

  @Override
  public void run() {
    while(true) {
      if (view != null && view.getHandler() != null)
        view.getHandler().post(new Runnable() {
          public void run() {
            view.invalidate();
          }
        });
      try {
        Thread.sleep(UPDATE_INTERVAL_MS);
      } catch (InterruptedException e) {
//        Log.d(YelicopterActivity.DEBUG_TAG, "failed at thread.sleeping", e);
      }
    }
  }
}
