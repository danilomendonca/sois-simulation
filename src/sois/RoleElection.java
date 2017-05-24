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

public class RoleElection extends SingleValueHolder implements CDProtocol, EDProtocol{
		
	public RoleElection(String prefix) {
		super(prefix);
	}

	/**
	 * Should update the value of the FS
	 */
	@Override
	public void nextCycle(Node node, int protocolID) {
		List<Node> currentPeers = getCurrentPeers();
			
		NodeData nodeData = NodeData.get(node);
		incrementGroupTime();			

		if(isFirstCycle() || !nodeData.isNewInGroup())
			if(nodeData.isInElection())			
				joinElection(node, protocolID);
			else{
				checkForElectionConditions(nodeData, currentPeers, protocolID);
				if(NodeData.get(node).isElected())
					playRole(nodeData);
			}

		tickLevels(nodeData);
		nodeData.setNewInGroup(false);
		nodeData.setPeers(currentPeers);
	}
	
	private void tickLevels(NodeData nodeData){
		nodeData.gpsLevel.tickValue();		
	}
	
	private void checkForElectionConditions(NodeData nodeData, List<Node> currentPeers, int protocolID){
		checkNewMember(nodeData, currentPeers, protocolID);
		checkVacancy(nodeData, currentPeers, protocolID);
		checkResignation(nodeData, protocolID);
		checkChallenge(nodeData);
	}
	
	//TODO: should not base on the elected node, as when a new member joins the position may happen to be vacant
	private void checkNewMember(NodeData nodeData, List<Node> currentPeers, int protocolID) {
		if(nodeData.isElected()){
			List<Node> newComers = nodeData.getNewcomers(currentPeers);
			if(!newComers.isEmpty())
				for(Node newPeer : newComers){
					RoleElection peerProtocol = (RoleElection) newPeer.getProtocol(protocolID);  
					peerProtocol.receiveRegistry(newPeer, nodeData, protocolID);
				}
		}
	}	
	
	
	private void playRole(NodeData nodeData) {
		incrementContributionCount(nodeData);
		drainBattery(nodeData);
	}
	
	private static final float CONTRIBUTION_DELTA = 1F;//1 unit each use
	
	private void incrementGroupTime() {
		setValue(getValue() + CONTRIBUTION_DELTA);
	}
	
	private void incrementContributionCount(NodeData nodeData) {
		nodeData.increamentContribution(CONTRIBUTION_DELTA);
	}
	
	private static final float BATTERY_DRAIN_DELTA = 0.01F;//1% each use
	
	private void drainBattery(NodeData nodeData){
		nodeData.drainBattery(BATTERY_DRAIN_DELTA);
	}
	
	private boolean isFirstCycle(){
		return ((int) CommonState.getTime()) == 0;
	}
	
	private double getContributionFactor(NodeData nodeData){		
		return 1;//(nodeData.contributionLevel.getValue() / getValue());
	}
	
	private double evalFitnessFunction(Node node){		
		NodeData nodeData = NodeData.get(node);
		double FF =  
			nodeData.gpsStatus.getValue() * 
			nodeData.gpsLevel.getValue() * 			
			nodeData.batteryLevel.getValue() *
			getContributionFactor(nodeData);
		System.out.println(FF);
		return FF;
	}
	
	private enum EventType{
		VACANCY,
		RESIGNATION,
		CHALLENGE
	}
	
	private void receiveRegistry(Node thisNode, NodeData nodeDataToBeCopied, int protocolID){
		NodeData thisNodeData = NodeData.get(thisNode);
		thisNodeData.copy(nodeDataToBeCopied);
		checkForElectionConditions(thisNodeData, getCurrentPeers(), protocolID);
	}
	
	private void triggerVacancy(Node node, int protocolID){
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        handleVacancy(NodeData.get(node), protocolID);
        for (int i=0; i<linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);
            // Failure handling
            if (!peer.isUp())
                continue;
            RoleElection peerProtocol = (RoleElection) peer.getProtocol(protocolID);
            peerProtocol.handleVacancy(NodeData.get(peer), protocolID);//should call a sendSomething method to simulate a network call
        }
        joinElection(node, protocolID);
	}
	
	@Override
	public void processEvent(Node node, int protocolID, Object event) {
		if((EventType) event == EventType.VACANCY)
			handleVacancy(NodeData.get(node), protocolID);
	}
	
	private void handleVacancy(NodeData nodeData, int protocolID) {
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
            RoleElection peerProtocol = (RoleElection) peer.getProtocol(protocolID);
            peerProtocol.handleResignation(peer, protocolID);
        }
        joinElection(node, protocolID);
	}
	
	private void handleResignation(Node node, int protocolID) {
		if(!NodeData.get(node).isInElection()){
			NodeData.get(node).startElection();
		}
	}

	private void triggerChallenge(Node node, Node electedNode){
		
	}
	
	private void joinElection(Node node, int protocolID){
		int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        Double FS_a = evalFitnessFunction(node);
        NodeData.get(node).receiveFS(node, FS_a);
        for (int i=0; i<linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);
            // Failure handling
            if (!peer.isUp())
                continue;

            NodeData.get(peer).receiveFS(node, FS_a);
        }
	}
	
	private void checkVacancy(NodeData nodeData, List <Node> currentPeers, int protocolId){
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
	
	private void checkResignation(NodeData nodeData, int protocolID){
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

	private void checkChallenge(NodeData nodeData){
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

}