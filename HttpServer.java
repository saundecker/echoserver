package httpserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class HttpServer {
	
	public static final int MAX_REQUEST_LENGTH = 8192;
	
	private static final Set<String> SUPPORTED_VERSIONS = 
			new HashSet<String>(Arrays.asList("HTTP/1.0", "HTTP/1.1"));

	
	public static boolean isVersionSupported(String httpVersion) {
		
		if(httpVersion == null) {
			throw new NullPointerException();
		}
		return SUPPORTED_VERSIONS.contains(httpVersion);
	}
	
	public static void processRequest(HttpCommunicator httpComm) 
		throws IllegalRequestException, IOException {
		
		if(httpComm == null) {
			throw new NullPointerException();
		}
		
		switch(httpComm.getHttpMethod()) {
		case "POST":
			HttpServer.processPOST(httpComm);
			return;
		case "GET":
		case "PUT":
		case "PATCH":
		case "DELETE":
		case "HEAD":
		case "OPTIONS":
			throw new IllegalRequestException(HttpStatusCode.NOT_IMPLEMENTED);
		default:
			throw new IllegalRequestException(HttpStatusCode.BAD_REQUEST);
		}
	}
	
	private static void processPOST(HttpCommunicator httpComm) throws IOException {
		
		switch (httpComm.getRequestURI()) {
		default:
			EchoBack.echo(httpComm);
			break;
		}
	}
	
	
}
