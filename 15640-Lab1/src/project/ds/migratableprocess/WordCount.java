package project.ds.migratableprocess;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;

import project.ds.transactionalio.TransactionalFileInputStream;
import project.ds.transactionalio.TransactionalFileOuptutStream;

public class WordCount implements MigratableProcess {

	private TransactionalFileInputStream inFile;
	private TransactionalFileOuptutStream outFile;
	private volatile boolean suspending = false;
	private HashMap<String, Integer> wordcount = new HashMap<String, Integer>();
	private int flag = 0;

	public WordCount(String[] args) {
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOuptutStream(args[1]);
	}

	@Override
	public void run() {
		try {
			while (!suspending) {
				flag = 0;
				String line = inFile.readLine();

				if (line == null) {
					for (String word : wordcount.keySet())
						outFile.printLn(word + " " + wordcount.get(word));
					break;
				}

				String[] words = line.split(" ");

				for (String word : words) {
					if (wordcount.containsKey(word))
						wordcount.put(word, wordcount.get(word) + 1);
					else
						wordcount.put(word, 1);
				}
				// Just giving time to migrate process
				Thread.sleep(10000);
			}
		} catch (EOFException e) {
			System.out.println("EOFException occurred unexpectedly: "
					+ e.getMessage());
		} catch (IOException e) {
			System.out.println("ProcessOne: Error: " + e);
		} catch (InterruptedException e) {
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
