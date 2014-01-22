package startPC;

import java.io.*;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import lejos.pc.comm.NXTConnector;

//import com.intel.bluetooth.btspp.Connection;

public class ClientServer implements DiscoveryListener {

	/** Creates a new instance of ClientServer */
	UUID RFCOMM_UUID = new UUID(0x0003);
	private StreamConnection m_StrmConn = null;
	private LocalDevice m_LclDevice = null;
	public InputStream m_Input = null;
	public OutputStream m_Output = null;
	public DataInputStream m_DInput = null;
	public DataOutputStream m_DOutput = null;
	private StreamConnectionNotifier m_StrmNotf = null;
	public boolean m_bIsServer = false, m_bServerFound = false,	m_bInitServer = false, m_bInitClient = false;
	private static String m_strUrl;
	private DiscoveryAgent m_DscrAgent = null;
	public Vector devList = new Vector();
	public boolean inquiryCompleted = false;
	private String remoteAddress;

	static String MAC_PC = "001583523E14";
//	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
//	private static String validDevices[] = { MAC_PC, MAC_NXT_TANK, MAC_NXT_RODAS };
	private static String validDevices[] = { MAC_PC, MAC_NXT_RODAS };
	private NodeStatus nodeStatus = NodeStatus.getInstance();
	int i = 0;

	public ClientServer(boolean isServer) {
		m_bIsServer = isServer;
		m_LclDevice = null;

		if (m_bIsServer) {
			InitServer();
		} else {
			InitClient();
		}

	}

	public RemoteDevice[] getDevices() {
		return m_DscrAgent.retrieveDevices(INQUIRY_COMPLETED);
	}

	public Vector getDevList() {
		while (!inquiryCompleted);
		return devList;
	}

	private void InitServer() {

//		m_strUrl = "btspp://localhost:" + RFCOMM_UUID + ";name=rfcommtest"+i+++";authorize=true";
		m_strUrl = "btspp://localhost:" + RFCOMM_UUID;
		
		try {
//			Thread.sleep(10000);
			startPC.display("[ClientServer][InitServer] getLocalDevice");
			m_LclDevice = LocalDevice.getLocalDevice();
			startPC.display("[ClientServer][InitServer] m_LclDevice.getBluetoothAddress " + m_LclDevice.getBluetoothAddress() + "  ,DiscoveryAgent");
//			m_LclDevice.setDiscoverable(DiscoveryAgent.GIAC);
//			m_LclDevice.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
			
			startPC.display("[ClientServer][InitServer] Apos DiscoveryAgent");
			
			m_StrmNotf = (StreamConnectionNotifier) Connector.open(m_strUrl, Connector.READ_WRITE, true);
			// m_StrmNotf = (StreamConnectionNotifier) Connector.open(m_strUrl);

			startPC.display("[ClientServer][InitServer] acceptAndOpen");
			m_StrmConn = m_StrmNotf.acceptAndOpen();

			remoteAddress = RemoteDevice.getRemoteDevice(m_StrmConn).getBluetoothAddress();
			startPC.display("[ClientServer][InitServer] remoteAddress = " + remoteAddress);
			Neighbors.getInstance().AddNeighbor(RemoteDevice.getRemoteDevice(m_StrmConn).getBluetoothAddress());

			m_bInitServer = true;

			m_DOutput = m_StrmConn.openDataOutputStream();
			m_DInput = m_StrmConn.openDataInputStream();
		} catch (BluetoothStateException e) {
			System.err.println("BluetoothStateException: " + e.getMessage());
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}

	}

	// private void InitServer() {
	//
	// // m_strUrl = "btspp://localhost:" + RFCOMM_UUID
	// // + ";name=rfcommtest;authorize=true";
	// m_strUrl = "btspp://";
	//
	// try {
	// m_LclDevice = LocalDevice.getLocalDevice();
	//
	// m_LclDevice.setDiscoverable(DiscoveryAgent.GIAC);
	//
	// NXTConnector conn = new NXTConnector();//create a new NXT connector
	// boolean connected = conn.connectTo("btspp://"); //try to connect to any
	// NXT over bluetooth
	// // m_StrmNotf = (StreamConnectionNotifier) Connector.open(m_strUrl,
	// Connector.READ_WRITE, true);
	// //m_StrmNotf = (StreamConnectionNotifier) Connector.open(m_strUrl);
	//
	// // m_StrmConn = m_StrmNotf.acceptAndOpen();
	//
	// // remoteAddress =
	// RemoteDevice.getRemoteDevice(m_StrmConn).getBluetoothAddress();
	// remoteAddress = conn.getNXTInfo().deviceAddress;
	// startPC.display("[ClientServer-InitServer] device Address = " +
	// conn.getNXTInfo().deviceAddress);
	// //Neighbors.getInstance().AddNeighbor(RemoteDevice.getRemoteDevice(m_StrmConn).getBluetoothAddress());
	//
	// m_bInitServer = true;
	//
	// // m_DOutput = m_StrmConn.openDataOutputStream();
	// // m_DInput = m_StrmConn.openDataInputStream();
	// OutputStream os = conn.getOutputStream();
	// InputStream is = conn.getInputStream();
	// m_DOutput = new DataOutputStream(os);
	// m_DInput = new DataInputStream(is);
	//
	// } catch (BluetoothStateException e) {
	// System.err.println("BluetoothStateException: " + e.getMessage());
	// } catch (IOException ex) {
	// System.err.println("IOException erro, ex.getMessage = " +
	// ex.getMessage());
	// ex.printStackTrace();
	// } catch (Exception e) {
	// System.err.println("Exception: " + e.getMessage());
	// }
	//
	// }

	public String getRemoteAddress() {
		return remoteAddress;
	}

	private void InitClient() {
		SearchAvailDevices();
	}

	public void SearchAvailDevices() {
		try {
			// First get the local device and obtain the discovery agent.
			startPC.display("[ClientServer][SearchAvailDevices] init");
			m_LclDevice = LocalDevice.getLocalDevice();
			m_DscrAgent = m_LclDevice.getDiscoveryAgent();
			m_DscrAgent.startInquiry(DiscoveryAgent.GIAC, this);
		} catch (BluetoothStateException ex) {
			startPC.display("Problem in searching the bluetooth devices");
			ex.printStackTrace();
		}

	}

	public void SendMessages(String v_strData) {
		if ((m_bInitClient) || (m_bInitServer)) {
			try {
				m_Output.write(v_strData.length());
				m_Output.write(v_strData.getBytes());

			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}

	public String Recieve() {

		StringBuffer stringbuffer = new StringBuffer();
		try {
			m_DInput.readChar();
			char c = m_DInput.readChar();

			while (c != '\n') {
				stringbuffer.append(c);
				c = (char) m_DInput.readChar();
				// startPC.display("[ClientServer][Recieve] char while = " + c);
			}
		} catch (IOException ioe) {
			startPC.display("IO Exception writing received bytes");
			startPC.display(" ::" + ioe.toString());
		} catch (Exception e) {
			startPC.display(" ::" + e.toString());
		}
		return stringbuffer.toString();
	}

	/*********************************************************************************************
	 * below are the pure virtual methods of discoverlistern
	 * 
	 * 
	 *******************************************************************************************/

	public void inquiryCompleted(int discType) {
		inquiryCompleted = true;
		startPC.display("InquiryCompleted");
	}

	// called when service search gets complete
	public void serviceSearchCompleted(int transID, int respCode) {
		startPC.display("serviceSearchCompleted");
	}

	void CloseAll() {
		try {
			startPC.display("[ClientServer][CloseAll] D_Output");
			if (m_DOutput != null)
				m_DOutput.close();

			startPC.display("[ClientServer][CloseAll] D_Output done, D_Input");
			if (m_DInput != null)
				m_DInput.close();

			startPC.display("[ClientServer][CloseAll] D_Input done, StrmNotf");
			if (m_StrmNotf != null)
				m_StrmNotf.close();

//			startPC.display("[ClientServer][CloseAll] StrmNotf done");
			 startPC.display("[ClientServer][CloseAll] StrmNotf done, LclDevice");
			 if (m_LclDevice != null){
				 m_LclDevice.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
				 startPC.display("[ClientServer][CloseAll] LclDevice done");
			 }
		} catch (IOException ex) {
			startPC.display("[ClientServer][CloseAll] Erro IO Exception");
			ex.printStackTrace();
		}

	}

	// called when service found during service search
	public void servicesDiscovered(int transID, ServiceRecord[] records) {

		startPC.display("[servicesDiscovered] init");
		for (int i = 0; i < records.length; i++) {
			m_strUrl = records[i].getConnectionURL(
					ServiceRecord.AUTHENTICATE_ENCRYPT, false);

			startPC.display("[servicesDiscovered] " + m_strUrl);
			if (m_strUrl.startsWith("btspp")) // we have found our service
												// protocol
			{
				m_bServerFound = true;
				m_bInitClient = true;
				break;
			}
		}
	}

	// Called when device is found during inquiry
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		try {
			startPC.display("[deviceDiscovered] econtrado = "+ btDevice.getBluetoothAddress());
			if (isValidDevice(btDevice.getBluetoothAddress())) {
				startPC.display("[deviceDiscovered] device é válido");
				Neighbors.getInstance().AddNeighbor(btDevice.getBluetoothAddress());
				devList.add(btDevice.getBluetoothAddress());
				// Get Device Info
				startPC.display("Device Discovered");
				startPC.display("Major Device Class: " + cod.getMajorDeviceClass() + " Minor Device Class: "
						+ cod.getMinorDeviceClass());
				startPC.display("Bluetooth Address: " + btDevice.getBluetoothAddress());
				startPC.display("Bluetooth Friendly Name: "	+ btDevice.getFriendlyName(true));

				// Search for Services
				UUID uuidSet[] = new UUID[1];
				uuidSet[0] = RFCOMM_UUID;
				// int searchID = m_DscrAgent.searchServices(null, uuidSet,
				// btDevice,this);
			}
		} catch (Exception e) {
			startPC.display("Device Discovered Error: " + e);
		}

	}
	
	public boolean isDevListEmpty()
	{
		if(devList.size()!=0)
			return false;
		else
			return true;
	}

	private boolean isValidDevice(String toCheck) {
		int i = 0, length = validDevices.length;
		for (i = 0; i < length; i++) {
			if (validDevices[i].equals(toCheck)) {
				startPC.display("[ClientServer][isValidDevice] return true");
				return true;
			}
		}
		return false;
	}
}
