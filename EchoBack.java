package httpserver;

import java.io.IOException;

public class EchoBack {
	
	/**
	 * Echoes back the bytes read from the connection
	 * 
	 * @throws IOException
	 */
	public static void echo (HttpCommunicator httpComm) 
			throws IOException {

		int bytesToSend;
		HttpHeader requestHeader = httpComm.getRequestHeader();
		HttpHeader responseHeader = new HttpHeader();

		// set the appropriate response headers
		String contentLength = requestHeader.getHeaderField("content-length");
		if (contentLength == null) {
			httpComm.sendResponse(HttpStatusCode.LENGTH_REQUIRED, null);
			return;
		}
		
		try {
			bytesToSend = Integer.valueOf(contentLength);
		} catch (NumberFormatException e) {
			httpComm.sendResponse(HttpStatusCode.LENGTH_REQUIRED, null);
			return;
		}
		
		responseHeader.setHeaderField("content-length", contentLength);
		
		String contentType = requestHeader.getHeaderField("content-type");
		if (contentType != null) {
			responseHeader.setHeaderField("content-type", contentType);
		} else {
			responseHeader.setHeaderField("content-type", "application/octet-stream");
		}
		
		responseHeader.setHeaderField("connection", "close");
		
		// send the response header
		httpComm.sendResponse(HttpStatusCode.OK, responseHeader);

		// read and echo back a total of 'content-length' bytes
		byte[] content = new byte[8192];
		int length;
		int bytesRead;
		
		while (bytesToSend > 0) {
			if ((bytesRead = httpComm.readData(content, 0, content.length)) != -1) {
				length = (bytesToSend - bytesRead) >= 0 ? bytesRead : bytesToSend;
				httpComm.writeData(content, 0, length);
				bytesToSend -= bytesRead;
			}
		}
	}
}
