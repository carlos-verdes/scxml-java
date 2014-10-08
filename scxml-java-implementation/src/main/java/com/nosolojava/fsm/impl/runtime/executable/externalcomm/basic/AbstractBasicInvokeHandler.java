package com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeInfo;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public abstract class AbstractBasicInvokeHandler implements InvokeHandler {
	private static final long serialVersionUID = -7602242020573620626L;
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	protected ConcurrentHashMap<String, Context> invokeIdContextMap = new ConcurrentHashMap<String, Context>();

	// abstract methods
	@Override
	public abstract String getType();

	public abstract void invokeServiceInternal(InvokeInfo invokeInfo, Context context);

	public abstract void sendMessageToService(Message message, Context context);

	public abstract void onEndSession(String invokeId, Context context);

	@Override
	public void invokeService(InvokeInfo input, Context context) {
		String invokeId = input.getInvokeId();
		invokeIdContextMap.put(invokeId, context);

		this.invokeServiceInternal(input, context);

	}

	@Override
	public void sendMessageToService(Message message) {
		String invokeId = message.getId();
		if (this.invokeIdContextMap.containsKey(invokeId)) {
			Context context = getContextByInvokeId(invokeId);
			this.sendMessageToService(message, context);
		} else {
			// TODO manage problem looking for session 
			logger.log(Level.SEVERE, "Error finding invoke session for event. InvokeId: {0}", new Object[] { invokeId });

		}
	}

	@Override
	public void endSession(String invokeId) {
		if (invokeIdContextMap.containsKey(invokeId)) {
			Context context = invokeIdContextMap.get(invokeId);

			onEndSession(invokeId, context);

			this.invokeIdContextMap.remove(invokeId);
		}
	}

	public Context getContextByInvokeId(String invokeId) {
		return this.invokeIdContextMap.get(invokeId);

	}

}
