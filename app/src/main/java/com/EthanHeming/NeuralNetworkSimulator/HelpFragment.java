package com.EthanHeming.NeuralNetworkSimulator;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HelpFragment extends Fragment {

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	    // Inflate the layout for this fragment
		
	    View fragmentView = inflater.inflate(R.layout.help_file, container, false);
	    
	    return fragmentView;
	}
}
