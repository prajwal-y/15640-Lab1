/**
 * Class to handle migration on the master.
 */
package project.ds.processmanager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import project.ds.migratableprocess.MigratableProcess;

public class MigrateMaster extends Thread {

	ServerSocket server = null;
	ProcessManager pm = null;
	ObjectOutputStream outStream = null;
	ObjectInputStream inStream = null;

	int slaveCounter = 1;
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
				clients.put(ProcessConstants.SLAVE + "-" + slaveCounter++,
						client);
			}
		} catch (IOException e) {
			System.out.println("Communication error with client: "
					+ e.getMessage());
		}
	}

	/**
	 * Migrates the process to the specified slave. If only one slave is
	 * specified, the process is migrated from master to slave. If two slaves
	 * are specified, process is transferred from first slave to the second
	 * slave
	 * 
	 * @param processId
	 * @param clientId1
	 * @param clientId2
	 */
	public void migrateProcess(String processId, String clientId1,
			String clientId2) {
		if (clients.containsKey(clientId1) && clientId2 == null) {
			ProcessObject pObj = pm.processList.get(processId);
			if (pObj == null) {
				System.out
						.println("Specified process not found on master. Please try again");
				return;
			}
			MigratableProcess migratableObj = pObj.migratableObj;
			Socket client = clients.get(clientId1);
			if (pObj.state != ProcessConstants.SUSPENDED)
				migratableObj.suspend();
			try {
				outStream = new ObjectOutputStream(client.getOutputStream());
				outStream.writeObject(migratableObj);
				pm.processList.remove(processId);
			} catch (IOException e) {
				System.out
						.println("Communication error with client: "
								+ clientId1
								+ ". Process remains in the master in the SUSPENDED state");
			}
		} else if (clientId1 == null || !clients.containsKey(clientId1)) {
			System.out.println("Slave " + clientId1
					+ " does not exist. Please enter a valid slave ID");
			return;
		} else if (clientId2 != null) {
			if (clients.containsKey(clientId2)) {
				Object obj = null;
				Socket client1 = clients.get(clientId1);
				try {
					outStream = new ObjectOutputStream(
							client1.getOutputStream());
					outStream.writeObject(processId);

					inStream = new ObjectInputStream(client1.getInputStream());
					obj = inStream.readObject();
				} catch (IOException e) {
					System.out.println("Communication error with slave: "
							+ clientId1 + ". Please try again.");
					return;
				} catch (ClassNotFoundException e) {
					System.out
							.println("ClassNotFoundException occurred in migrateProcess: "
									+ e.getMessage());
				}
				try {
					if (obj != null
							&& obj.getClass().getName()
									.contains("migratableprocess")) {
						Socket client2 = clients.get(clientId2);
						outStream = new ObjectOutputStream(
								client2.getOutputStream());
						outStream.writeObject(obj);
					} else {
						System.out
								.println("Specified process not found in slave");
					}
				} catch (IOException e) {
					System.out
							.println("Communication error with client: "
									+ clientId2
									+ ". Process will remain in the master in suspended state.");
					String pid = pm.getProcessId();
					pm.processList.put(processId, new ProcessObject(
							(MigratableProcess) obj, processId,
							ProcessConstants.SUSPENDED));
				}
			} else {
				System.out.println("Slave " + clientId2 + "does not exist.");
				return;
			}
		}
	}

	/**
	 * Method does a poll on all the clients
	 * 
	 * @param message
	 */
	public void requestAllClients(String message) {
		for (String clientId : clients.keySet()) {
			if (requestClient(clientId, message) == -1) {
				// Removes the client if it's not reachable
				clients.remove(clientId);
			}
		}
	}

	/**
	 * Method to poll a client and send/receive objects
	 * 
	 * @param clientId
	 * @param message
	 * @return 0 if the client is reachable -1 if the client is not reachable
	 */
	public int requestClient(String clientId, String message) {
		Socket client = clients.get(clientId);
		try {
			ObjectOutputStream outStream = new ObjectOutputStream(
					client.getOutputStream());
			outStream.writeObject(message);

			inStream = new ObjectInputStream(client.getInputStream());
			Object reply = inStream.readObject();
			if (reply.getClass().getName().contains("HashMap")) {
				System.out.println(clientId);
				ProcessManager
						.listProcesses((ConcurrentHashMap<String, ProcessObject>) reply);
			}
		} catch (IOException e) {
			if (!message.equals("poll"))
				System.out.println("Communication error with client: "
						+ clientId);
			return -1;

		} catch (ClassNotFoundException e) {
			System.out
					.println("ClassNotFoundException occurred in requestClient: "
							+ e.getMessage());
		}
		return 0;
	}

	/**
	 * Lists all the clients currently connected to the master
	 */
	public void listClients() {
		System.out.println("Slave nodes currently connected to the master: ");
		for (String client : clients.keySet())
			System.out.println(client);
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
			if (server != null)
				server.close();
		} catch (IOException e) {
			// Ignore here
		}
	}
}
