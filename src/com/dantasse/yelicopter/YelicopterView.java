package com.dantasse.yelicopter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class YelicopterView extends View {

  private Paint paint;
  
  /** 
   * The height that the copter will go toward. It doesn't just go there
   * immediately because we want the movement to be smooth.
   */
  private int targetHeight = 0;
  
  /**
   * The height that the copter is currently at.
   */
  private int actualHeight = 0;
  
  public YelicopterView(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint = new Paint();
    paint.setARGB(255, 255, 60, 10);
    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(5.0f);
    paint.setTextSize(50.0f);
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    adjustCopterHeight();
    drawCopter(canvas);
  }
  
  /** Move actualHeight a little closer to targetHeight */
  private void adjustCopterHeight() {
    actualHeight += ((targetHeight - actualHeight) / 10);
  }
  
  private void drawCopter(Canvas canvas) {
    canvas.drawRect(new Rect(50, 800 - actualHeight, 100, 850 - actualHeight),
        paint);
  }

  public void invalidateCopter() {
    invalidate(new Rect(45, 0, 105, 800));
  }

  public void setTargetHeight(int targetHeight) {
    this.targetHeight = targetHeight;
  }
}
