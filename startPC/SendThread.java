package startPC;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;

public class SendThread extends Thread {
	public boolean connectionOK = false;
	public boolean connectionTerminated = false;
	private WaitingForRREPBuffer RREPBuffer;
	private OutputBuffer buffer;
	private Neighbors neighbors;
	private Vector NeighborsVector;
	private RoutingTable routingTable;
	private Vector devList;
	boolean inquireComplete = false;
	private ClientServer Server;

	private DataInputStream dis = null;
	private DataOutputStream dos = null;

	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
	private static String validDevices[] = {MAC_PC, MAC_NXT_TANK, MAC_NXT_RODAS};
	private static NodeStatus nodeStatus = null;
	private NXTConnector btc = null; // conn = null;

	public SendThread() {
		buffer = OutputBuffer.getInstance();
		RREPBuffer = WaitingForRREPBuffer.getInstance();
		neighbors = Neighbors.getInstance();
		NeighborsVector = neighbors.GetNeighbors();
		routingTable = RoutingTable.getInstance();
		devList = null;
		nodeStatus = NodeStatus.getInstance();
		btc = new NXTConnector();
	}

	public void run() {
		while (true) {
			if (!buffer.IsEmpty()) {
				startPC.display("[SendT][run] Buffer nao está vazio e vai fazer GetNextMessage");
				msgBT toSend = buffer.GetNextMessage();
				startPC.display("[SendT][run] Sending Message from Buffer, Type = " + toSend.Type);
				if (toSend.Type.equals("DATA")) {
					ConnectAndSend((msgDATA) toSend);
				} else if (toSend.Type.equals("RREQ")) {
					ConnectAndSend((msgRREQ) toSend);
				} else if (toSend.Type.equals("RREP")) {
					startPC.display("[SendT][run] Need to Send RREP");
					ConnectAndSend((msgRREP) toSend);
				} else if (toSend.Type.equals("RERR")) {
					ConnectAndSend((msgRERR) toSend);
				}

				try {
					Thread.sleep(2000);
					
				} catch (Exception e) {
					startPC.display("[SendT][run] catch =  " + e.toString());
				}
			}
		}
	}

	private boolean ConnectAndSend(msgDATA msg) {
		startPC.display("[SendT][ConnectAndSend] Type = " + msg.Type+ " , msg.Data = " + msg.Data+ " , msg.DestMAC = " + msg.DestMAC+ " , msg.SrcMAC = " + msg.SrcMAC);
		if (KnownDestination(msg.DestMAC)) {
			startPC.display("[SendT][ConnectAndSend] Encontrou KnownDestination");
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			startPC.display("[SendT][ConnectAndSend] Number of Neighbors = "+NeighborsVector.size());
			
			for (int i = 0; i < (NeighborsVector.size());) {
				startPC.display("[SendT][ConnectAndSend] Connect attempt no. " +(++i) + "   , to " + routingTable.GetNextHopByDest(msg.DestMAC));

				for (int j = 0; j < 4;) {
					startPC.display("Connect attempt no. " +(++j));
					boolean connected = false;
					connected = btc.connectTo(null, routingTable.GetNextHopByDest(msg.DestMAC), NXTCommFactory.BLUETOOTH);				
					if (!connected) {
						System.err.println("Failed to connect to '"	+ routingTable.GetNextHopByDest(msg.DestMAC) + "'");
						try {
							startPC.display("[SendT][ConnectAndSend] NOT Connected to " + routingTable.GetNextHopByDest(msg.DestMAC));
							btc.close();
							Thread.sleep(10000);
						} catch (Exception e) {
							startPC.display("[SendT][ConnectAndSend] Erro inesperado");
						}

						// return false;
					} else {
						startPC.display("[SendT][ConnectAndSend] Connected to " + routingTable.GetNextHopByDest(msg.DestMAC));
						try {
							OutputStream os = btc.getOutputStream();
							InputStream is = btc.getInputStream();
							dos = new DataOutputStream(os);
							dis = new DataInputStream(is);

							startPC.display("[SendT][ConnectAndSend] doing SendmsgBT");
							SendmsgBT(msg);

							dis.close();
							dos.close();
							Thread.sleep(200);
							btc.close();
						} catch (Exception e) {
							startPC.display("[SendT][ConnectAndSend] ConnectAndSend Exception");
						}

						/*	try {

						Thread.sleep(10000);
					} catch (Exception e) {
						startPC.display("ConnectAndSend Exception");
					}

					msgDATA data1 = new msgDATA("00165309CF29", nodeStatus.myMAC(), "stop");
					buffer.AddOutputMsg(data1);*/

						return true;
					}
				}
			}
			generateRERR(routingTable.GetNextHopByDest(msg.DestMAC));
			
			return false;
		} else if (msg.DestMAC.equals("Multicast")) {
			return ConnectAndMultiSend(msg);
		}

		startPC.display("[SendT][ConnectAndSend] Saiu do IF do KnownDestination");
		RREPBuffer.AddOutputMsg(msg);
		try {
			SendRREQ(CreateRREQ(msg.DestMAC));
		} catch (Exception e) {
			System.err.println("Error SendRREQ");
		}
		return false;
	}

	private boolean ConnectAndSend(msgRREQ msg) {
		try {
			return SendRREQ(msg);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean ConnectAndSend(msgRREP msg) {
		if (KnownDestination(msg.DestMAC)) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			// while(nodeStatus.isBusy());
			// nodeStatus.setBusyMode(true);
			for (int i = 0; i < 3;) {
				startPC.display("[SendT][ConnectAndSend] Connect attempt no. " +(++i));
				boolean connected = false;
				connected = btc.connectTo(null, routingTable
						.GetNextHopByDest(msg.DestMAC),
						NXTCommFactory.BLUETOOTH);
				if (!connected) {
					System.err.println("Failed to connect to '"
							+ routingTable.GetNextHopByDest(msg.DestMAC) + "'");
					try {
						btc.close();
						Thread.sleep(2000);
					} catch (Exception e) {
					}

					// return false;
				} else {
					try {
						OutputStream os = btc.getOutputStream();
						InputStream is = btc.getInputStream();
						dos = new DataOutputStream(os);
						dis = new DataInputStream(is);

						startPC.display("Connected to '"+routingTable.GetNextHopByDest(msg.DestMAC)+"'");
						startPC.display("Send RREP to "+msg.DestMAC);
						SendmsgBT(msg);

						dis.close();
						dos.close();
						Thread.sleep(200);
						btc.close();
					} catch (Exception e) {
						startPC.display("ConnectAndSend Exception");
					}
					startPC.display("Sent OK");
					return true;
				}
			}
			return false;
		}
		return false;
	}

	private boolean ConnectAndSend(msgRERR msg) {
		return ConnectAndMultiSend(msg);
	}

	private boolean ConnectAndMultiSend(msgDATA msg) {
		int i = 0, numOfNeighbors = 0, sent = 0;
		String sendTo = "", exceptThisMAC1 = msg.SrcMAC, exceptThisMAC2 = routingTable
				.GetNextHopByDest(msg.SrcMAC);
		NeighborsVector = neighbors.GetNeighbors();
		numOfNeighbors = NeighborsVector.size();

		for (i = 0; i < numOfNeighbors; i++) {
			sendTo = (String) NeighborsVector.elementAt(i);
			if (!sendTo.equals(exceptThisMAC1) && !sendTo.equals(exceptThisMAC2)) {
				for (int j = 0; j < 3;) {
					startPC.display("Connect attempt no. " +(++j));
					boolean connected = false;
					connected = btc.connectTo(null, sendTo,	NXTCommFactory.BLUETOOTH);
					if (!connected) {
						System.err.println("Failed to connect to '" + sendTo + "'");
						try {
							btc.close();
							Thread.sleep(500);
						} catch (Exception e) {
						}

						// return false;
					} else {
						try {
							OutputStream os = btc.getOutputStream();
							InputStream is = btc.getInputStream();
							dos = new DataOutputStream(os);
							dis = new DataInputStream(is);

							SendmsgBT(msg);

							dis.close();
							dos.close();
							Thread.sleep(200);
							btc.close();
						} catch (Exception e) {
							startPC.display("ConnectAndMultiSend Exception");
						}
						sent++;
						startPC.display("Sent OK");
						break;
					}
				}
			}
		}
		if (numOfNeighbors == sent)
			return true;
		return false;
	}

	private boolean ConnectAndMultiSend(msgRERR msg) {
		int i = 0, numOfNeighbors = 0, sent = 0;
		String sendTo = "";
		NeighborsVector = neighbors.GetNeighbors();
		numOfNeighbors = NeighborsVector.size();

		for (i = 0; i < numOfNeighbors; i++) {
			sendTo = (String) NeighborsVector.elementAt(i);
			for (int j = 0; j < 3; j++) {
				startPC.display("Connect attempt no. " + j + 1);
				boolean connected = false;
				connected = btc.connectTo(null, sendTo,
						NXTCommFactory.BLUETOOTH);
				if (!connected) {
					System.err.println("Failed to connect to '" + sendTo + "'");
					try {
						btc.close();
						Thread.sleep(500);
					} catch (Exception e) {
					}

					// return false;
				} else {
					try {
						OutputStream os = btc.getOutputStream();
						InputStream is = btc.getInputStream();
						dos = new DataOutputStream(os);
						dis = new DataInputStream(is);

						SendmsgBT(msg);

						dis.close();
						dos.close();
						Thread.sleep(200);
						btc.close();
					} catch (Exception e) {
						startPC.display("ConnectAndMultiSend Exception");
					}
					sent++;
					startPC.display("Sent OK");
					break;
				}
			}
		}
		if (numOfNeighbors == sent)
			return true;
		return false;
	}
	
	private boolean ConnectAndMultiSend(msgRREQ msg) {
		startPC.display("[SendT][ConnectAndMultiSend][RREQ] Type = " + msg.Type+ " , msg.DestMAC = " + msg.DestMAC+ " , msg.DestSeqN = " + msg.DestSeqN
				+ " , msg.HopCount = " + msg.HopCount+ " , msg.OridinatorSeqN = " + msg.OridinatorSeqN+ " , msg.OriginatorMAC = " + msg.OriginatorMAC
				+ " , msg.RREQID = " + msg.RREQID);
		int i = 0, numOfNeighbors = 0, sent = 0;
		String addressTo = "";
		String exceptThisMAC1 = msg.OriginatorMAC;
		String exceptThisMAC2 = routingTable.GetNextHopByDest(msg.OriginatorMAC);
		
		startPC.display("[SendT][ConnectAndMultiSend][RREQ] msg.OriginatorMAC = " + msg.OriginatorMAC + "routingTable.GetNextHopByDest(msg.OriginatorMAC) = " + routingTable.GetNextHopByDest(msg.OriginatorMAC));
		NeighborsVector = neighbors.GetNeighbors();
		numOfNeighbors = NeighborsVector.size();
		for (i = 0; i < numOfNeighbors; i++) {
			addressTo = (String) NeighborsVector.elementAt(i);
			startPC.display("[SendT][ConnectAndMultiSend][RREQ] sendTo = " + addressTo);
			if (!addressTo.equals(exceptThisMAC1) && !addressTo.equals(exceptThisMAC2)) {
				for (int j = 0; j < 9; j++) {
					startPC.display("[SendT][ConnectAndMultiSend][RREQ] Connect attempt no. " + j + 1);
					boolean connected = false;
					connected = btc.connectTo(null, addressTo,	NXTCommFactory.BLUETOOTH);
					if (!connected) {
						System.err.println("Failed to connect to '" + addressTo + "'");
						try {
							btc.close();
							Thread.sleep(500);
						} catch (Exception e) {
						}
						// return false;
					} else {
						try {
							OutputStream os = btc.getOutputStream();
							InputStream is = btc.getInputStream();
							dos = new DataOutputStream(os);
							dis = new DataInputStream(is);

							startPC.display("[SendT][ConnectAndMultiSend][RREQ] Type = " + msg.Type+ " , Vai executar SendmsgBT");
							SendmsgBT(msg);

							dis.close();
							dos.close();
							Thread.sleep(200);
							btc.close();
							Thread.sleep(300);
						} catch (Exception e) {
							startPC.display("[SendT][ConnectAndMultiSend][RREQ] Exception");
						}
						sent++;
						startPC.display("[SendT][ConnectAndMultiSend][RREQ] Sent OK");
						break;
					}
				}
			}
		}
		if (numOfNeighbors == sent)
			return true;
		return false;
	}
	

	public boolean KnownDestination(String toCheck) {
		if (neighbors.Contains(toCheck))
			return true;
		return routingTable.HasDestination(toCheck);
	}

	private void Static_Inquire() throws Exception {
		inquireComplete = false;
		startPC.display("[SendT][Static_Inquire] Perfoming Inquire");
		if(!neighbors.Contains(MAC_NXT_RODAS))
			neighbors.AddNeighbor(MAC_NXT_RODAS);
		inquireComplete = true;
	}
	
	private void Inquire() throws Exception {
		startPC.display("[SendT][Inquire] started");
		inquireComplete = false;
		Server = new ClientServer(false);
		startPC.display("[SendT][Inquire] espera que inquire esteja completo");
		System.out.print("Performing Inquire");
		while (!Server.inquiryCompleted){
			System.out.print(".");
			sleep(2000);
		}
		startPC.display("[SendT][Inquire] inquire completo, e vai terminar o cliente");
		Server.CloseAll();
		Thread.sleep(100);
		inquireComplete = true;
	}

	private void SendmsgBT(msgBT msg) throws Exception {
		startPC.display("[SendT][SendmsgBT] init");
		Send(msg.Type);
		if (msg.Type.equals("DATA")) {
			startPC.display("[SendT][SendmsgBT] DATA");
			msgDATA DATA = (msgDATA) msg;
			Send(DATA.DestMAC);
			Send(DATA.SrcMAC);
			Send(DATA.Data);
			Send(DATA.Type);
		} else if (msg.Type.equals("RREQ")) {
			startPC.display("[SendT][SendmsgBT] RREQ");
			msgRREQ AODV_RREQ = (msgRREQ) msg;
			Send("" + AODV_RREQ.RREQID);
			Send("" + AODV_RREQ.HopCount);
			Send(AODV_RREQ.DestMAC);
			Send("" + AODV_RREQ.DestSeqN);
			Send(AODV_RREQ.OriginatorMAC);
			Send("" + AODV_RREQ.OridinatorSeqN);
		} else if (msg.Type.equals("RREP")) {
			startPC.display("[SendT][SendmsgBT] RREP");
			msgRREP AODV_RREP = (msgRREP) msg;
			Send("" + AODV_RREP.Lifetime);
			Send("" + AODV_RREP.HopCount);
			Send(AODV_RREP.DestMAC);
			Send("" + AODV_RREP.DestSeqN);
			Send(AODV_RREP.OriginatorMAC);
			Send("" + AODV_RREP.OridinatorSeqN);
		} else if (msg.Type.equals("RERR")) {
			startPC.display("[SendT][SendmsgBT] RRER");
			msgRERR AODV_RERR = (msgRERR) msg;
			Send("" + AODV_RERR.errors);
			Send("" + AODV_RERR.hops);
			for (int i = 0; i < AODV_RERR.errors; i++) {
				startPC.display("[SendT][SendmsgBT] for de erros");
				Send("" + AODV_RERR.ErrorRouteMAC[i]);
			}
		}
	}

	private void Send(String output) throws Exception {
		dos.writeChars(output + "\n");
		dos.flush();
	}

	private msgRREQ CreateRREQ(String searchMAC) {
		return new msgRREQ(0, 1, searchMAC, 0, nodeStatus.myMAC(), 0);
	}

	private boolean SendRREQ(msgRREQ msg) throws Exception {
		startPC.display("[SendT][SendRREQ] Vai fazer inquire");
		Static_Inquire();
		startPC.display("[SendT][SendRREQ] Apos inquire e vai fazer return");
		return ConnectAndMultiSend(msg);
	}
	
	private boolean generateRERR(String errorRoute){
		
		
		return false;
	}
}