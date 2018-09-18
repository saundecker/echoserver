package httpserver;

public enum HttpStatusCode {

	// 1XX - Informational
	CONTINUE (100, "Continue"),
	
	// 2XX - Success
	OK (200, "OK"),
	CREATED (201, "Created"),
	ACCEPTED (202, "Accepted"),
	NO_RESPONSE (204, "No Content"),
	PARTIAL_CONTENT (206, "Partial Content"),
	
	// 3XX - Redirection
	MOVED_PERMANENTLY (301, "Moved Permanently"),
	MOVED_TEMPORARILY (302, "Moved Temporarily"),

	// 4XX - Client Error
	BAD_REQUEST (400, "Bad Request"),
	UNAUTHORIZED (401, "Unauthorized"),
	FORBIDDEN (403, "Forbidden"),
	NOT_FOUND (404, "Not Found"),
	METHOD_NOT_ALLOWED (405, "Method Not Allowed"),
	NOT_ACCEPTABLE (406, "Not Acceptable"),
	LENGTH_REQUIRED (411, "Length Required"),
	REQUEST_TOO_LARGE(413, "Request Entity Too Large"),
	REQUEST_UTI_TOO_LONG(414, "Request-URI Too Long"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	
	// 5XX - Server Error
	SERVER_ERROR (500, "Internal Server Error"),
	NOT_IMPLEMENTED (501, "Not Implemented"),
	BAD_GATEWAY (502, "Bad Gateway"),
	SERVICE_UNAVAILABLE (503, "Service Unavailable"),
	GATEWAY_TIMEOUT (504, "Gateway Timeout"),
	VERSION_NOT_SUPPORTED (505, "HTTP Version Not Supported");
	
	public final int statusCode;
	public final String reasonPhrase;
	
	private HttpStatusCode(int codeValue, String reasonPhrase) {
		this.statusCode = codeValue;
		this.reasonPhrase = reasonPhrase;
	}
}
