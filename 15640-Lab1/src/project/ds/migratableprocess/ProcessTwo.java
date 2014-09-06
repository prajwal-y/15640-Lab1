package project.ds.migratableprocess;

public class ProcessTwo implements MigratableProcess {

	public ProcessTwo(String[] args) {
		
	}
	
	@Override
	public void run() {
		System.out.println("Running ProcessTwo!");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("End of ProcessTwo");
		// TODO Auto-generated method stub

	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub

	}

}
