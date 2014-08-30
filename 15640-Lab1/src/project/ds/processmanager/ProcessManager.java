/**
 * 
 */
package project.ds.processmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author prajwal
 *
 */
public class ProcessManager {

	private static void processInput() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean flag = true;
		while (flag) {
			System.out.print("ProcessManager>");
			try {
				String input = in.readLine();
				CommandTypes cmd = CommandTypes.valueOf(input.split(" ")[0]);
				switch(cmd) {
					case QUIT: flag = false; break;
					case PS: break; //TODO
					case PROCESS: break; //TODO
					case SUSPEND: break; //TODO
					case MIGRATE: break; //TODO
					default: System.out.println("Invalid command\n");
				}
			}catch(IOException e){
				System.out.println("IOException occurred\n");
			}catch(IllegalArgumentException e) {
				System.out.println("Invalid Command\n");
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		processInput();

	}

}
