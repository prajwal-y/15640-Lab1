package project.ds.processmanager;

public class ProcessObject {
	Object object;
	String processId;
	String state;
	
	public ProcessObject(Object obj, String pid, String st) {
		object = obj;
		processId = pid;
		state = st;
	}
}
