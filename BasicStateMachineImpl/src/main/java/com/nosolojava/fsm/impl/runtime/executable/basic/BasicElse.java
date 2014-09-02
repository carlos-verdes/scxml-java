package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.util.ArrayList;
import java.util.List;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Else;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicElse implements Else {
	private static final long serialVersionUID = 6417335395404522096L;
	private final List<Executable> executables = new ArrayList<Executable>();

	public BasicElse() {
		super();
	}

	public BasicElse(List<Executable> executables) {
		super();
		this.executables.addAll(executables);
	}

	@Override
	public void run(Context context) {
		for (Executable executable : this.executables) {
			executable.run(context);
		}
	}

}
