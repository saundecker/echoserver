package httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerMain {

	private static final int CORE_POOL_SIZE = 20;
	private static final int MAX_POOL_SIZE = 200;
	private static final int DEFAULT_PORT = 8080;
	private static final int BACKLOG = 20;
	private static AtomicBoolean run = new AtomicBoolean(true);
	private static AtomicBoolean isRunning = new AtomicBoolean(true);

	public static void main(String[] args) {

		int port = DEFAULT_PORT;

		if (args.length == 0) {
			System.err.println("Using Default Port " + DEFAULT_PORT);
		} else {

			try {
				port = Integer.parseInt(args[0]);
				if ((port < 1) || (port > 65535)) {
					printUsage("invalid port '" + port + "'. Provide a valid port number [1-65535]");
					System.exit(-1);
				}
			} catch (NumberFormatException e) {
				printUsage("invalid port '" + args[0] + "'. Provide a valid port number [1-65535]");
				System.exit(-1);
			}
		}

		try {
			
			ServerSocket ss = new ServerSocket(port, BACKLOG);
			
			System.err.println("Server Listening on Port " + port + "\n---------------------------------\n");
			
			ExecutorService executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 60L, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(), new BlockRequestsPolicy());

			while (run.get()) {
				Socket s = ss.accept();
				System.err.println("Accepted Connection from : " + s.getRemoteSocketAddress().toString());
				executor.execute(new HttpRequestHandler(s));
			}

			System.err.println("Server Shutting Down...");
			executor.shutdown();
			ss.close();

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	
	/**
	 * blocks the main thread
	 */
	public static void blockRequests() {
		synchronized (isRunning) {
			try {
				isRunning.set(false);
				isRunning.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				isRunning.set(true);
			}
		}
	}
	
	/**
	 * unblocks the main thread
	 */
	public static void unblockRequests() {
		if (isRunning.compareAndSet(false, true)) {
			synchronized (isRunning) {
				isRunning.notify();
			}
		}
	}

	/**
	 * request server termination
	 */
	public static void shutdownServer() {
		run.set(false);
	}

	private static void printUsage(String msg) {
		System.err.println(msg);
		System.err.println("Usage: \tjava -jar httpserver.jar [port]");
		System.err.println("\tdefault port 8080");
	}

}
