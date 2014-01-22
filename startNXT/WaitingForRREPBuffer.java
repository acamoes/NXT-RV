package startNXT;

import java.util.Hashtable;
import java.util.Vector;

public class WaitingForRREPBuffer {

	static WaitingForRREPBuffer instance = null;
	//private HashMap<String, Vector<msgBT>> table;
	private Hashtable table;
	//private Vector<msgBT> msgVector;
	private Vector msgVector;
	private boolean busy;

	private WaitingForRREPBuffer() {
		
		table = new Hashtable();
		msgVector=null;
		busy=false;
	}

	public static WaitingForRREPBuffer getInstance()
	{
		if(instance == null) instance = new WaitingForRREPBuffer();
		return instance;
	}
	
	public boolean AddOutputMsg(msgDATA msg) {

		while(busy);
		busy=true;
		msgVector = (Vector)table.get(msg.DestMAC);
		if (msgVector!=null) {
			if (!Contains(msg)) {
				msgVector.addElement(msg);
				table.put(msg.DestMAC, msgVector);
				busy=false;
				return true;
			}
		} else {
			msgVector = new Vector();
			msgVector.addElement(msg);
			table.put(msg.DestMAC, msgVector);
			busy=false;
			return true;
		}
		busy=false;
		return false;
	}
	
	public boolean RemoveOutputMsg(msgDATA msg) {
		while(busy);
		busy=true;
		msgVector = (Vector)table.get(msg.DestMAC);
		if (msgVector!=null) {
			if (msgVector.removeElement(msg)) {
				if (msgVector.isEmpty())
				{
					table.put(msg.DestMAC, null);
					busy=false;
					return true;
				}
				table.put(msg.DestMAC, msgVector);
				busy=false;
				return true;
			}
		}
		busy=false;
		return true;
	}
	
	public Vector GetMessagesByDest(String DestMAC) {
		return (Vector) table.get(DestMAC);
	}
	
	public boolean IsBusy() {
		return busy;
	}
	
	private boolean Contains(msgBT msg) {
		int length=msgVector.size();
		if (msg.Type.equals("DATA"))
		{
			for(int i=0; i<length; i++){
				if (((msgDATA)(msgVector.elementAt(i))).equals((msgDATA)msg))
					return true;
			}	
		}
		return false;
	}
}
