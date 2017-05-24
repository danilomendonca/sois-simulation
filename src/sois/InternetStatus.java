package sois;

import peersim.core.CommonState;
import peersim.vector.SingleValueHolder;

public class InternetStatus extends SingleValueHolder{
	
	public final static double INTERNET_CELL = 0;
	public final static double INTERNET_WIFI = 1;

	public InternetStatus() {
		super("");
		initValue();
	}
	
	public void initValue(){
		setValue(CommonState.r.nextInt(1));
	}
	
	public boolean isCell(){
		return getValue() == INTERNET_CELL;
	}
	
	public boolean isWiFi(){
		return getValue() == INTERNET_WIFI;
	}
}