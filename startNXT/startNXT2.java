package startNXT;

public class startNXT2 {

	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
	static NodeStatus nodeStatus = null;

	public static void main(String[] args) throws Exception {		
		
		nodeStatus = NodeStatus.getInstance();
		
	/*	if (nodeStatus.myMAC().equals("00165309CF29")){
			msgDATA data = new msgDATA(MAC_GAL_PC, nodeStatus.myMAC(), "Testing 123");
			OutputBuffer buffer = OutputBuffer.getInstance();
			buffer.AddOutputMsg(data);		
		}*/

		EmergencyThread emergThread = new EmergencyThread();
		emergThread.start();
		
		STOPThread PressToStop = new STOPThread();
		PressToStop.start();

		ReceiveThread ReceiveConnection = new ReceiveThread();
		ReceiveConnection.start();
		
		SendThread SendConnection = new SendThread();
		SendConnection.start();
		
		NXT2 Movement = new NXT2();
		Movement.start();
	}
}