package startPC;

public class msgRREP extends msgBT {

	int Lifetime;
	int HopCount;
	String DestMAC;
	int DestSeqN;
	String OriginatorMAC;
	int OridinatorSeqN;

	public msgRREP(int _Lifetime, int _HopCount, String _DestMAC, int _DestSeqN, String _OriginatorMAC, int _OridinatorSeqN) {
		super("RREP");
		Lifetime = _Lifetime;
		HopCount = _HopCount;
		DestMAC = _DestMAC;
		DestSeqN = _DestSeqN;
		OriginatorMAC = _OriginatorMAC;
		OridinatorSeqN = _OridinatorSeqN;
	}

	public boolean equals(msgRREP toCheck) {
		if (Lifetime == toCheck.Lifetime)
			if (HopCount == toCheck.HopCount)
				if (DestMAC.equals(toCheck.DestMAC))
					if (DestSeqN == toCheck.DestSeqN)
						if (OriginatorMAC.equals(toCheck.OriginatorMAC))
							if (OridinatorSeqN == toCheck.OridinatorSeqN)
								return super.equals(toCheck);
		return false;
	}
}
