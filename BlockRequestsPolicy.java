package httpserver;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class BlockRequestsPolicy implements RejectedExecutionHandler {

	/**
	 * Blocks the main thread until a worker thread is available to accept the task
	 * 
	 */
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		ServerMain.blockRequests();
		executor.submit(r);
	}
}
