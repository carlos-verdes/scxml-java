package com.nosolojava.fsm.impl.runtime.basic;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nosolojava.fsm.impl.model.basic.jexl.JexlFSMContext;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.invokeHandler.ConsoleInvokeHandler;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.invokeHandler.ScxmlInvokeHandler;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.io.ConsoleIOProcessor;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.io.ScxmlIOProcessor;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.StateMachineParser;
import com.nosolojava.fsm.parser.XppActionParser;
import com.nosolojava.fsm.parser.XppStateMachineParser;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.ContextFactory;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.FSMLogCallback;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.StateMachineFramework;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;

public class BasicStateMachineEngine implements StateMachineEngine {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private final ConcurrentMap<String, IOProcessor> ioProcessorMap = new ConcurrentHashMap<String, IOProcessor>();

	private ConcurrentMap<String, Context> scxmlSessionMap = new ConcurrentHashMap<String, Context>();
	private ConcurrentMap<String, InvokeHandler> invokeHandlerMap = new ConcurrentHashMap<String, InvokeHandler>();
	private StateMachineParser parser;
	private StateMachineFramework framework;

	private FSMLogCallback logCallback;

	//start/shutdown control
	protected final ReentrantLock startStopLock = new ReentrantLock();
	protected volatile AtomicBoolean isActive = new AtomicBoolean(false);

	//dispatcher executor
	private ExecutorService dispatcherExecutor = null;

	// list of available context ordered by event contents (most priority is the context with external events and no internal events --> the less busy ones)
	private final BlockingQueue<Context> availableSessions = createBusyPriorityBlockingQueue();
	protected final ReentrantLock sessionLock = new ReentrantLock();
	private ConcurrentMap<String, Context> busySessionMap = new ConcurrentHashMap<String, Context>();
	private ConcurrentMap<String, Context> emptySessionMap = new ConcurrentHashMap<String, Context>();
	//context executor
	private ExecutorService contextEventsExecutor = Executors.newCachedThreadPool();

	ContextFactory contextFactory = new ContextFactory() {

		@Override
		public Context createContext(String sessionId, String parentSessionId, StateMachineModel model,
				StateMachineEngine engine, Map<String, Serializable> initValues) throws ConfigurationException {
			return new JexlFSMContext(sessionId, parentSessionId, model, engine, initValues);
		}
	};

	public BasicStateMachineEngine() throws ConfigurationException {
		this(null);

	}

	public BasicStateMachineEngine(List<XppActionParser> customActionParsers) throws ConfigurationException {
		this(customActionParsers, null);

	}

	public BasicStateMachineEngine(List<XppActionParser> customActionParsers, FSMLogCallback logCallback)
			throws ConfigurationException {
		super();
		this.logCallback = logCallback;

		if (customActionParsers != null) {
			this.parser = new XppStateMachineParser(customActionParsers);
		} else {
			this.parser = new XppStateMachineParser();
		}

		initDefaultValues();

	}

	protected void initDefaultValues() {

		this.framework = new BasicStateMachineFramework(this.logCallback);
		this.framework.setEngine(this);
		IOProcessor ioProcessor = new ScxmlIOProcessor(this);
		this.ioProcessorMap.put(ioProcessor.getName(), ioProcessor);
		ioProcessor = new ConsoleIOProcessor(this);
		this.ioProcessorMap.put(ioProcessor.getName(), ioProcessor);

		InvokeHandler invokeHandler = new ScxmlInvokeHandler(this);
		this.invokeHandlerMap.put(invokeHandler.getType(), invokeHandler);
		invokeHandler = new ConsoleInvokeHandler();
		this.invokeHandlerMap.put(invokeHandler.getType(), invokeHandler);

	}

	public void start() {

		if (this.isActive.get()) {
			throw new RuntimeException("Engine can't be started twice, create a new instance");
		}

		this.startStopLock.lock();
		try {
			if (this.isActive.compareAndSet(false, true)) {
				this.dispatcherExecutor = Executors.newSingleThreadExecutor();
				this.dispatcherExecutor.execute(new DispatchEventsTask());
			} else {
				throw new RuntimeException("Engine can't be started twice, create a new instance");
			}
		} finally {
			this.startStopLock.unlock();
		}

	}

	@Override
	public boolean shutdownAndWait(long timeout, TimeUnit unit) throws InterruptedException {

		boolean result = false;

		//if the engine is active --> set to inactive and start shutdown
		this.startStopLock.lock();
		try {
			if (this.isActive.compareAndSet(true, false)) {
				this.dispatcherExecutor.shutdown();
			} else {
				throw new RuntimeException(
						"This engine has not been started or has been shutdown before, create a new instance.");
			}
		} finally {
			this.startStopLock.unlock();
		}

		if (timeout > -1) {
			result = this.dispatcherExecutor.awaitTermination(timeout, unit);
		}
		//			System.out.println("shutdown result: "+result);
		this.contextEventsExecutor.shutdown();
		this.scxmlSessionMap.clear();

		return result;
	}

	@Override
	public void forceShutdown() {
		try {
			shutdownAndWait(-1, null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Context startFSMSession(URI fsmModelUri) throws ConfigurationException, IOException {

		return this.startFSMSession(null, null, fsmModelUri, null);
	}

	@Override
	public Context startFSMSession(String parentSessionId, URI fsmModelUri) throws ConfigurationException, IOException {
		return startFSMSession(null, parentSessionId, fsmModelUri, null);
	}

	@Override
	public Context startFSMSession(String sessionId, String parentSessionId, URI fsmModelUri,
			Map<String, Serializable> initValues) throws ConfigurationException, IOException {

		this.startStopLock.lock();
		Context context;
		try {
			if (!this.isActive.get()) {
				throw new RuntimeException(
						"This engine has not been started or has been shutdown before, create a new instance.");
			}
		} finally {
			this.startStopLock.unlock();

		}

		//create the model
		StateMachineModel model = this.parser.parseScxml(fsmModelUri);

		//create the context (the session)
		context = this.contextFactory.createContext(sessionId, parentSessionId, model, this, initValues);

		//save the session so events could arrive while initiating
		this.startStopLock.lock();
		try {
			if (this.isActive.get()) {
				this.scxmlSessionMap.put(context.getSessionId(), context);
			} else {
				throw new RuntimeException(
						"This engine has not been started or has been shutdown before, create a new instance.");
			}
		} finally {
			this.startStopLock.unlock();

		}

		//starts the FSM (this is synchronous)
		this.framework.initFSM(context);

		// offer the session to runtime so it can process events
		this.startStopLock.lock();
		try {
			if (this.isActive.get()) {
				this.availableSessions.offer(context);
			} else {
				throw new RuntimeException(
						"This engine has not been started or has been shutdown before, create a new instance.");
			}
		} finally {
			this.startStopLock.unlock();

		}

		return context;
	}

	/* this class runs in a single thread !! */
	class DispatchEventsTask implements Runnable {

		@Override
		public void run() {

			try {
				//while engine is active or is there any active session
				BasicStateMachineEngine engineInstance = BasicStateMachineEngine.this;
				while (engineInstance.isActive.get() || !engineInstance.scxmlSessionMap.isEmpty()) {

					//get next available context, if timeout then check end condition
					Context availableContext = engineInstance.availableSessions.poll(5000, TimeUnit.MILLISECONDS);

					//if there is any available context
					if (availableContext != null) {

						//if has any pending event
						if (availableContext.hasExternalEvents()) {
							//then create a push event task
							Event event = availableContext.pollExternalEvent();
							PushEventTask pushEventtask = new PushEventTask(event, availableContext);

							sessionLock.lock();
							try {
								busySessionMap.put(availableContext.getSessionId(), availableContext);
							} finally {
								sessionLock.unlock();
							}

							//and execute the macrostep
							engineInstance.contextEventsExecutor.execute(pushEventtask);

						} else {
							// returns to available queue (it will has the lest priority)
							engineInstance.availableSessions.offer(availableContext);
						}
					}

				}
			} catch (InterruptedException e) {
				// TODO review interrupted exception handling
				logger.log(Level.SEVERE, "Interrupted exception");

			}
		}
	}

	class PushEventTask implements Runnable {
		private final Context context;
		private final Event event;

		public PushEventTask(Event event, Context context) {
			super();
			this.event = event;
			this.context = context;
		}

		@Override
		public void run() {

			//run the macro step
			BasicStateMachineEngine.this.getStateMachineFramework().handleExternalEvent(event, context);

			//lock to update maps (the session has to pass from busy to active or empty in an atomic way)
			sessionLock.lock();
			try {
				//remove from busy map
				BasicStateMachineEngine.this.busySessionMap.remove(context.getSessionId());

				//if the session is still active
				if (BasicStateMachineEngine.this.scxmlSessionMap.containsKey(context.getSessionId())) {

					//if has any event
					if (context.hasExternalEvents()) {

						//offer again to available queue
						BasicStateMachineEngine.this.availableSessions.offer(context);

					} else {
						//offer to empty sessions
						BasicStateMachineEngine.this.emptySessionMap.put(context.getSessionId(), context);
					}

				}
			} finally {
				sessionLock.unlock();
			}

		}

	}

	@Override
	public Context getSession(String sessionId) {
		return this.scxmlSessionMap.get(sessionId);
	}

	@Override
	public void pushEvent(String sessionId, Event event) {

		if (this.scxmlSessionMap.containsKey(sessionId)) {
			//remove from busy map
			sessionLock.lock();
			try {
				Context context = this.scxmlSessionMap.get(sessionId);

				//if is in empty map
				if (this.emptySessionMap.containsKey(sessionId)) {
					//offer as available session
					this.emptySessionMap.remove(sessionId);
					this.availableSessions.offer(context);
				}

				context.offerExternalEvent(event);
			} finally {
				sessionLock.unlock();
			}
		}
	}

	@Override
	public boolean isSessionActive(String sessionId) {
		return this.scxmlSessionMap.containsKey(sessionId);
	}

	@Override
	public void endSession(String sessionId) {
		this.scxmlSessionMap.remove(sessionId);
	}

	@Override
	public void registerIOProcessor(IOProcessor ioProcessor) {
		ioProcessor.setEngine(this);
		this.ioProcessorMap.put(ioProcessor.getName(), ioProcessor);
	}

	@Override
	public void unRegisterIOProcessor(String name) {
		if (this.ioProcessorMap.containsKey(name)) {
			this.ioProcessorMap.get(name).setEngine(null);
			this.ioProcessorMap.remove(name);

		}
	}

	@Override
	public void registerInvokeHandler(InvokeHandler invokeHandler) {
		if (invokeHandler != null && invokeHandler.getType() != null) {
			this.invokeHandlerMap.put(invokeHandler.getType(), invokeHandler);
		}
	}

	@Override
	public void unRegisterInvokeHandler(String type) {
		this.invokeHandlerMap.remove(type);
	}

	public StateMachineParser getParser() {
		return parser;
	}

	public void setParser(StateMachineParser parser) {
		this.parser = parser;
	}

	@Override
	public void setStateMachineFramework(StateMachineFramework framework) {
		this.framework = framework;
	}

	@Override
	public Set<IOProcessor> getIOProcessors() {
		return new HashSet<IOProcessor>(this.ioProcessorMap.values());
	}

	@Override
	public IOProcessor getIOProcessor(String name) {
		return this.ioProcessorMap.get(name);
	}

	@Override
	public Set<InvokeHandler> getInvokeHandlers() {
		return new HashSet<InvokeHandler>(this.invokeHandlerMap.values());
	}

	@Override
	public InvokeHandler getInvokeHandler(String type) {
		return this.invokeHandlerMap.get(type);
	}

	@Override
	public StateMachineFramework getStateMachineFramework() {
		return this.framework;
	}

	protected PriorityBlockingQueue<Context> createBusyPriorityBlockingQueue() {
		return new PriorityBlockingQueue<Context>(11, new Comparator<Context>() {

			@Override
			public int compare(Context object1, Context object2) {
				if (object1 == object2) {
					return 0;
				} else {
					if (object1 == null) {
						return 1;
					} else if (object2 == null) {
						return -1;
					} else {
						if (object1.hasExternalEvents() && !object1.hasInternalEvents()) {
							return -1;
						} else if (object1.hasExternalEvents() && object1.hasInternalEvents()) {
							if (object2.hasExternalEvents() && !object2.hasInternalEvents()) {
								return 1;
							} else {
								return -1;
							}
						} else if (object2.hasExternalEvents()) {
							return 1;
						} else {
							return -1;
						}
					}
				}
			}
		});
	}

	@Override
	public Collection<Context> getActiveSessions() {
		return this.scxmlSessionMap.values();
	}

	@Override
	public void setContextFactory(ContextFactory contextFactory) {
		this.contextFactory = contextFactory;
	}

	@Override
	public void setLogCallback(FSMLogCallback logCallback) {
		this.logCallback = logCallback;

	}

	@Override
	public FSMLogCallback getLogCallback() {
		return this.logCallback;
	}

}
