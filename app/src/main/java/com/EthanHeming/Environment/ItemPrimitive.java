package com.EthanHeming.Environment;

public class ItemPrimitive {
	
	
	private final static int STATE_X   = 0;
	private final static int STATE_Y   = 1;
	private final static int STATE_A   = 2;
	private final static int STATE_dX  = 3;
	private final static int STATE_dY  = 4;
	private final static int STATE_dA  = 5;
	private final static int STATE_ddX = 6;
	private final static int STATE_ddY = 7;
	private final static int STATE_ddA = 8;
	
	private float state[] = null;
	public float invmass = 0;
	public float friction = 0;
	
	public float getMass() {
		if (invmass==0)
			return 0;
		else
			return 1/invmass;
	}
	
	public float[] getLocation() {
		if (state != null)
			return new float[] { state[STATE_X], state[STATE_Y], state[STATE_A] };
		else
			return null;
	}
	
	public float[] getVelocity() {
		if (state != null)
			return new float[] { state[STATE_dX], state[STATE_dY], state[STATE_dA] };
		else
			return null;
	}
	
	public float[] getAcceleration() {
		if (state != null)
			return new float[] { state[STATE_ddX], state[STATE_ddY], state[STATE_ddA] };
		else
			return null;
	}
	
	public float[] getState() {
		if (state != null)
			return new float[] {  state[STATE_X],  state[STATE_Y],  state[STATE_A],
								 state[STATE_dX], state[STATE_dY], state[STATE_dA],
								state[STATE_ddX],state[STATE_ddY],state[STATE_ddA] };
		else
			return null;
	}
	
	public void setLocation(float X, float Y, float A) {
		if (!(state==null)) {
			state[STATE_X] = X;
			state[STATE_Y] = Y;
			state[STATE_A] = A;
		}
	}
	
	public void translate(float dX, float dY, float dA) {
		if (!(state==null)) {
			state[STATE_X] = state[STATE_X]+dX;
			state[STATE_Y] = state[STATE_Y]+dY;
			state[STATE_A] = state[STATE_A]+dA;
		}
	}
	
	public void setVelocity(float dX, float dY, float dA) {
		if (!(state==null)) {
			state[STATE_dX] = dX;
			state[STATE_dY] = dY;
			state[STATE_dA] = dA;
		}
	}
	
	public void accelerate(float ddX, float ddY, float ddA) {
		if (!(state==null)) {
			state[STATE_dX] = state[STATE_dX]+ddX;
			state[STATE_dY] = state[STATE_dY]+ddY;
			state[STATE_dA] = state[STATE_dA]+ddA;
		}
	}
	
	public void setAcceleration(float ddX, float ddY, float ddA) {
		if (!(state==null)) {
			state[STATE_ddX] = ddX;
			state[STATE_ddY] = ddY;
			state[STATE_ddA] = ddA;
		}
	}
	
	public void inputCommand(float[] command) {
		
	}
	
	public int getCommandSource() {
		return -1;
	}
	
	public float outputSensor() {
		return 0;
	}
	
	public int getSensorType() {
		return -1;
	}
	
	public void onPause() {
		
	}
	
	public void onResume() {
		
	}
	
	public void onUpdate(float timestep) {
		if (state != null) {
			state[STATE_X] =  state[STATE_X]  + state[STATE_dX]*timestep;
			state[STATE_Y] =  state[STATE_Y]  + state[STATE_dY]*timestep;
			state[STATE_A] =  state[STATE_A]  + state[STATE_dA]*timestep;
			state[STATE_dX] = state[STATE_dX] + state[STATE_ddX]*timestep;
			state[STATE_dY] = state[STATE_dY] + state[STATE_ddY]*timestep;
			state[STATE_dA] = state[STATE_dA] + state[STATE_ddA]*timestep;
		}
		
		
		
	}
}
