package sois;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import peersim.vector.SingleValueHolder;

public class RoleFitness extends SingleValueHolder implements CDProtocol, EDProtocol{
		
	public RoleFitness(String prefix) {
		super(prefix);
	}

	/**
	 * Should update the value of the FS
	 */
	@Override
	public void nextCycle(Node node, int protocolID) {
		NodeElection.addNode(node);
		setValue(evalFitnessFunction(node));
		List<Node> currentPeers = getCurrentPeers();
		
		checkVacancy(node, currentPeers, protocolID);
		checkResignation(node);
		checkChallenge(node);
		
		
		NodeElection.get(node).setPeers(currentPeers);
	}
	
	private enum EventType{
		VACANCY,
		RESIGNATION,
		CHALLENGE
	}
	
	private void triggerVacancy(Node node, int protocolID){
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        handleVacancy(node, protocolID);
        for (int i=0; i<linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);
            // Failure handling
            if (!peer.isUp())
                continue;
            
    		/*((Transport)node.getProtocol(FastConfig.getTransport(protocolID))).
			send(
				node,
				peer,
				EventType.VACANCY,
				protocolID
			);*/
            
            RoleFitness peerProtocol = (RoleFitness) peer.getProtocol(protocolID);
            peerProtocol.handleVacancy(peer, protocolID);//should call a sendSomething method to simulate a network call
        }
        joinElection(node, protocolID);
	}
	
	@Override
	public void processEvent(Node node, int protocolID, Object event) {
		if((EventType) event == EventType.VACANCY)
			handleVacancy(node, protocolID);
	}
	
	private void handleVacancy(Node node, int protocolID) {
		if(!NodeElection.get(node).isVacancy()){
			NodeElection.get(node).startVacancyElection();
		}
	}
	
	private void joinElection(Node node, int protocolID){
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        Double FS_a = evalFitnessFunction(node);
        NodeElection.get(node).updateFS(node, FS_a);
        for (int i=0; i<linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);
            // Failure handling
            if (!peer.isUp())
                continue;

            NodeElection.get(peer).updateFS(node, FS_a);
        }
	}

	private void triggerResignation(Node electedNode){
		System.out.println(this.value);
	}
	
	private void triggerChallenge(Node node, Node electedNode){
		System.out.println(this.value);
	}
	
	private void checkVacancy(Node node, List <Node> currentPeers, int protocolId){
		NodeElection nodeData = NodeElection.get(node);
		if(!nodeData.isVacancy())
			if(!nodeData.electedNodes.isEmpty()){				
				Node electedNode = nodeData.electedNodes.keySet().iterator().next();
				List<Node> leavers = nodeData.getLeavers(currentPeers);
				if(leavers.contains(electedNode)){
					System.out.println("Vacancy detected; Elected node " + electedNode.getID() + " has quit");
					triggerVacancy(node, protocolId);
				}
			}else{
				System.out.println("Node " + node.getID() + ": Vacancy detected; Role position has not yet been elected");
				triggerVacancy(node, protocolId);
			}
		else
			joinElection(node, protocolId);
			
	}
	
	public static final double DELTA_MINUS = 0.8;
	
	private void checkResignation(Node node){
		NodeElection nodeData = NodeElection.get(node);
		if(nodeData.electedNodes.containsKey(node)){
			Double FS_e = nodeData.electedNodes.get(node);
			Double FS_a = evalFitnessFunction(node);
			if(FS_a < FS_e * DELTA_MINUS)
				triggerResignation(node);
		}
	}

	public static final double DELTA_PLUS = 1.2;

	private void checkChallenge(Node node){
		NodeElection nodeData = NodeElection.get(node);
		for(Node electedNode : nodeData.electedNodes.keySet()){
			if(node.equals(electedNode))
				continue;
			Double FS_e = nodeData.electedNodes.get(electedNode);
			Double FS_a = evalFitnessFunction(node);
			if(FS_e > FS_a * DELTA_PLUS)
				triggerChallenge(node, electedNode);
		}
	}
	
	private List<Node> getCurrentPeers(){
		return new ArrayList<Node>(Arrays.asList(Network.nodes()));
	}	
	
	private double evalFitnessFunction(Node node){
		NodeElection nodeData = NodeElection.get(node);
		return nodeData.batteryLevel.getValue();
	}

}