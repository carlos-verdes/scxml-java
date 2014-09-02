package com.nosolojava.fsm.model.transition;

import java.io.Serializable;
import java.util.List;

import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Executable;

public interface Transition extends Serializable {
	String getEventName();

	State getTargetState(Context context);

	State getSourceState(Context context);

	String getGuardCondition();
	boolean passGuardCondition(Context context);

	boolean isInternal();

	List<Executable> getExecutables();

	void addExecutable(Executable executable);

	void addExecutables(List<Executable> executables);

	void clearAndSetExecutables(List<Executable> executables);
	
	String toStringVerbose();

}
