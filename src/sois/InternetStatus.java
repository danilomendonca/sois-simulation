package sois;

import peersim.util.ExtendedRandom;
import peersim.vector.SingleValueHolder;

public class InternetStatus extends SingleValueHolder{
	
	ExtendedRandom r;
	public final static double INTERNET_CELL = 0;
	public final static double INTERNET_WIFI = 1;

	public InternetStatus() {
		super("");
		initRandom();	
		initValue();
	}
	
	private void initRandom(){
		this.r = new ExtendedRandom(System.currentTimeMillis());
	}
	
	public void initValue(){
		setValue(r.nextInt(1));
	}
	
	public boolean isCell(){
		return getValue() == INTERNET_CELL;
	}
	
	public boolean isWiFi(){
		return getValue() == INTERNET_WIFI;
	}
}