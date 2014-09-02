package com.nosolojava.fsm.parser;

import java.io.IOException;
import java.net.URI;

import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;

public interface StateMachineParser {

	/**
	 * Used to check if this parser could manage the URI.
	 * 
	 * @param source
	 * @return
	 */
	boolean validURI(URI source);

	StateMachineModel parseScxml(URI source) throws ConfigurationException, IOException;
	
}
