
package startNXT;

public class msgBT {

	String Type; // all AODV types or DATA
	//String destMAC;
	//String srcMAC;

	public msgBT(String _Type) {
		Type = _Type;
	}

/*	public msgBT(String _Type, String _destMAC) {
		Type = _Type;
		//destMAC = _destMAC;
		//srcMAC = _srcMAC;
	}*/

	public boolean equals(msgBT toCheck) {
		if (Type.equals(toCheck.Type))
			return true;
		return false;
	}
}
