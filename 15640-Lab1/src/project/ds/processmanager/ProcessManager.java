/**
 * ProcessManager.java
 * @author Prajwal Yadapadithaya (Andrew ID: pyadapad) Rohit Upadhyaya
 *         (AndrewID: rjupadhy)
 *         
 * This is the class that provides a command line user interface 
 * and manages process migration. 
 */
package project.ds.processmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import project.ds.migratableprocess.MigratableProcess;

public class ProcessManager implements ProcessCallback {

	public ConcurrentHashMap<String, ProcessObject> processList = new ConcurrentHashMap<String, ProcessObject>();
	private static MigrateMaster master = null;
	private static MigrateSlave slave = null;

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
					System.out
							.println("=> launch <processName> <arguments>: Launches a new process of the specified name, with the arguments specified in the master. Process should be of the type MigratableProcess. Also please specify fully qualified name of the process class.");
					System.out
							.println("=> ps : Lists the currently running processes. the format is <processId>:<processName>.");
					System.out
							.println("=> list: Lists all the slaves currently connected to the master (only on master)");
					System.out
							.println("=> migrate <processId> <slaveId1> [<slaveId2>]: Migrate the process to the slave node (Default from master and can be used only from master).");
					System.out
							.println("=> suspend <processId> : Suspend a running process.");
					System.out
							.println("=> resume <processId> : Resume a suspended process.");
					System.out.println("=> quit : Exit PROCESS_MIGRATOR v1.0");
					break;
				case quit:
					flag = false;
					break;
				case ps:
					if (master != null) {
						master.requestAllClients("poll");
						System.out.println("Master:");
						listProcesses(processList);
						master.requestAllClients("ps");
					} else {
						listProcesses(processList);
					}
					break;
				case launch:
					if (args.length <= 1) {
						System.out
								.print("Process name not specified. Please enter a valid process name.");
						break;
					}
					String className = args[1];
					if (!className.contains("migratableprocess")) {
						System.out
								.print("This process is not a migratable process. Please specify processes that implement the class MigratableProcess.");
						break;
					}
					String[] arguments = null;
					if (args.length >= 3)
						arguments = Arrays.copyOfRange(args, 2, args.length);
					startProcess(className, arguments);
					break;
				case suspend:
					if (args.length != 2) {
						System.out
								.print("Invalid arguments. Please use 'help' to get the usage of the command");
						break;
					}
					String pid1 = args[1];
					suspend(pid1);
					break;
				case resume:
					if (args.length != 2) {
						System.out
								.print("Invalid arguments. Please use 'help' to get the usage of the command");
						break;
					}
					String pid2 = args[1];
					resume(pid2);
					break;
				case list:
					if (master == null) {
						System.out
								.println("This command is not valid on slave");
						break;
					}
					master.requestAllClients("poll");
					System.out.println("Master: Currently connnected to "
							+ master.clients.size() + " slaves");
					master.listClients();
					break;
				case migrate:
					if (master == null) {
						System.out
								.println("This command is not valid on slave");
						break;
					}
					if (args.length < 3) {
						System.out
								.print("Invalid arguments. Please specified required arguments to migrate");
						break;
					}
					String processName = args[1];
					String slaveId1 = null,
					slaveId2 = null;
					slaveId1 = args[2];
					if (args.length == 4)
						slaveId2 = args[3];
					if (slaveId1.equals(slaveId2)) {
						System.out
								.println("Destination machine has to be different from source machine");
						break;
					}
					migrateProcess(processName, slaveId1, slaveId2);
					break;
				default:
					System.out
							.print("Invalid command. Please use 'help' command");
				}
			} catch (IOException e) {
				System.out.print("IOException occurred: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				System.out.print("Invalid command. Please use 'help' command");
			}
		}
		if (!flag) {
			if (master != null)
				master.close();
			if (slave != null)
				slave.close();
			System.exit(0);
		}
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
			// Adding the process object to the process list
			String processId = getProcessId();
			processList.put(processId, new ProcessObject(
					(MigratableProcess) inst, processId,
					ProcessConstants.RUNNING));
			// Start the process in a new thread.
			// Process received on destination node start running by default,
			// even if they were suspended from the host node
			RunProcess prc = new RunProcess((MigratableProcess) inst, this,
					processId);
			new Thread((Runnable) prc).start();
		} catch (InvocationTargetException e) {
			System.out
					.print("Invalid arguments. Please specified required arguments to the process");
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

	/**
	 * Method to list the processes in a node
	 * @param list
	 */
	public static void listProcesses(ConcurrentHashMap<String, ProcessObject> list) {
		if (list.isEmpty())
			System.out.println("No processes running currently");
		else {
			for (String processId : list.keySet())
				System.out.println(processId
						+ " : "
						+ list.get(processId).migratableObj.getClass()
								.getName() + " : " + list.get(processId).state);
		}
	}

	/**
	 * Suspends a specified process
	 * 
	 * @param process
	 */
	private void suspend(String processId) {
		ProcessObject pObj = processList.get(processId);
		if (pObj == null)
			System.out
					.println("Specified process not found on master. Please try again");
		pObj.migratableObj.suspend();
	}

	/**
	 * Resumes a specified process
	 * @param processId
	 */
	private void resume(String processId) {
		ProcessObject pObj = processList.get(processId);
		if (pObj == null)
			System.out
					.println("Specified process not found on master. Please try again");
		else if (pObj.state != ProcessConstants.SUSPENDED)
			System.out.println("Specified process is already running");
		else {
			pObj.state = ProcessConstants.RUNNING;
			RunProcess prc = new RunProcess(pObj.migratableObj, this,
					pObj.processId);
			new Thread((Runnable) prc).start();
		}
	}

	/**
	 * Migrates the process to the specified node
	 * @param processId
	 * @param slaveId1
	 * @param slaveId2
	 */
	private void migrateProcess(String processId, String slaveId1,
			String slaveId2) {
		master.migrateProcess(processId, slaveId1, slaveId2);
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
		processList.put(processId, new ProcessObject(inst, processId,
				ProcessConstants.RUNNING));
		RunProcess prc = new RunProcess(inst, this, processId);
		new Thread((Runnable) prc).start();
	}

	/**
	 * Gets a unique identifier to each process. Identifier is of the format
	 * "PRC-<ID>"
	 * 
	 * @return
	 */
	public String getProcessId() {
		int id = processList.size() + 1;
		return ProcessConstants.PROCESS + "-" + id;
	}

	@Override
	public void processEnd(String processId) {
		processList.remove(processId);
	}

	@Override
	public void processSuspend(String processId) {
		ProcessObject pObj = processList.get(processId);
		if (pObj != null)
			pObj.state = ProcessConstants.SUSPENDED;
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
		// -m argument passed. ProcessManager will function as slave
		else if (args[0].equals("-m")) {
			String host = args[1];
			slave = new MigrateSlave(host, pm);
			slave.start();
		}
		// Exit if any invalid arguments are passed.
		else {
			System.out.println("Invalid arguments passed. Please try again");
			System.exit(0);
		}
		pm.processInput();
	}
}
