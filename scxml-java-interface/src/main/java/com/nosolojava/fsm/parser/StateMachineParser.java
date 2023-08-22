package com.nosolojava.fsm.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;

public interface StateMachineParser {

	/**
	 * Used to check if this parser could manage the URI.
	 * 
	 * @param source
	 * @return
	 */
	boolean validURI(URI source);

	StateMachineModel parseScxml(URI source) throws ConfigurationException, IOException,SCXMLParserException;

	StateMachineModel parseScxml(InputStream source) throws ConfigurationException, IOException,SCXMLParserException;

}
