package project.ds.transactionalio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TransactionalFileOuptutStream {
	
	OutputStream out;
	String filePath;
	
	public TransactionalFileOuptutStream(String filePath){
		this.filePath = filePath;
	}
	
	//http://www.journaldev.com/881/how-to-append-to-a-file-in-java
	public void printLn(String line){
        try {
            //below true flag tells OutputStream to append
            out = new FileOutputStream(new File(filePath), true);
            out.write(line.getBytes(), 0, line.length());
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

}
