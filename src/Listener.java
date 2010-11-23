import java.io.*;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.lang.Thread;
import javax.net.ssl.*;

public class Listener{
	static BufferedReader configFile = null;
	static String brokerHost = null;
	static int brokerPort = 0;
	static int localPort = 0;
	static int maxLoad = 0;
	static String serverRoot = "";
	static String tmpRoot = "";
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
	
	SSLServerSocket listenerSocket = null;
	SSLSocket clientSocket = null;
	public Listener(String fileName) {
		tmpRoot = "tmp_" + serverRoot;
		File folder = new File(Listener.tmpRoot);
		if(!folder.exists()) {
			folder.mkdir();
		} else { 
			File[] listOfFiles = folder.listFiles();
			for(int i = 0; (i < listOfFiles.length); i++) {
				listOfFiles[i].delete();
			}
		}
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
    	//End of broker-populate
    	
    	//Start of SSL Server Socket creation
    	String ksName = "herong.jks";
		char ksPass[] = "HerongJKS".toCharArray();
		char ctPass[] = "HerongJKS".toCharArray();
    	try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(ksName), ksPass);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ctPass);
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			listenerSocket = (SSLServerSocket) ssf.createServerSocket(localPort);
			listenerSocket.setEnabledCipherSuites(listenerSocket.getSupportedCipherSuites());
	    }
	    catch (IOException e) {
	    	System.out.println("Unable to create listener socket: " + e);
	    	System.exit(1);
	    }
	    catch (Exception e) {
	         System.err.println(e.toString());
	    }
	    System.out.println ("Created listener socket successfully on port no. "+ localPort);
	    while(true){
	    	try{
		    	clientSocket = (SSLSocket) listenerSocket.accept();
		    	clientSocket.startHandshake();
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
