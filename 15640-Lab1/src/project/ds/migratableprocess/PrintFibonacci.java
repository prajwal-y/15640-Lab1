package project.ds.migratableprocess;

import project.ds.transactionalio.TransactionalFileOuptutStream;

public class PrintFibonacci implements MigratableProcess {
	private TransactionalFileOuptutStream outFile;
	private volatile boolean suspending = false;
	private int n;
	private int count = 0;
	private int flag = 0;

	public PrintFibonacci(String[] args) {
		n = Integer.parseInt(args[0]);
		outFile = new TransactionalFileOuptutStream(args[1]);
	}

	@Override
	public void run() {
		try {
			int prev1 = 0, prev2 = 0;
			while (!suspending && (count < n)) {
				if(count == 0) {
					outFile.printLn("1");
					prev1 = 1;
				}
				else if(count == 1) {
					outFile.printLn("1");
					prev1 = 1;
					prev2 = 1;
				}
				else {
					int temp = prev1;
					prev1 = prev1 + prev2;
					prev2 = temp;
					outFile.printLn("" + prev1);
				}
				count++;
				// Just giving time to migrate process
				Thread.sleep(1000);
			}
		}catch (InterruptedException e) {
			// ignore it
		}
		if(suspending)
			flag = 1;
		suspending = false;
	}

	@Override
	public void suspend() {
		suspending = true;
		while (suspending);
	}
	
	@Override
	public int getFlag() {
		return flag;
	}

}
