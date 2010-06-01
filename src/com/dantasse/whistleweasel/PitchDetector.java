package com.dantasse.whistleweasel;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class PitchDetector {

  FloatFFT_1D fft;

  public PitchDetector(int bufferSize) {
    fft = new FloatFFT_1D(bufferSize);
  }

  int detectPitch(short[] audioBuffer) {
    // ugh, convert a short[] to a float[]
    float[] transformBuffer = new float[audioBuffer.length];
    for(int j = 0; j < audioBuffer.length; j++) {
      transformBuffer[j] = audioBuffer[j];
    }
    fft.realForward(transformBuffer);
    
    float maxValue = 0.0f;
    int maxIndex = 0;
    for(int k = 0; k < transformBuffer.length; k++) {
      if (transformBuffer[k] > maxValue) {
        maxValue = transformBuffer[k];
        maxIndex = k;
      }
    }
    return maxIndex;
  }
}
