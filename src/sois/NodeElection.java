package sois;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.core.Network;
import peersim.core.Node;

public class NodeElection {
	
	private static Map<Node, NodeElection> nodesData = new HashMap<>();;
	
	public static void addNode(Node node){
		if(!nodesData.containsKey(node))
			nodesData.put(node, new NodeElection(node));
	}
	
	public static NodeElection get(Node node){
		return nodesData.get(node);
	}
	
	public static void removeNodeData(Node node){
		nodesData.remove(node);
	}
	
	private Node node;
	List <Node> peers;
	Map <Node, Double> fitnessScores;
	Map <Node, Double> electedNodes;
	BatteryLevel batteryLevel;
	boolean vacancy = false;
	
	public NodeElection(Node node){
		this.node = node;
		peers = new ArrayList<>();
		fitnessScores = new HashMap <Node, Double> ();
		electedNodes = new HashMap <Node, Double> ();
		batteryLevel = new BatteryLevel();
	}
	
	public void updateFS(Node updatedNode, Double FS_a) {
		//System.out.println("Node " + node.getID() + ": FS for " + updatedNode.getID() + " have been updated with " + FS_a);
		fitnessScores.put(updatedNode, FS_a);
		if(allScored())
        	finishElection();
	}
	
	public boolean allScored(){
		return fitnessScores.size() == Network.size();
	}
	
	public void startVacancyElection() {
		setVacancy(true);
		fitnessScores.clear();
		electedNodes.clear();
		System.out.println("Node " + node.getID() + ": Vacancy election started");
	}
	
	public void finishElection(){
		Node winner = null;
		for(Node node : fitnessScores.keySet())
			if(winner == null || fitnessScores.get(node) > fitnessScores.get(winner))
				winner = node;
		electedNodes.put(winner, fitnessScores.get(winner));
		setVacancy(false);
		System.out.println("Node " + node.getID() + ": We have a winner!!! Node " + winner.getID() + " with FS_e " + fitnessScores.get(winner));
	}

	public List<Node> getLeavers(List<Node> currentPeers){
		List<Node> leavers = new ArrayList<Node>();
		for(Node peer : peers){
			if(peer == null || !currentPeers.contains(peer) || !peer.isUp())
				leavers.add(peer);
		}
		return leavers;
	}
	
	public List<Node> getNewcomers(List<Node> currentPeers){
		List<Node> newcomers = new ArrayList<Node>();
		for(Node peer : currentPeers){
			if(!peers.contains(peer))
				newcomers.add(peer);
		}
		return newcomers;
	}
	
	public boolean isVacancy() {
		return vacancy;
	}

	public void setVacancy(boolean vacancy) {
		this.vacancy = vacancy;
	}

	public List<Node> getPeers() {
		return peers;
	}

	public void setPeers(List<Node> peers) {
		this.peers = peers;
	}
}
