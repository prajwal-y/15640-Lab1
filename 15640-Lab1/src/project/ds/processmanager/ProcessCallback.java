package project.ds.processmanager;

public interface ProcessCallback {
	/**
	 * Method to change the state of the process to SUSPENDED
	 * @param threadId
	 */
	public void processSuspend(String threadId);
	
	/**
	 * Method to remove the process from the processList
	 * @param threadId
	 */
	public void processEnd(String threadId);
}
