package sois;

import java.util.Map;

import peersim.util.ExtendedRandom;
import peersim.vector.SingleValueHolder;

public class BatteryLevel extends SingleValueHolder{
	
	ExtendedRandom r;
	Map<Long, Double> levels;

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
}