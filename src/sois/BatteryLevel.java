package sois;

import peersim.util.ExtendedRandom;
import peersim.vector.SingleValueHolder;

public class BatteryLevel extends SingleValueHolder{
	
	ExtendedRandom r;

	public BatteryLevel() {
		super("");
		initRandom();
		initValue();
	}
	
	private void initRandom(){
		this.r = new ExtendedRandom(System.currentTimeMillis());
	}
	
	public void initValue(){
		setValue(r.nextDouble());
	}

	private static final float DELTA_USE = 0.8F;
	
	public void use() {
		setValue(getValue() * DELTA_USE);
	}
}