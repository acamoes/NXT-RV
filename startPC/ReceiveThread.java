package startPC;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import lejos.nxt.LCD;

public class ReceiveThread extends Thread {

	private String connected = "Connected";
	private String waiting = "Waiting for NXT...";
	private String closing = "Closing...";
	public boolean connectionOK = false;
	public boolean connectionTerminated = false;

	private OutputBuffer buffer;
	private WaitingForRREPBuffer RREPBuffer;
	private RoutingTable routingTable;
	private Neighbors neighbors;
	private static NodeStatus nodeStatus = null;

	// static String myMAC;
	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";

	boolean GotMulti = false;
	int valor = 0;
	
	boolean trinco = false;

	ClientServer Server;

	// DataInputStream dis;
	// DataOutputStream dos;

	public ReceiveThread() {
		buffer = OutputBuffer.getInstance();
		RREPBuffer = WaitingForRREPBuffer.getInstance();
		routingTable = RoutingTable.getInstance();
		neighbors = Neighbors.getInstance();
		nodeStatus = NodeStatus.getInstance();
	}

	public void run() {
		int i = 0;
		try {
			Thread.sleep(400);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		while (true) {
			
			// while (!GotMulti) {
//			try {
//				Thread.sleep(3500);
//			} catch (InterruptedException e2) {
//				e2.printStackTrace();
//			}
			startPC.display("[ReceiveT][run] " + waiting + " " + i++);
			Server = new ClientServer(true);
			if(Server.getRemoteAddress()==null);
//			{
//				Server.CloseAll();
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				Server = null;
//				continue;
//			}
			connectionOK = true;
					
			startPC.display("[ReceiveT][run] Connected to: "+Server.getRemoteAddress());

			startPC.display("[ReceiveT][run] Neighbor vai ser adicionado");
			if(!neighbors.Contains(Server.getRemoteAddress()))
					neighbors.AddNeighbor(Server.getRemoteAddress());
			startPC.display("[ReceiveT][run] Neighbor foi adicionado com address = " + Server.getRemoteAddress());
			// inputData = Server.RecieveMessages();

			try {
				startPC.display("[ReceiveT][run] antes Recieve");
				String type = Server.Recieve();
				startPC.display("[ReceiveT][run] depois Recieve");
				msgHandler(type);
				startPC.display("[ReceiveT][run] msgHandler done");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
//			inputData = inputData + Server.RecieveMessages();

//			startPC.display(closing);
//			Server.CloseAll();
//			Server = null;
//			connectionOK = false;
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}
//			if (connectionTerminated)
//				break;
			// }
		}
	}

//	public void run() {
//		int i = 0;
//		try {
//			Thread.sleep(400);
//		} catch (InterruptedException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//
//		startPC.display("[ReceiveT][run] " + waiting + " " + i++);
//		Server = new ClientServer(true);
//		connectionOK = true;
//
//		startPC.display("[ReceiveT][run] Connected to: "+Server.getRemoteAddress());
//
//		startPC.display("[ReceiveT][run] Neighbor vai ser adicionado");
//		neighbors.AddNeighbor(Server.getRemoteAddress());
//		startPC.display("[ReceiveT][run] Neighbor foi adicionado com address = " + Server.getRemoteAddress());
//		// inputData = Server.RecieveMessages();
//		while (true) {
//			try {
//				if(Server == null){
//					Server = new ClientServer(true);
//					connectionOK = true;
//				}
//				startPC.display("[ReceiveT][run] antes Recieve");
//				String type = Server.Recieve();
//				startPC.display("[ReceiveT][run] depois Recieve");
//				msgHandler(type);
//				startPC.display("[ReceiveT][run] msgHandler done");
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
////			inputData = inputData + Server.RecieveMessages();
//
////			startPC.display(closing);
////			Server.CloseAll();
////			Server = null;
////			connectionOK = false;
//			try {
//				Thread.sleep(2000);
//			} catch (Exception e) {
//			}
////			if (connectionTerminated)
////				break;
//			// }
//		}
//	}
	
	public void msgHandler(String type) throws Exception {
		startPC.display("[ReceiveT][msgHanlder] Received: "+type);
		if (type.equals("DATA")){
			startPC.display("[ReceiveT][msgHanlder] type = DATA , vai executar DATAHandler");
			DATAHandler();
		}
		else{
			startPC.display("[ReceiveT][msgHanlder] type != DATA , vai executar AODVHandler");
			AODVHandler(type);
		}

		startPC.display("[ReceiveT][msgHanlder] " + closing);
		if (Server != null) {
			Server.CloseAll();
			Thread.sleep(600);  //200
		}
		Server = null;
		connectionOK = false;
		// connectionTerminated = true;
	}

	public void AODVHandler(String type) throws Exception {
		startPC.display("[ReceiveT][AODVHandler] init");
		if (type.equals("RREQ")) {
			startPC.display("[ReceiveT][AODVHandler] RREQ");
			int RREQ_ID = Integer.parseInt(Server.Recieve()), hopcount = Integer.parseInt(Server.Recieve());
			String destMac = Server.Recieve();
			int DestSeqN = Integer.parseInt(Server.Recieve());
			String OriginatorMAC = Server.Recieve();
			int OridinatorSeqN = Integer.parseInt(Server.Recieve());
			msgRREQ AODV_RREQ = new msgRREQ(RREQ_ID, hopcount, destMac,	DestSeqN, OriginatorMAC, OridinatorSeqN);
			RREQHandler(AODV_RREQ);
			return;
		} else if (type.equals("RREP")) {
			startPC.display("[ReceiveT][AODVHandler] RREP");
			int lifetime = Integer.parseInt(Server.Recieve()), hopcount = Integer.parseInt(Server.Recieve());
			String destMac = Server.Recieve();
			int DestSeqN = Integer.parseInt(Server.Recieve());
			String OriginatorMAC = Server.Recieve();
			int OridinatorSeqN = Integer.parseInt(Server.Recieve());
			msgRREP AODV_RREP = new msgRREP(lifetime, hopcount, destMac, DestSeqN, OriginatorMAC, OridinatorSeqN);
			RREPHandler(AODV_RREP);
			return;
		} else if (type.equals("RERR")) {
			startPC.display("[ReceiveT][AODVHandler] RERR");
			int errors = Integer.parseInt(Server.Recieve()), hops = Integer.parseInt(Server.Recieve());
			String errorsList[] = new String[errors];
			for (int i = 0; i < errors; i++) {
				errorsList[i] = Server.Recieve();
			}
			msgRERR AODV_RERR = new msgRERR(errors, hops, errorsList);
			RERRHandler(AODV_RERR);
			return;
		}

		if (Server != null) {
			startPC.display("[ReceiveT][AODVHandler] " + closing);
			Server.CloseAll();
			Thread.sleep(400);
		}
		Server = null;
		connectionOK = false;
		// connectionTerminated = true;
	}

	public void RREPHandler(msgRREP msg) {
		startPC.display("[ReceiveT][RREPHandler] init");
		String srcMAC = Server.getRemoteAddress();
		RoutingRecord record = new RoutingRecord(msg.OriginatorMAC,	msg.OridinatorSeqN, srcMAC, msg.HopCount, 0);
		startPC.display("[ReceiveT][RREPHandler] originador = " + msg.OriginatorMAC + " e o source = "  +srcMAC);
		routingTable.AddRoutingRecord(msg.OriginatorMAC, record);
		startPC.display("[ReceiveT][RREPHandler] Adicionou ao endereço " + msg.OriginatorMAC + " uma Routing Records");
		
		startPC.display("[ReceiveT][RREPHandler] msg.DestMAC = " + msg.DestMAC + "  , myMAC = " + nodeStatus.myMAC());
		if (msg.DestMAC.equals(nodeStatus.myMAC())) {
			msgDATA data = new msgDATA(MAC_NXT_RODAS, nodeStatus.myMAC(), "info", "info");
			buffer.AddOutputMsg(data);
			
			
//			startPC.display("[ReceiveT][RREPHandler] Entrou no if");
//			Vector<msgDATA> messages = RREPBuffer.GetMessagesByDest(msg.OriginatorMAC);
//			startPC.display("[ReceiveT][RREPHandler] depois de bsucar a mensagem data");
////			if(messages.size()!=0)
////			{
////				startPC.display("[ReceiveT][RREPHandler] vector vazio");
////			}else
////			{
////				startPC.display("[ReceiveT][RREPHandler] vector com coisas");
////			}
//			int msgSum = messages.size();
//			startPC.display("[ReceiveT][RREPHandler] numero de mensagens de data pa por no buffer = " + msgSum);
//			for (int i = 0; i < msgSum; i++) {
////				startPC.display("[RREPHanlder] Next message to AddOutputMsg = " + messages.);
//				buffer.AddOutputMsg((msgBT) messages.elementAt(i));
//			}
		} else {
			buffer.AddOutputMsg(msg);
		}
	}

	public void RREQHandler(msgRREQ msg) {
		startPC.display("[ReceiveT][RREQHandler] msgRREQ.DestMAC: " + msg.DestMAC);
		startPC.display("[ReceiveT][RREQHandler] myMAC          : " + nodeStatus.myMAC());

		if (msg.OriginatorMAC.equals(nodeStatus.myMAC()))
			return;
		
		String srcMAC = Server.getRemoteAddress();
		RoutingRecord record = new RoutingRecord(msg.OriginatorMAC,
				msg.OridinatorSeqN, srcMAC, msg.HopCount, 0);
		routingTable.AddRoutingRecord(msg.OriginatorMAC, record);
		
		if (msg.DestMAC.equals(nodeStatus.myMAC())) {
			buffer.AddOutputMsg(new msgRREP(0, msg.HopCount, msg.OriginatorMAC,
					msg.OridinatorSeqN, nodeStatus.myMAC(), 0)); ///// make function: 0 => nodeStatus.mySeqN()
			startPC.display("[ReceiveT][RREQHandler] Received 'msgRREQ' for me");
		} else if (routingTable.HasDestination(msg.DestMAC)){
			
			record = routingTable.GetRoutingRecordByDest(msg.DestMAC);
			int hopCount = record.HopCount;
			String nextHop = record.nextHop;
			int nextHopSeqN = 0;
			int destSeqN = record.DestSeqN;
			
			buffer.AddOutputMsg(new msgRREP(0, hopCount, nextHop,
					nextHopSeqN, msg.DestMAC, destSeqN));
		} else {
			startPC.display("[ReceiveT][RREQHandler] Forwarding the " +msg.Type + " to " +msg.DestMAC);
			msg.HopCount = msg.HopCount + 1;
			buffer.AddOutputMsg(msg);
		}
	}

	public void RERRHandler(msgRERR msg) {

	}

	public void DATAHandler() throws Exception {
		startPC.display("[ReceiveT][DATAHandler] init");
		String destMAC = Server.Recieve();
		String srcMAC = Server.Recieve();
		String data = Server.Recieve();
		String type = Server.Recieve();
		if (destMAC.equals(nodeStatus.myMAC())) { // / data to me
			startPC.display("[ReceiveTT][DATAHandler] Received Message: '" + data + "' from: " + srcMAC + " , type = " + type);

			if(type.equals("info")){
				int a = 0;
				int b = 0;

				StringTokenizer st = new StringTokenizer(data, " "); 
				while(st.hasMoreTokens()) {
					startPC.display(st.nextToken());
					a = Integer.parseInt(st.nextToken());
					startPC.display(st.nextToken());
					b = Integer.parseInt(st.nextToken());
					break;
				}

				valor = (b-a-2);
				startPC.display("*** VALOR *** :" + valor);

				if(valor<15)
					trinco = true;
				else
					trinco = false;

				//distancia do ponto inicial a rsu 14 passos normais e a distancia entre os dois nxt e um passo grande
				msgDATA msg = new msgDATA(MAC_NXT_TANK, nodeStatus.myMAC(), "32 42", "posl");
				buffer.AddOutputMsg(msg);
			}
			else if(type.equals("respPosL") && data.equals("ligaTANK"))
			{
				startPC.lights(true, trinco);
			}
			else if(type.equals("respPosL") && data.equals("desligaTANK"))
			{
				startPC.lights(false, trinco);
			}
			else if(type.equals("respPosL") && data.equals("ligaRODAS"))
			{
				startPC.lights(true, trinco);
			}
			else if(type.equals("respPosL") && data.equals("desligaRODAS"))
			{
				startPC.lights(false, trinco);}
			else if(type.equals("emergency"))
			{
				startPC.emergency();
			} else
				startPC.display("ERRO INESPERADO....");
		} else { // / data to forward
			msgDATA msg = new msgDATA(destMAC, srcMAC, data, type);
			buffer.AddOutputMsg(msg);
		}
	}
	/*
	 * 
	 * public static void DataExchange(NXTConnector conn) throws Exception {
	 * DataOutputStream dos = conn.getDataOut(); DataInputStream dis =
	 * conn.getDataIn();
	 * 
	 * // String output = "PC data"; String output = "Multicast";
	 * startPC.display("Sent: " + output); Send(output, dos, dis); //
	 * startPC.display("Received " + Recieve(dos, dis)); dis.close();
	 * dos.close(); }
	 * 
	 * public static void DataExchange2(NXTConnector conn) throws Exception {
	 * DataOutputStream dos = conn.getDataOut(); DataInputStream dis =
	 * conn.getDataIn();
	 * 
	 * String input = Recieve(dos, dis); startPC.display("Received " +
	 * input); Send(input + " ACK", dos, dis); dis.close(); dos.close();
	 * 
	 * }
	 * 
	 * private static String Recieve(DataOutputStream dos, DataInputStream dis)
	 * {
	 * 
	 * StringBuffer stringbuffer = new StringBuffer(); try { char c =
	 * dis.readChar(); while (c != '\n') { stringbuffer.append(c); c =
	 * dis.readChar(); } } catch (IOException ioe) {
	 * startPC.display("IO Exception writing received bytes");
	 * startPC.display(" ::" + ioe.toString()); } catch (Exception e) {
	 * startPC.display(" ::" + e.toString()); } return
	 * stringbuffer.toString(); }
	 * 
	 * public static void Send(String output, DataOutputStream dos,
	 * DataInputStream dis) throws Exception { dos.writeChars(output + "\n");
	 * dos.flush(); }
	 */
}