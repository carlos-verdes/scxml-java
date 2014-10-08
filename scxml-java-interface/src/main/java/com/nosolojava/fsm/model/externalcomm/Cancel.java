package com.nosolojava.fsm.model.externalcomm;

import com.nosolojava.fsm.runtime.executable.Executable;

public interface Cancel extends Executable {

	String getSendId();

	void setSendId(String sendId);

	String getSendIdExpression();

	void setSendIdExpression(String sendIdExpr);

}
