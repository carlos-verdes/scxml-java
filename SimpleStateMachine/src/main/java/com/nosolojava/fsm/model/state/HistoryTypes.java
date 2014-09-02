package com.nosolojava.fsm.model.state;

public enum HistoryTypes {

	DEEP("deep"),SHALLOW("shallow");
	
	private HistoryTypes(String type){
		this.type=type;
	}
	private String type;

	public static HistoryTypes fromString(String type){
		HistoryTypes result=null;
		if(DEEP.type.equals(type)){
			result=DEEP;
		}else if(SHALLOW.type.equals(type)){
			result=SHALLOW;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return type;
	}
	
	
}
