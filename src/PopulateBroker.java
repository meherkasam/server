import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class PopulateBroker implements Runnable {
	static ObjectInputStream is = null;
	static ObjectOutputStream os = null;
	PopulateBroker() {
		
	}
	
	public void run() {
		Socket serverSocket;
		try {
			serverSocket = new Socket(Listener.brokerHost, Listener.brokerPort);
			os = new ObjectOutputStream(serverSocket.getOutputStream());
			is = new ObjectInputStream(serverSocket.getInputStream());
			
			File folder = new File(Listener.serverRoot);
			String fileList = "";
			File[] listOfFiles = folder.listFiles();
			for(int i = 0; (i < listOfFiles.length); i++) {
				fileList += listOfFiles[i].getName() + " ";
			}
			DataObject helloObject = new DataObject(0);
			Listener.reqNo++;
			helloObject.senderId = Listener.senderId;
			helloObject.message = "Req Hello " + Listener.reqNo + " " + Listener.localPort + " " + Listener.maxLoad + " " + fileList;
			os.writeObject(helloObject);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not connect to broker server");
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error communicating with broker server");
			System.exit(1);
		}
	}
}
