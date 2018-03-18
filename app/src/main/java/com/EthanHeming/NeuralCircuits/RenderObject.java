package com.EthanHeming.NeuralCircuits;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;


public class RenderObject {
	
	public static final int NEURON=1, AXON=2, SPIKES=3, ELECTRODE=4, GRAPH=5;
	
	int type;				// the type of object to render
	public String subtype; 	// the sub-type of object to render
	float[] loc;			// the location of the object (if applicable)
	Path path;				// path
	float[] v;				// the second value of the object (if applicable)
	public Bitmap bm;		// A cached bitmap for semi-permanent items
	Canvas cshCanvas;		// Canvas for drawing on bm;
	boolean reCache;		// Cache redraw switch
	
	public RenderObject(int R, String neuron_type, float[] L, Path P, float[] V) {

		type = R;
		subtype = neuron_type;
		reCache = true;
		
		if (P!=null) {
			path = new Path(P);
		}
		else path = null;
		
		if (L!=null) {
			loc = new float[L.length];
            System.arraycopy(L,0,loc,0,L.length);
			//for (int i=0;i<L.length;i++)
			//	loc[i] = L[i];
		} else loc= new float[2];
		
		if (V!=null) {
			v = new float[V.length];
            System.arraycopy(V,0,v,0,V.length);
			//for (int i=0;i<V.length;i++)
			//	v[i] = V[i];
		}
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public void setSubtype(String sub) {
		this.subtype = sub;
	}
	
	public void setLoc(float[] L) {
		if (L!=null) {
			loc = new float[L.length];
            System.arraycopy(L,0,loc,0,L.length);
			//for (int i=0;i<L.length;i++)
			//	loc[i] = L[i];
		} else loc=null;
	}
	
	public void setV(float[] V) {
		if (V!=null) {
			v = new float[V.length];
            System.arraycopy(V,0,v,0,V.length);
			//for (int i=0;i<V.length;i++)
			//	v[i] = V[i];
		}
	}
	
	public float[] getV() {
		return v;
	}
	
	public void redraw() {
		reCache = true;
	}
	
	public synchronized void render(Canvas canvas,GraphicsPalette palette) {
		
		switch (type) {
		case NEURON:
			
			if (palette.BM_Neuron_Types.containsKey(subtype)) {
				canvas.drawBitmap(
					palette.BM_Neuron_Types.get(subtype), 
					loc[0]-palette.BM_Neuron_Types.get(subtype).getWidth()/2,
					loc[1]-palette.BM_Neuron_Types.get(subtype).getHeight()/2,
					null);
			}
			else {
				canvas.drawBitmap(
					palette.BM_Neuron, 
					loc[0]-palette.BM_Neuron.getWidth()/2,
					loc[1]-palette.BM_Neuron.getHeight()/2,
					null);
			}
			
			palette.T.setAlpha(Math.max(Math.min(  (int)((v[0]+65)*3  ),255),0));
			canvas.drawBitmap(
					palette.BM_Firing, 
					loc[0]-palette.BM_Firing.getWidth()/2,
					loc[1]-palette.BM_Firing.getHeight()/2,
					palette.T);
			
			break;
		case AXON:
			if (reCache) {
				int borders = 20;
				RectF bounds = new RectF();
				
				path.computeBounds(bounds, true);

                bm = Bitmap.createBitmap((int)bounds.width()+2*borders, (int)bounds.height()+2*borders, Bitmap.Config.ARGB_8888);
                try {
                    cshCanvas = new Canvas(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

                path.offset(-bounds.left+borders, -bounds.top+borders);
				palette.A.setARGB(255, (int)(255-(v[0]+25)*5), 0, (int)((v[0]+25)*5));
				palette.A.setAlpha(125);
				palette.A.setStrokeWidth(v[1]/40*palette.mxAxon);
				cshCanvas.drawPath(path, palette.A);
				palette.A.setAlpha(255);
				palette.A.setStrokeWidth(v[1]/80*palette.mxAxon);
				cshCanvas.drawPath(path, palette.A);
				path.offset(bounds.left-borders, bounds.top-borders);
				
				loc[0] = bounds.left-borders;
				loc[1] = bounds.top -borders;
				
				reCache = false;
			}
			
			
			canvas.drawBitmap(
					bm,
					loc[0],
					loc[1],
					null);
			
			
			break;
		case SPIKES:
			for(int i=0;i<loc.length/2;i++)
				canvas.drawBitmap(
						palette.BM_Spike,
						loc[i*2]-palette.BM_Spike.getWidth()/2,
						loc[i*2+1]-palette.BM_Spike.getHeight()/2,
						null);

			break;
		case ELECTRODE:
			
			palette.G.setColor(Color.parseColor(subtype));
			
			canvas.drawBitmap(
					palette.BM_Electrode, 
					loc[0],
					loc[1]-palette.BM_Electrode.getHeight(),
					null);
			
			canvas.drawCircle(
					loc[0]+palette.BM_Electrode.getWidth(),
					loc[1]-palette.BM_Electrode.getHeight(),
					palette.mxAxon/2,
					palette.G);
			
			break;
		case GRAPH:
			
			palette.G.setColor(Color.parseColor(subtype));
			float vm = 0;
            for (float tv : v) {
				vm=vm+tv/v.length;
			}
			float[] pts = new float[v.length*4];
			
			pts[0]=0;
			pts[1]=0;
			
			for (int i=0;i<(v.length-1);i++) {
				pts[i*4+2]=pts[i*4+4]=(float)i/v.length*palette.graphWidth+loc[0];
				pts[i*4+3]=pts[i*4+5]=(v[i]-vm)*palette.graphHeight*0.5f+loc[1];
			}
			
			pts[pts.length-2]=palette.graphWidth+loc[0];
			pts[pts.length-1]=(v[v.length-1]-vm)/10+loc[1];
			
			canvas.drawLines(pts, palette.G);
			canvas.drawCircle(pts[0], pts[1], palette.mxAxon/2, palette.G);
			
			break;
		default:

		}
	}
	
	public RectF getBounds(GraphicsPalette palette) {
		
		RectF bounds = null;
		
		switch (type) {
		case NEURON:
			
			bounds = new RectF(
					loc[0]-palette.BM_Neuron.getWidth()/2,
					loc[1]-palette.BM_Neuron.getHeight()/2,
					loc[0]+palette.BM_Neuron.getWidth()/2,
					loc[1]+palette.BM_Neuron.getHeight()/2
					);
			break;
		case AXON:
			
			bounds = new RectF();


			path.computeBounds(bounds, true);

			break;
		
		case ELECTRODE:
			
			bounds = new RectF(
					loc[0],
					loc[1]-palette.BM_Electrode.getHeight(),
					loc[0]+palette.BM_Electrode.getWidth(),
					loc[1]
					);
			break;
			
		case SPIKES:
		case GRAPH:
		default:
		}
		
		return bounds;
	}

}
