package project.ds.transactionalio;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable{
	
	//Variable to maintain state of file read
	int numLines;
	int numBytes;
	InputStream in;
	String filePath;
	
	public TransactionalFileInputStream(String filePath){
		numLines = 0;
		numBytes = 0;
		this.filePath = filePath;
	}
	
	public String readLine() throws IOException, EOFException{
		File file = new File(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        for(int i = 0; i < numLines; i++){
        	reader.readLine();
        }
        numLines++;
        line = reader.readLine();
        reader.close();
        return line;
	}
	
	public byte[] readBytes(int bytes) throws IOException{
		byte[] result = new byte[bytes];
		/*Path file = Paths.get(filePath);
		try {
			in = Files.newInputStream(file);
		} catch (IOException e) {
			System.out.println("IOException in TransactionalFileInputStream: "+ e.getMessage());
		}*/
		
		//Seek to numBytes beyond beginning
		in.read(result, 0, bytes);
		return null;
		
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
