package com.dantasse.yelicopter;

import java.text.NumberFormat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class YelicopterActivity extends Activity implements OnClickListener {

  public static final String DEBUG_TAG = "YELICOPTER";
  
  private TextView textView;
  private Button button1;
  private RecordingThread recordingThread;
  private YelicopterView yelicopterView;
  private GraphicsUpdaterThread graphicsUpdaterThread;
  
  private Handler handler;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    textView = (TextView) findViewById(R.id.TextView01);
    button1 = (Button) findViewById(R.id.Button01);
    
    yelicopterView = (YelicopterView) findViewById(R.id.YelicopterView);
    handler = new Handler();
    button1.setOnClickListener(this);
  }
  
  private void startRecordingThread() {
    graphicsUpdaterThread = new GraphicsUpdaterThread(yelicopterView);
    graphicsUpdaterThread.start();
    
    recordingThread = new RecordingThread(this, handler);
    recordingThread.start();
  }
  
  public void updateUi(int newTargetHeight) {
    NumberFormat format = NumberFormat.getIntegerInstance();
    format.setMinimumIntegerDigits(5);
    format.setGroupingUsed(false);
    
    textView.setText("frequency: " + format.format(newTargetHeight));
    yelicopterView.setTargetHeight(newTargetHeight);
    yelicopterView.invalidateCopter();
  }
  public void onClick(View v) {
    if (v.equals(button1)) {
      startRecordingThread();
    }
  }
}
