package startNXT;

import lejos.nxt.*;



public class EmergencyThread extends Thread {

	double start,end;
	double number;
	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
	static NodeStatus nodeStatus;
	static private OutputBuffer buffer;
	String identidade;
	
	public EmergencyThread()
	{
		
		nodeStatus = NodeStatus.getInstance();
		buffer = OutputBuffer.getInstance();
	}
	
	public void run() {
		Button.LEFT.waitForPressAndRelease();

		LCD.drawString("left pressed", 0, 5);
		LCD.refresh();
		if (nodeStatus.myMAC().equals(MAC_NXT_RODAS))
		{
			identidade = "RODAS";
		}
		else if (nodeStatus.myMAC().equals(MAC_NXT_TANK))
		{
			identidade = "TANK";
		}
		msgDATA msg = new msgDATA(MAC_PC, nodeStatus.myMAC(), "emergency " + identidade, "emergency");
		buffer.AddOutputMsg(msg);

		
	}
}