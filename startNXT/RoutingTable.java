package startNXT;

import java.util.Hashtable;

public class RoutingTable {

	static RoutingTable instance = null;
	private Hashtable table = null;

	private RoutingTable() {
		table = new Hashtable();
	}

	public static RoutingTable getInstance() {
		if (instance == null) instance = new RoutingTable();
		return instance;
	}

	public void AddRoutingRecord(String DestMAC, RoutingRecord record) {
		table.put(DestMAC, record);
	}

	public RoutingRecord GetRoutingRecordByDest(String DestMAC) {
		return ((RoutingRecord) (table.get(DestMAC)));
	}

	public boolean HasDestination(String DestMAC) {
		if (table.get(DestMAC) != null)
			return true;
		return false;
	}

	public String GetNextHopByDest(String DestMAC) {
		RoutingRecord record = (RoutingRecord) (table.get(DestMAC));
		if (record != null)
			return (record.nextHop);
		return null;
	}

	public int GetHopCountByDest(String DestMAC) {
		return (((RoutingRecord) (table.get(DestMAC))).HopCount);
	}

	public void RemoveRoutingRecord(String DestMAC) {
		table.put(DestMAC, null);
	}
}
