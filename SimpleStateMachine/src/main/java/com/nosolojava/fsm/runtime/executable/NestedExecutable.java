package com.nosolojava.fsm.runtime.executable;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.Context;

public interface NestedExecutable extends Serializable {
	void executeNested(Context context);
}
