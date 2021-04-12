package org.vitact.result.exceptions;

public class AnalyzerException extends RuntimeException{
	public static int FILE_LINE_BAD_FORMAT = 0;
	public static int DATA_FILE_NOT_FOUND = 1;
	public static int OUTPUT_FILE_WRITE_ERROR = 2;


	int code;
	String message;
	Throwable cause;

	public AnalyzerException(int code, String message, Throwable cause) {
		super(message, cause);
		this.cause = cause;
		this.code = code;
		this.message = message;
	}
}
