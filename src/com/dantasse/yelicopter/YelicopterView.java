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
    // height used to be between 100 and 400; scale it to the screen size.
    int range = YelicopterActivity.FREQ_TOP_OF_SCREEN - YelicopterActivity.FREQ_BOTTOM_OF_SCREEN;
    int heightToDraw = (actualHeight - YelicopterActivity.FREQ_BOTTOM_OF_SCREEN) * canvas.getHeight() / range;
    canvas.drawRect(new Rect(50, canvas.getHeight() - heightToDraw - 50, 100,
        canvas.getHeight() - heightToDraw),
        paint);
  }

  public void invalidateCopter() {
    invalidate(new Rect(45, this.getTop(), 105, this.getBottom()));
  }

  public void setTargetHeight(int targetHeight) {
    this.targetHeight = targetHeight;
  }
}
