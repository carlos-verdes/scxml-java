package com.nosolojava.fsm.model.datamodel;

import com.nosolojava.fsm.runtime.Context;

public interface Content{

	<T> T evaluateContent(Context context);
}
