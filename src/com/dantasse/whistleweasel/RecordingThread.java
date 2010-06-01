package com.dantasse.whistleweasel;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class RecordingThread extends Thread {

  // I think only 44100, 22050, 11025 work.  Or maybe not even those! At any
  // rate, 44100 seems to work.
  private static final int SAMPLING_RATE = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
  private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

  private WWActivity wwActivity;

  // This class always crashes hard on the emulator!  Womp womp.
  private AudioRecord audioRecord;

  public RecordingThread(WWActivity wwActivity) {
    this.wwActivity = wwActivity;
  }

  public void run() {

    // no particular reason bigBufferSize is equal to SAMPLING_RATE.
    // you'll FFT 1 second at a time.
//    int bigBufferSize = SAMPLING_RATE;
    int bigBufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_CONFIG, ENCODING);
    
    audioRecord = new AudioRecord(AudioSource.MIC, SAMPLING_RATE, 
        CHANNEL_CONFIG, ENCODING, bigBufferSize);
    audioRecord.startRecording();

    int smallBufferSize = bigBufferSize;
    final short[] audioBuffer = new short[smallBufferSize];
    PitchDetector pitchDetector = new PitchDetector(smallBufferSize);
//    ArrayList<Short> slidingBuffer = new ArrayList<Short>();

    for(;;) {
      long startTime = System.currentTimeMillis();
      audioRecord.read(audioBuffer, 0, smallBufferSize);
      long readTime = System.currentTimeMillis() - startTime;

//      startTime = System.currentTimeMillis();
//      for (short s : audioBuffer) {
//        slidingBuffer.add(s);
//      }
//      while (slidingBuffer.size() > bigBufferSize) {
//        slidingBuffer.remove(0);
//      }
      long bufferSlidingTime = System.currentTimeMillis() - startTime;

      // slidingBuffer.size() == bigBufferSize now (except for the first few runs)
      // FFT it and find out the pitch.
//      final int maxIndex = pitchDetector.detectPitch(
//          slidingBuffer.toArray(new Short[0]));
      int maxIndex = pitchDetector.detectPitch(audioBuffer);
      
      long computeTime = System.currentTimeMillis() - startTime;
//      Log.d(YelicopterActivity.DEBUG_TAG, "Read time: " + readTime +
//          ", buffer sliding time: " + bufferSlidingTime + ", compute time: " +
//          computeTime);

      // arbitrary limits: if it's under 100 you're probably taking a
      // breath; if it's over 1000 it's probably just a skip or something.
      if (maxIndex < WWActivity.FREQ_BOTTOM_OF_SCREEN &&
          maxIndex > WWActivity.FREQ_LOWER_LIMIT) {
        maxIndex = WWActivity.FREQ_BOTTOM_OF_SCREEN;
      }
      if (maxIndex > WWActivity.FREQ_TOP_OF_SCREEN &&
          maxIndex < WWActivity.FREQ_UPPER_LIMIT) {
        maxIndex = WWActivity.FREQ_TOP_OF_SCREEN;
      }
      if (maxIndex > WWActivity.FREQ_LOWER_LIMIT && 
          maxIndex < WWActivity.FREQ_UPPER_LIMIT) {
        // post the sum back to the UI thread
        final int finalMaxIndex = maxIndex;
        wwActivity.getUiThreadHandler().post(new Runnable() {
          public void run() {
            wwActivity.setWeaselTop(finalMaxIndex);
            wwActivity.updateUi();
          }
        });
      }
    }
  }
}
