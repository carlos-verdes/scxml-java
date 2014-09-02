package com.nosolojava.fsm.impl.model.basic.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.nosolojava.fsm.impl.model.basic.datamodel.BasicDataModel;
import com.nosolojava.fsm.impl.model.basic.transition.BasicTransition;
import com.nosolojava.fsm.impl.runtime.basic.StateMachineUtils;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Data;
import com.nosolojava.fsm.model.datamodel.DataModel;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.model.state.DoneData;
import com.nosolojava.fsm.model.state.HistoryTypes;
import com.nosolojava.fsm.model.state.InitialState;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.transition.Transition;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicState implements State {
	// private static final String INITIAL_STATE_NOT_FOUND_ERROR =
	// "Initial state not found";

	private static final String INITIAL_STATE_ALREADY_SET_ERROR = "Initial state already set";

	private static final long serialVersionUID = 4170507445167998847L;

	private final String name;
	private final State parent;

	private final List<Transition> transitions = new ArrayList<Transition>();
	private final List<Executable> onEntryExecutables = new ArrayList<Executable>();
	private final List<Executable> onExitExecutables = new ArrayList<Executable>();
	private final List<Invoke> invokes = new ArrayList<Invoke>();

	private final DataModel dataModel = new BasicDataModel();

	private int documentOrder = -1;

	private InitialState initialState = null;

	private final boolean finalState;
	protected boolean historyState = false;

	private final List<State> childrens = new ArrayList<State>();
	protected final List<State> historyChildrens = new ArrayList<State>();

	private boolean parallel = false;
	private final DoneData doneData;

	public final static String ROOT_STATE_NAME = "scxml";
	private final static String ROOT_NAME_RESERVED_ERROR = ROOT_STATE_NAME + " is a reserved state name.";
	private static final String PARENT_CANT_BE_NULL_ERROR = "Parent can't be null";

	protected BasicState(String name, State parent, DoneData doneData, boolean finalState)
			throws ConfigurationException {
		super();
		this.name = name;
		this.parent = parent;

		if (parent != null) {
			if (parent.isHistoryState()) {
				throw new ConfigurationException("History states can't have children");
			}
			parent.getChildrens().add(this);
			recalculateDocumentOrderAndValidateNames();
		} else {
			this.documentOrder = 0;
		}

		this.doneData = doneData;
		this.finalState = finalState;
	}

	public static BasicState createRootState() throws ConfigurationException {
		return new BasicState(ROOT_STATE_NAME, null, null, false);
	}

	public static BasicState createBasicState(String name, State parent) throws ConfigurationException {
		if (parent == null) {
			throw new ConfigurationException(PARENT_CANT_BE_NULL_ERROR);
		}

		if (ROOT_STATE_NAME.equals(name)) {
			throw new ConfigurationException(ROOT_NAME_RESERVED_ERROR);
		}

		return new BasicState(name, parent, null, false);
	}

	public static BasicState createFinalState(String name, State parent, DoneData donedata)
			throws ConfigurationException {
		if (parent == null) {
			throw new ConfigurationException(PARENT_CANT_BE_NULL_ERROR);
		}

		if (ROOT_STATE_NAME.equals(name)) {
			throw new ConfigurationException(ROOT_NAME_RESERVED_ERROR);
		}

		return new BasicState(name, parent, donedata, true);
	}

	public static BasicState createParallelState(String name, State parent) throws ConfigurationException {
		BasicState state = BasicState.createBasicState(name, parent);
		state.setParallel(true);
		return state;
	}

	public boolean isRootState() {
		return ROOT_STATE_NAME.equals(this.name);
	}

	private void recalculateDocumentOrderAndValidateNames() {
		// we are new children and atomic so, we are in the right branch of our
		// parent
		this.documentOrder = calculateDocumentOrder(parent);

		// update right siblings
		updateRightSiblingsOrder(parent, this.documentOrder);
	}

	private int calculateDocumentOrder(State parent) {
		int result;
		// we have left siblings
		if (parent.hasChildrens() && parent.getChildrens().size() > 1) {
			// document order is our big brother last children +1
			int bigBrotherIndex = parent.getChildrens().size() - 2;
			State bigBrother = parent.getChildrens().get(bigBrotherIndex);
			State bigBrotherLastChildren = getLastChildren(bigBrother);
			result = bigBrotherLastChildren.getDocumentOrder() + 1;
		} else {
			// no "big brothers" so, our order is our parent one +1
			result = parent.getDocumentOrder() + 1;
		}
		return result;
	}

	private State getLastChildren(State state) {
		State lastChildren;
		if (state.hasChildrens()) {
			State aux = state.getChildrens().get(state.getChildrens().size() - 1);
			lastChildren = getLastChildren(aux);
		} else {
			lastChildren = state;
		}
		return lastChildren;
	}

	@Override
	public Transition addTransition(String event, String targetState) {
		Transition transition = new BasicTransition(this.name, event, targetState);
		addTransition(transition);

		return transition;

	}

	@Override
	public Transition addTransition(State source, String event, String targetState) {
		// TODO validate source state is a children for transitions
		Transition transition = new BasicTransition(source.getName(), event, targetState);
		addTransition(transition);

		return transition;

	}

	@Override
	public Transition addTransition(State source, String event, String targetState, String guardCondition) {
		Transition transition = new BasicTransition(source.getName(), event, targetState, guardCondition);
		addTransition(transition);

		return transition;

	}

	@Override
	public Transition addTransition(String event, String targetState, String guarCondition) {
		Transition transition = new BasicTransition(this.name, event, targetState, guarCondition);
		addTransition(transition);
		return transition;
	}

	@Override
	public Transition addInternalTransition(String event, String targetState) {
		Transition transition = new BasicTransition(this.name, event, targetState, null, true);
		addTransition(transition);
		return transition;

	}

	@Override
	public Transition addInternalTransition(String event, String targetState, String guarCondition) {
		Transition transition = new BasicTransition(this.name, event, targetState, guarCondition, true);
		addTransition(transition);
		return transition;
	}

	@Override
	public Transition addInternalTransition(State source, String event, String targetState) {
		Transition transition = new BasicTransition(source.getName(), event, targetState, null, true);
		addTransition(transition);
		return transition;

	}

	@Override
	public Transition addInternalTransition(State source, String event, String targetState, String guarCondition) {
		Transition transition = new BasicTransition(source.getName(), event, targetState, guarCondition, true);
		addTransition(transition);
		return transition;
	}

	private void updateRightSiblingsOrder(State parent, int lastDocumentOrder) {
		State grandParent = parent.getParent();
		if (grandParent != null) {
			Iterator<State> grandpaChildrenIter = grandParent.getChildrens().iterator();
			State aunt;
			while (grandpaChildrenIter.hasNext()) {
				aunt = grandpaChildrenIter.next();
				// find the aunts older than my parent
				if (aunt.getDocumentOrder() > parent.getDocumentOrder()) {
					lastDocumentOrder = updateAuntDocumentOrder(aunt, lastDocumentOrder);
				}
			}
			updateRightSiblingsOrder(grandParent, lastDocumentOrder);
		}

	}

	private int updateAuntDocumentOrder(State aunt, int lastDocumentOrder) {
		aunt.setDocumentOrder(++lastDocumentOrder);
		for (State cousin : aunt.getChildrens()) {
			lastDocumentOrder = updateAuntDocumentOrder(cousin, lastDocumentOrder);
		}
		return lastDocumentOrder;
	}

	@Override
	public int compareTo(State right) {
		return StateMachineUtils.entryOrderComparator.compare(this, right);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		BasicState other = (BasicState) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean isDescendant(State otherState) {
		boolean result = false;

		State aux = this.getParent();
		// state could NOT be a descendant of itself
		while (!result && aux != null) {
			result = aux.equals(otherState);
			aux = aux.getParent();
		}

		return result;
	}

	protected void calculateBreadCrumb(List<State> breadcrumb) {
		BasicState aux = (BasicState) parent;
		if (aux != null) {
			aux.calculateBreadCrumb(breadcrumb);
			breadcrumb.add(aux);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<Transition> getTransitions() {
		return transitions;
	}

	public void addTransition(Transition transition) {
		this.transitions.add(transition);
	}

	@Override
	public List<Executable> getOnEntryExecutables() {
		return this.onEntryExecutables;
	}

	@Override
	public void addOnEntryExecutable(Executable varName) {
		this.onEntryExecutables.add(varName);
	}

	@Override
	public void addOnEntryExecutables(List<Executable> varNames) {
		this.onEntryExecutables.addAll(varNames);
	}

	@Override
	public void clearAndSetOnEntryExecutables(List<Executable> varNames) {
		this.onEntryExecutables.clear();
		addOnEntryExecutables(varNames);
	}

	@Override
	public List<Executable> getOnExitExecutables() {
		return this.onExitExecutables;
	}

	@Override
	public void addOnExitExecutable(Executable executable) {
		this.onExitExecutables.add(executable);
	}

	@Override
	public void addOnExitExecutables(List<Executable> executables) {
		this.onExitExecutables.addAll(executables);
	}

	@Override
	public void clearAndSetOnExitExecutables(List<Executable> executables) {
		this.onExitExecutables.clear();
		addOnExitExecutables(executables);
	}

	@Override
	public List<Invoke> getInvokes() {
		return this.invokes;
	}

	@Override
	public void addInvoke(Invoke invoke) {
		this.invokes.add(invoke);
	}

	@Override
	public void addInvokes(List<Invoke> invokes) {
		this.invokes.addAll(invokes);
	}

	@Override
	public void clearAndSetInvokes(List<Invoke> invokes) {
		this.invokes.clear();
		addInvokes(invokes);
	}

	@Override
	public void addData(Data data) {
		this.dataModel.addData(data);
	}

	@Override
	public void addData(List<Data> dataList) {
		this.dataModel.addDataList(dataList);
	}

	@Override
	public DataModel getDataModel() {
		return this.dataModel;
	}

	@Override
	public State getParent() {
		return this.parent;
	}

	@Override
	public List<State> getBreadCrumb() {
		List<State> breadcrumb = new ArrayList<State>();
		calculateBreadCrumb(breadcrumb);

		return breadcrumb;
	}

	@Override
	public int getDocumentOrder() {
		return documentOrder;
	}

	@Override
	public void setDocumentOrder(int documentOrder) {
		this.documentOrder = documentOrder;
	}

	@Override
	public boolean hasChildrens() {
		return this.childrens.size() > 0;
	}

	@Override
	public List<State> getChildrens() {
		return this.childrens;
	}

	@Override
	public List<State> getHistoryStates() {
		return this.historyChildrens;
	}

	@Override
	public InitialState getInitialState() {
		if (this.initialState == null) {
			synchronized (this) {
				if (this.initialState == null) {
					State firstChildren = getChildrens().get(0);
					createInitialFromName(firstChildren.getName());
				}
			}
		}

		return this.initialState;
	}

	private void createInitialFromName(String initialName) {
		String uid = UUID.randomUUID().toString();
		this.initialState = new BasicInitialState(uid, this.name, initialName);
	}

	@Override
	public void setInitialState(InitialState initialState) throws ConfigurationException {
		synchronized (this) {
			if (this.initialState != null) {
				throw new ConfigurationException(INITIAL_STATE_ALREADY_SET_ERROR);
			}
			this.initialState = initialState;
		}
	}

	@Override
	public void setInitialStateName(String initialStateName) throws ConfigurationException {
		synchronized (this) {

			if (this.initialState != null) {
				throw new ConfigurationException(INITIAL_STATE_ALREADY_SET_ERROR);
			}

			createInitialFromName(initialStateName);

		}

	}

	@Override
	public boolean isHistoryState() {
		return this.historyState;
	}

	@Override
	public HistoryTypes getHistoryType() {
		return null;
	}

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	public DoneData getDoneData() {
		return doneData;
	}

	@Override
	public boolean isFinal() {
		return this.finalState;
	}

	public static void main(String[] args) throws ConfigurationException {

		State s = BasicState.createBasicState("s", BasicState.createRootState());
		s.setInitialStateName("s1");

		State s1 = BasicState.createBasicState("s1", s);
		State s11 = BasicState.createBasicState("s11", s1);
		s1.setInitialStateName("s11");

		State s2 = BasicState.createBasicState("s2", s);
		BasicState s21 = BasicState.createBasicState("s21", s2);
		s2.setInitialStateName("s21");

		List<State> breadcrumb = s11.getBreadCrumb();
		for (State state : breadcrumb) {
			System.out.print(state.getName() + ".");
		}
		System.out.println();
		breadcrumb = s21.getBreadCrumb();
		for (State state : breadcrumb) {
			System.out.print(state.getName() + ".");
		}

	}

}
