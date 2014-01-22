package startNXT;

import lejos.nxt.comm.Bluetooth;

public class NodeStatus {

	private static NodeStatus instance=null;
	private static String myMAC = null;
	private static boolean busy;
	private static boolean waiting;
	private static boolean inMotion;
	
	private NodeStatus() {
		myMAC = Bluetooth.getLocalAddress();
		busy = false;
		waiting=false;
		inMotion=false;
	}
	
	public static NodeStatus getInstance()
	{
		if(instance == null) instance = new NodeStatus();
		return instance;
	}
	
	public String myMAC() {
		return myMAC;
	}
	
	public boolean isBusy(){
		return busy;
	}
	
	public boolean isWaiting(){
		return waiting;
	}
	
	public void setBusyMode(boolean mode){
		busy = mode;
	}
	
	public void setWaitingMode(boolean mode){
		waiting = mode;
	}
	
	public void setInMotion(boolean mode){
		inMotion = mode;
	}
	
	public boolean isInMotion(){
		return inMotion;
	}
}
