package test.com.nosolojava.fsm.basic;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import com.nosolojava.fsm.impl.model.basic.BasicStateMachineModel;
import com.nosolojava.fsm.impl.model.basic.jexl.JexlFSMContext;
import com.nosolojava.fsm.impl.model.basic.state.BasicState;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public class UtilsForTestFSM {
	
	public static final int DEFAULT_TIMEOUT_IN_MILLIS = 100;

	public static Context createBasicContext() throws ConfigurationException{
		BasicState root = BasicState.createRootState();
		StateMachineModel model = new BasicStateMachineModel(root);

		Context ctx = new JexlFSMContext("sessionId", null, model, null, null);
		
		return ctx;
	}
	
	public static void finishEngine(StateMachineEngine engine) throws InterruptedException{
		finishEngine(DEFAULT_TIMEOUT_IN_MILLIS, engine);
	}
	
	public static void finishEngine(long timeoutInMillis,StateMachineEngine engine) throws InterruptedException{
		boolean finishedOK= engine.shutdownAndWait(100, TimeUnit.MILLISECONDS);
		Assert.assertTrue("The FSM doesn't finished on time", finishedOK);

	}

}
