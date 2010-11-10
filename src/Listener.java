import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.SecureRandom;
import java.lang.Thread;

public class Listener{
	static BufferedReader configFile = null;
	static String brokerHost = null;
	static int brokerPort = 0;
	static int localPort = 0;
	static int maxLoad = 0;
	static String serverRoot = "";
	static String senderId = "";
	static int reqNo;
	boolean ReadConfig(String fileName) {
		try {
			configFile = new BufferedReader(new FileReader (fileName));
			String configLine = configFile.readLine();
			String[] params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("broker-host:") == 0) {
				brokerHost = params[1];
			}
			else
				return false;
			
			configLine = configFile.readLine();
			params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("broker-port:") == 0) {
				brokerPort = Integer.parseInt(params[1]);
			}
			else
				return false;
			
			configLine = configFile.readLine();
			params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("local-port:") == 0) {
				localPort = Integer.parseInt(params[1]);
			}
			else
				return false;
			
			configLine = configFile.readLine();
			params = configLine.split(" ");
			if(params[0].compareToIgnoreCase("max-load:") == 0) {
				maxLoad = Integer.parseInt(params[1]);
			}
			else
				return false;
		}
		catch (IOException e) {
			return false;
		}
		return true;
	}
	
	ServerSocket listenerSocket = null;
	Socket clientSocket = null;
	public Listener(String fileName) {
		reqNo = 0;
		if(!ReadConfig(fileName)) {
			System.out.println("Error in configuration file. Exiting");
			System.exit(1);
		}
		SecureRandom keyGen = new SecureRandom();
		senderId = new BigInteger(130, keyGen).toString(32);
		PopulateBroker myTalker = new PopulateBroker();
    	Thread p = new Thread(myTalker);
    	p.start();
	    try{
	    		listenerSocket = new ServerSocket(localPort);
	    }
	    catch (IOException e){
	    	System.out.println("Unable to create listener socket: " + e);
	    	System.exit(1);
	    }
	    System.out.println ("Created listener socket successfully on port no. "+ localPort);
	    while(true){
	    	try{
		    	clientSocket = listenerSocket.accept();
		    	System.out.println("Client connected");
		    	MultiListen myListener = new MultiListen(clientSocket);
		    	Thread t = new Thread(myListener);
		    	t.start();
		    }
		    catch(IOException e){
		    	System.out.println(e);
		    }	    	
	    }
	}
	
}
