package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.util.List;

import com.nosolojava.fsm.runtime.executable.Elif;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicElif extends BasicConditional implements Elif {
	private static final long serialVersionUID = 5674362454244831050L;

	public BasicElif(String condition) {
		super(condition);
	}

	public BasicElif(String condition, List<Executable> executables) {
		super(condition, executables);
	}

}
