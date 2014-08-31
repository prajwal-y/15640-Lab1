/**
 * 
 */
package project.ds.processmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Arrays;

import project.ds.migratableprocess.MigratableProcess;

/**
 * @author Prajwal Yadapadithaya (Andrew ID: pyadapad) 
 *         Rohit Upadhyaya (Andrew ID: rjupadhy)
 */
public class ProcessManager {

	/**
	 * Provides interactive console to the user. Accepts commands and processes
	 * them
	 */
	private static void processInput() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean flag = true;
		while (flag) {
			System.out.print("PM>");
			try {
				String input = in.readLine();
				String[] args = input.split(" ");
				CommandTypes cmd = CommandTypes.valueOf(args[0]);
				switch (cmd) {
				case QUIT:
					flag = false;
					break;
				case PS:
					break; // TODO
				case PROCESS:
					String className = args[1];
					startProcess(className);
					break; // TODO
				case SUSPEND:
					break; // TODO
				case MIGRATE:
					break; // TODO
				default:
					System.out.println("Invalid command");
				}
			} catch (IOException e) {
				System.out.println("IOException occurred");
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid Command");
			}
		}
	}

	/**
	 * Starts the specified process
	 * @param className
	 */
	private static void startProcess(String className) {
		
	}
	
	/**
	 * Suspends the specified process
	 * 
	 * @param process
	 */
	private static void suspend(MigratableProcess process) {
		return;
	}
	
	/**
	 * Migrates the process to the specified node
	 * 
	 * @param process
	 */
	private void migrate(MigratableProcess process) {
		return;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//No arguments, ProcessManager will be functioning as master
		if (args[0] == null) {
			(new MigrateMaster()).start();
		}
		//-c argument passed. ProcessManager will function as slave
		else if (args[0].equals("-c")) {
			String host = args[1];
			(new MigrateSlave(host)).start();
		}
		//Exit if any invalid arguments are passed.
		else { 
			System.out.println("Invalid arguments passed. Please try again");
			System.exit(0);
		}
		processInput();
	}

}
