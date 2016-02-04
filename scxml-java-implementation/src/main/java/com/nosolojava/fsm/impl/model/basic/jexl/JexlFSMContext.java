package com.nosolojava.fsm.impl.model.basic.jexl;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.Script;

import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.io.ScxmlIOProcessor;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Data;
import com.nosolojava.fsm.model.datamodel.DataModel;
import com.nosolojava.fsm.model.datamodel.URLDataHandler;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.ContextInstance;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.SerializableContextInstance;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;

public class JexlFSMContext implements Context {
    // TODO avoid multi threading
    public static final String SERIALIZATION_FILENAME_PATTERN = "com.nosolojava.fsm.context.{0}";
    protected final MessageFormat serializationFilenameMF = new MessageFormat(SERIALIZATION_FILENAME_PATTERN);

    private transient Logger logger = Logger.getLogger(this.getClass().getName());

    protected final String sessionId;
    protected final String parentSessionId;
    protected final StateMachineModel model;

    protected final Queue<Event> externalEventQueue = new LinkedBlockingQueue<Event>();
    protected final Queue<Event> internalEventQueue = new LinkedBlockingQueue<Event>();

    protected final Set<String> activeInvokeSessions = new HashSet<String>();

    private final SerializableMapContext runtimeContext;
    private final SortedSet<State> activeStates = new ConcurrentSkipListSet<State>();
    private final SortedSet<State> statesToInvoke = new ConcurrentSkipListSet<State>();

    private final ConcurrentMap<String, State> states = new ConcurrentHashMap<String, State>();
    private final Map<String, URLDataHandler> dataHandlers = new ConcurrentHashMap<String, URLDataHandler>();

    private transient JexlEngine jexl;
    private SerializableContextInstance lastKnownConfiguration = null;
    private transient StateMachineEngine engine;

    private final AtomicBoolean stateChanged = new AtomicBoolean(true);


    public static final String getEventName() {
        return Context.EVENT_NAME;
    }

    ;

    public static final MessageFormat IN_EXPRESSION = new MessageFormat("In(''{0}'')");

    // private final MessageFormat ASSIGN_EXPRESSION = new
    // MessageFormat("{0}={1}");

    public JexlFSMContext(String sessionId, String parentSessionId, StateMachineModel model, StateMachineEngine engine,
                          Map<String, Serializable> initValues) throws ConfigurationException {
        super();

        if (sessionId == null) {
            this.sessionId = UUID.randomUUID().toString();
        } else {
            this.sessionId = sessionId;
        }
        this.parentSessionId = parentSessionId;
        this.model = model;
        this.engine = engine;

        jexl = new JexlFSMEngine(this);
        runtimeContext = new SerializableMapContext();

        // load datamodel and states
        loadRootState(model.getRootState(), initValues);

    }

    private void loadRootState(State rootState, Map<String, Serializable> initValues) throws ConfigurationException {

        // load state
        String statename = rootState.getName();
        checkRepeteadId(statename);
        this.states.put(statename, rootState);

        // check and load datamodel
        if (rootState.getDataModel() != null) {
            for (Data data : rootState.getDataModel().getDataList()) {
                checkRepeteadId(data.getId());
                registerData(data);
            }
        }

        // override root state data
        if (initValues != null && initValues.size() > 0) {
            for (Entry<String, Serializable> entry : initValues.entrySet()) {
                updateDataIfExists(entry.getKey(), entry.getValue());
            }
        }

        // load childrens
        for (State state : rootState.getChildrens()) {
            loadState(state);
        }

    }

    private void loadState(State state) throws ConfigurationException {

        // load state
        String statename = state.getName();
        checkRepeteadId(statename);
        this.states.put(statename, state);

        // check datamodel
        if (state.getDataModel() != null) {
            for (Data data : state.getDataModel().getDataList()) {
                checkRepeteadId(data.getId());
            }
        }

        // check recursive
        if (state.hasChildrens()) {
            for (State aux : state.getChildrens()) {
                loadState(aux);
            }
        }

    }

    private void checkRepeteadId(String id) throws ConfigurationException {

        if (runtimeContext.has(id) || states.containsKey(id)) {
            throw new ConfigurationException("Ids repeated not allowed {0} ", new Object[]{id});

        }

    }

    @Override
    public void clearAndSetURLDataHandlers(List<URLDataHandler> dataHandlers) {

        dataHandlers.clear();
        addURLDataHandlers(dataHandlers);
    }

    public void addURLDataHandlers(List<URLDataHandler> dataHandlers) {

        for (URLDataHandler handler : dataHandlers) {
            addURLDataHandler(handler);
        }

    }

    public void addURLDataHandler(URLDataHandler handler) {
        this.dataHandlers.put(handler.getProtocol(), handler);
    }

    @Override
    public void loadDataModel(DataModel dataModel) {
        // for each data in the model
        for (Data data : dataModel.getDataList()) {
            // register an immutable data in the context
            registerData(data);
        }
    }

    @Override
    public void removeDatamodel(DataModel dataModel) {

        for (Data data : dataModel.getDataList()) {
            runtimeContext.set(data.getId(), null);
        }

    }

    private void registerData(Data data) {

        String id = data.getId();
        runtimeContext.set(id, data.evaluateData(this));
    }

    @Override
    public void setCurrentEvent(Event event) {
        runtimeContext.set(EVENT_NAME, event);

    }

    @Override
    public Event getCurrentEvent() {
        return (Event) runtimeContext.get(EVENT_NAME);
    }

    @Override
    public <T> T getDataByName(String name) {
        @SuppressWarnings("unchecked")
        T result = (T) runtimeContext.get(name);
        return result;
    }

    @Override
    public <T> T getDataByExpression(String expression) {
        Expression e;
        try {
            e = jexl.createExpression(expression);
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, String.format("Error evaluating expresion %s", expression), ex);
            return null;
        }
        @SuppressWarnings("unchecked")
        T data = (T) e.evaluate(runtimeContext);

        return data;
    }

    @Override
    public boolean evaluateConditionGuardExpresion(String expression) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "evaluateConditionGuardExpresion({0})", new Object[]{expression});
            logger.log(Level.FINEST, "" + this.runtimeContext.get("breadcrumb"));
        }
        Expression e = jexl.createExpression(expression);
        Boolean result = (Boolean) e.evaluate(runtimeContext);

        return result;
    }

    // @Override
    // public void updateData(Data data) {
    // validateBeanExistInContext(data.getId());
    // registerData(data);
    // }

    // @Override
    // public void updateBeanByExpression(String name, String expression) {
    // validateBeanExistInContext(name);
    // // TODO validate data value when assign
    // Expression e = jexl.createExpression(name + "=" + expression);
    // e.evaluate(runtimeContext);
    //
    // }

    private void validateBeanExistInContext(String name) {
        if (!runtimeContext.has(name)) {
            // TODO send error.execution instead of exception when data is not
            // found in an assignment
            throw new RuntimeException(String.format(
                    "Updating bean not found in datamodel, bean name: %s, active states: %s", name,
                    this.getActiveStates()));
        }
    }

    @Override
    public SortedSet<State> getActiveStates() {
        return new TreeSet<State>(this.activeStates);
    }

    @Override
    public void addActiveState(State state) {
        this.activeStates.add(state);
    }

    public void addActiveStates(SortedSet<State> states) {
        this.activeStates.addAll(states);
    }

    public void clearAndSetActiveStates(SortedSet<State> states) {
        this.activeStates.clear();
        addActiveStates(states);
    }

    @Override
    public void removeActiveState(State state) {
        this.activeStates.remove(state);
    }

    @Override
    public boolean isActiveStateByName(String stateName) {
        boolean result = false;
        State aux = null;
        Iterator<State> activeIter = activeStates.iterator();
        while (!result && activeIter.hasNext()) {
            aux = activeIter.next();
            result = aux.getName().equals(stateName);
        }
        return result;
    }

    @Override
    public boolean isActiveState(State state) {
        return isActiveStateByName(state.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDataFromURL(URL url) {
        String protocol = url.getProtocol();

        URLDataHandler dataHandler = this.dataHandlers.get(protocol);
        T data = null;
        if (dataHandler == null) {
            // try to get from JVM
            try {
                Object aux = url.getContent();
                if (aux != null) {
                    data = (T) aux;
                }
            } catch (ClassCastException e) {
                throw new RuntimeException(String.format("The resource class is not compatible with expected, url: %s",
                        url), e);

            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "Custom protocol {0} not supported and the url {1} can't be loaded with error {2}",
                        new Object[]{url.getProtocol(), url, e.getMessage()});

                logger.log(
                        Level.SEVERE,
                        "This could happen for two reasons, you are using a custom protocol and you haven't registered the URLDataHandler or you are using a default Java URL and there has been an IOException.");

                throw new RuntimeException("Custom protocol not supported and standard URL failed.", e);
            }
        } else {
            data = dataHandler.getData(url);
        }

        return data;
    }

    @Override
    public void updateData(String id, Object value) {
        validateBeanExistInContext(id);

        runtimeContext.set(id, value);

    }

    @Override
    public void createVarIfDontExist(String name, Object value) {
        if (!runtimeContext.has(name)) {
            runtimeContext.set(name, value);
        }
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getParentSessionId() {
        return parentSessionId;
    }

    @Override
    public StateMachineModel getModel() {
        return this.model;
    }

    @Override
    public boolean hasInternalEvents() {
        return !this.internalEventQueue.isEmpty();
    }

    @Override
    public Event pollInternalEvent() {
        return this.internalEventQueue.poll();
    }

    @Override
    public void offerInternalEvent(Event event) {
        this.internalEventQueue.offer(event);
    }

    @Override
    public State getState(String stateName) {
        return states.get(stateName);
    }

    @Override
    public void executeScript(String code) {
        Script script = this.jexl.createScript(code);
        script.execute(runtimeContext);

    }

    @Override
    public void executeScript(URL codeUri) throws IOException {
        Script script = this.jexl.createScript(codeUri);
        script.execute(runtimeContext);

    }

    @Override
    public void clearStatesToInvoke() {
        this.statesToInvoke.clear();

    }

    @Override
    public void addStateToInvoke(State state) {
        this.statesToInvoke.add(state);
    }

    @Override
    public void removeStateToInvoke(State state) {
        this.statesToInvoke.remove(state);

    }

    @Override
    public SortedSet<State> getStatesToInvoke() {
        return new TreeSet<State>(this.statesToInvoke);
    }

    @Override
    public String getSessionName() {
        return this.model.getName();
    }

    @Override
    public IOProcessor searchIOProcessor(String name) {
        return this.engine.getIOProcessor(name);
    }

    @Override
    public IOProcessor getScxmlIOProcessor() {
        return this.engine.getIOProcessor(ScxmlIOProcessor.NAME);
    }

    @Override
    public Set<IOProcessor> getIOProcessors() {
        return this.engine.getIOProcessors();
    }

    @Override
    public Set<InvokeHandler> getInvokeHandlers() {
        return this.engine.getInvokeHandlers();
    }

    @Override
    public InvokeHandler getInvokeHandler(String type) {
        return this.engine.getInvokeHandler(type);
    }

    @Override
    public void offerExternalEvent(String eventName) {
        BasicEvent event = new BasicEvent(eventName);
        this.externalEventQueue.offer(event);
    }

    @Override
    public void offerExternalEvent(String eventName, Object data) {
        BasicEvent event = new BasicEvent(eventName, data);
        this.externalEventQueue.offer(event);
    }

    @Override
    public void offerExternalEvent(Event event) {
        this.externalEventQueue.offer(event);
    }

    @Override
    public Event pollExternalEvent() {
        return this.externalEventQueue.poll();
    }

    @Override
    public void registerInvokeSessionId(String invokeSessionId) {
        this.activeInvokeSessions.add(invokeSessionId);
    }

    @Override
    public void unRegisterInvokeSessionId(String invokeSessionId) {
        this.activeInvokeSessions.remove(invokeSessionId);
    }

    @Override
    public boolean isInvokeSessionActive(String invokeSessionId) {
        return this.activeInvokeSessions.contains(invokeSessionId);
    }

    @Override
    public boolean hasExternalEvents() {
        return !this.externalEventQueue.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 659;
        int result = 1;
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JexlFSMContext other = (JexlFSMContext) obj;
        if (sessionId == null) {
            if (other.sessionId != null)
                return false;
        } else if (!sessionId.equals(other.sessionId))
            return false;
        return true;
    }

    @Override
    public boolean existsVarName(String id) {
        return this.runtimeContext.has(id);
    }

    @Override
    public void updateDataIfExists(String id, Object value) {
        if (this.runtimeContext.has(id)) {
            updateData(id, value);
        }
    }

    @Override
    public void saveCurrentConfiguration() {
        this.lastKnownConfiguration = new JexlMapFSMContextInstance(sessionId, parentSessionId, runtimeContext,
                activeStates);

    }

    @Override
    public ContextInstance getLastStableConfiguration() {
        return this.lastKnownConfiguration;

    }

    @Override
    public boolean hasStateChangedGetAndSet(boolean newValue) {
        return stateChanged.getAndSet(newValue);
    }

}
