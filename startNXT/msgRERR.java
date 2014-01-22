package startNXT;

public class msgRERR extends msgBT {

	int errors;
	int hops;
	String[] ErrorRouteMAC;

	public msgRERR(int _hops, int _errors, String[] _ErrorRouteMAC) {
		super("AODV");
		errors=_errors;
		hops=_hops;
		ErrorRouteMAC = new String[_errors];
		ErrorRouteMAC = _ErrorRouteMAC;
	}

	public boolean equals(msgRERR toCheck) {
		if (SameErrors(toCheck.ErrorRouteMAC))
			return super.equals(toCheck);
		return false;
	}

	private boolean SameErrors(String[] toCheck) {
		int length = ErrorRouteMAC.length;
		if (length == toCheck.length){
			for (int i = 0; i < length; i++) {
				if (ErrorRouteMAC[i] != toCheck[i])
					return false;
			}
			return true;
		}
		return false;
	}
}
