package com.dantasse.yelicopter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

public class StartScreen extends View implements OnClickListener {

  private Paint paint;
  private YelicopterActivity activity;
  
  /** This is the constructor that the xml layout file calls, I think. */
  public StartScreen(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint = new Paint();
    paint.setColor(Color.GREEN);
    paint.setStrokeWidth(2f);
    paint.setTextSize(25f);
    this.setOnClickListener(this);
  }
  
  /** 
   * You need to tell this view about the main activity so it can show another
   * view when you want it to.
   */
  public void setMainActivity(YelicopterActivity activity) {
    this.activity = activity;
  }

  public void onDraw(Canvas canvas) {
    canvas.drawText("Whistle to steer the Yelicopter up and down.", 50f, 100f, paint);
    canvas.drawText("Click to start.", 50f, 200f, paint);
  }

  public void onClick(View v) {
    activity.startMainScreen();
  }
}
