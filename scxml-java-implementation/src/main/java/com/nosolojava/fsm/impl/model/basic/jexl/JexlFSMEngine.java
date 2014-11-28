package com.nosolojava.fsm.impl.model.basic.jexl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl2.JexlEngine;

public class JexlFSMEngine extends JexlEngine {

	public JexlFSMEngine(JexlFSMContext context) {
		super();
		
		Map<String, Object> funcs = new HashMap<String, Object>();
		funcs.put(null, new Functions(context));
		this.setFunctions(funcs);
	}

	
}
