package com.dantasse.whistleweasel;

import java.util.ArrayList;

import com.dantasse.whistleweasel.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WWView extends View {

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

  // how many pixels per redraw the pineapples will move.
  private static final int PINEAPPLE_SPEED = 5;
  
  // odds that you'll get a new pineapple on any given frame.
  private static final double PINEAPPLE_PROBABILITY = .03;

  /** 
   * The height that the copter will go toward. It doesn't just go there
   * immediately because we want the movement to be smooth.
   */
  private int targetHeight = 0;

  /**
   * The most recent frequency reading.
   */
  private int actualHeight = 0;

  /** Our hero. */
  private Drawable weasel;

  /**
   * The angle that the weasel will be displayed at (tilted up if he just went
   * up, tilted down if he just went down).
   */
//  private float rotation = 0f;

  /** Harmless screen decorations. */
  private ArrayList<Drawable> clouds = new ArrayList<Drawable>();

  /** These are tasty!  Get them! */
  private ArrayList<Drawable> pineapples = new ArrayList<Drawable>();
  
  Context context;
  
  /** 
   * We hold a reference back to the activity to set the score.
   * TODO(dantasse) refactor this mess.
   */
  private WWActivity activity;
  
  public WWView(Context context, AttributeSet attrs) {
    super(context, attrs);
    weasel = context.getResources().getDrawable(R.drawable.gliding_weasel);
    actualHeight = getHeight() / 2;
    this.context = context;
  }
  
  public void setActivity(WWActivity activity) {
    this.activity = activity;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    long drawStart = System.currentTimeMillis();
    adjustWeaselHeight();
    adjustClouds();
    adjustPineapples();
    // draw in this order so weasel is on top, then pineapples, then clouds.
    drawClouds(canvas);
    drawPineapples(canvas);
    drawWeasel(canvas);
//    Log.d(YelicopterActivity.DEBUG_TAG, 
//        "Draw time: " + (System.currentTimeMillis() - drawStart));
  }

  /** Move actualHeight a little closer to targetHeight */
  private void adjustWeaselHeight() {
    float difference = (targetHeight - actualHeight) / 10.0f;
    
    // biggest nosedive = 90 degrees. biggest upward tilt = -90 degrees.    
//    rotation = - difference * 90 / ((YelicopterActivity.FREQ_TOP_OF_SCREEN -
//        YelicopterActivity.FREQ_BOTTOM_OF_SCREEN) / 20);
    actualHeight += difference;
  }

  private void drawWeasel(Canvas canvas) {
    // scale it to the screen size
    int frequencyRange = WWActivity.FREQ_TOP_OF_SCREEN -
        WWActivity.FREQ_BOTTOM_OF_SCREEN;
    int heightToDraw = (actualHeight - WWActivity.FREQ_BOTTOM_OF_SCREEN)
        * (canvas.getHeight() - WEASEL_HEIGHT) / frequencyRange;

    weasel.setBounds(50 /*left*/,
        canvas.getHeight() - heightToDraw - WEASEL_HEIGHT /*top*/,
        50 + WEASEL_WIDTH /*right*/,
        canvas.getHeight() - heightToDraw /*bottom*/);

    // Rotate the weasel by rotating the canvas.  Save and restore the canvas
    // before and after so you don't rotate anything else.
    // TODO(dantasse) this is commented out because, afterward, you can't rely
    // on the weasel's height; he's in a different coordinate plane.  fix this.
//    canvas.save();
//    Matrix matrix = canvas.getMatrix();
//    matrix.setRotate(rotation, weasel.getBounds().centerX(),
//        weasel.getBounds().centerY());
//    canvas.setMatrix(matrix);
    
    weasel.draw(canvas);
//    canvas.restore();
  }
  
  private void adjustClouds() {
    // maybe make a new cloud
    if (Math.random() < CLOUD_PROBABILITY) {
      int top = (int) (Math.random() * getHeight());
      Drawable newCloud = context.getResources().getDrawable(R.drawable.cloud);
      newCloud.setBounds(getRight() /*left*/,
          top /*top*/,
          getRight() + newCloud.getMinimumWidth(),
          top + newCloud.getMinimumHeight());
      clouds.add(newCloud);
    }
    // move each cloud, and delete any clouds that are off the screen.
    ArrayList<Drawable> cloudsToRemove = new ArrayList<Drawable>();
    for(Drawable cloud : clouds) {
      cloud.setBounds(cloud.getBounds().left - CLOUD_SPEED,
          cloud.getBounds().top,
          cloud.getBounds().right - CLOUD_SPEED,
          cloud.getBounds().bottom);
      if (cloud.getBounds().right < getLeft()) {
        cloudsToRemove.add(cloud);
      }
    }
    clouds.removeAll(cloudsToRemove);
  }
  
  private void drawClouds(Canvas canvas) {
    for(Drawable cloud : clouds) {
      cloud.draw(canvas);
    }
  }
  
  private void adjustPineapples() {
    // maybe make a new pineapple
    if (Math.random() < PINEAPPLE_PROBABILITY) {
      int top = (int) (Math.random() * getHeight());
      
      // make a pineapple.  This shares state with all other pineapple images
      // (so if I edited the base pineapple image they would all change) because
      // I didn't call Drawable.mutate().
      Drawable newPineapple = context.getResources().getDrawable(R.drawable.pineapple);
      newPineapple.setBounds(getRight() /*left*/,
          top /*top*/,
          getRight() + newPineapple.getMinimumWidth() /*right*/,
          top + newPineapple.getMinimumHeight() /*bottom*/);
      pineapples.add(newPineapple);
    }
    // Move each pineapple. If any collide with the weasel, mark them for
    // eating.  If any are off the screen, mark them for deletion.
    ArrayList<Drawable> pineapplesToRemove = new ArrayList<Drawable>();
    ArrayList<Drawable> pineapplesEaten = new ArrayList<Drawable>();
    for(Drawable pineapple : pineapples) {
      Rect old = pineapple.getBounds();
      pineapple.setBounds(old.left - PINEAPPLE_SPEED, old.top,
          old.right - PINEAPPLE_SPEED, old.bottom);
      if (old.right < getLeft()) {
        pineapplesToRemove.add(pineapple);
      } else if (old.left < weasel.getBounds().right - 7) {
        if (old.top > (weasel.getBounds().top - old.height()) && 
            old.top < weasel.getBounds().bottom) {
          pineapplesEaten.add(pineapple);
          activity.scoreAPoint();
          activity.updateUi();
        }
      }
    }
    
    for(Drawable pineapple : pineapplesEaten) {
      pineapplesToRemove.add(pineapple);
    }
    // Delete any pineapples that are off the screen.
    pineapples.removeAll(pineapplesToRemove);
  }
  
  private void drawPineapples(Canvas canvas) {
    for(Drawable pineapple : pineapples) {
      pineapple.draw(canvas);
    }
  }

  public void setTargetHeight(int targetHeight) {
    this.targetHeight = targetHeight;
  }
}
