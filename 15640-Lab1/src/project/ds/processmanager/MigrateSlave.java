/**
 * Class to handle migration on client
 */
package project.ds.processmanager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import project.ds.migratableprocess.MigratableProcess;

public class MigrateSlave extends Thread {

	String host = null;
	Socket client = null;
	ProcessManager pm = null;
	ObjectInputStream inStream = null;
	ObjectOutputStream outStream = null;

	public MigrateSlave(String hostname, ProcessManager p) {
		host = hostname;
		pm = p;
	}

	@Override
	public void run() {
		try {
			client = new Socket(host, ProcessConstants.serverport);
			while (true) {
				inStream = new ObjectInputStream(client.getInputStream());
				Object obj = inStream.readObject();
				String name = obj.getClass().getName();
				if (name.contains("migratableprocess")) {
					pm.receiveProcess((MigratableProcess) obj);
				} else if (name.contains("String")) {
					if (obj.equals("ps")) {
						outStream = new ObjectOutputStream(
								client.getOutputStream());
						outStream.writeObject(pm.processList);
					}
					if (((String) obj).contains("PRC")) {
						outStream = new ObjectOutputStream(
								client.getOutputStream());
						ProcessObject pObj = (ProcessObject)pm.processList.get(obj);
						MigratableProcess migratableObj = null;
						if(pObj != null) {
							migratableObj = (MigratableProcess)pm.processList.get(obj).migratableObj;
							if(pObj.state != ProcessConstants.SUSPENDED)
								migratableObj.suspend();
							pm.processList.remove(pObj.processId);
						}
						outStream.writeObject(migratableObj);
					}
				}
			}
		} catch (UnknownHostException e) {
			System.out
					.println("Communication error with master: "
							+ e.getMessage());
		} catch (IOException e) {
			System.out.println("Communication error with master: "
					+ e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out
					.println("ClassNotFoundException occurred in Client with error "
							+ e.getMessage());
		}
	}

	public void close() {
		try {
			if(inStream != null)
				inStream.close();
			if(outStream != null)
				outStream.close();
			if(client != null)
				client.close();
		} catch (IOException e) {
			//Ignore here
		}
	}
}
