package startPC;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;

public class NodeStatus {

	private static NodeStatus instance=null;
	private static String myMAC = null;
	private static boolean busy;
	private static boolean waiting;
	
	private NodeStatus() {	
		try {
			myMAC = LocalDevice.getLocalDevice().getBluetoothAddress();
		} catch (BluetoothStateException e) {
		}
		busy = false;
		waiting=false;
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
}
