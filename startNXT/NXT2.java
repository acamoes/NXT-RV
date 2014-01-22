package startNXT;

import lejos.nxt.*; import lejos.robotics.navigation.DifferentialPilot;
// this is required for all programs that run on the NXT

public class NXT2 extends Thread {
	
	private static NodeStatus nodeStatus = null;
	private static NXT2 instance = null;
	
	DifferentialPilot pilot = new DifferentialPilot(2.25f, 5.5f, Motor.A, Motor.B);
//	LightSensor ls = new LightSensor(SensorPort.S1);
//	int currentLight;

//	int lastColour = 0; //0=white, 1=black
//	float start = System.currentTimeMillis();    
//	float currentTime = 0;
//	float lastTime = 0;
//	float timeBetweenPostion = 0;
//	float speed = 0;
//	int tamanhoBloco = 21; //folha papel 21 cm's
	
	static private OutputBuffer buffer;
	static int _position = 0;
	static int _speed = 0;
	static int _posLigar = 0;
	static int _posDesligar = 0;
	static boolean _jaMandouLigar = false;
	static boolean _jaMandouDesligar = false;
	static String MAC_PC = "001583523E14";
	static String MAC_NXT_TANK = "00165308E592";
	static String MAC_NXT_RODAS = "00165302CFCB";
	
	
	static String identidade;

	public NXT2() {
		nodeStatus = NodeStatus.getInstance();
		buffer = OutputBuffer.getInstance();
	}

	public static NXT2 getInstance() {
		if (instance == null) instance = new NXT2();
		return instance;
	}
	
	public void run() {
		if(nodeStatus.myMAC().equals(MAC_NXT_TANK))
				_position = -16;
		while(true)
		{
			try {
				Thread.sleep(5000);
				_position++;
				LCD.drawString("P:" + _position,0,6);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (nodeStatus.myMAC().equals(MAC_NXT_RODAS))
			{
				identidade = "RODAS";
			}
			else if (nodeStatus.myMAC().equals(MAC_NXT_TANK))
			{
				identidade = "TANK";
			}
			
//			if (Button.LEFT.isDown()){
//				LCD.drawString("left pressed", 0, 6);
//				LCD.refresh();
//				//msgDATA msg = new msgDATA(MAC_PC, nodeStatus.myMAC(), "emergency " + identidade, "emergency");
//				//buffer.AddOutputMsg(msg);
//			}
			
			if(_posLigar!=0)
			{
				LCD.drawString(_position + " " + _posLigar +" " + _jaMandouLigar, 0, 3);
				LCD.refresh();
				if(_position >= _posLigar && !_jaMandouLigar)
				//if(_position >= _posLigar)
				{
					msgDATA msg = new msgDATA(MAC_PC, nodeStatus.myMAC(), "liga" + identidade, "respPosL");
					buffer.AddOutputMsg(msg);
					_jaMandouLigar = true;
					LCD.drawString("send liga" + identidade, 0, 1);
					LCD.refresh();
				}
				if(_position >= _posDesligar && !_jaMandouDesligar)
				//if(_position >= _posDesligar)
				{
					msgDATA msg = new msgDATA(MAC_PC, nodeStatus.myMAC(), "desliga" + identidade, "respPosL");
					buffer.AddOutputMsg(msg);
					_jaMandouDesligar = true;
					LCD.drawString("send desliga" + identidade, 0, 1);
					LCD.refresh();
				}
			}
			
			pilot.setTravelSpeed(2);
			pilot.forward();
			
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
//				LCD.clear(); LCD.drawString(currentLight + " BLACK" + "\nPostion = " + position + 
//						"\nc Time = " + currentTime + "\nb Time = " + timeBetweenPostion + "\nSpeed = " + speed,0,0);
//			}
//			else {
//				if(lastColour == 1){
//					position++;
//					lastColour = 0;
//					timeBetweenPostion = currentTime - lastTime;
//					speed = tamanhoBloco/timeBetweenPostion;
//					lastTime = currentTime;
//				}
//				LCD.clear(); LCD.drawString(currentLight + " WHITE" + "\nPostion = " + position + "\nc Time = " + currentTime + "\nb Time = " + timeBetweenPostion + "\nSpeed = " + speed,0,0);
//			}

					
		}
	}
}
	
