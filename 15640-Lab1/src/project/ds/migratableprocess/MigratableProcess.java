package project.ds.migratableprocess;

import java.io.Serializable;

public interface MigratableProcess extends Runnable, Serializable {
	
	public void run();
	
	public void suspend();
	
}
