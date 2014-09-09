package project.ds.processmanager;

import java.io.Serializable;

import project.ds.migratableprocess.MigratableProcess;

public class ProcessObject implements Serializable{
	MigratableProcess migratableObj;
	String processId;
	String state;
	
	public ProcessObject(MigratableProcess obj, String pid, String st) {
		migratableObj = obj;
		processId = pid;
		state = st;
	}
}
