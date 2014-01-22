package startNXT;

import java.io.*;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import lejos.nxt.LCD;

public class Client implements DiscoveryListener {

	/** Creates a new instance of ClientServer */
//	UUID RFCOMM_UUID = new UUID(0x0003);
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

//	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
//	static String MAC_NXT_RODAS = "00165302CFCB";
	private static String validDevices[] = { MAC_NXT_TANK };
//	private static String validDevices[] = { MAC_PC, MAC_NXT_RODAS };
	private NodeStatus nodeStatus = NodeStatus.getInstance();
	int i = 0;

	public Client() {
		m_LclDevice = null;
		InitClient();

	}

	public RemoteDevice[] getDevices() {
		return m_DscrAgent.retrieveDevices(INQUIRY_COMPLETED);
	}

	public Vector getDevList() {
		while (!inquiryCompleted);
		return devList;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	private void InitClient() {
		SearchAvailDevices();
	}

	public void SearchAvailDevices() {
		try {
			// First get the local device and obtain the discovery agent.
			m_LclDevice = LocalDevice.getLocalDevice();
			m_DscrAgent = m_LclDevice.getDiscoveryAgent();
			m_DscrAgent.startInquiry(DiscoveryAgent.GIAC, this);
		} catch (BluetoothStateException ex) {
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
		} catch (Exception e) {
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
	}

	// called when service search gets complete
	public void serviceSearchCompleted(int transID, int respCode) {
	}

	void CloseAll() {
		try {
			if (m_DOutput != null)
				m_DOutput.close();

			if (m_DInput != null)
				m_DInput.close();

			if (m_StrmNotf != null)
				m_StrmNotf.close();

//			startPC.display("[ClientServer][CloseAll] StrmNotf done");
			 if (m_LclDevice != null){
				 m_LclDevice.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
			 }
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}


	// Called when device is found during inquiry
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		try {
			if (isValidDevice(btDevice.getBluetoothAddress())) {
				Neighbors.getInstance().AddNeighbor(btDevice.getBluetoothAddress());
				devList.addElement(btDevice.getBluetoothAddress());
			}
		} catch (Exception e) {
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
				return true;
			}
		}
		return false;
	}
}
