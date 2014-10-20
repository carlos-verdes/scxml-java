package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;

import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public abstract class AbstractTest {

	StateMachineEngine engine;
	Context ctx;

	private long timeoutBeforeShutdown=100;
	private ContextTask afterEndTask=null;
	
	
	public abstract String getFSMUri();
	
	@Before
	public void initSMContexts() throws ConfigurationException, URISyntaxException, IOException, SCXMLParserException{
		// set the debug on/off
		BasicStateMachineFramework.DEBUG.set(true);
		
		//low rate for checks on new sessions (be carefull in a mobile this could consume a lot of battery)
		BasicStateMachineEngine.CHECK_AVAILABLE_SESSIONS_PERIOD_IN_MILLIS.set(1);
		
		engine = new BasicStateMachineEngine();
		engine.start();
		URI fsmModelUri = new URI(getFSMUri());
		this.ctx = engine.startFSMSession(fsmModelUri);
		
	}
	
	@After
	public void shutdownEngine() throws InterruptedException{
		boolean shutdownInTime=engine.shutdownAndWait(getTimeout(), TimeUnit.MILLISECONDS);
		Logger.getLogger(this.getClass().getCanonicalName()).log(Level.INFO,"Shutdown engine in time? {0}",new Object[]{shutdownInTime});
		afterShutdown(shutdownInTime);
	}

	protected void afterShutdown(boolean shutdownInTime){
		Logger.getLogger(this.getClass().getCanonicalName()).log(Level.INFO,"After shutdown");
		ContextTask task;
		if((task=getAfterEndTask())!=null){
			task.setContext(this.ctx);
			task.setEngine(this.engine);
			task.setHasFinishedOnTime(shutdownInTime);
			task.run();
		}
	}
	
	public static abstract class ContextTask implements Runnable{

		private Context context;
		private StateMachineEngine engine;
		private boolean hasFinishedOnTime;
		
		
		public boolean isHasFinishedOnTime() {
			return hasFinishedOnTime;
		}

		public void setHasFinishedOnTime(boolean hasFinishedOnTime) {
			this.hasFinishedOnTime = hasFinishedOnTime;
		}

		public ContextTask() {
			super();
		}

		public abstract void run (Context context, StateMachineEngine engine,boolean hasFinishedOnTime);
		
		@Override
		public void run() {
			run(this.context,this.engine,this.hasFinishedOnTime);
		}

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

		public StateMachineEngine getEngine() {
			return engine;
		}

		public void setEngine(StateMachineEngine engine) {
			this.engine = engine;
		}
		
		
		
	} 
	
	
	public long getTimeout() {
		return timeoutBeforeShutdown;
	}

	public void setTimeout(long timeout) {
		this.timeoutBeforeShutdown = timeout;
	}

	public ContextTask getAfterEndTask() {
		return afterEndTask;
	}

	public void setAfterEndTask(ContextTask afterEndTask) {
		this.afterEndTask = afterEndTask;
	}

}
