package com.nosolojava.fsm.parser.exception;

/**
 *Exception raised when there is any error parsing an SCXML.
 * @author cverdes
 *
 */
public class SCXMLParserException extends Exception {
	private static final long serialVersionUID = -7764250451712148929L;

	public SCXMLParserException() {
		super();
	}

	public SCXMLParserException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SCXMLParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public SCXMLParserException(String message) {
		super(message);
	}

	public SCXMLParserException(Throwable cause) {
		super(cause);
	}

	
	
}
