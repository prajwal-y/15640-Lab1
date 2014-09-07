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
				switch (cmd) {
				case help:
					System.out.println("     PROCESS_MIGRATOR v1.0       ");
					System.out.println("---------------------------------");
					System.out.println("These are the acceptable commands");
					System.out.println("=> process <processName> <arguments> : Starts a new process of the specified name, with the arguments specified. Process should be of the type MigratableProcess. Also please specify fully qualified name of the process class.");
					System.out.println("=> ps : Lists the currently running processes. the format is <processId>:<processName>.");
					System.out.println("=> migrate <processId> : Migrate the process to the slave node (Applicable only on master).");
					System.out.println("=> suspend <processId> : Suspend the process.");
					System.out.println("=> quit : Exit PROCESS_MIGRATOR v1.0");
					break;
				case quit:
					flag = false;
					break;
				case ps:
					listProcesses();
					break;
				case process:
					if(args.length <= 1) {
						System.out.print("Process name not specified. Please enter a valid process name.");
						break;
					}
					String className = args[1];
					if(!className.contains("migratableprocess")) {
						System.out.print("This process is not a migratable process. Please specify processes that implement the class MigratableProcess.");
						break;
					}
					String[] arguments = null;
					if (args.length >= 3)
						arguments = Arrays.copyOfRange(args, 2, args.length);
					startProcess(className, arguments);
					break;
				case suspend:
					break; // TODO
				case migrate:
					String processName = args[1];
					migrateProcess(processName);
					break;
				default:
					System.out.print("Invalid command. Please use 'help' command");
				}
			} catch (IOException e) {
				System.out.print("IOException occurred: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				System.out.print("Invalid command. Please use 'help' command");
			}
		}
		if (!flag)
			System.exit(0);
	}

	/**
	 * Starts the specified process
	 * 
	 * @param className
	 */
	private void startProcess(String className, String[] args) {
		Class<?> c;
		Constructor<?> constructor;
		try {
			c = Class.forName(className);
			constructor = c.getConstructor(String[].class);
			Object inst = constructor.newInstance((Object) args);
			//Adding the process object to the process list
			String processId = getProcessId();
			processList.put(processId, new ProcessObject(
					(MigratableProcess) inst, processId, "running"));
			//Start the process in a new thread
			RunProcess prc = new RunProcess((MigratableProcess) inst, this,
					processId);
			new Thread((Runnable) prc).start();
		} catch (InvocationTargetException e) {
			System.out.print("Invalid arguments. Please specified required arguments to the process");
		} catch (NoSuchMethodException e) {
			System.out.print("NoSuchMethodException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.out.print("Specified process not found. Please try again.");
		} catch (InstantiationException e) {
			System.out.print("InstatiationException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			System.out.print("IllegalAccessException: " + e.getMessage());
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
	/*private void suspend(MigratableProcess process) {
		return;
	}*/

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
