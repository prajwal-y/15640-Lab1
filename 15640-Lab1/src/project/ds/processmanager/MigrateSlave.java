package project.ds.processmanager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MigrateSlave extends Thread {

	String host = null;
	Socket client = null;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	public MigrateSlave(String hostname) {
		host = hostname;
	}
	
	@Override
	public void run() {
		try {
			client = new Socket("Master-Machine", ProcessConstants.serverport);
			in = new ObjectInputStream(client.getInputStream());
			out = new ObjectOutputStream(client.getOutputStream());
			client.close();
		} catch (UnknownHostException e) {
			System.out.println("UnknownHostException occurred in Client with error "+e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException occurred in Client with error "+e.getMessage());
		}
		

	}

}
