package sois;

import peersim.core.CommonState;
import peersim.vector.SingleValueHolder;

public class GPSStatus extends SingleValueHolder{
	
	public final static double GPS_OFF = 0;
	public final static double GPS_ON = 1;

	public GPSStatus() {
		super("");
		initValue();
	}
	
	public void initValue(){
		setValue(CommonState.r.nextInt(10) > 3 ? GPS_ON : GPS_OFF);
	}
	
	public boolean isOff(){
		return getValue() == GPS_OFF;
	}
	
	public boolean isOn(){
		return getValue() == GPS_ON;
	}
}