package project.ds.processmanager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import project.ds.migratableprocess.MigratableProcess;

public class MigrateMaster extends Thread {

	ServerSocket server = null;
	Socket client = null;

	@Override
	public void run() {
		//while (true) {
			try {
				server = new ServerSocket(ProcessConstants.serverport);
				//server.close();
				client = server.accept();
			} catch (IOException e) {
				System.out.println("IOException occurred in Server with error "
						+ e.getMessage());
			//}
		}
	}

	public void migrateProcess(MigratableProcess migratableObj) {
		try {
			OutputStream out = client.getOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(out);
			stream.writeObject(migratableObj);
			stream.close();
			out.close();
			client.close();
		} catch (IOException e) {
			System.out.println("IOException occurred: " + e.getMessage());
		}
	}

	public Socket getClientSocket() {
		return client;
	}
}
