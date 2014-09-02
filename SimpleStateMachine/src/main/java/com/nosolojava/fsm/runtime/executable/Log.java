package com.nosolojava.fsm.runtime.executable;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.nosolojava.fsm.runtime.Context;

public class Log implements Executable {
	private static final long serialVersionUID = -8138866223857895052L;
	private final String label;
	private final String expression;
	
	
	
	public Log(String label, String expression) {
		super();
		this.label = label;
		this.expression = expression;
	}



	@Override
	public void run(Context context) {
		String text= context.getDataByExpression(expression);
		Logger.getLogger(label).log(Level.FINE, text);
	}

}
