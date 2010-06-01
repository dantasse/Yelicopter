package com.dantasse.whistleweasel;

import java.text.NumberFormat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class WWActivity extends Activity {

  public static final String DEBUG_TAG = "WHISTLE_WEASEL";

  // if you get a reading below this, it's a mistake, ignore it
  static final int FREQ_LOWER_LIMIT = 50;
  // if you whistle this frequency, you'll be right at the bottom of the screen.
  static final int FREQ_BOTTOM_OF_SCREEN = 100;
  // if you whistle this frequency, you'll be right at the top of the screen.
  static final int FREQ_TOP_OF_SCREEN = 280;
  // if you get a reading above this, it's a mistake, ignore it
  static final int FREQ_UPPER_LIMIT = 400;

  private TextView textView;
  private RecordingThread recordingThread;
  private WWView wwView;
  private GraphicsUpdaterThread graphicsUpdaterThread;

  private Handler handler;
  private WakeLock wakeLock;

  // how many pineapples the player has collected
  private int score;
  // how far the weasel is from the top of the screen
  // TODO(dantasse) refactor; we shouldn't have to set this here.
  private int weaselTop;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start);
    findViewById(R.id.StartScreen).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startMainScreen();
      }
    });
 
    handler = new Handler();

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
    wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WhistleWeasel");
    wakeLock.acquire();
  }

  @Override
  protected void onPause() {
    super.onPause();
    wakeLock.release();
    finish();
  }

  public void startMainScreen() {
    setContentView(R.layout.main);
    textView = (TextView) findViewById(R.id.TextView01);
    wwView = (WWView) findViewById(R.id.WWView);
    wwView.setActivity(this);
    startRecordingAndGraphicsUpdaterThreads();
  }

  private void startRecordingAndGraphicsUpdaterThreads() {
    graphicsUpdaterThread = new GraphicsUpdaterThread(wwView);
    graphicsUpdaterThread.start();

    recordingThread = new RecordingThread(this);
    recordingThread.start();
  }

  public void scoreAPoint() {
    score++;
  }

  public void setWeaselTop(int newWeaselTop) {
    weaselTop = newWeaselTop;
  }

  public void updateUi() {
    NumberFormat format = NumberFormat.getIntegerInstance();
    format.setMinimumIntegerDigits(5);
    format.setGroupingUsed(false);

    textView.setText("Pineapples: " + score + ", height: " + weaselTop);
    wwView.setTargetHeight(weaselTop);
    wwView.invalidate();
  }

  /** Used to tell the UI thread to do things. */
  public Handler getUiThreadHandler() {
    return handler;
  }
}
