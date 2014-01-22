package startNXT;

import lejos.nxt.*;



public class STOPThread extends Thread {

	double start,end;
	double number;
	
	public STOPThread()
	{
		start = (double)System.currentTimeMillis();
	}
	
	public void run() {
		
		if (Button.LEFT.isDown()){
			LCD.drawString("left pressed", 0, 6);
			LCD.refresh();
			//msgDATA msg = new msgDATA(MAC_PC, nodeStatus.myMAC(), "emergency " + identidade, "emergency");
			//buffer.AddOutputMsg(msg);
		}
		
		Button.ESCAPE.waitForPressAndRelease();
		end = (double)System.currentTimeMillis();
		Runtime.getRuntime().freeMemory();
		LCD.clear();
		if ((end-start)/1000 < 60){
			LCD.drawString((end-start)/1000 + "  seconds", 1 , 3);
		}else 
			LCD.drawString(((double)((int)((end-start)/60)))/1000 + "  minutes", 1, 3);
		LCD.drawString("Runtime:  ", 0, 2);
		LCD.drawString("Shuting Down...", 0, 5);
		LCD.refresh();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		
//		MotorPort.C.controlMotor(0, 1);
		
		System.exit(0);
	}
}