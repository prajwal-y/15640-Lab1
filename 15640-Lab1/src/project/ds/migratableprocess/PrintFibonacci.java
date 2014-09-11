/**
 * Example for a migratable process.
 * This process prints the fibonacci numbers to a file.
 * arguments: arg[0] - count of the numbers to be printed
 * arg[1] - Output file
 */
package project.ds.migratableprocess;

import project.ds.transactionalio.TransactionalFileOuptutStream;

public class PrintFibonacci implements MigratableProcess {
	private TransactionalFileOuptutStream outFile;
	private volatile boolean suspending = false;
	private int n;
	private int count;
	private int flag = 0;
	private int prev1 = 0, prev2 = 0;

	public PrintFibonacci(String[] args) {
		n = Integer.parseInt(args[0]);
		outFile = new TransactionalFileOuptutStream(args[1]);
	}

	@Override
	public void run() {
		try {
			while (!suspending && (count < n)) {
				flag = 0;
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
