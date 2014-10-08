package com.nosolojava.fsm.model.state;

import java.io.Serializable;
import java.util.List;

import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Data;
import com.nosolojava.fsm.model.datamodel.DataModel;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.model.transition.Transition;
import com.nosolojava.fsm.runtime.executable.Executable;

public interface State extends Serializable, Comparable<State> {

	// name
	String getName();

	// parent
	State getParent();

	// root management
	boolean isRootState();

	// document order
	int getDocumentOrder();

	void setDocumentOrder(int documentOrder);

	// indicates if state is final
	boolean isFinal();

	// for final states
	DoneData getDoneData();

	// indicates if state is compound
	boolean hasChildrens();

	// childrens
	List<State> getChildrens();

	// initial state (attribute or element)
	void setInitialStateName(String stateName) throws ConfigurationException;

	void setInitialState(InitialState initialState) throws ConfigurationException;

	InitialState getInitialState();

	//history state
	boolean isHistoryState();
	HistoryTypes getHistoryType();
	List<State> getHistoryStates();
	
	// indicates if state is parallel
	boolean isParallel();

	void setParallel(boolean parallel);

	// bread crumb
	List<State> getBreadCrumb();

	// datamodel
	DataModel getDataModel();

	void addData(Data data);

	void addData(List<Data> dataList);

	// transitions
	List<Transition> getTransitions();

	void addTransition(Transition currentTransition);

	Transition addTransition(String event, String targetStatename);

	Transition addTransition(String event, String targetStatename, String guarCondition);

	Transition addTransition(State source, String event, String targetStatename);

	Transition addTransition(State source, String event, String targetStatename, String guardCondition);

	Transition addInternalTransition(String event, String targetStatename);

	Transition addInternalTransition(String event, String targetStatename, String guarCondition);

	Transition addInternalTransition(State source, String event, String targetStatename);

	Transition addInternalTransition(State source, String event, String targetState, String guarCondition);

	// Executable content
	List<Executable> getOnEntryExecutables();

	void addOnEntryExecutable(Executable executable);

	void addOnEntryExecutables(List<Executable> executables);

	void clearAndSetOnEntryExecutables(List<Executable> executables);

	List<Executable> getOnExitExecutables();

	void addOnExitExecutable(Executable var);

	void addOnExitExecutables(List<Executable> vars);

	void clearAndSetOnExitExecutables(List<Executable> vars);

	// invokes
	List<Invoke> getInvokes();

	void addInvoke(Invoke invoke);

	void addInvokes(List<Invoke> invokes);

	void clearAndSetInvokes(List<Invoke> invokes);

	// is descendant
	boolean isDescendant(State otherState);


}
