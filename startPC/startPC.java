/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package startPC;

/*
 * This relies on having the Java Look and Feel Graphics Repository
 * (jlfgr-1_0.jar) in the class path.  You can download this file
 * from http://java.sun.com/developer/techDocs/hi/repository/.
 * Put it in the class path using one of the following commands
 * (assuming jlfgr-1_0.jar is in a subdirectory named jars):
 *
 *   java -cp .;jars/jlfgr-1_0.jar startPC [Microsoft Windows]
 *   java -cp .:jars/jlfgr-1_0.jar startPC [UNIX]
 *
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class startPC extends JPanel
implements ItemListener {
	protected static JTextArea textArea;
	protected static String newline = "\n";
	protected Action leftAction, middleAction, rightAction;
	protected JCheckBoxMenuItem[] cbmi;

	static String inputData = null;
	static SerialTest serialtest = SerialTest.getInstance();
	DataOutputStream dos;
	DataInputStream dis;
	boolean state = true;

	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
	static NodeStatus nodeStatus = NodeStatus.getInstance();
	static OutputBuffer buffer = OutputBuffer.getInstance();
	static int numCar = 0;

	public startPC() {
		super(new BorderLayout());

		//Create a scrolled text area.
		textArea = new JTextArea(5, 30);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		//Lay out the content pane.
		setPreferredSize(new Dimension(500, 600));
		add(scrollPane, BorderLayout.CENTER);

		//Create the actions shared by the toolbar and menu.
		leftAction =   new LeftAction(  "Move",
				createNavigationIcon("Back24"),
				"This is the left button.", 
				new Integer(KeyEvent.VK_L));
		middleAction = new MiddleAction("Emergency",
				createNavigationIcon("Up24"),
				"This is the middle button.", 
				new Integer(KeyEvent.VK_M));
		rightAction =  new RightAction( "Lights",
                                        createNavigationIcon("Forward24"),
                                        "This is the right button.", 
                                        new Integer(KeyEvent.VK_R));
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createNavigationIcon(String imageName) {
		String imgLocation = "toolbarButtonGraphics/navigation/"
				+ imageName
				+ ".gif";
		java.net.URL imageURL = startPC.class.getResource(imgLocation);

		if (imageURL == null) {
//			System.err.println("Resource not found: "
//					+ imgLocation);
			return null;
		} else {
			return new ImageIcon(imageURL);
		}
	}

	public JMenuBar createMenuBar() {
		JMenuItem menuItem = null;
		JMenuBar menuBar;

		//Create the menu bar.
		menuBar = new JMenuBar();

		//Create the first menu.
		JMenu mainMenu = new JMenu("Road Side Unit - Inteligent Ilumination System IIS 1.0");

		Action[] actions = {leftAction, middleAction};//, rightAction};
		for (int i = 0; i < actions.length; i++) {
			menuItem = new JMenuItem(actions[i]);
			menuItem.setIcon(null); //arbitrarily chose not to use icon
			mainMenu.add(menuItem);
		}

		//Set up the menu bar.
		menuBar.add(mainMenu);
		//menuBar.add(createAbleMenu());
		return menuBar;
	}

	public void createToolBar() {
		JButton button = null;

		//Create the toolbar.
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.PAGE_START);

		//first button
		button = new JButton(leftAction);
		if (button.getIcon() != null) {
			button.setText(""); //an icon-only button
		}
		toolBar.add(button);

		//second button
		button = new JButton(middleAction);
		if (button.getIcon() != null) {
			button.setText(""); //an icon-only button
		}
		toolBar.add(button);

		//third button
		 button = new JButton(rightAction);
        if (button.getIcon() != null) {
            button.setText(""); //an icon-only button
        }
        toolBar.add(button);
	}

	protected JMenu createAbleMenu() {
		JMenu ableMenu = new JMenu("Action State");
		cbmi = new JCheckBoxMenuItem[3];

		cbmi[0] = new JCheckBoxMenuItem("First action enabled");
		cbmi[1] = new JCheckBoxMenuItem("Second action enabled");
		cbmi[2] = new JCheckBoxMenuItem("Third action enabled");

		for (int i = 0; i < cbmi.length; i++) {
			cbmi[i].setSelected(true);
			cbmi[i].addItemListener(this);
			ableMenu.add(cbmi[i]);
		}

		return ableMenu;
	}

	public void itemStateChanged(ItemEvent e) {
		JCheckBoxMenuItem mi = (JCheckBoxMenuItem)(e.getSource());
		boolean selected =
				(e.getStateChange() == ItemEvent.SELECTED);

		//Set the enabled state of the appropriate Action.
		if (mi == cbmi[0]) {
			leftAction.setEnabled(selected);
		} else if (mi == cbmi[1]) {
			middleAction.setEnabled(selected);
		} else if (mi == cbmi[2]) {
			rightAction.setEnabled(selected);
		}
	}

	public static void display(String toDisplay) {
		String s = (toDisplay+newline);
		textArea.append(s);
	}

	public class LeftAction extends AbstractAction {
		public LeftAction(String text, ImageIcon icon,
				String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		public void actionPerformed(ActionEvent e) {
			//display("Action for first button/menu item");
					move();
		}
	}

	public class MiddleAction extends AbstractAction {
		public MiddleAction(String text, ImageIcon icon,
				String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		public void actionPerformed(ActionEvent e) {
			//display("Action for second button/menu item");
					STOP();
		}
	}

	public class RightAction extends AbstractAction {
        public RightAction(String text, ImageIcon icon,
                           String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }
        public void actionPerformed(ActionEvent e) {
        	lights(state, false);
        	System.out.println("Lights was called with state = " + state);
        	if(state==true)
        	{
        		state=false;
        	}
        	else
        	{
        		state=true;
        	}
        }
    }

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the 
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("startPC");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create/set menu bar and content pane.
		startPC demo = new startPC();
		frame.setJMenuBar(demo.createMenuBar());
		demo.createToolBar();
		demo.setOpaque(true); //content panes must be opaque
		frame.setContentPane(demo);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) throws Exception{

		//myMAC = LocalDevice.getLocalDevice().getBluetoothAddress();
		buffer = OutputBuffer.getInstance();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		Thread.sleep(300);

		////// Starting All Threads
		ReceiveThread ReceiveConnection = new ReceiveThread();
		ReceiveConnection.start();

		SendThread SendConnection = new SendThread();
		SendConnection.start();

		Thread.sleep(500);
		startPC.display("myMAC          : "+nodeStatus.myMAC());
		
//		boolean noOneInRange = true;
//		ClientServer client = new ClientServer(false);
//		while(noOneInRange)
//		{
//			if(client.inquiryCompleted && client.isDevListEmpty())
//				client.SearchAvailDevices();
//			else if(client.inquiryCompleted && !client.isDevListEmpty()){
//				noOneInRange = false;
//			}
//		}
//		Vector v = client.devList;
//		int vectorSize = v.size();
//		for(int i=0; i<vectorSize; i++){
//			startPC.display("v[" + i + "] = " + v.get(i));	
//		}
		//TODO talvez está mal
		msgRREQ rreq = new msgRREQ(0, 1, MAC_NXT_RODAS, 0, nodeStatus.myMAC(), 0);
		buffer.AddOutputMsg(rreq);
	}
	
	public static void lights(boolean state, boolean distanciaPequena)
    {
		if(state == true){
			startPC.display("== Lights On called ===");
			numCar++;
	    	serialtest.setState(state);
	    	serialtest.writeSerial();
		}
		else if(state == false && distanciaPequena == false && numCar==1){
			startPC.display("== Lights Off called ===");
			numCar--;
	    	serialtest.setState(state);
	    	serialtest.writeSerial();
		}
//		else if(state == false && distanciaPequena == false && numCar > 1){
//			startPC.display("== Lights Off called TRINCO ===");
//			numCar--;
//		}
		else if(state == false && distanciaPequena == true && numCar==1){
			startPC.display("== Lights Off called ===");
			numCar--;
	    	serialtest.setState(state);
	    	serialtest.writeSerial();
		}
		else if(state == false && distanciaPequena == true && numCar > 1){
			startPC.display("== Lights On called TRINCO ===");
			numCar--;
		}

//    	serialtest.setState(state);
//    	serialtest.writeSerial();
    }
	
	public static void emergency()
	{
		startPC.display("Emergency Called");
		serialtest.writeSerialEmergency();
	}
	
	public static void move(){
		if (nodeStatus.myMAC().equals(MAC_PC)){
			msgDATA data = new msgDATA(MAC_NXT_RODAS, nodeStatus.myMAC(), "move", "ssssss");
			buffer.AddOutputMsg(data);
		}
	}

	public static void STOP(){
		
		emergency();
//		if (nodeStatus.myMAC().equals(MAC_PC)){
//			msgDATA data = new msgDATA(MAC_NXT_TANK, nodeStatus.myMAC(), "stop", "dddddddd");
//			buffer.AddOutputMsg(data);
//		}
	}
}
