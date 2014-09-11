/**
 * Interface for migratableprocess. 
 * All the processes that need to be migrated need to implement this interface.
 */
package project.ds.migratableprocess;

import java.io.Serializable;

public interface MigratableProcess extends Runnable, Serializable {

	/*
	 * Method to suspend the process
	 */
	public void suspend();

	/**
	 * Each migratable process should return a flag indicating if it was
	 * suspended or ended naturally.
	 * @return: 1 for suspended case and 0 for normal exit.
	 */
	public int getFlag();
}
