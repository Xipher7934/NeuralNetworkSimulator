package com.EthanHeming.NeuralCircuits;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class Axon implements Parcelable {

	// required axon variables
	private float strength, speed;
	private ArrayList<Float> spikes;
	public ArrayList<Float> postspikes;
	private Path path;
	
	// variables for speed
	private PathMeasure pm;
	private float length;
	private float[] start, end;
	private RectF bounds;
	private boolean stim;
	private RenderObject ROA, ROS;

	// temporary variables
	private int i;
	
	public Axon(Path p1) {
		strength = 20;
		speed = 20;
		spikes = new ArrayList<>();
		postspikes = new ArrayList<>();
		path = new Path(p1);
		setup();
	}
	
	public Axon(Parcel in) {
		strength = in.readFloat();
		speed = in.readFloat();
		
		float[] temp;
		
		temp = in.createFloatArray();
		for (i=0;i<temp.length;i++)
			spikes.add(temp[i]);
		
		temp = in.createFloatArray();
		for (i=0;i<temp.length;i++)
			postspikes.add(temp[i]);
		
		temp=in.createFloatArray();
		path = new Path();
		path.moveTo(temp[0], temp[1]);
		for (i=2;i<temp.length;i=i+2)
			path.lineTo(temp[i], temp[i+1]);
		setup();
	}
	
	public Axon(BufferedReader file, float [] offset) {
		strength = 20;
		speed = 20;
		spikes = new ArrayList<>();
		postspikes = new ArrayList<>();
		path = new Path();
		boolean firstnode=false;
		
		String s;
		String[] sp;

		try {

			while ((s = file.readLine()) != null && !s.contains("</AXON>") && !s.contains("<AXON>") && !s.contains("<NEURON>")) {
				sp=s.trim().split("[ <>]+");
				if (sp[1].compareToIgnoreCase("NODE")==0)
					if(!firstnode)
						{path.moveTo(Float.parseFloat(sF(sp[2]))-offset[0], Float.parseFloat(sF(sp[3]))-offset[1]);firstnode=true;}
					else
						path.lineTo(Float.parseFloat(sF(sp[2]))-offset[0], Float.parseFloat(sF(sp[3]))-offset[1]);
				if (sp[1].compareToIgnoreCase("SPIKE")==0)
					spikes.add(Float.parseFloat(sF(sp[2])));
				if (sp[1].compareToIgnoreCase("SPEED")==0)
					speed=Float.parseFloat(sF(sp[2]));
				if (sp[1].compareToIgnoreCase("STRENGTH")==0)
					strength=Float.parseFloat(sF(sp[2]));
			}

		} catch (IOException e) {
			
		}
		
		setup();
	}
	
	private void setup() {
		// variables for speed
		pm = new PathMeasure(path, false);
		length = pm.getLength();
		start = new float[2];
		end = new float[2];
		pm.getPosTan(1, start, null);
		pm.getPosTan(length, end, null);
		stim = false;
		bounds = new RectF();
		path.computeBounds(bounds, false);
		bounds.union(bounds.left-50, bounds.top-50);
		bounds.union(bounds.right+50, bounds.bottom+50);

		// Set up renderers
		ROA = new RenderObject(RenderObject.AXON, null, null, path, new float[] {strength, speed});
		ROS = new RenderObject(RenderObject.SPIKES, null, null, null, null);
	}
	
	public boolean update() {
		stim=false;
		
		for(i=spikes.size()-1;i>=0;i--) {
			spikes.set(i,spikes.get(i)+speed);
			if(spikes.get(i)>length) {
				spikes.remove(i);
				postspikes.add((float) 1);
				stim=true;
			}
		}
		
		for(i=postspikes.size()-1;i>=0;i--) {
			postspikes.set(i,postspikes.get(i)+1);
			if(postspikes.get(i)>10) {
				postspikes.remove(i);
			}
		}
		return stim;
	}
	
	public void STDP() {
		for(i=1; i<spikes.size();i++)
			if (spikes.get(i)+speed*10 > length)
				strength=strength-0.2f;
		for(i=0; i<postspikes.size();i++)
			strength=strength+0.1f;
		strength=Math.min(Math.max(strength-0.1f,-25),25);
	}
	
	
	public void add_spike() {
		spikes.add((float) 0);
	}
	
	public void add_spike(float t) {
		spikes.add(t);
	}
	
	public float start_distance(float[] loc) {
		return (float) Math.sqrt((start[0]-loc[0])*(start[0]-loc[0])+(start[1]-loc[1])*(start[1]-loc[1]));
	}
	
	public float end_distance(float[] loc) {
		return (float) Math.sqrt((end[0]-loc[0])*(end[0]-loc[0])+(end[1]-loc[1])*(end[1]-loc[1]));
	}
	
	public float shortest_distance(float[] loc) {
		float dist = 10000000;
		float td;
		float temp[] = new float[2];
		for(int i=0;i<length;i=i+5) {
			pm.getPosTan(i, temp, null);
			td= ((temp[0]-loc[0])*(temp[0]-loc[0])+(temp[1]-loc[1])*(temp[1]-loc[1]));
			if(td<dist) dist = td;
		}
		return (float) Math.sqrt(dist);
	}
	
	public float first_contact(float[] loc,float min) {

		if (!bounds.contains(loc[0], loc[1]))
			return -1;
		float temp[] = new float[2];
		for(int i=0;i<length;i=i+5) {
			pm.getPosTan(i, temp, null);
			if (((temp[0]-loc[0])*(temp[0]-loc[0])+(temp[1]-loc[1])*(temp[1]-loc[1]))<=(min*min))
				return (float)i;
		}
		return -1;
	}
	
	public void setStr(float str) {
		strength = str;
	}
	
	public float getStr() {
		return strength;
	}
	
	public void setSpeed(float set) {
		speed = set;
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public RenderObject getRenderA() {
		float[] temp = new float[] {strength, speed};
		if (!Arrays.equals(ROA.getV(), temp)) {
			ROA.setV(temp);
			ROA.redraw();
		}
		return ROA;
	}
	
	public RenderObject getRenderS() {
		float[] temp = new float[2];
		float[] S = new float[spikes.size()*2];
		for(i=0;i<spikes.size();i++) {
			pm.getPosTan(spikes.get(i), temp, null);
			S[i*2]=temp[0];
			S[i*2+1]=temp[1];
		}
		ROS.setLoc(S);
		return ROS;
	}

	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<Axon> CREATOR
    	= new Parcelable.Creator<Axon>() {
		public Axon createFromParcel(Parcel in) {
		return new Axon(in);
		}

		public Axon[] newArray(int size) {
			return new Axon[size];
		}
	};
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeFloat(strength);
		out.writeFloat(speed);
		
		float[] temp = new float[spikes.size()];
		float[] temp2= new float[2];
		
		for (i=0;i<spikes.size();i++)
			temp[i]=spikes.get(i);
		out.writeFloatArray(temp);
		
		temp = new float[postspikes.size()];
		
		for (i=0;i<postspikes.size();i++)
			temp[i]=postspikes.get(i);
		out.writeFloatArray(temp);
		
		temp = new float[((int) (length/5)+1)*2];
		for (i=0;i<(length/5);i=i+5) {
			pm.getPosTan(i*5, temp2, null);
			temp[i]=temp2[0];
			temp[i+1]=temp2[1];
		}
		pm.getPosTan(length, temp2, null);
		temp[temp.length-2]=temp2[0];
		temp[temp.length-1]=temp2[1];
		out.writeFloatArray(temp);
		
	}
	
	public void writeToFile(BufferedWriter file, float[] offset) {
		
		float[] temp2 = new float[2];
		
		
		try {
			file.write("<AXON>\r\n");
			file.write("   <SPEED>"+new DecimalFormat("##.##").format(speed)+"</SPEED>\r\n");
			file.write("   <STRENGTH>"+new DecimalFormat("##.##").format(strength)+"</STRENGTH>\r\n");
			
			for (i=0;i<(length/5);i=i+5) {
				file.write("   <NODE>");
				pm.getPosTan(i*5, temp2, null);
				file.write(new DecimalFormat("##.##").format(temp2[0]-offset[0])+" "+new DecimalFormat("##.##").format(temp2[1]-offset[1]));
				file.write("</NODE>\r\n");
			}
			file.write("   <NODE>");
			file.write(new DecimalFormat("##.##").format(end[0]-offset[0])+" "+new DecimalFormat("##.##").format(end[1]-offset[1]));
			file.write("</NODE>\r\n");
			if (spikes.size()!=0) {
				
				for (i=0;i<spikes.size();i++) {
					file.write("   <SPIKE>");
					file.write(new DecimalFormat("##.##").format(spikes.get(i)));
					file.write("</SPIKE>\r\n");
				}
				
			}
			if (postspikes.size()!=0) {
				
				for (i=0;i<postspikes.size();i++) {
					file.write("   <POSTSPIKE>");
					file.write(new DecimalFormat("##.##").format(postspikes.get(i)));
					file.write("</POSTSPIKE>\r\n");
				}
				
			}
			file.write("</AXON>\r\n");
		
		} catch (Exception e) {
  		  e.printStackTrace();
  		}
		
	}


    private String sF(String in) {
        return in.replace(",",".").replace("–","-").replace("٠",".");
    }


}



