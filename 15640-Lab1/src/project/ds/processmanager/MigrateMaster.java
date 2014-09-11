package project.ds.processmanager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import project.ds.migratableprocess.MigratableProcess;

public class MigrateMaster extends Thread {

	ServerSocket server = null;
	ProcessManager pm = null;
	ObjectOutputStream outStream = null;
	ObjectInputStream inStream = null;

	int slaveCounter = 0;
	HashMap<String, Socket> clients = new HashMap<String, Socket>();

	public MigrateMaster(ProcessManager p) {
		pm = p;
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(ProcessConstants.serverport);
			while (true) {
				Socket client = server.accept();
				clients.put(ProcessConstants.SLAVE + "-" + slaveCounter++, client);
			}
		} catch (IOException e) {
			System.out
					.println("Communication error with client: "
							+ e.getMessage());
		}
	}

	public void migrateProcess(String processId, String clientId1,
			String clientId2) {
		if (clients.containsKey(clientId1) && clientId2 == null) {
			ProcessObject pObj = pm.processList.get(processId);
			if(pObj == null) {
				System.out.println("Specified process not found on master. Please try again");
			}
			MigratableProcess migratableObj = pObj.migratableObj;
			Socket client = clients.get(clientId1);
			migratableObj.suspend();
			pm.processList.remove(processId);
			try {
				outStream = new ObjectOutputStream(client.getOutputStream());
				outStream.writeObject(migratableObj);
			} catch (IOException e) {
				System.out.println("Communication error with client: "
						+ e.getMessage());
			}
		} else if (clientId1 == null) {
			System.out.println("Slave " + clientId1
					+ " does not exist. Please enter a valid slave ID");
			return;
		} else if (clientId2 != null) {
			if (clients.containsKey(clientId2)) {
				Socket client1 = clients.get(clientId1);
				try {
					outStream = new ObjectOutputStream(
							client1.getOutputStream());
					outStream.writeObject(processId);

					inStream = new ObjectInputStream(client1.getInputStream());
					Object obj = inStream.readObject();

					if (obj != null
							&& obj.getClass().getName()
									.contains("migratableprocess")) {
						Socket client2 = clients.get(clientId2);
						outStream = new ObjectOutputStream(
								client2.getOutputStream());
						outStream.writeObject(obj);
					}
					else {
						System.out.println("Specified process not found in slave");
					}
				} catch (IOException e) {
					System.out
							.println("Communication error with client: "
									+ e.getMessage());
				} catch (ClassNotFoundException e) {
					System.out
							.println("ClassNotFoundException occurred in migrateProcess: "
									+ e.getMessage());
				}
			} else {
				System.out.println("Slave " + clientId2
						+ " does not exist. Please enter a valid slave ID");
				return;
			}
		}
	}

	public void requestAllClients(String message) {
		for (String clientId : clients.keySet()) {
			requestClient(clientId, message);
		}
	}

	public void requestClient(String clientId, String message) {
		Socket client = clients.get(clientId);
		/*
		 * System.out.println("Retrieved socket for client: " + clientId +
		 * " with IP: " + client.getInetAddress().toString());
		 */
		try {
			ObjectOutputStream outStream = new ObjectOutputStream(
					client.getOutputStream());
			outStream.writeObject(message);
			/*
			 * System.out.println("Written message: " + message +
			 * " to output stream");
			 */

			inStream = new ObjectInputStream(client.getInputStream());
			Object reply = inStream.readObject();
			// System.out.println("Received message: from client");
			if (reply.getClass().getName().contains("HashMap")) {
				System.out.println(clientId);
				ProcessManager
						.listProcesses((HashMap<String, ProcessObject>) reply);
			}

		} catch (IOException e) {
			System.out.println("Communication error with client: "
					+ e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out
					.println("ClassNotFoundException occurred in requestClient: "
							+ e.getMessage());
		}
	}
	
	public void listClients() {
		System.out.println("Slave nodes currently connected to the master: ");
		for(String client : clients.keySet()) {
			System.out.println(client);
		}
	}

	public void close() {
		try {
			if (inStream != null)
				inStream.close();
			if (outStream != null)
				outStream.close();
			for (String clientId : clients.keySet()) {
				clients.get(clientId).close();
			}
		} catch (IOException e) {
			System.out.println("IOException occurred in requestClient: "
					+ e.getMessage());
		}
	}
}
