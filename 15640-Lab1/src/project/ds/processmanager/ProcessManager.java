/**
 * 
 */
package project.ds.processmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

import project.ds.migratableprocess.MigratableProcess;

/**
 * @author Prajwal Yadapadithaya (Andrew ID: pyadapad) Rohit Upadhyaya
 *         (AndrewID: rjupadhy)
 */
public class ProcessManager implements ProcessCallback {

	private static HashMap<String, ProcessObject> processList = null;
	private static MigrateMaster master;

	/**
	 * Provides interactive console to the user. Accepts commands and processes
	 * them
	 */
	private void processInput() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean flag = true;
		while (flag) {
			System.out.print("\nPM>");
			try {
				String input = in.readLine();
				String[] args = input.split(" ");
				CommandTypes cmd = CommandTypes.valueOf(args[0]);
				System.out.println(cmd);
				switch (cmd) {
				case QUIT:
					flag = false;
					break;
				case PS:
					listProcesses();
					break;
				case PROCESS:
					String className = args[1];
					System.out.println(className);
					String[] arguments = null;
					if (args.length >= 3)
						arguments = Arrays.copyOfRange(args, 2, args.length);
					System.out.println(Arrays.toString(arguments));
					startProcess(className, arguments);
					break;
				case SUSPEND:
					break; // TODO
				case MIGRATE:
					String processName = args[1];
					migrateProcess(processName);
					break;
				default:
					System.out.print("Invalid command");
				}
			} catch (IOException e) {
				System.out.print("IOException occurred: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				System.out.print("IllegalArgumentException: " + e.getMessage());
			}
		}
	}

	/**
	 * Starts the specified process
	 * 
	 * @param className
	 */
	private void startProcess(String className, String[] args) {
		Class<?> c;
		Constructor constructor;
		try {
			c = Class.forName(className);
			constructor = c.getConstructor(String[].class);
			Object inst = constructor.newInstance((Object) args);
			// Adding the process object to the process list
			String processId = getProcessId();
			processList.put(processId, new ProcessObject(
					(MigratableProcess) inst, processId, "running"));
			// Start the process in a new thread
			RunProcess prc = new RunProcess((MigratableProcess) inst, this,
					processId);
			new Thread((Runnable) prc).start();
			// new Thread((Runnable) inst).start();
		} catch (InvocationTargetException e) {
			System.out.println("InvocationTargetException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			System.out.println("NoSuchMethodException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		} catch (InstantiationException e) {
			System.out.println("InstatiationException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccessException: " + e.getMessage());
		}
	}

	private void listProcesses() {
		if (processList.isEmpty())
			System.out.print("No processes running currently");
		else {
			for (String processId : processList.keySet())
				System.out.println(processId
						+ ": "
						+ processList.get(processId).migratableObj.getClass()
								.getName());
		}
	}

	/**
	 * Suspends the specified process
	 * 
	 * @param process
	 */
	private void suspend(MigratableProcess process) {
		return;
	}

	/**
	 * Migrates the process to the specified node
	 * 
	 * @param process
	 */
	private void migrateProcess(String processId) {
		MigratableProcess migratableObj = processList.get(processId).migratableObj;
		migratableObj.suspend();
		master.migrateProcess(migratableObj);
		return;
	}

	/**
	 * This method is called when the slave receives the process after migration
	 * from master.
	 * 
	 * @param inst
	 */
	public void receiveProcess(MigratableProcess inst) {
		String processId = getProcessId();
		processList.put(processId,
				new ProcessObject(inst, processId, "running"));
		RunProcess prc = new RunProcess(inst, this, processId);
		new Thread((Runnable) prc).start();
	}

	/**
	 * Gets a unique identifier to each process. Identifier is of the format
	 * "Process:<ID>"
	 * 
	 * @return
	 */
	private String getProcessId() {
		int id = processList.size() + 1;
		return "Process:" + id;
	}

	@Override
	public void processCallback(String processId) {
		processList.remove(processId);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ProcessManager pm = new ProcessManager();

		// No arguments, ProcessManager will be functioning as master
		if (args != null && args.length == 0) {
			master = new MigrateMaster(pm);
			master.start();
		}
		// -c argument passed. ProcessManager will function as slave
		else if (args[0].equals("-c")) {
			String host = args[1];
			(new MigrateSlave(host, pm)).start();
		}
		// Exit if any invalid arguments are passed.
		else {
			System.out.println("Invalid arguments passed. Please try again");
			System.exit(0);
		}
		processList = new HashMap<String, ProcessObject>();
		pm.processInput();
	}

}
