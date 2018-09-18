package httpserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class HttpCommunicator implements AutoCloseable {

	private static final int BUFSIZE = 8192;

	private byte[] buf; // buffer used for reading the request
	private int lmark;  // first valid data byte in the buffer
	private int hmark;  // last valid data byte in the buffer
	private int requestLength;
	
	private boolean isCR;
	private StringBuilder iBuf;
	private String httpMethod;
	private String requestURI;
	private String httpVersion;
	private HttpHeader requestHeader;

	private Socket clientSocket;
	private InputStream instream;
	private OutputStream outstream;

	public HttpCommunicator(Socket s) throws IOException {

		if (s == null) {
			throw new NullPointerException("Null Socket Connection");
		}
		clientSocket = s;
		instream = new BufferedInputStream(s.getInputStream());
		outstream = new BufferedOutputStream(s.getOutputStream());
		buf = new byte[BUFSIZE];
		hmark = 0;
		lmark = 1;
	}

	public HttpHeader getRequestHeader() {
		return this.requestHeader;
	}

	public String getHttpMethod() {
		return this.httpMethod;
	}

	public String getRequestURI() {
		return this.requestURI;
	}
	
	public String getHttpVersion() {
		return this.httpVersion;
	}

	
	/**
	 * Reads the Complete request and returns the request line string
	 * 
	 * @throws IllegalRequestException
	 *             - If the request is an invalid HTTP request
	 * @throws IOException
	 */
	public void getRequest() throws IllegalRequestException, IOException {

		requestLength = 0;
		String[] tokens = new String[3];

		if(getRequestTokens(3, Consts.SPACE, tokens) < 3) {
			throw new IllegalRequestException("Invalid Request Line");
		}

		httpMethod = tokens[0].toUpperCase();
		requestURI = tokens[1];
		httpVersion = tokens[2].toUpperCase();

		if (!HttpServer.isVersionSupported(httpVersion)) {
			throw new IllegalRequestException(HttpStatusCode.VERSION_NOT_SUPPORTED);
		}

		requestHeader = new HttpHeader();

		while(getRequestTokens(2, Consts.COLON, tokens) == 2) {
			if (tokens[0].isEmpty()) {
				throw new IllegalRequestException("Invalid Header, empty key");
			}
			requestHeader.setHeaderField(tokens[0], tokens[1]);
		}
		
		if(tokens[0].isEmpty() == false) {
			throw new IllegalRequestException("Invalid Header, Key without a value");
		}
	}
	

	/**
	 * Sends the HTTP response status line along with the response header if any
	 * 
	 * @param code
	 *            HTTP Status Code
	 * @param responseHeader
	 *            Response Header if any
	 * @throws IOException
	 */
	public void sendResponse(HttpStatusCode code, HttpHeader responseHeader) throws IOException {

		String statusLine;
		String lineBreak;
		byte[] buf;

		if (isCR) {
			lineBreak = Consts.lineBreakCRLF;
		} else {
			lineBreak = Consts.lineBreakLF;
		}

		statusLine = httpVersion + " " + code.statusCode + " " + code.reasonPhrase + lineBreak;
		buf = statusLine.getBytes();
		outstream.write(buf, 0, buf.length);

		if (responseHeader != null) {
			String[] header = responseHeader.dumpHeader(lineBreak);
			for (String line : header) {
				buf = line.getBytes();
				outstream.write(buf, 0, buf.length);
			}
		}
		outstream.write(lineBreak.getBytes());
		outstream.flush();
		System.err.println("Request : " + httpMethod + " " + requestURI + " " + httpVersion + 
						 "\nResponse : " + statusLine);
	}

	/**
	 * Closes the connection
	 */
	@Override
	public void close() throws IOException {

		if (instream != null) {
			instream.close();
		}

		if (outstream != null) {
			outstream.flush();
			outstream.close();
		}

		clientSocket.close();
	}

	/**
	 * First returns any bytes already lying in the internal buffer, then reads
	 * the data from the socket.
	 * 
	 * @param buffer
	 *            - buffer in which to read the data
	 * @return Number of bytes read
	 * 
	 */
	public int readData(byte[] buffer, int offset, int length) throws IOException {

		if (buffer == null) {
			throw new NullPointerException();
		}

		int off = offset;
		int len = length;
		int bytesRead = 0;

		if (lmark <= hmark) {
			int bytesAvailable = hmark - lmark + 1;

			if (len >= bytesAvailable) {
				bytesRead = bytesAvailable;
			} else {
				bytesRead = len;
			}
			System.arraycopy(buf, lmark, buffer, off, bytesRead);
			lmark += bytesRead;
			return bytesRead;
		}

		bytesRead = instream.read(buffer, off, len);
		return bytesRead;
	}

	
	/**
	 * Writes data on the socket
	 */
	public void writeData(byte[] buffer, int offset, int length) throws IOException {
		if (buffer == null) {
			throw new NullPointerException();
		}
		outstream.write(buffer, offset, length);
		outstream.flush();
	}

	
	/**
	 * Extracts tokens from a 'line' read from the input stream, based on the specified delimiter
	 * 
	 * @param numTokens
	 *            - Maximum number of tokens to get, actual tokens found could be less
	 * @param delim
	 *            - Delimiter
	 * @param tokens
	 *            - Array of Strings in which the tokens will be returned
	 * @return Actual number of tokens found
	 * @throws IllegalRequestException
	 * @throws IOException
	 */	
	private int getRequestTokens(int numTokens, byte delim, String[] tokens) 
			throws IllegalRequestException, IOException {
		
		boolean copySpannedBytes = false;
		boolean endOfLine = false;
		boolean foundCR = false;
		boolean grabToken = false;
		boolean tokSpilled = false;
		boolean tokStarted = false;
		int tokLength = 0;
		int tokStartIdx = 0;
		int tokensFound = 0;
		
		Arrays.fill(tokens, null);
		while(!endOfLine) {
			
			if (lmark > hmark) {
				lmark = 0;
				int bytesRead;
				if ((bytesRead = instream.read(buf)) != -1) {
					hmark = bytesRead - 1;
				} else {
					throw new IllegalRequestException("Incomplete Request");
				}
			}
			
			if (tokSpilled) {
				// start of a token that has spanned across buffered reads
				tokSpilled = false;
				tokStartIdx = 0;
				tokStarted = true;
			}
			
			while (lmark <= hmark) {

				if(++requestLength > HttpServer.MAX_REQUEST_LENGTH) {
					throw new IllegalRequestException(HttpStatusCode.REQUEST_TOO_LARGE);
				}

				// if the last byte found was a CR, this one has to be LF
				if ((foundCR) && (buf[lmark]) != Consts.LF) {
					throw new IllegalRequestException("Invalid Request");
				}

				if(!tokStarted) {
					if((buf[lmark] != Consts.SPACE) && (buf[lmark] != Consts.HTAB)) {
						tokStarted = true;
						grabToken = true;
						tokStartIdx = lmark;
					} else {
						grabToken = false;
						lmark++;
						continue;
					}
				}
				
				if(buf[lmark] == delim) {
					if(tokensFound < numTokens-1) {
						tokLength = lmark - tokStartIdx;
						tokStarted = false;
						break;
					} else if((delim == Consts.SPACE) || (delim == Consts.HTAB)) {
						throw new IllegalRequestException("Invalid Request");
					} else {
						lmark++;
						continue;
					}
				} else if(buf[lmark] == Consts.CR) {
					foundCR = true;
					tokLength = lmark - tokStartIdx;
					break;
				} else if(buf[lmark] == Consts.LF) {
					if (foundCR) {
						// processed a line ending with CRLF
						isCR = true;
						lmark++;
						return tokensFound;
					} else {
						// processed a line ending with LF
						tokLength = lmark - tokStartIdx;
						endOfLine = true;
						break;
					}
				} else {
					lmark++;
				}
			}
			
			if ((tokStarted) && (!foundCR)) {
				// we have a partial token in the buffer. with a large enough buffer, we'd never get here
				tokSpilled = true;
				if(iBuf == null) {
					iBuf = new StringBuilder(new String(buf, tokStartIdx, lmark - tokStartIdx));
				} else {
					iBuf.append(new String(buf, tokStartIdx, lmark - tokStartIdx));
				}
				copySpannedBytes = true;
				continue;
			}
			
			if (copySpannedBytes) {
				// found the rest of the token that spanned across buffered reads
				iBuf.append(new String(buf, tokStartIdx, tokLength));
				tokens[tokensFound++] = iBuf.toString();
				iBuf.delete(0, iBuf.length());
				copySpannedBytes = false;
			} else {
				if(grabToken) {
					tokens[tokensFound++] = new String(buf, tokStartIdx, tokLength);
				}
			}
			lmark++;
		}
		return tokensFound;
	}

}
