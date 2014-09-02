package com.nosolojava.fsm.runtime.executable.externalcomm;

import java.net.URI;

public interface InvokeInfo {

	/**
	 * Identifies the invoke session
	 * @return
	 */
	String getInvokeId();
	
	/**
	 * Only source or content will be received.
	 * @return source for input data
	 */
	URI getSource();

	/**
	 * 
	 * @return body of the invoke
	 */
	Object getBody();

}
