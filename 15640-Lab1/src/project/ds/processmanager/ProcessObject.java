package project.ds.processmanager;

import project.ds.migratableprocess.MigratableProcess;

public class ProcessObject {
	MigratableProcess migratableObj;
	String processId;
	String state;
	
	public ProcessObject(MigratableProcess obj, String pid, String st) {
		migratableObj = obj;
		processId = pid;
		state = st;
	}
}
