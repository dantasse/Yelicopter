package com.dantasse.yelicopter;

import java.util.ArrayList;

import com.dantasse.yelicopter.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class YelicopterView extends View {

  // the dimensions of the weasel picture, as saved in the file.
  private static final int WEASEL_FILE_HEIGHT = 45;
  private static final int WEASEL_FILE_WIDTH = 55;

  // the dimensions of the weasel, as displayed on screen.
  private static final int WEASEL_HEIGHT = 2 * WEASEL_FILE_HEIGHT;
  private static final int WEASEL_WIDTH = 2 * WEASEL_FILE_WIDTH;
  
  // how many pixels per redraw the clouds will move.
  private static final int CLOUD_SPEED = 3;
  
  // odds that you'll get a new cloud on any given frame.
  private static final double CLOUD_PROBABILITY = .03;
  private Paint redPaint;
  private Paint greenPaint;

  /** 
   * The height that the copter will go toward. It doesn't just go there
   * immediately because we want the movement to be smooth.
   */
  private int targetHeight = 0;

  /**
   * The most recent frequency reading.
   */
  private int actualHeight = 0;

  /**
   * Our hero.
   */
  private Drawable weasel;

  /**
   * The angle that the weasel will be displayed at (tilted up if he just went
   * up, tilted down if he just went down).
   */
  private float rotation = 0f;

  /**
   * These are harmless screen decorations.
   */
  private ArrayList<Rect> clouds = new ArrayList<Rect>();
  
  public YelicopterView(Context context, AttributeSet attrs) {
    super(context, attrs);
    redPaint = new Paint();
    redPaint.setARGB(255, 255, 60, 10);
    redPaint.setStyle(Style.STROKE);
    redPaint.setStrokeWidth(5.0f);
    redPaint.setTextSize(50.0f);
    greenPaint = new Paint();
    greenPaint.setARGB(255, 10, 255, 10);
    greenPaint.setStyle(Style.STROKE);
    greenPaint.setStrokeWidth(5.0f);
    greenPaint.setTextSize(50.0f);
    weasel = context.getResources().getDrawable(R.drawable.gliding_weasel);
    actualHeight = getHeight() / 2;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    long drawStart = System.currentTimeMillis();
    canvas.drawColor(Color.BLUE, Mode.LIGHTEN);
    adjustCopterHeight();
    drawWeasel(canvas);
    adjustClouds();
    drawClouds(canvas);
    Log.d(YelicopterActivity.DEBUG_TAG, 
        "Draw time: " + (System.currentTimeMillis() - drawStart));
  }

  /** Move actualHeight a little closer to targetHeight */
  private void adjustCopterHeight() {
    float difference = (targetHeight - actualHeight) / 10.0f;
    
    // biggest nosedive = 90 degrees. biggest upward tilt = -90 degrees.    
    rotation = - difference * 90 / ((YelicopterActivity.FREQ_TOP_OF_SCREEN -
        YelicopterActivity.FREQ_BOTTOM_OF_SCREEN) / 20);
    actualHeight += difference;
  }

  private void drawWeasel(Canvas canvas) {
    // scale it to the screen size
    int frequencyRange = YelicopterActivity.FREQ_TOP_OF_SCREEN -
        YelicopterActivity.FREQ_BOTTOM_OF_SCREEN;
    int heightToDraw = (actualHeight - YelicopterActivity.FREQ_BOTTOM_OF_SCREEN)
        * (canvas.getHeight() - WEASEL_HEIGHT) / frequencyRange;

    weasel.setBounds(50 /*left*/,
        canvas.getHeight() - heightToDraw - WEASEL_HEIGHT /*top*/,
        50 + WEASEL_WIDTH /*right*/,
        canvas.getHeight() - heightToDraw /*bottom*/);

    // Rotate the weasel by rotating the canvas.  Save and restore the canvas
    // before and after so you don't rotate anything else.
    canvas.save();
    Matrix matrix = canvas.getMatrix();
    matrix.setRotate(rotation, weasel.getBounds().centerX(),
        weasel.getBounds().centerY());
    canvas.setMatrix(matrix);
    weasel.draw(canvas);
    canvas.restore();
  }
  
  private void adjustClouds() {
    // maybe make a new cloud
    if (Math.random() < CLOUD_PROBABILITY) {
      int top = (int) (Math.random() * getHeight());
      clouds.add(new Rect(getWidth(), top, getWidth() + 50, top + 50));
    }
    // move each cloud, and delete any clouds that are off the screen.
    ArrayList<Rect> cloudsToRemove = new ArrayList<Rect>();
    for(Rect cloud : clouds) {
      cloud.left -= CLOUD_SPEED;
      cloud.right -= CLOUD_SPEED;
      if (cloud.right < getLeft()) {
        cloudsToRemove.add(cloud);
      }
    }
    clouds.removeAll(cloudsToRemove);
  }
  
  private void drawClouds(Canvas canvas) {
    for(Rect r : clouds) {
      canvas.drawRect(r, greenPaint);
    }
  }

  public void invalidateWeasel() {
    // Oh hell just redraw the whole screen, I don't think it takes very long.
    invalidate();
  }

  public void setTargetHeight(int targetHeight) {
    this.targetHeight = targetHeight;
  }
}
