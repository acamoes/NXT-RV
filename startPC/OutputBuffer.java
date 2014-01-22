package startPC;

import java.util.Vector;

public class OutputBuffer {

	static OutputBuffer instance = null;
	private Vector msgVector;
	private boolean busy;

	private OutputBuffer() {
		
		msgVector = new Vector();
		busy=false;
	}

	public static OutputBuffer getInstance()
	{
		if(instance == null) instance = new OutputBuffer();
		return instance;
	}
	
	public boolean AddOutputMsg(msgBT msg) {
		while(busy);
		busy=true;
		if (!Contains(msg)) {
			msgVector.addElement(msg);
			busy=false;
			return true;
		}
		busy=false;
		return false;
	}
	
	public msgBT GetNextMessage() {
		while(busy);
		busy=true;
		int last=msgVector.size()-1;
		msgBT toReturn = (msgBT)msgVector.elementAt(last);
		msgVector.removeElementAt(last);
		busy=false;
		return toReturn;
	}
	
	public boolean IsEmpty() {
		return msgVector.isEmpty();
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
		}else if (msg.Type.equals("RREQ")){
			for(int i=0; i<length; i++){
				if (((msgRREQ)(msgVector.elementAt(i))).equals((msgRREQ)msg))
					return true;
			}	
		}else if (msg.Type.equals("RREP")){
			for(int i=0; i<length; i++){
				if (((msgRREP)(msgVector.elementAt(i))).equals((msgRREP)msg))
					return true;
			}	
		}else if (msg.Type.equals("RERR")){
			for(int i=0; i<length; i++){
				if (((msgRERR)(msgVector.elementAt(i))).equals((msgRERR)msg))
					return true;
			}	
		}
		
		return false;
	}
}
