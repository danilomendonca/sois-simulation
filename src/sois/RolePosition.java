package sois;

import peersim.core.Node;

public class RolePosition {
	
	Node node;
	double FF_e;
	
	public RolePosition(Node node, double FF_e) {
		this.node = node;
		this.FF_e = FF_e;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public double getFF_e() {
		return FF_e;
	}

	public void setFF_e(double fF_e) {
		FF_e = fF_e;
	}
	
	

}
