package httpserver;

public class IllegalRequestException extends Exception {

	private static final long serialVersionUID = 5114304583950928782L;
	
	public final HttpStatusCode statusCode; 
	
	public IllegalRequestException(String message) {
		super(message); 
		this.statusCode = null;
	}
	
	public IllegalRequestException(HttpStatusCode statusCode) {
		super(statusCode.reasonPhrase);
		this.statusCode = statusCode;
	}
	
	public IllegalRequestException(HttpStatusCode statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}
}
