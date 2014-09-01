package project.ds.processmanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MigrateMaster extends Thread {

	ServerSocket server = null;
	Socket client = null;

	@Override
	public void run() {
		try {
			server = new ServerSocket(ProcessConstants.serverport);
			server.close();
			//client = server.accept();
		} catch (IOException e) {
			System.out.println("IOException occurred in Server with error "+e.getMessage());
		}

	}

}
