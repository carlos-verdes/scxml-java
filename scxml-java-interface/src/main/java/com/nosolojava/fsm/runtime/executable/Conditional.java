package com.nosolojava.fsm.runtime.executable;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.Context;


public interface Conditional extends Serializable,Executable {
	boolean runIf(Context context);
}
