package com.nosolojava.fsm.impl.model.basic.state;

import java.util.List;

import com.nosolojava.fsm.impl.model.basic.transition.BasicTransition;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Data;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.model.state.HistoryTypes;
import com.nosolojava.fsm.model.state.InitialState;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.transition.Transition;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicHistoryState extends BasicState {
	private static final long serialVersionUID = -6188798255574594718L;
	private final HistoryTypes historyType;

	public BasicHistoryState(State parent, String id, HistoryTypes historyType, String target)
			throws ConfigurationException {
		super(id, parent, null, false);

		this.historyType = historyType;

		BasicTransition transition = new BasicTransition(parent.getName(), target);
		super.addTransition(transition);

		parent.getHistoryStates().add(this);
	}

	@Override
	public HistoryTypes getHistoryType() {
		return this.historyType;
	}

	@Override
	public void setInitialStateName(String stateName) throws ConfigurationException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setInitialState(InitialState initialState) throws ConfigurationException {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean isHistoryState() {
		return true;
	}

	@Override
	public boolean isParallel() {
		return false;
	}

	@Override
	public void setParallel(boolean parallel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addData(Data data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addData(List<Data> dataList) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTransition(Transition currentTransition) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Transition addTransition(String event, String targetStatename) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transition addTransition(String event, String targetStatename, String guarCondition) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transition addTransition(State source, String event, String targetStatename) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transition addTransition(State source, String event, String targetStatename, String guardCondition) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transition addInternalTransition(String event, String targetStatename) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transition addInternalTransition(String event, String targetStatename, String guarCondition) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transition addInternalTransition(State source, String event, String targetStatename) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transition addInternalTransition(State source, String event, String targetState, String guarCondition) {
		throw new UnsupportedOperationException();
	}

	public void addOnEntryExecutable(Executable executable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addOnEntryExecutables(List<Executable> executables) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearAndSetOnEntryExecutables(List<Executable> executables) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addOnExitExecutable(Executable var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addOnExitExecutables(List<Executable> vars) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearAndSetOnExitExecutables(List<Executable> vars) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addInvoke(Invoke invoke) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addInvokes(List<Invoke> invokes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearAndSetInvokes(List<Invoke> invokes) {
		throw new UnsupportedOperationException();
	}

}
