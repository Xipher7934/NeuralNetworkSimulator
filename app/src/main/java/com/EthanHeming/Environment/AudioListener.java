package com.EthanHeming.Environment;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

public class AudioListener extends Thread {

	private OnAudioListener mCallback = null;
	
	private static final int sampleRate = 44100;
	private int buffersize;
    AudioRecord ar;
	private boolean running;
	
	public interface OnAudioListener {
		public void OnAudio(double sum);
	}
	
	public void registerListener (OnAudioListener A) {
		mCallback = A;
	}
	public void unregisterListener () {
		mCallback = null;
	}
	
	
	public AudioListener() {
		buffersize = AudioRecord.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		//android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		ar=new AudioRecord(AudioSource.DEFAULT, sampleRate,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				buffersize);
	}
	
	public int getARState() {
		if (ar!=null)
			return ar.getState();
		else
			return AudioRecord.STATE_UNINITIALIZED;
	}
	
	public void run() {
		
		running=true;
		
		if (ar.getState() != AudioRecord.STATE_INITIALIZED) {
			running = false;
			return;
		}
		try {
			ar.startRecording();
		} catch (IllegalStateException e) {
			running = false;
			return;
		}
		double sum;
        short[] data = new short[buffersize];
		while (running && ar!=null) {
			sum=0;
			int readSize = ar.read(data, 0, data.length);
			for (int i = 0; i < readSize; i++) {
				sum += data[i] * data[i];
			}
			if ((mCallback!=null) && readSize>0)
				mCallback.OnAudio(Math.sqrt(sum));
		}
				
	}
	
	public void stopListener() {
		running=false;
		ar.stop();
		ar.release();
		ar = null;
	}
	
	
}
