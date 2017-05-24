package sois;

import peersim.core.CommonState;
import peersim.vector.SingleValueHolder;

public class BatteryLevel extends SingleValueHolder{
	
	public BatteryLevel() {
		super("");
		initValue();
	}
	
	public void initValue(){
		setValue(Math.max(0.1, CommonState.r.nextDouble()));

	}

	public void use(float delta) {
		setValue(Math.max(getValue() - delta, 0));
	}
}