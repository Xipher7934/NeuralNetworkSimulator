package com.EthanHeming.NeuralCircuits;

import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Brain implements Parcelable {
	
	public boolean 		running;
	private BrainThread thread;
	private long 		tick;
	
	private List<Neuron>	neurons =		new ArrayList<>();
	private List<Axon>		axons =			new ArrayList<>();
	private List<Electrode>	electrodes = 	new ArrayList<>();
	
	private boolean[][] CMout;
	private boolean[][] CMin;
	
	private int n_bound = 5;
	private boolean STDP = false;
	
	private OnSpikeListener mCallback = null;
	
	public interface OnSpikeListener {
		public void OnSpike(String string);
		public HashMap<String, Float[]> getIOLevels();
	}
	
	public void setSpikeListener (OnSpikeListener A) {
		mCallback = A;
	}
	
	// CONSTRUCTOR
	public Brain() {
		tick = 20; // 20ms per cycle
		onResume();
		
	}
	
	// RECONSTRUCTOR
	public Brain(Parcel in) {
		tick = in.readLong();
		in.readTypedList(neurons, Neuron.CREATOR);
		in.readTypedList(axons, Axon.CREATOR);
		in.readTypedList(electrodes, Electrode.CREATOR);
		STDP = in.readByte() != 0;
		
		update_CTM();
		onResume();
	}
	
	// UPDATE THE BRAIN
	private synchronized void Update() {
		
		HashMap<String, Float[]> S=null;
		if (mCallback!=null)
			S = mCallback.getIOLevels();
		
		for(int i=0;i<axons.size();i++) {
			if(axons.get(i).update()) {
				for(int j=0;j<neurons.size();j++)
					if (CMin[i][j])
							neurons.get(j).stimulate(axons.get(i).getStr());
			}
			
		}
    	
		for(int j=0;j<neurons.size();j++) {
			if(neurons.get(j).update(S)) {
				for(int i=0;i<axons.size();i++) {
					if (CMout[i][j])
						axons.get(i).add_spike();
					if ((CMin[i][j]) && STDP)
						axons.get(i).STDP();
				}
				if ((mCallback!=null) && (!neurons.get(j).getType().equals("INTERNEURON")))
					mCallback.OnSpike(neurons.get(j).getType());
			}
		}
		
		for(int j=0;j<electrodes.size();j++)
			electrodes.get(j).update(neurons);
		
	}
	
	// THREAD THAT UPDATES THE BRAIN
	private class BrainThread extends Thread {
		
		Brain B;
		
		BrainThread(Brain Br) {
			B=Br;
		}
		
		@Override
		public void run() {
			long begin_time = SystemClock.elapsedRealtime();
			long cycle_time;
			
			B.running = true;
			
			while (B.running) {
				cycle_time = SystemClock.elapsedRealtime()-begin_time;
				
				
				if (cycle_time<tick){
					try {
						Thread.sleep(tick-cycle_time,0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				begin_time = SystemClock.elapsedRealtime();
				
				Update();
				
			}
			
		}
	
	}
	
	// ADDING FUNCTIONS
	public synchronized Neuron addNeuron(Context context,float[] loc) {
		Neuron N = new Neuron(context, loc[0], loc[1]);
		neurons.add(N);
		update_CTM();
		return N;
	}
	public synchronized Axon addAxon(Path drawPath) {
		Axon A = new Axon(drawPath);
		axons.add(A);
		update_CTM();
		return A;
	}
	public synchronized Axon addAxon(Path drawPath,float str,float speed) {
		Axon A = new Axon(drawPath);
		axons.add(A);
		A.setStr(str);
		A.setSpeed(speed);
		update_CTM();
		return A;
	}
	public synchronized Electrode addElectrode(float[] loc) {
		Electrode E = new Electrode(loc);
		electrodes.add(E);
		return E;
	}
	
	// REMOVING FUNCTIONS
	public synchronized void removeNeuron(Neuron N) {
		neurons.remove(N);
		update_CTM();
	}
	public synchronized void removeNeuron(int i) {
		neurons.remove(i);
		update_CTM();
	}
	public synchronized void removeAxon(Axon A) {
		axons.remove(A);
		update_CTM();
	}
	public synchronized void removeAxon(int i) {
		axons.remove(i);
		update_CTM();
	}
	public synchronized void removeElectrode(Electrode E) {
		electrodes.remove(E);
	}
	public synchronized void removeElectrode(int i) {
		electrodes.remove(i);
	}
	public synchronized void removeByPath(Path path, float dist) {
    	PathMeasure pm = new PathMeasure(path, false);
    	float[] mpl = new float[2];
    	for(int i=0;i<pm.getLength();i=i+5) {
    		pm.getPosTan(i, mpl, null);
    		
    		for(int j=neurons.size()-1;j>=0;j--)
    			if(neurons.get(j).distance(mpl)<dist) {
    				for (int k=axons.size()-1;k>=0;k--)
    					if ((axons.get(k).start_distance(neurons.get(j).getLocation())<=dist) ||
    							(axons.get(k).end_distance(neurons.get(j).getLocation())<=dist))
    						axons.remove(k);
    				neurons.remove(j);
    			}
    		
    		for(int j=axons.size()-1;j>=0;j--)
    			if (axons.get(j).first_contact(mpl,dist/2)>0)
    				axons.remove(j);


    	}
    	update_CTM();
    	
    }
	
	//FINDING THINGS
	public synchronized Neuron getNeuron(float[] loc,float min_dist) {
    	Neuron temp=null;
    	float dist=10000;
    	for(int i=0;i<neurons.size();i++) {
    		if (neurons.get(i).distance(loc)<dist) {
    			temp=neurons.get(i);
    			dist = neurons.get(i).distance(loc);
    		}
    	}
    	if(dist>min_dist) return null;
    	else return temp;
    }
    public synchronized Axon getAxon(float[] loc,float min_dist) {
    	Axon tempA=null;
    	float dist=10000;
    	for(int i=0;i<axons.size();i++) {
    		if (axons.get(i).shortest_distance(loc)<dist) {
    			tempA=axons.get(i);
    			dist = axons.get(i).shortest_distance(loc);
    		}
    	}
    	if(dist>min_dist) return null;
    	else return tempA;
    }
    public synchronized Electrode getElectrode(float[] loc,float min_dist) {
    	Electrode tempA=null;
    	float dist=10000;
    	for(int i=0;i<electrodes.size();i++) {
    		if (electrodes.get(i).distance(loc)<dist) {
    			tempA=electrodes.get(i);
    			dist = electrodes.get(i).distance(loc);
    		}
    	}
    	if(dist>min_dist) return null;
    	else return tempA;
    }

    public synchronized Electrode getElecFromGraph(int num) {
        if (electrodes.size()<num)
            return null;

    	return electrodes.get(num);
    }
    
    public synchronized void setElectrode(Electrode E, float[] loc) {
    	E.move_electrode(loc);
    }
    
    public synchronized void setElectrode(int i, float[] loc) {
    	electrodes.get(i).move_electrode(loc);
    }
	
	// UPDATE THE CONNECTOME
	private void update_CTM() {
    	CMin = new boolean[axons.size()][neurons.size()];
    	CMout= new boolean[axons.size()][neurons.size()];
    	for(int i=0;i<axons.size();i++) {
    		for(int j=0;j<neurons.size();j++) {
    			CMout[i][j]=
    					(axons.get(i).start_distance(neurons.get(j).getLocation()) < n_bound); 
    			CMin[i][j]=
    					(axons.get(i).  end_distance(neurons.get(j).getLocation()) < n_bound);
    		}
    	}
    }
	
	// STOP THE BRAIN ON PAUSE
	public synchronized void onPause() {
		if (thread!=null) {
			running = false;
			thread = null;
		}
	}
	
	// START THE BRAIN ON RESUME
	public synchronized void onResume() {
		
		if (thread==null) {
			thread = new BrainThread(this);
			thread.start();
		}
	}
	
	public synchronized ArrayList<RenderObject> getRenders() {
		ArrayList<RenderObject> temp = new ArrayList<>();
		
		for(int i=0;i<axons.size();i++) {
			temp.add(axons.get(i).getRenderA());
			temp.add(axons.get(i).getRenderS());
		}
		for(int i=0;i<neurons.size();i++)
			temp.add(neurons.get(i).getRender());

		
		for(int i=0;i<electrodes.size();i++) {
			temp.add(electrodes.get(i).getRenderE());
		}
		return temp;
	}

    public synchronized ArrayList<RenderObject> getGraphs() {
        ArrayList<RenderObject> temp = new ArrayList<>();

        for(int i=0;i<electrodes.size();i++) {
            temp.add(electrodes.get(i).getRenderG(Math.max(Math.min((int) (2000/tick),500),50)));
        }
        return temp;
    }
	
	public synchronized void setSpeed(long speed) {
		tick = Math.min(Math.max(speed, 2),100);
	}
	
	public synchronized int getNumNeurons() {
		return neurons.size();
	}
	
	public synchronized int getNumAxons() {
		return axons.size();
	}
	
	public synchronized int getNumElectrodes() {
		return electrodes.size();
	}
	
	public synchronized float[] getCenterOfMass() {
		float[] ret = {0,0};
		for(int i=0;i<neurons.size();i++) {
			ret[0]=ret[0]+neurons.get(i).getX();
			ret[1]=ret[1]+neurons.get(i).getY();
		}
		ret[0]=ret[0]/neurons.size();
		ret[1]=ret[1]/neurons.size();
		return ret;
		
	}
	
	// CLEAR THE THREAD WHEN BRAIN DELETED
	@Override
	protected void finalize() throws Throwable {
		
		onPause();
		
		super.finalize();
	}

	public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Brain> CREATOR
            = new Parcelable.Creator<Brain>() {
        public Brain createFromParcel(Parcel in) {
            return new Brain(in);
        }

        public Brain[] newArray(int size) {
            return new Brain[size];
        }
    };
    
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(tick);
        
        out.writeTypedList(neurons);
        out.writeTypedList(axons);
        out.writeTypedList(electrodes);
        out.writeByte((byte) (STDP ? 1 : 0));

    }
    
    public synchronized boolean saveBrain(BufferedWriter file){
    	
    	float[] offset=getCenterOfMass();
    	
        try {
        	if (STDP)
        		file.write("<STDP>\r\n");
        	for(int i=0;i<neurons.size();i++)
        		neurons.get(i).writeToFile(file,offset);
        	
        	for(int i=0;i<axons.size();i++)
        		axons.get(i).writeToFile(file,offset);
    		
            file.close();
        } catch (IOException e) {      } 
        
        return true;
    }
    
    public synchronized boolean loadBrain(BufferedReader file, float[] offset) {
    	String s;
    	
        try {
        	while((s = file.readLine()) != null) {
        		if(s.contains("<NEURON>"))
        			neurons.add(new Neuron(file,offset));
        		if(s.contains("<AXON>"))
        			axons.add(new Axon(file,offset));
        		if(s.contains("<STDP>"))
        			STDP=true;
        	}
            file.close();
        } catch (IOException e) {      }
        
        update_CTM();
        
    	return true;
    }
    
	
}
