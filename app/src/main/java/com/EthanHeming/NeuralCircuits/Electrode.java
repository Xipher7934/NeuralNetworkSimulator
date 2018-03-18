package com.EthanHeming.NeuralCircuits;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;


public class Electrode implements Parcelable {

	
	private float[] eLoc;
    private float[] v;
	private int tLen=500;
	private String colorSeed;
	private boolean init=false;
	
	private RenderObject ROE;
	private RenderObject ROG;
	
	
	public Electrode(float[] iLoc) {

		eLoc = new float[2];
		eLoc[0]=iLoc[0];
		eLoc[1]=iLoc[1];
		colorSeed = "#FF"
						+Integer.toHexString((int)(Math.random()*200)+55)
						+Integer.toHexString((int)(Math.random()*200)+55)
						+Integer.toHexString((int)(Math.random()*200)+55);
		
		v= new float[tLen];
		setup();
	}
	
	private Electrode(Parcel in) {
		eLoc = in.createFloatArray();
		v = in.createFloatArray();
		colorSeed = in.readString();
		setup();
	}
	
	private void setup() {
		ROE = new RenderObject(RenderObject.ELECTRODE, colorSeed, eLoc,null, null);
		ROG = new RenderObject(RenderObject.GRAPH, colorSeed, null, null, v);
	}
	
	public void update(List<Neuron> neurons) {


        System.arraycopy(v,1,v,0,tLen-1);
		
		v[tLen-1]=0;
		for(int i =0; i <neurons.size(); i++)
			v[tLen-1]-=(neurons.get(i).v * Math.min((1/neurons.get(i).distance(eLoc)),.03));
		
		if(!init) {
			for (int i =0; i <(tLen-1); i++) {
				v[i]=v[tLen-1];
			}
			init=true;
		}

	}

	
	public float distance(float[] loc1) {
		return (float) Math.sqrt(
				(eLoc[0]-loc1[0])*(eLoc[0]-loc1[0])+
				(eLoc[1]-loc1[1])*(eLoc[1]-loc1[1]));
	}
	
	public void move_electrode(float[] loc) {
		eLoc[0]=loc[0];
		eLoc[1]=loc[1];
	}
	
	public RenderObject getRenderE() {
		ROE.setLoc(eLoc);
		return ROE;
	}

	public RenderObject getRenderG(int len) {
        float[] temp = new float[len];
        System.arraycopy(v,v.length-len,temp,0,len);
		ROG.setV(temp);
		return ROG;
	}
	
	public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Electrode> CREATOR
            = new Parcelable.Creator<Electrode>() {
        public Electrode createFromParcel(Parcel in) {
            return new Electrode(in);
        }

        public Electrode[] newArray(int size) {
            return new Electrode[size];
        }
    };
    
    public void writeToParcel(Parcel out, int flags) {
    	out.writeFloatArray(eLoc);
    	out.writeFloatArray(v);
    	out.writeString(colorSeed);
    }
	
}
