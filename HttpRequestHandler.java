package httpserver;

import java.io.IOException;
import java.net.Socket;

public class HttpRequestHandler implements Runnable {

	private Socket clientSocket;

	public HttpRequestHandler(Socket s) {
		if(s == null) {
			throw new NullPointerException("Socket is Null");
		}
		clientSocket = s;
	}


	@Override
	public void run() {

		try (HttpCommunicator httpComm = new HttpCommunicator(clientSocket)) {

			try {
				// get the request	
				httpComm.getRequest();
				
				// process the request
				HttpServer.processRequest(httpComm);

			} catch (IllegalRequestException ire) {
				if (ire.statusCode != null) {
					httpComm.sendResponse(ire.statusCode, null);
				} else {
					System.err.println(ire.getMessage());
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServerMain.unblockRequests();
		}
	}
}
