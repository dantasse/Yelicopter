package com.dantasse.whistleweasel;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.util.Log;

public class RecordingThread extends Thread {

  // 44100, Mono, 16bit is guaranteed to work by the Android Compatibility
  // Test Suite.
  private static final int SAMPLING_RATE = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
  private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

  private WWActivity wwActivity;
  private Handler recordingThreadHandler;

  // This class always crashes hard on the emulator!  Womp womp.
  // You can make it work on the emulator with SAMPLING_RATE = 8000 (but even
  // then, it's kind of a shot in the dark.)
  private AudioRecord audioRecord;

  public RecordingThread(WWActivity wwActivity) {
    this.wwActivity = wwActivity;
    recordingThreadHandler = new Handler();
  }

  @Override
  public void run() {

    int bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, CHANNEL_CONFIG, ENCODING);

    audioRecord = new AudioRecord(AudioSource.MIC, SAMPLING_RATE,
        CHANNEL_CONFIG, ENCODING, bufferSize);
    audioRecord.startRecording();

    int smallBufferSize = bufferSize;
    final short[] audioBuffer = new short[bufferSize];
    PitchDetector pitchDetector = new PitchDetector(bufferSize);

    for(;;) {
      audioRecord.read(audioBuffer, 0, bufferSize);

      int maxIndex = pitchDetector.detectPitch(audioBuffer);

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

  public Handler getRecordingThreadHandler() {
    return recordingThreadHandler;
  }
}
