package com.nosolojava.fsm.impl.runtime.executable.basic;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Script;

public class BasicLocalScript implements Script {
	private static final long serialVersionUID = -7654496973339387873L;
	private final String codeContent;
	
	public BasicLocalScript(String codeContentExpr) {
		super();
		this.codeContent = codeContentExpr;
	}


	@Override
	public void run(Context context) {
		context.executeScript(codeContent);
	}


}
