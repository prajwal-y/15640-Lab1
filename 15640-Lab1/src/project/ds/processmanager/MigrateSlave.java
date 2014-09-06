package project.ds.processmanager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import project.ds.migratableprocess.MigratableProcess;

public class MigrateSlave extends Thread {

	String host = null;
	Socket client = null;
	ObjectInputStream in;
	
	public MigrateSlave(String hostname) {
		host = hostname;
	}
	
	@Override
	public void run() {
		try {
			client = new Socket(host, ProcessConstants.serverport);
			System.out.println("WAitig");
			in = new ObjectInputStream(client.getInputStream());
			System.out.println("Waiting for process...");
			MigratableProcess migratableObj = (MigratableProcess)in.readObject();
			System.out.println("Received process object :" + migratableObj.toString());
		    new Thread((Runnable)migratableObj).start();
			in.close();
			client.close();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException occurred in Client with error "+e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException occurred in Client with error "+e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException occurred in Client with error "+e.getMessage());
		}
	}
}
