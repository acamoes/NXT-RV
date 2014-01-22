package startNXT;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.*;
import lejos.nxt.comm.*;

public class SendThread extends Thread {
	private BTConnection btc;
	public boolean connectionOK = false;
	public boolean connectionTerminated = false;
	private WaitingForRREPBuffer RREPBuffer;
	private OutputBuffer buffer;
	private Neighbors neighbors;
	private Vector NeighborsVector;
	private RoutingTable routingTable;
	private ArrayList<RemoteDevice> devList = null;
	// private boolean inquireComplete = false;
	private RemoteDevice btrd;

	private DataInputStream dis;
	private DataOutputStream dos;

	// static String myMAC = Bluetooth.getLocalAddress();
	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
	private static String validDevices[] = {MAC_PC , MAC_NXT_TANK , MAC_NXT_RODAS};
	private static NodeStatus nodeStatus = null;
	private int i=0;

	public SendThread() {
		buffer = OutputBuffer.getInstance();
		RREPBuffer = WaitingForRREPBuffer.getInstance();
		neighbors = Neighbors.getInstance();
		NeighborsVector = neighbors.GetNeighbors();
		routingTable = RoutingTable.getInstance();
		btc = null;
		devList = Bluetooth.getKnownDevicesList();
		nodeStatus = NodeStatus.getInstance();
	}

	public void run() {
		while (true) {
			if (!buffer.IsEmpty()) {

				while (nodeStatus.isBusy());		
				
				nodeStatus.setBusyMode(true);

				msgBT toSend = buffer.GetNextMessage();

				LCD.drawString("Sending " + toSend.Type, 0, 4);
				LCD.refresh();
				try {
					Thread.sleep(300);
				} catch (Exception e) {
					LCD.drawString(e.toString(), 0, 6);
				}

				if (toSend.Type.equals("DATA")) {
					ConnectAndSend((msgDATA) toSend);
				} else if (toSend.Type.equals("RREQ")) {
					ConnectAndSend((msgRREQ) toSend);
				} else if (toSend.Type.equals("RREP")) {
					ConnectAndSend((msgRREP) toSend);
				} else if (toSend.Type.equals("RERR")) {
					ConnectAndSend((msgRERR) toSend);
				}

				nodeStatus.setBusyMode(false);
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					LCD.drawString(e.toString(), 0, 6);
				}
			}
			
			if(nodeStatus.myMAC().equals(MAC_NXT_RODAS) == true)
			{
				try {
					if(NeighborsVector.size()==0){
						Client client = new Client();
						Thread.sleep(5000);
					} else
					{
						String n = (String) NeighborsVector.elementAt(0);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean ConnectAndSend(msgDATA msg) {
		if (KnownDestination(msg.DestMAC)) {
			for (int i = 0; i < 10; i++) {
				LCD.drawString("Connect.. #" + (i + 1)+"    ", 0, 7);
				btc = null;

				if(msg.Type.equals("respPosL") && !neighbors.Contains(MAC_PC))
					neighbors.AddNeighbor(MAC_PC);
					
				if (neighbors.Contains(msg.DestMAC)) {
					btc = Bluetooth.connect(msg.DestMAC, NXTConnection.LCP);
				} else{
					btc = Bluetooth.connect(routingTable.GetNextHopByDest(msg.DestMAC), NXTConnection.LCP);
				}
				if (btc != null) {
//					LCD.drawString("ja fez connect..", 0 , 4);
					dis = btc.openDataInputStream();
					dos = btc.openDataOutputStream();

					try {
						SendmsgBT(msg);
					} catch (Exception e1) {
					}

					try {
						dis.close();
						dos.close();
						Thread.sleep(200);
					} catch (Exception e) {
						LCD.drawString(e.toString(), 0, 6);
					}
					
					btc.close();
					LCD.drawString("...sent " + i++, 0, 5);
					LCD.refresh();
					return true;
				}
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
			return false;

		} else if (msg.DestMAC.equals("Multicast")) {
			LCD.drawString("Multicast", 0, 3);
			LCD.refresh();
			return ConnectAndMultiSend(msg);
		}

		RREPBuffer.AddOutputMsg(msg);
		try {
			SendRREQ(CreateRREQ(msg.DestMAC));
		} catch (Exception e) {
			LCD.drawString("Error SendRREQ", 0, 7);
			LCD.refresh();
		}
		return false;
	}

	private boolean ConnectAndSend(msgRREQ msg) {
		// return ConnectAndMultiSend(msg);
		try {
			return SendRREQ(msg);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean ConnectAndSend(msgRREP msg) {
		if (KnownDestination(msg.DestMAC)) {
			for (int i = 0; i < 10; i++) {
				LCD.drawString("Connect.. #" + (i + 1)+"    ", 0, 7);
				//LCD.drawString("to: "+ routingTable.GetNextHopByDest(msg.DestMAC), 0, 6);
				try {
					Thread.sleep(300);
				} catch (Exception e2) {
				}
				LCD.refresh();
				btc = null;
				btc = Bluetooth.connect(routingTable.GetNextHopByDest(msg.DestMAC), NXTConnection.LCP);
				if (btc != null) {
					dis = btc.openDataInputStream();
					dos = btc.openDataOutputStream();

					try {
						SendmsgBT(msg);
					} catch (Exception e1) {
					}

					try {
						dis.close();
						dos.close();
						Thread.sleep(200);
					} catch (Exception e) {
						LCD.drawString(e.toString(), 0, 6);
					}
					btc.close();
					LCD.drawString("...sent ", 0, 5);
					LCD.refresh();
					return true;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
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
		btc = null;
		for (i = 0; i < numOfNeighbors; i++) {
			
			sendTo = (String) NeighborsVector.elementAt(i);
			if (!sendTo.equals(exceptThisMAC1)
					&& !sendTo.equals(exceptThisMAC2)) {
				for (int j = 0; j < 10; j++) {
					btc = null;
					btc = Bluetooth.connect(sendTo, NXTConnection.LCP);
					if (btc != null) {
						dis = btc.openDataInputStream();
						dos = btc.openDataOutputStream();

						try {
							SendmsgBT(msg);
						} catch (Exception e1) {
						}

						try {
							dis.close();
							dos.close();
							btc.close();
							Thread.sleep(200);
						} catch (Exception e) {
							LCD.drawString(e.toString(), 0, 6);
						}
						sent++;
						break;
					}
					try {
						Thread.sleep(500);
					} catch (Exception e) {
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
		btc = null;
		for (i = 0; i < numOfNeighbors; i++) {
			sendTo = (String) NeighborsVector.elementAt(i);
			for (int j = 0; j < 10; j++) {
				btc = null;
				btc = Bluetooth.connect(sendTo, NXTConnection.LCP);

				if (btc != null) {
					dis = btc.openDataInputStream();
					dos = btc.openDataOutputStream();

					try {
						SendmsgBT(msg);
					} catch (Exception e) {
					}

					try {
						dis.close();
						dos.close();
						btc.close();
						Thread.sleep(200);
					} catch (Exception e) {
						LCD.drawString(e.toString(), 0, 6);
					}
					sent++;
					break;
				}
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
		}
		if (numOfNeighbors == sent)
			return true;
		return false;
	}

	private boolean ConnectAndMultiSend(msgRREQ msg) {
		int i = 0, numOfNeighbors = 0, sent = 0;
		String neighborAddress = "";
		String exceptThisMAC1 = msg.OriginatorMAC;
		String exceptThisMAC2 = routingTable.GetNextHopByDest(msg.OriginatorMAC);

		NeighborsVector = neighbors.GetNeighbors();
		numOfNeighbors = NeighborsVector.size();
		btc = null;
		for (i = 0; i < numOfNeighbors; i++) {
			neighborAddress = (String) NeighborsVector.elementAt(i);
			if (!neighborAddress.equals(exceptThisMAC1) && !neighborAddress.equals(exceptThisMAC2)) {
				for (int j = 0; j < 10; j++) {
					LCD.drawString("Connect to " + i + " #" + j+" ", 0, 7);
					LCD.refresh();
					btc = null;
					btc = Bluetooth.connect(neighborAddress, NXTConnection.LCP);
					if (btc != null) {
						dis = btc.openDataInputStream();
						dos = btc.openDataOutputStream();
						try {
							SendmsgBT(msg);
						} catch (Exception e) {
						}

						try {
							dis.close();
							dos.close();
							btc.close();
							Thread.sleep(200);
						} catch (Exception e) {
							LCD.drawString(e.toString(), 0, 6);
						}
						sent++;
						LCD.drawString("...sent ", 0, 5);
						LCD.drawString("                ", 0, 7);
						LCD.refresh();
						break;
					}

				//	try {
					//	Thread.sleep(500);
					//} catch (Exception e) {
				//	}
				}
			}
		}
		if (numOfNeighbors == sent)
			return true;
		return false;
	}


	private boolean KnownDestination(String toCheck) {
		if (neighbors.Contains(toCheck))
			return true;
		return routingTable.HasDestination(toCheck);
	}

//	private void Real_Inquire() throws Exception {
//		// inquireComplete = false;
//		int j=0;
//		while(5 > j++){
//			int[] cod = { 0, 0, 0, 0 }; // Any
//			devList = null;
//			devList = Bluetooth.inquire(5, 2, cod[j]);
//			if (devList != null) {
//				for (int i = 0; i < devList.size(); i++) {
//					btrd = devList.get(i);
//					if (isValidDevice(btrd.getBluetoothAddress())) {
//						Bluetooth.addDevice(btrd);
//						neighbors.AddNeighbor(btrd.getBluetoothAddress());
//						LCD.drawString("NI " + btrd.getBluetoothAddress(), 0, 5);
//						LCD.refresh();
//					}
//				}
//			}
//		}
//		// inquireComplete = true;
//	}

	private void Static_Inquire() throws Exception {
		LCD.drawString("Inquire...", 0, 6);
		LCD.refresh();

		if (nodeStatus.myMAC().equals(MAC_NXT_RODAS) && !neighbors.Contains(MAC_NXT_TANK)) {
			neighbors.AddNeighbor(MAC_NXT_TANK);
		}
	}

	private void SendmsgBT(msgBT msg) throws Exception {
		Send(msg.Type);
		if (msg.Type.equals("DATA")) {
			msgDATA DATA = (msgDATA) msg;
			Send(DATA.DestMAC);
			Send(DATA.SrcMAC);
			Send(DATA.Data);
			Send(DATA.Type);
		} else if (msg.Type.equals("RREQ")) {
			msgRREQ AODV_RREQ = (msgRREQ) msg;
			Send("" + AODV_RREQ.RREQID);
			Send("" + AODV_RREQ.HopCount);
			Send(AODV_RREQ.DestMAC);
			Send("" + AODV_RREQ.DestSeqN);
			Send(AODV_RREQ.OriginatorMAC);
			Send("" + AODV_RREQ.OridinatorSeqN);
		} else if (msg.Type.equals("RREP")) {
			msgRREP AODV_RREP = (msgRREP) msg;
			Send("" + AODV_RREP.Lifetime);
			Send("" + AODV_RREP.HopCount);
			Send(AODV_RREP.DestMAC);
			Send("" + AODV_RREP.DestSeqN);
			Send(AODV_RREP.OriginatorMAC);
			Send("" + AODV_RREP.OridinatorSeqN);
		} else if (msg.Type.equals("RERR")) {
			msgRERR AODV_RERR = (msgRERR) msg;
			Send("" + AODV_RERR.errors);
			Send("" + AODV_RERR.hops);
			for (int i = 0; i < AODV_RERR.errors; i++) {
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
		Static_Inquire();
		return ConnectAndMultiSend(msg);
	}
}