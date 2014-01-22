package startNXT;

public class SendHelloInterval extends Thread {

	OutputBuffer buffer = null;

	public SendHelloInterval() throws Exception {
		buffer = OutputBuffer.getInstance();
	}

	public void run() {
		while (true) {
			buffer.AddOutputMsg(new msgHELLO());
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
			}
		}
	}

}