package sois;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.core.Node;

public class NodeData {
	
	private static Map<Node, NodeData> nodesData = new HashMap<>();;	
	
	public static void addNode(Node node){
		if(!hasNode(node))
			nodesData.put(node, new NodeData(node));
	}
	
	public static NodeData get(Node node){
		return nodesData.get(node);
	}
	
	public static void removeNodeData(Node node){
		nodesData.remove(node);
	}
	
	private Node node;
	List <Node> peers;
	Map <Node, Double> fitnessScores;
	List <RolePosition> rolePositions;
	BatteryLevel batteryLevel;
	GPSLevel gpsLevel;
	GPSStatus gpsStatus;
	InternetStatus internetStatus;
	ContributionLevel contributionLevel;

	boolean inElection = false;
	boolean newInGroup = true;
	
	public NodeData(Node node){
		this.node = node;
		peers = new ArrayList<>();
		fitnessScores = new HashMap <Node, Double> ();
		rolePositions = new ArrayList<RolePosition>();
		batteryLevel = new BatteryLevel();
		gpsLevel = new GPSLevel();
		gpsStatus = new GPSStatus();
		internetStatus = new InternetStatus();
		contributionLevel= new ContributionLevel();
	}
	
	public void clearNodeReference(Node electedNode) {
		fitnessScores.remove(electedNode);
		rolePositions.remove(electedNode);
	}
	
	public void receiveFS(Node updatedNode, Double FS_a) {
		//System.out.println("Node " + node.getID() + ": FS for " + updatedNode.getID() + " have been updated with " + FS_a);
		fitnessScores.put(updatedNode, FS_a);
		if(isInElection() && allScored())
        	finishElection();
	}
	
	public boolean allScored(){
		return fitnessScores.size() == peers.size();
	}
	
	public void startElection() {
		setInElection(true);
		fitnessScores.clear();
		System.out.println("Node " + node.getID() + ": Election started");
	}
	
	public void finishElection(){
		Node winner = null;
		for(Node node : fitnessScores.keySet())
			if(!isNodeElected(node)  && (winner == null || fitnessScores.get(node) > fitnessScores.get(winner)))
				winner = node;
		
		electNode(winner);
		setInElection(false);
	}
	
	public void electNode(Node winner){
		rolePositions.add(new RolePosition(winner, fitnessScores.get(winner)));
		System.out.println("Node " + node.getID() + ": We have a winner!!! Node " + winner.getID() + " with FS_e " + fitnessScores.get(winner));
	}

	public void updateFitnessScore(Node node, double FS_a) {
		fitnessScores.put(node, FS_a);
	}
	
	public void increamentContribution(float delta) {
		contributionLevel.inc(delta);		
	}
	
	public void drainBattery(float batteryDrainDelta) {
		batteryLevel.use(batteryDrainDelta);
	}
	
	public void tickGPS() {
		gpsLevel.tickValue();
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
			if(peer != null && !peers.contains(peer))
				newcomers.add(peer);
		}
		return newcomers;
	}
	
	public boolean isInElection() {
		return inElection;
	}

	public void setInElection(boolean vacancy) {
		this.inElection = vacancy;
	}

	public List<Node> getPeers() {
		return peers;
	}

	public void setPeers(List<Node> peers) {
		this.peers = peers;
	}
	
	public Map<Node, Double> getFitnessScores() {
		return fitnessScores;
	}

	public void setFitnessScores(Map<Node, Double> fitnessScores) {
		this.fitnessScores = fitnessScores;
	}

	public List<RolePosition> getRolePositions() {
		return rolePositions;
	}
	
	public RolePosition getRolePositionFF() {
		
		for(RolePosition rolePosition : rolePositions)
			if(rolePosition.getNode().equals(node))
				return rolePosition;
						
		return null;
	}
	
	public List<Node> getElectedNodes(){
		List<Node> electedNodes = new ArrayList<>();
		for(RolePosition rolePosition : rolePositions)
			electedNodes.add(rolePosition.getNode());
		
		return electedNodes;
	}

	public void setRolePositions(List<RolePosition> electedNodes) {
		this.rolePositions = electedNodes;
	}

	public boolean isElected() {
		return isNodeElected(node);
	}		
	
	public boolean isNodeElected(Node node){
		for(RolePosition rp : rolePositions)
			if(rp.getNode().equals(node))
				return true;
		
		return false;
	}

	public static boolean hasNode(Node node) {
		return nodesData.containsKey(node);
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	
	public boolean isNewInGroup() {
		return newInGroup;
	}

	public void setNewInGroup(boolean newInGroup) {
		this.newInGroup = newInGroup;
	}

	public void copy(NodeData peer) {
		peers = new ArrayList<>(peer.getPeers());
		fitnessScores = new HashMap <Node, Double> (peer.getFitnessScores());
		rolePositions = new ArrayList<RolePosition> (peer.getRolePositions());
		inElection = peer.isInElection();
	}

	public void replacePosition(Node challendgedNode, Node challengerNode, double FS_a) {
		
		RolePosition toBeRemoved = null;
		for(RolePosition position : rolePositions)
			if(position.getNode().equals(challendgedNode))
				toBeRemoved = position;
				
		if(toBeRemoved != null)
			rolePositions.remove(toBeRemoved);
		updateFitnessScore(challengerNode, FS_a);
		finishElection();
	}

}
