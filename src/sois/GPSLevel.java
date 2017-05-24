package sois;

import peersim.util.ExtendedRandom;
import peersim.vector.SingleValueHolder;

public class GPSLevel extends SingleValueHolder{
	
	ExtendedRandom r;

	public GPSLevel() {
		super("");
		initRandom();	
		tickValue();
	}
	
	private void initRandom(){
		this.r = new ExtendedRandom(System.currentTimeMillis());
	}
	
	public void tickValue(){
		setValue(Math.max(0, Math.min(1, r.nextGaussian()/5 + 0.7)))   ;
	}

	public void use(float delta) {
		setValue(Math.max(getValue() - delta, 0));
	}
}