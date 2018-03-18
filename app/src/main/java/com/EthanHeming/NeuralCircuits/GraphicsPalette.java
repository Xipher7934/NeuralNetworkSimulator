package com.EthanHeming.NeuralCircuits;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;
import java.util.HashMap;

public class GraphicsPalette implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Bitmap BM_Neuron;
	public Bitmap BM_Firing;
	public Bitmap BM_Spike;
	public Bitmap BM_Electrode;
    public Bitmap BM_GraphDrawer;
	
	public HashMap<String,Bitmap> BM_Neuron_Types;

	
	public Bitmap H;
	
	public Bitmap E_A;
	
	public Paint  T;
	public Paint  A;
	public Paint  G;
    public Paint BG;
	public Paint  D;
	public Paint  text;
    public Paint  large_text;
	
	public float mxAxon;

    public int graphWidth=300;
    public int graphHeight=30;
	
	public GraphicsPalette(Context C) {
		
		int Id;
		
		Id = C.getResources().getIdentifier("ic_neuron", "drawable", C.getPackageName());
		BM_Neuron = BitmapFactory.decodeResource(C.getResources(), Id);
		Id = C.getResources().getIdentifier("ic_firing", "drawable", C.getPackageName());
		BM_Firing = BitmapFactory.decodeResource(C.getResources(), Id);
		Id = C.getResources().getIdentifier("ic_spike", "drawable", C.getPackageName());
		BM_Spike = BitmapFactory.decodeResource(C.getResources(), Id);
		Id = C.getResources().getIdentifier("ic_electrode", "drawable", C.getPackageName());
		BM_Electrode = BitmapFactory.decodeResource(C.getResources(), Id);

        Id = C.getResources().getIdentifier("ic_graphdrawer", "drawable", C.getPackageName());
        BM_GraphDrawer = BitmapFactory.decodeResource(C.getResources(), Id);
		
		BM_Neuron_Types = new HashMap<String,Bitmap>();
		
		Id = C.getResources().getIdentifier("ic_neuronaccelerometer", "drawable", C.getPackageName());
		BM_Neuron_Types.put("SENSORY_ACCELEROMETER", BitmapFactory.decodeResource(C.getResources(), Id));
		Id = C.getResources().getIdentifier("ic_neuronlight", "drawable", C.getPackageName());
		BM_Neuron_Types.put("SENSORY_LIGHTMETER", BitmapFactory.decodeResource(C.getResources(), Id));
		Id = C.getResources().getIdentifier("ic_neuronmagnetic", "drawable", C.getPackageName());
		BM_Neuron_Types.put("SENSORY_MAGNETOMETER", BitmapFactory.decodeResource(C.getResources(), Id));
		Id = C.getResources().getIdentifier("ic_neuronauditory", "drawable", C.getPackageName());
		BM_Neuron_Types.put("SENSORY_AUDITORY", BitmapFactory.decodeResource(C.getResources(), Id));
		
		Id = C.getResources().getIdentifier("ic_neuronvibration", "drawable", C.getPackageName());
		BM_Neuron_Types.put("MOTOR_VIBRATION", BitmapFactory.decodeResource(C.getResources(), Id));

		
		Id = C.getResources().getIdentifier("ic_edit_axon", "drawable", C.getPackageName());
		E_A = BitmapFactory.decodeResource(C.getResources(), Id);
		
		Id = C.getResources().getIdentifier("ic_help_button", "drawable", C.getPackageName());
		H = BitmapFactory.decodeResource(C.getResources(), Id);
		
		T = new Paint();
		T.setARGB(255,255,255,255);
		
		mxAxon= BM_Neuron.getWidth()/8;
		
		A = new Paint();
		A.setAntiAlias(true);
		A.setStyle(Paint.Style.STROKE);
		A.setStrokeWidth((float) mxAxon/3);
		A.setStrokeJoin(Paint.Join.ROUND);
		A.setStrokeCap(Paint.Cap.ROUND);
		
		G = new Paint();
		G.setColor(Color.BLUE);
		G.setAntiAlias(true);
		G.setStyle(Paint.Style.STROKE);
		G.setStrokeWidth((float) mxAxon/3);
		G.setStrokeJoin(Paint.Join.ROUND);
		G.setStrokeCap(Paint.Cap.ROUND);

        BG = new Paint();
        BG.setColor(Color.BLUE);
        BG.setAlpha(50);
        BG.setAntiAlias(true);
        BG.setStyle(Paint.Style.FILL_AND_STROKE);
        BG.setStrokeWidth((float) 50);
        BG.setStrokeJoin(Paint.Join.ROUND);
        BG.setStrokeCap(Paint.Cap.ROUND);
		
		D = new Paint();
		D.setColor(Color.WHITE);
		D.setAntiAlias(true);
		D.setStyle(Paint.Style.STROKE);
		D.setStrokeWidth((float) mxAxon/3);
		D.setStrokeJoin(Paint.Join.ROUND);
		D.setStrokeCap(Paint.Cap.ROUND);
		
		text = new Paint();
		text.setColor(Color.WHITE);
		text.setAntiAlias(true);
		text.setStyle(Paint.Style.FILL_AND_STROKE);
		text.setStrokeWidth(mxAxon/10);
		text.setTextSize(mxAxon);
		text.setStrokeJoin(Paint.Join.ROUND);
		text.setStrokeCap(Paint.Cap.ROUND);
		text.setTextAlign(Paint.Align.CENTER);

        large_text=new Paint(text);
        large_text.setTextSize(mxAxon*3);
	}
	
	
}
