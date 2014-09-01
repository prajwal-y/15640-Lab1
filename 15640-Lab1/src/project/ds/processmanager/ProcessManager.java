/**
 * 
 */
package project.ds.processmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import project.ds.migratableprocess.MigratableProcess;

/**
 * @author Prajwal Yadapadithaya (Andrew ID: pyadapad) 
 * Rohit Upadhyaya (AndrewID: rjupadhy)
 */
public class ProcessManager {

	private static ArrayList<ProcessObject> processList = null;

	/**
	 * Provides interactive console to the user. Accepts commands and processes
	 * them
	 */
	private static void processInput() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean flag = true;
		while (flag) {
			System.out.print("\nPM>");
			try {
				String input = in.readLine();
				String[] args = input.split(" ");
				CommandTypes cmd = CommandTypes.valueOf(args[0]);
				switch (cmd) {
				case QUIT:
					flag = false;
					break;
				case PS:
					listProcesses();
					break;
				case PROCESS:
					String className = args[1];
					startProcess(className);
					break;
				case SUSPEND:
					break; // TODO
				case MIGRATE:
					break; // TODO
				default:
					System.out.print("Invalid command");
				}
			} catch (IOException e) {
				System.out.print("IOException occurred");
			} catch (IllegalArgumentException e) {
				System.out.print("Invalid Command");
			}
		}
	}

	/**
	 * Starts the specified process
	 * 
	 * @param className
	 */
	private static void startProcess(String className) {
		Class<?> c;
		try {
			c = Class.forName(className);
			Object inst = c.newInstance();
			// Adding the process object to the process list
			processList.add(new ProcessObject(inst, className, "running"));
			new Thread((Runnable) inst).start(); // Start the process in a new
													// thread
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		} catch (InstantiationException e) {
			System.out.println("InstatiationException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException: " + e.getMessage());
		}
	}

	private static void listProcesses() {
		if (processList.isEmpty())
			System.out.print("No processes running currently");
		else {
			for (ProcessObject processObject : processList)
				System.out.println("Process: " + processObject.processId);
		}
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

		// No arguments, ProcessManager will be functioning as master
		if (args != null && args.length == 0) {
			(new MigrateMaster()).start();
		}
		// -c argument passed. ProcessManager will function as slave
		else if (args[0].equals("-c")) {
			String host = args[1];
			(new MigrateSlave(host)).start();
		}
		// Exit if any invalid arguments are passed.
		else {
			System.out.println("Invalid arguments passed. Please try again");
			System.exit(0);
		}
		processList = new ArrayList<ProcessObject>();
		processInput();
	}

}
