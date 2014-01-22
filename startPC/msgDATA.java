package startPC;

public class msgDATA extends msgBT {

	String Data;
	String DestMAC;
	String SrcMAC;
	String Type;

	public msgDATA(String _DestMAC, String _SrcMAC, String _Data, String _Type) {
		super("DATA");
		Data = _Data;
		DestMAC = _DestMAC;
		SrcMAC = _SrcMAC;
		Type = _Type;
	}

	public boolean equals(msgDATA toCheck) {
		return super.equals(toCheck);
	}
}