package com.nosolojava.fsm.model.config.exception;

import java.text.MessageFormat;

import com.nosolojava.fsm.model.transition.Transition;


public class ParallelSiblingTransactionException extends ConfigurationException {
	private static final long serialVersionUID = -7785513311497666245L;

	private static String ERROR_MESSAGE="Transitions between parallel siblings are not allowed. {0}";
	private static final MessageFormat MF= new MessageFormat(ERROR_MESSAGE);
	
	public ParallelSiblingTransactionException(Transition transition) {
		this(MF.format(new Object[]{transition}));
	}


	private ParallelSiblingTransactionException(String message) {
		super(message);
	}


	
}
