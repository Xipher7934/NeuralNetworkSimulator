package com.EthanHeming.NeuralCircuits;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class Neuron implements Parcelable {

	private float[] loc;
	public double v,u;
	private double a,b,c,d;
	private String neuron_type;
	private int subtype;
	private RenderObject RO;
	
	// BASIC CONSTRUCTOR
	public Neuron(Context context, float x1, float y1) {
		neuron_type = "INTERNEURON";
		subtype=0;
		loc = new float[] {x1, y1};
		setup();
	}
	// CONSTRUCTOR FROM PARCEL
    private Neuron(Parcel in) {
    	neuron_type = in.readString();
    	subtype=in.readInt();
		loc= in.createFloatArray();
		setup();
    }
    // CONSTRUCTOR FROM FILE STREAM
    public Neuron(BufferedReader file, float [] offset) {
		neuron_type = "INTERNEURON";
		subtype=0;
		loc = new float[] {0,0};
		a=0.1;
		b=0.2;
		c=-65;
		d=8;
		v=c;
		u=0;
		
		
		String s;
		String[] sp;

		try {

			while ((s = file.readLine()) != null && !s.contains("</NEURON>") && !s.contains("<AXON>") && !s.contains("<NEURON>")) {
				sp=s.trim().split("[ <>]+");
				if (sp[1].compareToIgnoreCase("LOCATION")==0)
					{loc[0]=Float.parseFloat(sF(sp[2]))-offset[0]; loc[1]=Float.parseFloat(sF(sp[3]))-offset[1];}
				if (sp[1].compareToIgnoreCase("TYPE")==0)
					neuron_type=sp[2];
				if (sp[1].compareToIgnoreCase("SUBTYPE")==0)
					subtype=Integer.parseInt(sp[2]);
				if (neuron_type.equals("0")) neuron_type = "INTERNEURON";
				if (sp[1].compareToIgnoreCase("V")==0)
					v=Float.parseFloat(sF(sp[2]));
				if (sp[1].compareToIgnoreCase("U")==0)
					u=Float.parseFloat(sF(sp[2]));
			}

		} catch (IOException e) {
			
		}
		
		
		RO = new RenderObject(RenderObject.NEURON, neuron_type, loc, null, new float[] {(float)v});
	}
	
	private void setup() {
		a=0.1;
		b=0.2;
		c=-65;
		d=8;
		v=c;
		u=0;
		
		RO = new RenderObject(RenderObject.NEURON, neuron_type, loc, null, new float[] {(float)v});
		
	}
	
	public boolean update(HashMap<String, Float[]> s) {
		
		Float[] inStim=null;
		
		if ((s!=null) && s.containsKey(neuron_type))
			inStim=s.get(neuron_type);
		if ((inStim!=null) && inStim.length>subtype)
			stimulate(inStim[subtype]*(float)(Math.random()*0.3+.85));
		
		v = v+0.5*((0.04*v+5)*v+140-u);
		v = v+0.5*((0.04*v+5)*v+140-u);
		u = u+a*(b*v-u);
		if(v>100) v=100;
		if(v<-100)v=-100;
		
		if(v>30) {
			v=c;
			u=u+d;
			return true;
		} else return false;
	}
	
	public float distance(float[] loc1) {
		return (float) Math.sqrt((loc[0]-loc1[0])*(loc[0]-loc1[0])+(loc[1]-loc1[1])*(loc[1]-loc1[1]));
	}
	
	
	public void stimulate(float stim) {
		v=v+stim;

	}
	
	public float[] getLocation() {
		return loc;
	}
	
	public float getX() {
		return loc[0];
	}
	
	public float getY() {
		return loc[1];
	}
	
	public void setType(String type) {
		neuron_type = type;
	}
	
	public String getType() {
		return neuron_type;
	}
	
	public RenderObject getRender() {
		RO.setV( new float[] {(float)v});
		RO.setSubtype(neuron_type);
		return RO;
	}

	public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Neuron> CREATOR
            = new Parcelable.Creator<Neuron>() {
        public Neuron createFromParcel(Parcel in) {
            return new Neuron(in);
        }

        public Neuron[] newArray(int size) {
            return new Neuron[size];
        }
    };
    
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(neuron_type);
        out.writeInt(subtype);
        out.writeFloatArray(loc);
    }

	public void writeToFile(BufferedWriter file,float[] offset) {

		try {
			file.write("<NEURON>\r\n");
			file.write("   <LOCATION>" 
					+ new DecimalFormat("##.##").format(loc[0]-offset[0]) + " "
					+ new DecimalFormat("##.##").format(loc[1]-offset[1]) + "</LOCATION>\r\n");
			file.write("   <TYPE>" + neuron_type + "</TYPE>\r\n");
			if (subtype!=0) 
				file.write("   <SUBTYPE>" + new DecimalFormat("##").format(subtype) + "</SUBTYPE>\r\n");
			file.write("   <V>" + new DecimalFormat("##.##").format(v) + "</V>\r\n");
			file.write("   <U>" + new DecimalFormat("##.##").format(u) + "</U>\r\n");
			file.write("</NEURON>\r\n");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

    private String sF(String in) {
        return in.replace(",",".").replace("–","-").replace("٠",".");
    }
}
