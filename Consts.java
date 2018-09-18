package httpserver;

public final class Consts {
	
	// Constants required for parsing
	public static final byte CR = (byte) '\r';
	public static final byte LF = (byte) '\n';
	public static final byte HTAB = (byte) '\t';
	public static final byte SPACE = (byte) ' ';
	public static final byte COLON = (byte) ':';
	
	public static final String lineBreakLF = "\n";
	public static final String lineBreakCRLF = "\r\n";
	
	private Consts() {
		throw new AssertionError();
	}
}
