package startNXT;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

import lejos.nxt.*;
import lejos.nxt.comm.*;

public class ReceiveThread extends Thread {
	private BTConnection btConnection;
	private String connected = "Connected";
	private String waiting = "Waiting..";
	private String closing = "Closing..";
	public boolean connectionOK = false;
	public boolean connectionTerminated = false;
	private OutputBuffer buffer;
	private WaitingForRREPBuffer RREPBuffer;
	private RoutingTable routingTable;
	private Neighbors neighbors;
//	private NXT2 nxt2;
//	int posLigar;
//	int posDesligar;
//	boolean jaMandouLigar = false;
//	boolean jaMandouDesligar = false;
	
//	int currentLight;
//	int lastColour = 0; //0=white, 1=black
//	float start = System.currentTimeMillis();    
//	float currentTime = 0;
//	float lastTime = 0;
//	float timeBetweenPostion = 0;
//	float speed = 0;
//	int tamanhoBloco = 21; //folha papel 21 cm's
//	LightSensor ls = new LightSensor(SensorPort.S1);
	
	// static String myMAC = Bluetooth.getLocalAddress();
	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
	private static NodeStatus nodeStatus = null;

	boolean GotMulti = false;

	DataInputStream dis;
	DataOutputStream dos;

	public ReceiveThread() {
		buffer = OutputBuffer.getInstance();
		RREPBuffer = WaitingForRREPBuffer.getInstance();
		routingTable = RoutingTable.getInstance();
		neighbors = Neighbors.getInstance();
		nodeStatus = NodeStatus.getInstance();
//		nxt2 = NXT2.getInstance();
	}

	public void run() {
		int i = 0,j=1;

		while (true) {

			while (nodeStatus.isBusy());
			nodeStatus.setBusyMode(true);

			// LCD.clear();
			LCD.drawString(waiting + " " + i++, 0, 0);
			LCD.refresh();
			btConnection = Bluetooth.waitForConnection(12000, NXTConnection.LCP);
			if (btConnection != null) {
				connectionOK = true;

				LCD.clear();
				LCD.drawString(connected+" "+(j++)+"    ", 0, 0);
				LCD.refresh();

				if(!neighbors.Contains(btConnection.getAddress()))
						neighbors.AddNeighbor(btConnection.getAddress());
				LCD.refresh();

				dis = btConnection.openDataInputStream();
				dos = btConnection.openDataOutputStream();

				try {
					String type = Recieve();
					msgHandler(type);
				} catch (Exception e) {
					LCD.drawString("DataExchange Exception", 0, 0);
					LCD.refresh();
				}
			}
			nodeStatus.setBusyMode(false);
			btConnection = null;
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				LCD.drawString(e.toString(), 0, 6);
			}
			
//			position++;
//			LCD.drawString("P:" + position,0,6);
//			currentLight = ls.getNormalizedLightValue();
//			currentTime = (System.currentTimeMillis() - start)/1000F;
//			if(currentLight < 400){
//				if(lastColour == 0){
//					position++;
//					lastColour = 1;
//					timeBetweenPostion = currentTime - lastTime;
//					speed = tamanhoBloco/timeBetweenPostion;
//					lastTime = currentTime;
//				}
//				LCD.drawString("P:" + position + " S:" + speed,0,6);
//			}
//			else {
//				if(lastColour == 1){
//					position++;
//					lastColour = 0;
//					timeBetweenPostion = currentTime - lastTime;
//					speed = tamanhoBloco/timeBetweenPostion;
//					lastTime = currentTime;
//				}
//				LCD.drawString("P:" + position + " S:" + speed,0,6);
//			}
			
		}
	}

	public void msgHandler(String type) throws Exception {

		LCD.drawString("Received: "+type, 0, 2);
		LCD.refresh();
		//Thread.sleep(300);

		// Thread.sleep(300);
		// routingTable.AddRoutingRecord(src, new RoutingRecord(src, , src,
		// _TVM_sleepUntil, _TVM_sleepUntil));
		if (type.equals("DATA"))
			DATAHandler();
		else
			AODVHandler(type);

		dis.close();
		dos.close();

		//LCD.drawString(closing, 0, 0);
		//LCD.refresh();
		btConnection.close();
		Thread.sleep(200);
		connectionOK = false;
		// connectionTerminated = true;
	}

	public void AODVHandler(String type) throws Exception {

		if (type.equals("RREQ")) {
			int RREQ_ID = Integer.parseInt(Recieve()), hopcount = Integer.parseInt(Recieve());
			String destMac = Recieve();
			int DestSeqN = Integer.parseInt(Recieve());
			String OriginatorMAC = Recieve();
			int OridinatorSeqN = Integer.parseInt(Recieve());
			msgRREQ AODV_RREQ = new msgRREQ(RREQ_ID, hopcount, destMac,	DestSeqN, OriginatorMAC, OridinatorSeqN);
			RREQHandler(AODV_RREQ);
		} else if (type.equals("RREP")) {
			int lifetime = Integer.parseInt(Recieve()), hopcount = Integer
					.parseInt(Recieve());
			String destMac = Recieve();
			int DestSeqN = Integer.parseInt(Recieve());
			String OriginatorMAC = Recieve();
			int OridinatorSeqN = Integer.parseInt(Recieve());
			msgRREP AODV_RREP = new msgRREP(lifetime, hopcount, destMac,
					DestSeqN, OriginatorMAC, OridinatorSeqN);
			RREPHandler(AODV_RREP);
		} else if (type.equals("RERR")) {
			int errors = Integer.parseInt(Recieve()), hops = Integer
					.parseInt(Recieve());
			String errorsList[] = new String[errors];
			for (int i = 0; i < errors; i++) {
				errorsList[i] = Recieve();
			}
			msgRERR AODV_RERR = new msgRERR(errors, hops, errorsList);
			RERRHandler(AODV_RERR);
		}

		dis.close();
		dos.close();

		//LCD.drawString(closing, 0, 0);
		//LCD.refresh();
		btConnection.close();
		Thread.sleep(300);
		connectionOK = false;
		// connectionTerminated = true;
	}

	public void RREPHandler(msgRREP msg) {

		String nextHop = btConnection.getAddress();
		RoutingRecord record = new RoutingRecord(msg.OriginatorMAC,
				msg.OridinatorSeqN, nextHop, msg.HopCount, 0);
		routingTable.AddRoutingRecord(msg.OriginatorMAC, record);

		if (msg.DestMAC.equals(nodeStatus.myMAC())) {

			Vector messages = RREPBuffer.GetMessagesByDest(msg.OriginatorMAC);
			int msgSum = messages.size();
			for (int i = 0; i < msgSum; i++) {
				buffer.AddOutputMsg((msgBT) messages.elementAt(i));
			}
		} else {
			buffer.AddOutputMsg(msg);
		}
	}

	public void RREQHandler(msgRREQ msg) {

		if (msg.OriginatorMAC.equals(nodeStatus.myMAC()))
			return;

		String nextHop = btConnection.getAddress();
		RoutingRecord record = new RoutingRecord(msg.OriginatorMAC,	msg.OridinatorSeqN, nextHop, msg.HopCount, 0);
		routingTable.AddRoutingRecord(msg.OriginatorMAC, record);

		try {
			Thread.sleep(500);
		} catch (Exception e) {
			LCD.drawString(e.toString(), 0, 6);
		}

		if(msg.DestMAC.equals(nodeStatus.myMAC()) && msg.OriginatorMAC.equals(MAC_PC) && msg.HopCount == 1){
			Vector v = neighbors.GetNeighbors();
			if(v.size() != 0){
				String newAddress = (String) v.elementAt(0);
				
				msgRREQ newMsg = new msgRREQ(0, 2, newAddress, 0, msg.OriginatorMAC, 0);
				
				buffer.AddOutputMsg(newMsg);
			}
			else {
				buffer.AddOutputMsg(new msgRREP(0, msg.HopCount, msg.OriginatorMAC,
				msg.OridinatorSeqN, nodeStatus.myMAC(),0));///// make function: 0 => nodeStatus.mySeqN()
			}
		} else if(msg.DestMAC.equals(nodeStatus.myMAC()) && msg.OriginatorMAC.equals(MAC_PC) && msg.HopCount == 2)
		{
			buffer.AddOutputMsg(new msgRREP(0, msg.HopCount, msg.OriginatorMAC, msg.OridinatorSeqN, nodeStatus.myMAC(),0));
		} else if (routingTable.HasDestination(msg.DestMAC)){
			record = routingTable.GetRoutingRecordByDest(msg.DestMAC);
			int hopCount = record.HopCount;
			nextHop = record.nextHop;
			int nextHopSeqN = 0;
			int destSeqN = record.DestSeqN;

			buffer.AddOutputMsg(new msgRREP(0, hopCount, nextHop, nextHopSeqN, msg.DestMAC, destSeqN));
		}
		else
		{
			LCD.drawString("Nao pode acontecer", 0, 6);
		}
		
//		if (msg.DestMAC.equals(nodeStatus.myMAC())) {
//			buffer.AddOutputMsg(new msgRREP(0, msg.HopCount, msg.OriginatorMAC,
//					msg.OridinatorSeqN, nodeStatus.myMAC(),0));///// make function: 0 => nodeStatus.mySeqN()
//		} else if (routingTable.HasDestination(msg.DestMAC)){
//
//			record = routingTable.GetRoutingRecordByDest(msg.DestMAC);
//			int hopCount = record.HopCount;
//			nextHop = record.nextHop;
//			int nextHopSeqN = 0;
//			int destSeqN = record.DestSeqN;
//
//			buffer.AddOutputMsg(new msgRREP(0, hopCount, nextHop,
//					nextHopSeqN, msg.DestMAC, destSeqN));
//		} else {
//			msg.HopCount = msg.HopCount + 1;
//			buffer.AddOutputMsg(msg);
//		}
	}

	public void RERRHandler(msgRERR msg) {

	}

	public void DATAHandler() throws Exception {
		// LCD.drawString(data, 0, 2);
		// LCD.refresh();
		// Thread.sleep(300);

		String destMAC = Recieve();
		String srcMAC = Recieve();
		String data = Recieve();
		String type = Recieve();
		
		//MESSAGE identification of the NXT
		String Id = null;
		if(nodeStatus.myMAC().equals(MAC_NXT_RODAS))
			Id = "NXT_RODAS";
		else if (nodeStatus.myMAC().equals(MAC_NXT_TANK))
			Id = "NXT_TANK";
		
		//VAI BUSCAR AOS SENSORES!!!
			
		int position = NXT2._position;
		int speed = NXT2._speed;
		String dataToSendForward = Id + " " + position + " " + speed;
			
		if (destMAC.equals(nodeStatus.myMAC())) { // / data to me
			LCD.drawString("in: " + data + " " + type, 0, 5);
			LCD.refresh();

			if (type.equals("info")){
				msgDATA data1 = new msgDATA(MAC_PC, nodeStatus.myMAC(), dataToSendForward, type);
				buffer.AddOutputMsg(data1);
			}
			else if (type.equals("posl"))
			{
				StringTokenizer st = new StringTokenizer(data, " "); 
				while(st.hasMoreTokens()) {
					NXT2._posLigar = Integer.parseInt(st.nextToken());
					NXT2._posDesligar = Integer.parseInt(st.nextToken());
					LCD.drawString("pl " + NXT2._posLigar + " ,pd " + NXT2._posDesligar, 0, 5);
					LCD.refresh();
				} 
			}
		} else { // data to forward
			msgDATA msg;
			if(type.equals("info"))
			{
				if(destMAC.equals(MAC_PC))
					msg = new msgDATA(destMAC, srcMAC, data + ":" + dataToSendForward , type);
				else
					msg = new msgDATA(destMAC, srcMAC, data, type);
				buffer.AddOutputMsg(msg);
			} else if(type.equals("posl"))
			{
				StringTokenizer st = new StringTokenizer(data, " "); 
				while(st.hasMoreTokens()) { 
					NXT2._posLigar = Integer.parseInt(st.nextToken());
					NXT2._posDesligar = Integer.parseInt(st.nextToken());
					LCD.drawString("pl " + NXT2._posLigar + " ,pd " + NXT2._posDesligar, 0, 5);
					LCD.refresh();
				} 
				msg = new msgDATA(destMAC, srcMAC, data, type);
				buffer.AddOutputMsg(msg);
			}
			else if(type.equals("respPosL"))
			{
				LCD.drawString("respL TANK", 0, 7);
				msg = new msgDATA(destMAC, srcMAC, data, type);
				buffer.AddOutputMsg(msg);
			}
			else if(type.equals("emergency"))
			{
				LCD.drawString("Emergency", 0, 7);
				msg = new msgDATA(destMAC, srcMAC, data, type);
				buffer.AddOutputMsg(msg);
			}
			
			
			
			
			
		}
	}

	public String Recieve() {

		StringBuffer stringbuffer = new StringBuffer();
		try {
			char c = dis.readChar();
			//			LCD.drawString("[RECIEVER] " + c, 0, 4);
			//			LCD.refresh();
			while (c != '\n') {
				stringbuffer.append(c);
				c = dis.readChar();
			}
		} catch (IOException ioe) {
			System.out.println("IO Exception writing received bytes");
			System.out.println(" ::" + ioe.toString());
		} catch (Exception e) {
			System.out.println(" ::" + e.toString());
		}
		return stringbuffer.toString();
	}

	public void Send(String output) throws Exception {
		dos.writeChars(output + "\n");
		dos.flush();
	}
}