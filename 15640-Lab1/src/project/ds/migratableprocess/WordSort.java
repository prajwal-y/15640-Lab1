/**
 * Reads a file line by line, and prints the lines to a file
 * after sorting the words in the line. Input should have multiple lines
 * with multiple words in each line. Processing each line is done atomically. 
 */
package project.ds.migratableprocess;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import project.ds.transactionalio.TransactionalFileInputStream;
import project.ds.transactionalio.TransactionalFileOuptutStream;

public class WordSort implements MigratableProcess {

	private TransactionalFileInputStream inFile;
	private TransactionalFileOuptutStream outFile;
	private volatile boolean suspending = false;
	private ArrayList<String> wordList = new ArrayList<String>();
	private int flag = 0;
	
	public WordSort(String[] args) {
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOuptutStream(args[1]);
	}
	
	@Override
	public void run() {
		try {
			while (!suspending) {
				flag = 0;
				String line = inFile.readLine();
				if(line == null)
					break;
				String[] words = line.split(" ");
				for (String word : words) {
					wordList.add(word);
				}
				Collections.sort(wordList);
				String output = "";
				for (String word : wordList) {
					if(output.equals(""))
						output = word;
					else
						output = output + " " + word;
				}
				wordList.clear();
				outFile.printLn(output);
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
