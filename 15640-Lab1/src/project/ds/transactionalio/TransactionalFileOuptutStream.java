package project.ds.transactionalio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOuptutStream extends OutputStream implements
		Serializable {

	transient OutputStream out;
	String filePath;

	public TransactionalFileOuptutStream(String filePath) {
		this.filePath = filePath;
	}

	// http://www.journaldev.com/881/how-to-append-to-a-file-in-java
	public void printLn(String line) {
		try {
			// below true flag tells OutputStream to append
			out = new FileOutputStream(new File(filePath), true);
			out.write(line.getBytes(), 0, line.length());
			out.write(System.getProperty("line.separator").getBytes(), 0,
					System.getProperty("line.separator").length());
			out.close();
		} catch (IOException e) {
			System.out.println("IOException occurred: " + e.getMessage());
		}
	}

	@Override
	public void write(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

}
