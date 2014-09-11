package project.ds.processmanager;

public interface ProcessCallback {

	public void processSuspend(String threadId);
	
	public void processEnd(String threadId);
}
