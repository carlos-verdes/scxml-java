package com.nosolojava.fsm.runtime;

public interface FSMLogCallback {

	abstract public void logDebug(String text);

	abstract public void logDebug(String text, Object[] data);

	abstract public void logInfo(String text);

	abstract public void logWarning(String text);

	abstract public void logError(String text);

}
