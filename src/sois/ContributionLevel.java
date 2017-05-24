package sois;

import peersim.vector.SingleValueHolder;

public class ContributionLevel extends SingleValueHolder{
	
	public ContributionLevel() {
		super("");
		initValue();
	}
	
	public void initValue() {
		setValue(0);
	}
	
	public void inc(float delta) {
		setValue(getValue() + delta);
	}
}