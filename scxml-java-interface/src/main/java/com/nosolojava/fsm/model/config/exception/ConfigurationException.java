package com.nosolojava.fsm.model.config.exception;

import java.text.MessageFormat;

/**
 * Exception when the xml from a SCXML source doesn't fulfill all the SCXML requirements.
 * @author cverdes
 *
 */
public class ConfigurationException extends Exception {
	public static final long serialVersionUID = 7413205787975925674L;

	public ConfigurationException() {
		super();

	}

	public ConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);

	}

	public ConfigurationException(String arg0,Object[]params) {
		this(MessageFormat.format(arg0, params));
	}
	public ConfigurationException(String arg0) {
		super(arg0);

	}

	public ConfigurationException(Throwable arg0) {
		super(arg0);

	}

}
