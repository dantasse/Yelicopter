package com.dantasse.yelicopter;

import java.util.ArrayList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.util.Log;

public class RecordingThread extends Thread {

  private static final int SAMPLING_RATE = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
  private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

  private YelicopterActivity yelicopterActivity;
  private Handler uiThread;
  private AudioRecord audioRecord;
  
  public RecordingThread(YelicopterActivity yelicopterActivity, Handler uiThread) {
    this.yelicopterActivity = yelicopterActivity;
    this.uiThread = uiThread;
  }

  public void run() {
      
      // AudioRecord setup
      int minBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
          CHANNEL_CONFIG, ENCODING);
      // no particular reason bigBufferSize is equal to SAMPLING_RATE.
      int bigBufferSize = SAMPLING_RATE;
      audioRecord = new AudioRecord(AudioSource.MIC, SAMPLING_RATE, 
          CHANNEL_CONFIG, ENCODING, bigBufferSize);
      audioRecord.startRecording();
      
      int smallBufferSize = bigBufferSize / 2;
      final short[] audioBuffer = new short[smallBufferSize];
      PitchDetector pitchDetector = new PitchDetector(smallBufferSize);
      ArrayList<Short> slidingBuffer = new ArrayList<Short>();
      
      for(;;) {
        long startTime = System.currentTimeMillis();
        audioRecord.read(audioBuffer, 0, smallBufferSize);
        long readTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        for (short s : audioBuffer) {
          slidingBuffer.add(s);
        }
        while (slidingBuffer.size() > bigBufferSize) {
          slidingBuffer.remove(0);
        }
        // approx 80 ms
        long bufferSlidingTime = System.currentTimeMillis() - startTime;
        
        final int maxIndex = pitchDetector.detectPitch(
            slidingBuffer.toArray(new Short[0]));
        // approx 200-300 ms 
        long computeTime = System.currentTimeMillis() - startTime;
        Log.d(YelicopterActivity.DEBUG_TAG, "Read time: " + readTime +
            ", buffer sliding time: " + bufferSlidingTime + ", compute time: " +
            computeTime);
        
        // arbitrary limits: if it's under 100 you're probably taking a
        // breath; if it's over 1000 it's probably just a skip or something.
        if (maxIndex > 100 && maxIndex < 1000) {
          // post the sum back to the UI thread
          uiThread.post(new Runnable() {
            public void run() {
              yelicopterActivity.updateUi(maxIndex);
            }
          });
        }
      }
  }
}
