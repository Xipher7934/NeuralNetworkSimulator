package com.EthanHeming.NeuralNetworkSimulator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore.Images;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.EthanHeming.NeuralCircuits.Axon;
import com.EthanHeming.NeuralCircuits.Brain;
import com.EthanHeming.NeuralCircuits.Electrode;
import com.EthanHeming.NeuralCircuits.GraphicsPalette;
import com.EthanHeming.NeuralCircuits.Neuron;
import com.EthanHeming.NeuralCircuits.RenderObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorFragment extends Fragment implements OnTouchListener{
	
	private Brain B = null;
	private Neuron N = null;
	private Axon A = null;
	private Electrode E = null;
	private Path P = null;
	private float[] oLoc = {0,0,0,0,0,0};
	private boolean debug = false;
	
	private GraphicsPalette palette;
	private SurfaceView SV = null;
    private float[] offset;

    Matrix brain2screen = new Matrix();
    Matrix screen2brain = new Matrix();



	editorInterface mCallback;
	
	private Thread thread = null;
	public Boolean running = false;
	public float latency = 0;
	
	private final int EDITNONE  = 0;
	private final int SCROLL    = 1;
	private final int DELETE    = 2;
	private final int CREATEAXON= 3;
	private final int EDITNEURON= 4;
	private final int EDITAXON  = 5;
	private final int ELECTRODE = 6;
    private final int EDITGRAPH = 7;
	private int editMode = EDITNONE;
    private float graphsize;
    private boolean graphactive;
    private int numgraphs = 0;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putFloatArray("Offset", offset);
        savedInstanceState.putFloat("GraphSize", graphsize);
        savedInstanceState.putBoolean("GraphActive", graphactive);
	}
	
	public interface editorInterface {
        public Brain getBrain();
        public HashMap<String, HashMap<String, String>> getIOTypes();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (editorInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Brain=getBrain()");
        }
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	
		

		
		getActivity();
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
		        getString(R.string.editor_preferences_key), Context.MODE_PRIVATE);
		
		try {
			int currVersion = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
			if (sharedPref.getInt("HAS_DONE_TUTORIAL", 0)<currVersion) {
				sharedPref.edit().putInt("HAS_DONE_TUTORIAL", currVersion).commit();
				askTutorial();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		P=new Path();
		palette = new GraphicsPalette(getActivity());
		super.onCreate(savedInstanceState);
	}
	
	private void askTutorial() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("New to this?");
		builder.setMessage("It looks like you haven't viewed the help file for this version. Would you like to do that now?");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	
	            doHelp();
	        }
	     });
	    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     });
	     builder.show();
		
		
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		
	    View fragmentView = inflater.inflate(R.layout.fragment_editor, container, false);
		SV = (SurfaceView)fragmentView.findViewById(R.id.editorSurfaceView);
		SV.setOnTouchListener(this);
        ImageButton HB = (ImageButton) fragmentView.findViewById(R.id.help_button);
        ImageButton SB = (ImageButton) fragmentView.findViewById(R.id.save_button);
        ImageButton LB = (ImageButton) fragmentView.findViewById(R.id.load_button);
        ImageButton shB = (ImageButton) fragmentView.findViewById(R.id.share_button);
        SeekBar skBar = (SeekBar) fragmentView.findViewById(R.id.speed_seekBar);
		
		if (offset==null)
            if ((savedInstanceState==null)||(offset=savedInstanceState.getFloatArray("Offset"))==null) {
			offset=new float[4];
			offset[0]=0;offset[1]=0;offset[2]=1;offset[3]=0;
		}

        if (graphsize==0) {
            if (savedInstanceState!=null) {
                graphsize = savedInstanceState.getFloat("GraphSize",1);
                graphactive=savedInstanceState.getBoolean("GraphActive",true);
            } else {
                graphsize=1;
                graphactive=true;
            }
        }




		
		HB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                doHelp();
            }
        });
		
		SB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                doSaveBrain();
            }
        });
		
		LB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                doLoadBrain();
            }
        });
		
		shB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                doShare();
            }
        });
		
		skBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (B != null)
                    B.setSpeed(40 - seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }
        });
		
		return fragmentView;
	}

    private boolean inGraphSpace(float[] loc) {
        if (graphactive)
            return (
                 (loc[0]>SV.getWidth()/4-25)
              && (loc[0]<SV.getWidth()*3/4+25)
              && (loc[1]<SV.getHeight()/40f*graphsize*(numgraphs+0.5f)+35)
            );
        else
            return (
                    (loc[0]>SV.getWidth()/2-palette.BM_GraphDrawer.getWidth()/2)
                 && (loc[0]<SV.getWidth()/2+palette.BM_GraphDrawer.getWidth()/2)
                 && (loc[1]<palette.BM_GraphDrawer.getHeight())
            );
    }

    @Override
	public void onResume() {
		
		super.onResume();
		thread = new NFThread(this);
		thread.start();
	}
	
	@Override
	public void onPause() {
		
		if(thread != null) {
			running = false;
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {
					//try again shutting down the thread
				}
			}
			thread=null;
		}
		super.onPause();
	}
	
	
	public synchronized void setBrain(Brain B1) {
		B=B1;
	}
	
	// THINGS TO UPDATE VISUALS
	private synchronized void Update() {

        

        Canvas canvas = null;
		
		if(B==null) B=mCallback.getBrain();
		if(B==null) return;
		
		ArrayList<RenderObject> RS = B.getRenders();
        ArrayList<RenderObject> ES = B.getGraphs();
        numgraphs=ES.size();

		try {
			canvas = SV.getHolder().lockCanvas();
			if (canvas!=null)
				synchronized (SV.getHolder()) {

                    // Set the transformation matrixes that deal with converting from editor to brain space
                    brain2screen.reset();
                    brain2screen.preTranslate(-offset[0], -offset[1]);
                    brain2screen.preScale(offset[2], offset[2], offset[0] + SV.getWidth() / 2, offset[1] + SV.getHeight() / 2);
                    brain2screen.invert(screen2brain);

                    canvas.drawColor(Color.BLACK);
                    canvas.save();
                    canvas.concat(brain2screen);
                    for (int i = 0; i < RS.size(); i++)
                        RS.get(i).render(canvas, palette);
                    canvas.restore();

                    if (ES.size() > 0)
                        if (graphactive && graphsize>=1) {
                            canvas.drawRect(SV.getWidth()/4, 0, SV.getWidth()*3/4, SV.getHeight()/40f*graphsize*(numgraphs+0.5f)+10, palette.BG);
                            canvas.save();
                            canvas.translate(SV.getWidth() / 4, SV.getHeight() / 40);
                            //canvas.scale(1, graphsize);
                            palette.graphWidth = SV.getWidth() / 2;
                            palette.graphHeight = (int) (graphsize * 30);
                            for (int i = 0; i < ES.size(); i++) {
                                canvas.translate(0, SV.getHeight() / 40 * graphsize);
                                ES.get(i).render(canvas, palette);
                            }
                            canvas.restore();
                        }
                        else
                            canvas.drawBitmap(
                                    palette.BM_GraphDrawer,
                                    SV.getWidth()/2-palette.BM_GraphDrawer.getWidth()/2,
                                    0,
                                    null);




                    if (editMode==CREATEAXON) {
                        palette.A.setColor(Color.BLUE);
                        palette.A.setStrokeWidth(palette.mxAxon/3);
                        canvas.drawPath(P, palette.A);

                    }
					
					if ((editMode==EDITNEURON) && (oLoc[4]>0) && (SystemClock.elapsedRealtime()>(oLoc[4]+900))) {
						editMode=EDITNONE;
						oLoc[4]=0;
						getActivity().runOnUiThread(new Runnable() {
		                     public void run() {
		                    	 doEditNeuron(N);
		                    }
		                });
						
					}
					
					if (editMode==DELETE) {
						canvas.drawPath(P, palette.D);
					}
					
					if (editMode==EDITAXON) {
						canvas.drawBitmap(
								palette.E_A, 
								oLoc[0]-palette.E_A.getWidth()/2,
								oLoc[1]-palette.E_A.getHeight()/2,
								null);
					}
					
					
					//if (doingTutorial>0) {
					//	showTutorial(canvas);
					//}
						
					
					canvas.drawBitmap(
							palette.BM_Electrode, 
							0, 
							0, 
							null);
					
					if (debug) {
						int i=1;
						canvas.drawText("Value: "+String.valueOf(offset[2]), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
						canvas.drawText("Latency: "+String.valueOf((int)latency), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
						canvas.drawText("Editmode: "+String.valueOf(editMode), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
						canvas.drawText("Neuron: "+String.valueOf(N), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
						canvas.drawText("Axon: "+String.valueOf(A), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
						if (A!=null) canvas.drawText("Axon Strength: "+String.valueOf(A.getStr()), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
						if (A!=null) canvas.drawText("Axon post: "+String.valueOf(A.postspikes.size()), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
						canvas.drawText("Electrode: "+String.valueOf(E), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
                        canvas.drawText("Matrix: "+String.valueOf(brain2screen), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
                        canvas.drawText("Brain: "+String.valueOf(B), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
                        canvas.drawText("oLoc: "+String.valueOf(oLoc[1]), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;
                        canvas.drawText("oLoc: "+String.valueOf(oLoc[2]), canvas.getWidth()/2, palette.text.getTextSize()*i, palette.text);i++;

                    }
					
					
					
						
					
				}
		} finally {
			if (canvas != null) {
				SV.getHolder().unlockCanvasAndPost(canvas);
			}
		}
			
	}
	
	// THREAD TO RUN THE UPDATE()
	private class NFThread extends Thread {
		
		int tick = 15;
		EditorFragment parent;
		
		public NFThread(EditorFragment p) {
			parent = p;
		}
		
		@Override
		public void run() {
			long begin_time = SystemClock.elapsedRealtime();
			long cycle_time = SystemClock.elapsedRealtime();
			
			parent.running = true;
			
			while (parent.running) {
				cycle_time = SystemClock.elapsedRealtime()-begin_time;
				if (cycle_time<tick){
					try {
						Thread.sleep(tick-cycle_time,0);
					} catch (InterruptedException e) {
					}
				}
				begin_time = SystemClock.elapsedRealtime();
				
				parent.Update();
				parent.latency = parent.latency*9/10 + ((float)(SystemClock.elapsedRealtime()-begin_time))/10;
				
			}
		}
	}
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {


		if(B==null) return true;
		float[] trueLoc = {0,0};
		float[] eLoc = {0,0};
		trueLoc[0] = event.getX();
		trueLoc[1] = event.getY();
		float[] brainLoc = {0,0};
        screen2brain.mapPoints(brainLoc,trueLoc);

		eLoc[0]=brainLoc[0]-(palette.BM_Electrode.getWidth());
		eLoc[1]=brainLoc[1]+(palette.BM_Electrode.getHeight());
		
		
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			oLoc[0]=trueLoc[0];
			oLoc[1]=trueLoc[1];
			oLoc[4]= SystemClock.elapsedRealtime();
			N = B.getNeuron(brainLoc, palette.BM_Neuron.getWidth()/2);
			A = B.getAxon(brainLoc, palette.BM_Neuron.getWidth()/3);
			E = B.getElectrode(eLoc, palette.BM_Neuron.getWidth()/2);
			P.reset();
			
			if (N==null)
				P.moveTo(trueLoc[0],trueLoc[1]);
			else {
                float[] tempLoc={0,0};
                brain2screen.mapPoints(tempLoc,N.getLocation());
                P.moveTo(tempLoc[0], tempLoc[1]);
            }
            if (inGraphSpace(trueLoc)) {
                editMode=EDITGRAPH;
                oLoc[2]=graphsize;
            }
            else if (E!=null) {
				editMode=ELECTRODE;
			}
			else if ((trueLoc[0]<palette.BM_Electrode.getWidth()) && (trueLoc[1]<palette.BM_Electrode.getHeight())) {
				editMode=ELECTRODE;
				E=B.addElectrode(eLoc);
			}
			else if((N==null) && (A==null))
				editMode=DELETE;
			else if (N==null) {
				editMode=EDITAXON;
				oLoc[2]=A.getStr();
				oLoc[3]=A.getSpeed();
			}
			else {
				editMode=EDITNEURON;
			}

			break;
		case MotionEvent.ACTION_UP:
			Neuron tN = B.getNeuron(brainLoc,palette.BM_Neuron.getWidth()/2);
			PathMeasure PM= new PathMeasure(P, false);
			if (editMode==ELECTRODE) {
				if ((trueLoc[0]<palette.BM_Electrode.getWidth()) && (trueLoc[1]<palette.BM_Electrode.getHeight())) {
					B.removeElectrode(E);
                    if (numgraphs==1)
                        graphactive=true;
				}
			}
			else if (editMode==EDITNEURON) {
				if (debug)
					Toast.makeText(getActivity(), N.getType(), Toast.LENGTH_SHORT).show(); 
				N.stimulate(50);
			}
			else if ((editMode==CREATEAXON) & (tN != null)) {
                if (debug)
                    Toast.makeText(getActivity(), tN.getType(), Toast.LENGTH_SHORT).show();
                float[] tempLoc={0,0};
                brain2screen.mapPoints(tempLoc,tN.getLocation());
                P.lineTo(tempLoc[0], tempLoc[1]);

				Path D=new Path(P);
				D.transform(screen2brain);

				B.addAxon(D);
			}
			else if ((editMode==DELETE) & (PM.getLength()<palette.BM_Neuron.getWidth()/2)) {
				B.addNeuron(getActivity(),brainLoc);
			}
			else if (editMode==DELETE) {
				Path D=new Path(P);
				D.transform(screen2brain);
				B.removeByPath(D,palette.BM_Neuron.getWidth()/2);
			}
            else if (editMode==EDITGRAPH) {
                if (graphsize<1) {
                    graphsize=1;
                    graphactive=false;

                }
                else if ( ((oLoc[0]-trueLoc[0])*(oLoc[0]-trueLoc[0])+(oLoc[1]-trueLoc[1])*(oLoc[1]-trueLoc[1]))< palette.BM_Neuron.getWidth()/2 ) {
                    graphactive=!graphactive;
                }
            }
			
			editMode=EDITNONE;
			//N=null;
			//A=null;
			//E=null;
			for (int i=0;i<5;i++) oLoc[i]=0;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oLoc[0]=(event.getX(0)+event.getX(1))/2;
			oLoc[1]=(event.getY(0)+event.getY(1))/2;
			oLoc[2]=offset[0];
			oLoc[3]=offset[1];
            oLoc[4]=offset[2];
			oLoc[5]= (float) Math.sqrt(((event.getX(0) - event.getX(1)) * (event.getX(0) - event.getX(1))) + ((event.getY(0) - event.getY(1)) * (event.getY(0) - event.getY(1))));

			editMode=SCROLL;
			
			break;
		case MotionEvent.ACTION_POINTER_UP:
			editMode=EDITNONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (editMode==EDITNONE) break;
			
			if ((editMode==EDITNEURON) && oLoc[4]>0)
				if (B.getNeuron(brainLoc,palette.BM_Neuron.getWidth()/2)!=N)
					editMode=CREATEAXON;
			
			
			if ((editMode==CREATEAXON && B.getNeuron(brainLoc,palette.BM_Neuron.getWidth()/2)==null ) || editMode==DELETE) {
				P.lineTo(trueLoc[0], trueLoc[1]);
			}
			
			if (editMode==ELECTRODE) {
				E.move_electrode(eLoc);
			}

			else if(editMode==SCROLL) {
				offset[0]=oLoc[2]-((event.getX(0)+event.getX(1))/2-oLoc[0])/offset[2];
				offset[1]=oLoc[3]-((event.getY(0)+event.getY(1))/2-oLoc[1])/offset[2];
                offset[2]=(float) Math.max(Math.min(oLoc[4]*Math.sqrt( ((event.getX(0)-event.getX(1))*(event.getX(0)-event.getX(1))) + ((event.getY(0)-event.getY(1))*(event.getY(0)-event.getY(1)))  )/oLoc[5],1.0),0.1);
			}
			else if(editMode==EDITAXON) {
				A.setStr(Math.min(Math.max(-25,((oLoc[1]-trueLoc[1])/10+oLoc[2])),25));
				A.setSpeed(Math.min(Math.max(10,((-oLoc[0]+trueLoc[0])/4+oLoc[3])),60));
			}
            else if (editMode==EDITGRAPH) {
                if ((graphactive==false) && !inGraphSpace(trueLoc))
                    graphactive=true;
                else
                    graphsize=Math.max(Math.min(trueLoc[1]/oLoc[1]*oLoc[2],6f),0.8f);
            }
				
			break;
			
		
		}
		
		
		return true;
	}
	
	
	private void doHelp() {

		getActivity().getFragmentManager()
		.beginTransaction()
		.replace(R.id.mainLayout, new HelpFragment())
		.addToBackStack(null)
		.commit();
	}
	
	
	private void doSaveBrain() {
		
        if (!(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))) {
        	Toast.makeText(getActivity(), "No access to external storage.", Toast.LENGTH_SHORT).show();
        	return;
        }
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Save neural net as...");

		// Set up the input
		final EditText input = new EditText(getActivity());
		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		// Set up the buttons
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { 
			@Override
			public void onClick(DialogInterface dialog, int which) {

				String fname = input.getText().toString();
				if (fname.equals("")) {
					dialog.cancel();
				}

				try {
					File folder = new File(Environment.getExternalStorageDirectory() + "/NeuralNetworks");
					File file = new File(folder,fname+".brn");
                    if (!folder.isDirectory()) {
                        if (!folder.mkdirs()) {
                            Toast.makeText(getActivity(), "Error saving file.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
					FileWriter fW = new FileWriter(file);
					BufferedWriter bW = new BufferedWriter(fW);
					B.saveBrain(bW);
					fW.close();
					Toast.makeText(getActivity(), "Neural network saving to: "+fname, Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getActivity(), "Error saving file.", Toast.LENGTH_SHORT).show();
				}
		        
		        
		        
		        
		    }
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
		input.requestFocus();
		
		
		
		 
		
	}
	
	private void doLoadBrain() {
		
		if (!(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))) {
        	Toast.makeText(getActivity(), "No access to external storage.", Toast.LENGTH_SHORT).show();
        	return;
        }
		
		final File folder = new File(Environment.getExternalStorageDirectory() + "/NeuralNetworks");
        if (!folder.isDirectory()) {
            if (!folder.mkdirs()) {
                Toast.makeText(getActivity(), "Error accessing save directory.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
		
		String[] stringArray = folder.list();
		final ArrayList<String> fileList = new ArrayList<String>();

        for(String ts : stringArray) {
            if (ts.contains(".brn"))
                fileList.add(ts.replace(".brn",""));
        }

		if (fileList.size()<1) {
			Toast.makeText(getActivity(), "No saved neural networks to load.", Toast.LENGTH_SHORT).show();
			return;
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.load_file_item, fileList);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Load neural net from...");
		
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				final String fname = adapter.getItem(which);
				try {
					File folder = new File(Environment.getExternalStorageDirectory() + "/NeuralNetworks");
					File file = new File(folder,fname+".brn");
					
					FileReader fR = new FileReader(file);
					BufferedReader bR = new BufferedReader(fR);
					float[] temp = new float[] {SV.getWidth()/2,SV.getHeight()/2};
                    screen2brain.mapPoints(temp);
                    temp[0]=-temp[0];temp[1]=-temp[1];
					B.loadBrain(bR,temp);
					bR.close();
					Toast.makeText(getActivity(), "Brain loading from: "+fname, Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getActivity(), "Error loading file.", Toast.LENGTH_SHORT).show();
				}
			}
			

		});
		

		//builder.setView(modeList);

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
		
		
	}
	
	
	public void doEditNeuron(final Neuron NE) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Change neuron type... ");
		
		/*final RadioGroup RG = new RadioGroup(getActivity());
		dialogview.addView(RG);
		RadioButton RB;
		int toSet = -1;
		
		for (Map.Entry<String, String> e : mCallback.getIOTypes().entrySet()) {
			RB = new RadioButton(getActivity());
			RB.setText(e.getValue());
			RB.setTag(e.getKey());
			if(NE.getType().equals(e.getKey()))
				toSet=RB.getId();
			RG.addView(RB);
		}
		if (toSet!=-1)
			RG.check(toSet);*/
		
		ArrayList<HashMap<String, String>> typeset = new ArrayList<HashMap<String, String>>();
		for (Map.Entry<String, HashMap<String, String>> e : mCallback.getIOTypes().entrySet()) {
			typeset.add(e.getValue());
		}
		
		String[] from = { "icon","name","desc" };
		int[] to = { R.id.icon,R.id.name,R.id.desc};
		
		final SimpleAdapter adapter = new SimpleAdapter(getActivity(), typeset, R.layout.type_listitem, from, to);
		
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				@SuppressWarnings("unchecked")
				final HashMap<String, String> choice = (HashMap<String, String>) adapter.getItem(which);
				
				if (NE!=null)
					NE.setType(choice.get("type"));
				
				/*getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getActivity(), choice.get("name"),
								Toast.LENGTH_SHORT).show();
					}
				});*/
				
		    }
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();
		
	}
	
	public Bitmap doScreenShot(int resolution) {
		
		
		if(B==null) B=mCallback.getBrain();
		if(B==null) return null;
		
		long maxMem;
		RectF bounds = new RectF();
		RectF tempR;
		
		ArrayList<RenderObject> RS = B.getRenders();
		for (int i=0;i<RS.size();i++)
			if ((tempR = RS.get(i).getBounds(palette))!=null)
				bounds.union(tempR);
		if (resolution==0)
			maxMem = Runtime.getRuntime().maxMemory()/32;
		else
			maxMem=resolution*resolution;
		float scale = (bounds.width()*bounds.height())/(maxMem);
		if (scale<1) scale=1;
		else scale=(float) Math.sqrt(scale);
		try {
			Bitmap canvasBitmap = Bitmap.createBitmap((int)(bounds.width()/scale+100), (int)(bounds.height()/scale+100), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(canvasBitmap);
			canvas.save();
            canvas.scale(1/scale, 1/scale);
            canvas.translate(50-bounds.left,50-bounds.top);
			canvas.drawColor(Color.BLACK);
			for (int i=0;i<RS.size();i++)
				RS.get(i).render(canvas, palette);
			
			canvas.restore();
			
			return canvasBitmap;
		} catch (OutOfMemoryError e) {
			return null;
		}
		
		
		
		

	}
	
	public void doShareScreenshot() {
		
		final Bitmap screenshot = doScreenShot(0);

		if (screenshot == null) {
			Toast.makeText(getActivity(), "Could not obtain screenshot",Toast.LENGTH_SHORT).show();
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Share this picture?");
		
		ImageView IV = new ImageView(getActivity());
		
		
		IV.setImageBitmap(screenshot);
		
		
		builder.setView(IV);
		
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	
		    	String vers = "";
		    	try {
					vers=" v"+
							getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
				} catch (NameNotFoundException e) {
					
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		      	screenshot.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		    	String path;
				path = Images.Media.insertImage(
							getActivity().getContentResolver(), 
							screenshot, 
							"Neural Network", 
							"made with Neural Network Simulator"+ vers);
				
		        
		    	Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND); 
			    sharingIntent.setType("image/jpeg");
			    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Neural Network");
			    sharingIntent.putExtra(android.content.Intent.EXTRA_TITLE, "Neural Network");
			    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Built with Neural Network Simulator"+ vers + ", for Android.");
			    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));



			    startActivity(Intent.createChooser(sharingIntent, "Share via:"));
			    
		    }
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});
		
		builder.show();
		
		
		/**/
		
		
		
	}
	
	public void doShare() {
		doShareScreenshot();
		/*
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Picture or Video?");
		
		builder.setPositiveButton("Picture", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        doShareScreenshot();
		    }
		});
		
		builder.setNeutralButton("Video", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});
		
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		builder.show();*/
		
	}
	
}
