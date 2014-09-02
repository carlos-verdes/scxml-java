package com.nosolojava.fsm.runtime;

import com.nosolojava.fsm.runtime.Context;

public interface FSMListener {

	void onMacroStepFinished(Context context);

}
