package startPC;

import java.util.Vector;

public class Neighbors {

	static private Neighbors instance = null;
	private Vector NeighborVector = null;
	//private Vector devList=null;
	private boolean busy;

	private Neighbors() {
		NeighborVector = new Vector();
		//devList = new Vector();
		busy = false;
	}

	public static Neighbors getInstance() {
		if (instance == null)
			instance = new Neighbors();
		return instance;
	}

	public boolean AddNeighbor(String MAC) {
		while (busy);
		busy = true;
		if (!Contains(MAC)) {
			NeighborVector.addElement(MAC);
			busy = false;
			return true;
		}
		busy = false;
		return false;
	}

	public boolean RemoveNeighbor(String MAC) {
		while (busy);
		return NeighborVector.removeElement(MAC);
	}

	public Vector GetNeighbors() {
		return NeighborVector;
	}

	public String GetNeighborByIndex(int index) {
		return (String) NeighborVector.elementAt(index);
	}

/*	public RemoteDevice GetNeighborDeviceByMAC(String MAC) {
		while (busy);
		busy = true;
		if (Contains(MAC)){
			//Vector devList = Bluetooth.getKnownDevicesList();
			
			if (devList != null) {
				for (int i = 0; i < devList.size(); i++) {
					RemoteDevice device = (RemoteDevice) devList.elementAt(i);
					if (device.getBluetoothAddress().equals(MAC)) {
						busy = false;
						return device;
					}
				}
			}
		}
		busy = false;
		return null;
	}*/

	public int GetLength() {
		return NeighborVector.size();
	}

	public boolean IsBusy() {
		return busy;
	}

	public boolean Contains(String MAC) {
		int length = NeighborVector.size();
		for (int i = 0; i < length; i++) {
			if (((String)NeighborVector.elementAt(i)).equals(MAC))
				return true;
		}
		return false;
	}
}