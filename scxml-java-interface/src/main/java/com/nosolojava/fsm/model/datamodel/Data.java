package com.nosolojava.fsm.model.datamodel;

import java.io.Serializable;
import java.net.URL;

import com.nosolojava.fsm.runtime.Context;

public interface Data extends Serializable {

	String getId();

	URL getSrc();

	String getExpression();

	Object evaluateData(Context context);

}
