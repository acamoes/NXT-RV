package startPC;

public class RoutingRecord {

	public String DestMAC;
	int DestSeqN;
	String nextHop;// MAC
	int HopCount;
	int CreationTime;

	public RoutingRecord(String _DestMAC, int _DestSeqN, String _nextHop, int _HopCount, int _CreationTime) {
		DestMAC = _DestMAC;
		DestSeqN=_DestSeqN;
		nextHop = _nextHop;
		HopCount = _HopCount;
		CreationTime=_CreationTime;
	}
}
