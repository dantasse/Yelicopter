package com.dantasse.yelicopter;

import java.text.NumberFormat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class YelicopterActivity extends Activity {

  public static final String DEBUG_TAG = "YELICOPTER";
  
  // if you get a reading below this, it's a mistake, ignore it
  static final int FREQ_LOWER_LIMIT = 100;
  // if you whistle this frequency, you'll be right at the bottom of the screen.
  static final int FREQ_BOTTOM_OF_SCREEN = 200;
  // if you whistle this frequency, you'll be right at the top of the screen.
  static final int FREQ_TOP_OF_SCREEN = 700;
  // if you get a reading above this, it's a mistake, ignore it
  static final int FREQ_UPPER_LIMIT = 1000;

  private TextView textView;
  private RecordingThread recordingThread;
  private StartScreen startScreen;
  private YelicopterView yelicopterView;
  private GraphicsUpdaterThread graphicsUpdaterThread;

  private Handler handler;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start);
    startScreen = (StartScreen) findViewById(R.id.StartScreen01);
    startScreen.setMainActivity(this);
    
    handler = new Handler();
  }

  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }

  @Override
  protected void onStop() {
    super.onStop();
    finish();
  }

  public void startMainScreen() {
    setContentView(R.layout.main);
    textView = (TextView) findViewById(R.id.TextView01);
    yelicopterView = (YelicopterView) findViewById(R.id.YelicopterView);
    startRecordingAndGraphicsUpdaterThreads();
  }

  private void startRecordingAndGraphicsUpdaterThreads() {
    graphicsUpdaterThread = new GraphicsUpdaterThread(yelicopterView);
    graphicsUpdaterThread.start();

    recordingThread = new RecordingThread(this);
    recordingThread.start();
  }

  public void updateUi(int newTargetHeight) {
    NumberFormat format = NumberFormat.getIntegerInstance();
    format.setMinimumIntegerDigits(5);
    format.setGroupingUsed(false);

    textView.setText("frequency: " + format.format(newTargetHeight));
    yelicopterView.setTargetHeight(newTargetHeight);
    yelicopterView.invalidateWeasel();
  }

  /** Used to tell the UI thread to do things. */
  public Handler getUiThreadHandler() {
    return handler;
  }
}
