package sois;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
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
		List<Node> currentPeers = getCurrentPeers();
		
		//if(NodeElection.hasNode(node)){
		//	NodeElection.addNode(node);
		//}
			
		NodeElection nodeData = NodeElection.get(node);
		setValue(evalFitnessFunction(node));

		if(((int) CommonState.getTime()) == 0 || 
				!nodeData.isNewInGroup())
			if(nodeData.isInElection())			
				joinElection(node, protocolID);
			else
				checkForElectionConditions(nodeData, currentPeers, protocolID);

		nodeData.setNewInGroup(false);
		
		if(NodeElection.get(node).isElected())
			playRole(node);
		
		nodeData.setPeers(currentPeers);
	}
	
	private void checkForElectionConditions(NodeElection nodeData, List<Node> currentPeers, int protocolID){
		checkNewMember(nodeData, currentPeers, protocolID);
		checkVacancy(nodeData, currentPeers, protocolID);
		checkResignation(nodeData, protocolID);
		checkChallenge(nodeData);
	}
	
	//TODO: should not base on the elected node, as when a new member joins the position may happen to be vacant
	private void checkNewMember(NodeElection nodeData, List<Node> currentPeers, int protocolID) {
		if(nodeData.isElected()){
			List<Node> newComers = nodeData.getNewcomers(currentPeers);
			if(!newComers.isEmpty())
				for(Node newPeer : newComers){
					RoleFitness peerProtocol = (RoleFitness) newPeer.getProtocol(protocolID);  
					peerProtocol.receiveRegistry(newPeer, nodeData, protocolID);
				}
		}
	}	
	
	private void playRole(Node node) {
		NodeElection.get(node).batteryLevel.use();
	}
	
	private void receiveRegistry(Node thisNode, NodeElection nodeDataToBeCopied, int protocolID){
		NodeElection thisNodeData = NodeElection.get(thisNode);
		thisNodeData.copy(nodeDataToBeCopied);
		checkForElectionConditions(thisNodeData, getCurrentPeers(), protocolID);
	}

	private enum EventType{
		VACANCY,
		RESIGNATION,
		CHALLENGE
	}
	
	private void triggerVacancy(Node node, int protocolID){
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        handleVacancy(NodeElection.get(node), protocolID);
        for (int i=0; i<linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);
            // Failure handling
            if (!peer.isUp())
                continue;
            RoleFitness peerProtocol = (RoleFitness) peer.getProtocol(protocolID);
            peerProtocol.handleVacancy(NodeElection.get(peer), protocolID);//should call a sendSomething method to simulate a network call
        }
        joinElection(node, protocolID);
	}
	
	@Override
	public void processEvent(Node node, int protocolID, Object event) {
		if((EventType) event == EventType.VACANCY)
			handleVacancy(NodeElection.get(node), protocolID);
	}
	
	private void handleVacancy(NodeElection nodeData, int protocolID) {
		if(!nodeData.isInElection()){
			nodeData.startElection();
		}
	}
	
	private void triggerResignation(Node node, int protocolID){
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        handleResignation(node, protocolID);
        for (int i=0; i<linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);
            // Failure handling
            if (!peer.isUp())
                continue;
            RoleFitness peerProtocol = (RoleFitness) peer.getProtocol(protocolID);
            peerProtocol.handleResignation(peer, protocolID);
        }
        joinElection(node, protocolID);
	}
	
	private void handleResignation(Node node, int protocolID) {
		if(!NodeElection.get(node).isInElection()){
			NodeElection.get(node).startElection();
		}
	}

	private void triggerChallenge(Node node, Node electedNode){
		
	}
	
	private void joinElection(Node node, int protocolID){
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        Double FS_a = evalFitnessFunction(node);
        NodeElection.get(node).receiveFS(node, FS_a);
        for (int i=0; i<linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);
            // Failure handling
            if (!peer.isUp())
                continue;

            NodeElection.get(peer).receiveFS(node, FS_a);
        }
	}
	
	private void checkVacancy(NodeElection nodeData, List <Node> currentPeers, int protocolId){
		Node node = nodeData.getNode();
		if(!nodeData.isInElection()){
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
		}
	}
	
	public static final double DELTA_MINUS = 0.8;
	
	private void checkResignation(NodeElection nodeData, int protocolID){
		Node node = nodeData.getNode();
		if(!nodeData.isInElection()){
			if(nodeData.electedNodes.containsKey(node)){
				Double FS_e = nodeData.electedNodes.get(node);
				Double FS_a = evalFitnessFunction(node);
				if(FS_a < FS_e * DELTA_MINUS){
					System.out.println("Node " + node.getID() + ": has resigned.");
					triggerResignation(node, protocolID);
				}
			}
		}
	}

	public static final double DELTA_PLUS = 1.2;

	private void checkChallenge(NodeElection nodeData){
		Node node = nodeData.getNode();
		for(Node electedNode : nodeData.electedNodes.keySet()){
			if(node.equals(electedNode))
				continue;
			Double FS_e = nodeData.electedNodes.get(electedNode);
			Double FS_a = evalFitnessFunction(node);
			if(FS_e > FS_a * DELTA_PLUS)
				triggerChallenge(node, electedNode);
		}
	}
	
	//TODO: this method returns ALL nodes including this one; the term peer refers to the other nodes connected to this one; should two different methods: getCurrentNodes and getCurrentPeers 
	private List<Node> getCurrentPeers(){
		return new ArrayList<Node>(Arrays.asList(Network.nodes()));
	}	
	
	private double evalFitnessFunction(Node node){
		NodeElection nodeData = NodeElection.get(node);
		return nodeData.batteryLevel.getValue();
	}

}