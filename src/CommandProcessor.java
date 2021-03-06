import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Iterator;

/**
 * 
 * @author meher
 *
 * This class acts as a processor of the various commands
 * issued by the client. It processes the input, calls
 * the relevant functions which complete the task and control
 * is returned to the MultiListen class.
 * 
 */

public class CommandProcessor {
	public final static int KB2B = 1024;
	private static ConcurrentHashMap<String, File> lookedUpFiles = null;
	FileOutputStream streamToBeWritten = null;
	public static ConcurrentHashMap<String, FileObject> listOfFileObjects = null;
	public CommandProcessor() {
		lookedUpFiles = new ConcurrentHashMap<String, File>();
		listOfFileObjects = new ConcurrentHashMap<String, FileObject>();
		File folder = new File(Listener.serverRoot);
		int i = 0;
		File[] listOfFiles = folder.listFiles();
		for(i = 0; (i < listOfFiles.length); i++) {
			FileObject a = new FileObject (listOfFiles[i]);
			listOfFileObjects.put(listOfFiles[i].getName(),a);
		}
	}
	public DataObject process(DataObject input) {
		String[] tokens = input.message.split(" ");
		if(tokens[0].compareToIgnoreCase("Req")==0) {
			if(tokens[1].compareToIgnoreCase("HELLO") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				Hello(a);
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("BYE") == 0) {
				DataObject a = new DataObject(0, -1);
				Bye(a);
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("LIST") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				List(a, Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("GET") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				Get(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PULL") == 0) {
				DataObject a = new DataObject(Integer.parseInt(tokens[5]), Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				Pull(a, tokens[3], Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PUT") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				Put(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("PUSH") == 0) {
				DataObject a = new DataObject(Integer.parseInt(tokens[6]), Integer.parseInt(tokens[2]));
				a.data = input.data;
				a.length = input.length;
				a.senderId = input.senderId;
				boolean isLast = false;
				if(tokens[4].compareTo("LAST") == 0) {
					isLast = true;
				}
				Push(a, tokens[3], Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]), isLast);
				return a;
			}
			else if(tokens[1].compareToIgnoreCase("DELETE") == 0) {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.senderId = input.senderId;
				Delete(a, tokens[3], Integer.parseInt(tokens[4]));
				return a;
			}
			else {
				DataObject a = new DataObject(0, Integer.parseInt(tokens[2]));
				a.message = "RSP 0x003";
				return a;
			}
		}
		return null;
	}
	DataObject Hello(DataObject a) {
		a.message = "Rsp Hello " + String.valueOf(a.reqNo);
		return a;
	}
	DataObject Bye(DataObject a) {
		a.message = "Rsp Bye";
		return a;
	}
	DataObject List(DataObject a, int start, int max, int priority) {
		//File folder = new File(serverRoot);
		String fileList = "";
		int j = 0;
		a.message = "Rsp List " + String.valueOf(a.reqNo);
	    //File[] listOfFiles = folder.listFiles();
		Iterator<FileObject> ii = listOfFileObjects.values().iterator();
		while(ii.hasNext()) {
			FileObject currFile = (FileObject) ii.next();
			//listOfFileObjects.get(fileName).lock.getReadLock(priority);
			String fileName = currFile.fileHandle.getName();
			//listOfFileObjects.get(fileName).lock.readerDone();
			fileList += " " + fileName;
			j++;
		}
	    /*for (i = start; (j < max) && (i < listOfFiles.length); i++) {
	        if (listOfFiles[i].isFile()) {
	        	String fileName = listOfFiles[i].getName();
	        	listOfFileObjects.get(fileName).lock.getReadLock(priority);
	        	fileList += " " + fileName;
	        	listOfFileObjects.get(fileName).lock.readerDone();
	        	j++;
	        }
	    }*/
	    a.message += " " + Integer.toString(start) + " " + Integer.toString(j) + fileList;
	    return a;
	}
	DataObject Get(DataObject a, String fileName, int priority) {
		boolean fileFound = false;
		a.message = "Rsp Get " + String.valueOf(a.reqNo);
		if(listOfFileObjects.containsKey(fileName)) {
			fileFound = true;
		}
		if(!fileFound) {
			a.message += " " + fileName + " FAILURE 0x005";
			a.success = false;
		}
		else {
			try{
				//FileObject x = listOfFileObjects.get(fileName);
				//x.lock.getReadLock(priority);
			}
			catch (Exception E){
				System.out.println(E);
			}
			SecureRandom keyGen = new SecureRandom();
			String randFileId = new BigInteger(130, keyGen).toString(32);
			a.message += " " + fileName + " READY " + randFileId;
			lookedUpFiles.put(a.senderId + ":" + randFileId, listOfFileObjects.get(fileName).fileHandle);
		}
		return a;
	}
	DataObject Put(DataObject a, String fileName, int priority) {
		boolean fileFound = false;
		a.message = "Rsp Put " + String.valueOf(a.reqNo);
		if(listOfFileObjects.containsKey(fileName)) {
			fileFound = true;
		}
		if(fileFound) {
			//a.message += " " + fileName + " FAILURE 0x005";
			//a.success = false;
			//listOfFileObjects.get(fileName).lock.getWriteLock(priority);
			listOfFileObjects.get(fileName).fileHandle.delete();
			//listOfFileObjects.get(fileName).lock.writerDone();
			//listOfFileObjects.remove(fileName);
		}
		SecureRandom keyGen = new SecureRandom();
		String randFileId = new BigInteger(130, keyGen).toString(32);
		a.message += " " + fileName + " READY " + randFileId;
		File fileToBeWritten = new File(Listener.tmpRoot + "tmp_" + fileName);
		if(!fileFound)
			listOfFileObjects.put(fileName, new FileObject(fileToBeWritten));
		//listOfFileObjects.get(fileName).lock.getWriteLock(priority);
		lookedUpFiles.put(a.senderId + ":" + randFileId, listOfFileObjects.get(fileName).fileHandle);
		//FileObject newFileHandle = new FileObject (fileToBeWritten);
		//listOfFileObjects.put(listOfFiles[i].getName(),a);
		try {
			streamToBeWritten = new FileOutputStream(fileToBeWritten);
		}
		catch (IOException E) {
			
		}
		return a;
	}
	DataObject Pull(DataObject a, String fileName, int startByte, int length) {
		System.out.println("Pull requested: " + fileName);
		a.message = "Rsp Pull " + String.valueOf(a.reqNo);
		File folder = new File(Listener.serverRoot);
		File[] listOfFiles = folder.listFiles();
		File fileToRead = null;
		for(int i = 0; (i < listOfFiles.length); i++) {
			String fName = listOfFiles[i].getName();
			if(fName.compareTo(fileName) == 0)
				fileToRead = listOfFiles[i];
		}
		if(fileToRead != null) {
			DataObject b = FileRead(a, fileToRead, startByte, length);
			if (b == null) {
				a.message += " FAILURE 0x005";
				a.success = false;
			}
			else {
				if(a.length < length){
					a.message += " " + "SUCCESS LAST " + Integer.toString(startByte) + " " + Integer.toString(b.length);
				}
				else {
					a.message += " " + "SUCCESS NOTLAST " + Integer.toString(startByte) + " " + Integer.toString(b.length);
				}
			}
		}
		return a;
	}
	DataObject Push(DataObject a, String fileName, int startByte, int length, boolean isLast) {
		a.message = "Rsp Push " + String.valueOf(a.reqNo);
		System.out.println("Push requested: " + fileName);
		File fileToBeWritten = new File(Listener.tmpRoot + "tmp_" + fileName);
		try {
			if(streamToBeWritten == null)
				streamToBeWritten = new FileOutputStream(fileToBeWritten);
			streamToBeWritten.write(a.data, 0, length);
			streamToBeWritten.flush();
			if (isLast) {
				streamToBeWritten.close();
				streamToBeWritten = null;
				File targetDir = new File (Listener.serverRoot);
				fileToBeWritten.renameTo(new File(targetDir, fileName));
			}
		}
		catch(IOException e) {
			a.message += " " + "FAILURE 0x005";
			a.success = false;
			return a;
		}
		a.message += " " + "SUCCESS " + Integer.toString(length);
		return a;
	}
	DataObject FileRead(DataObject a, File fileToBeRead, int startByte, int length) {
		FileInputStream is = null;
		try {
			is = new FileInputStream(fileToBeRead);
			is.skip(startByte);
			int finalLength = is.read(a.data, 0, length);
			a.length = finalLength;
		}
		catch(IOException e) {
			return null;
		}
		catch(Exception e){
			System.out.println(e);	
		}
		return a;
	}
	DataObject Delete(DataObject a, String fileName, int priority) {
		boolean deleted = false, error = false;
		FileObject objectToBeDeleted = listOfFileObjects.get(fileName);
		File toBeDeleted = null;
		if(objectToBeDeleted != null)
			toBeDeleted = objectToBeDeleted.fileHandle;
		else {
			error = true;
		}
		if(!error) {
			//listOfFileObjects.get(fileName).lock.getWriteLock(priority);
			deleted = toBeDeleted.delete();
			//listOfFileObjects.get(fileName).lock.writerDone();
			listOfFileObjects.remove(fileName);
		}
		a.message = "Rsp Delete " + String.valueOf(a.reqNo) + " " + fileName;
		if(deleted && !error) {
			a.message += " SUCCESS";
		}
		else {
			a.success = false;
			a.message += " FAILURE 0x005";
		}
		return a;
	}
}
