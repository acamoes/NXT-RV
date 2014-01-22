package startNXT;

public class msgRREQ extends msgBT {

	int RREQID;
	int HopCount;
	String DestMAC;
	int DestSeqN;
	String OriginatorMAC;
	int OridinatorSeqN;

	public msgRREQ(int _RREQID, int _HopCount, String _DestMAC, int _DestSeqN,
			String _OriginatorMAC, int _OridinatorSeqN) {
		super("RREQ");
		RREQID = _RREQID;
		HopCount = _HopCount;
		DestMAC = _DestMAC;
		DestSeqN = _DestSeqN;
		OriginatorMAC = _OriginatorMAC;
		OridinatorSeqN = _OridinatorSeqN;
	}

	public boolean equals(msgRREQ toCheck) {
		if (RREQID == toCheck.RREQID)
			if (HopCount == toCheck.HopCount)
				if (DestMAC.equals(toCheck.DestMAC))
					if (DestSeqN == toCheck.DestSeqN)
						if (OriginatorMAC.equals(toCheck.OriginatorMAC))
							if (OridinatorSeqN == toCheck.OridinatorSeqN)
								return super.equals(toCheck);
		return false;
	}
}
