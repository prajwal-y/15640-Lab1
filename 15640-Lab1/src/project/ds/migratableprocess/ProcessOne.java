package project.ds.migratableprocess;

import java.io.EOFException;
import java.io.IOException;

import project.ds.transactionalio.TransactionalFileInputStream;
import project.ds.transactionalio.TransactionalFileOuptutStream;

public class ProcessOne implements MigratableProcess {
	
	private TransactionalFileInputStream  inFile;
	//private TransactionalFileOutputStream outFile;
	private volatile boolean suspending = false;
	
	public ProcessOne(String args[]){
		inFile = new TransactionalFileInputStream(args[0]);
		//outFile = new TransactionalFileOuptutStream(args[1]);
	}
	
	@Override
	public void run() {
		try{
			while(!suspending){
				String line = inFile.readLine();
				
				if(line == null) break;
				
				//outFile.printLn(line);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		}catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("ProcessOne: Error: " + e);
		}


		suspending = false;

	}

	@Override
	public void suspend() {
		suspending = true;
		while (suspending);
	}

}
