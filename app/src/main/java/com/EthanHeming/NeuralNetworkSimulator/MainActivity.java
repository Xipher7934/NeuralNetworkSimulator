package com.EthanHeming.NeuralNetworkSimulator;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;

import com.EthanHeming.Environment.AudioListener;
import com.EthanHeming.Environment.AudioListener.OnAudioListener;
import com.EthanHeming.NeuralCircuits.Brain;
import com.EthanHeming.NeuralCircuits.Brain.OnSpikeListener;
import com.EthanHeming.NeuralNetworkSimulator.EditorFragment.editorInterface;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MainActivity extends Activity implements editorInterface, OnSpikeListener, SensorEventListener, OnAudioListener {

	Brain B = null;
	
	SensorManager sm;
	AudioListener al;

	
	Vibrator v;
	HashMap<String,Float[]> IOValues;
	HashMap<String,HashMap<String,String>> IODescriptions;
	HashMap<String, String> hm;
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelable("Brain", B);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState!=null) {
			B = savedInstanceState.getParcelable("Brain");
		} else {
		
			B = new Brain();
			
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.mainLayout, new EditorFragment())
				.commit();

		}

		super.onCreate(savedInstanceState);
	
	}
	
	public void onPause() {
		
		super.onPause();
		B.onPause();
		sm.unregisterListener(this);
		if (al!=null) {
			al.unregisterListener();
			al.stopListener();
			al=null;
		}
		
		
	}
	
	public void onResume() {
		B.onResume();
		super.onResume();
		B.setSpikeListener(this);
		
		IOValues = new LinkedHashMap<>();
		IODescriptions = new LinkedHashMap<>();
		
		hm = new HashMap<>();
		hm.put("icon",Integer.toString(R.drawable.ic_neuron));
		hm.put("name","Interneuron (Normal)");
		hm.put("desc", "Integrates input from incoming axons - basic neuron");
		hm.put("type","INTERNEURON");
		IODescriptions.put("INTERNEURON",hm);
		
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		if(sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size()!=0){
			IOValues.put("SENSORY_ACCELEROMETER", new Float[] {0f});
			hm = new HashMap<>();
			hm.put("icon",Integer.toString(R.drawable.ic_neuronaccelerometer));
			hm.put("name","Acceleration (Sensory)");
			hm.put("desc", "Senses acceleration of your phone");
			hm.put("type","SENSORY_ACCELEROMETER");
			IODescriptions.put("SENSORY_ACCELEROMETER",hm);
			sm.registerListener(this,sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(sm.getSensorList(Sensor.TYPE_LIGHT).size()!=0){
			IOValues.put("SENSORY_LIGHTMETER", new Float[] {0f});
			hm = new HashMap<>();
			hm.put("icon",Integer.toString(R.drawable.ic_neuronlight));
			hm.put("name","Light (Sensory)");
			hm.put("desc", "Senses ambient light level");
			hm.put("type","SENSORY_LIGHTMETER");
			IODescriptions.put("SENSORY_LIGHTMETER",hm);
			sm.registerListener(this,sm.getSensorList(Sensor.TYPE_LIGHT).get(0), SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size()!=0){
			IOValues.put("SENSORY_MAGNETOMETER", new Float[] {0f});
			hm = new HashMap<>();
			hm.put("icon",Integer.toString(R.drawable.ic_neuronmagnetic));
			hm.put("name","Magnetic (Sensory)");
			hm.put("desc", "Senses ambient magnetic field");
			hm.put("type","SENSORY_MAGNETOMETER");
			IODescriptions.put("SENSORY_MAGNETOMETER",hm);
			sm.registerListener(this,sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		al = new AudioListener();
		if (al.getARState() == AudioRecord.STATE_INITIALIZED) {
			IOValues.put("SENSORY_AUDITORY", new Float[] {0f});
			hm = new HashMap<>();
			hm.put("icon",Integer.toString(R.drawable.ic_neuronauditory));
			hm.put("name","Auditory (Sensory)");
			hm.put("desc", "Senses ambient sound level");
			hm.put("type","SENSORY_AUDITORY");
			IODescriptions.put("SENSORY_AUDITORY",hm);
			al.registerListener(this);
			al.start();

		} else {al=null;}
		
		if ((v = (Vibrator) getSystemService(VIBRATOR_SERVICE)).hasVibrator()) {
			hm = new HashMap<>();
			hm.put("icon", Integer.toString(R.drawable.ic_neuronvibration));
			hm.put("name", "Vibration (Motor)");
			hm.put("desc", "Activates your phone's vibrator");
			hm.put("type","MOTOR_VIBRATION");
			IODescriptions.put("MOTOR_VIBRATION", hm);
		}

	}

	@Override
	public Brain getBrain() {
		return B;
	}

	@Override
	public void OnSpike(String type) {
		if (type.equals("MOTOR_VIBRATION"))
			v.vibrate(100);

		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, Float[]> getIOLevels() {
		return IOValues;
	}

	@Override
	public HashMap<String, HashMap<String,String>> getIOTypes() {
		return IODescriptions;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			IOValues.get("SENSORY_ACCELEROMETER")[0]= (float) (Math.sqrt(
    			event.values[0]*event.values[0]+
    			event.values[1]*event.values[1]+
    			event.values[2]*event.values[2])-9.81
    			);
			break;
		case Sensor.TYPE_LIGHT:
			IOValues.get("SENSORY_LIGHTMETER")[0]= (float) (Math.log(
					event.values[0]+1));
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			IOValues.get("SENSORY_MAGNETOMETER")[0]= (float) Math.log(Math.max(Math.sqrt(
	    			event.values[0]*event.values[0]+
	    			event.values[1]*event.values[1]+
	    			event.values[2]*event.values[2])
	    			-45,1));
			break;
		}

	}

	@Override
	public void OnAudio(double audioData) {
		
		IOValues.get("SENSORY_AUDITORY")[0]= (float) Math.max(audioData,0)/40000;
		
	}
	
	
}
