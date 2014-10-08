package com.nosolojava.fsm.model.datamodel;

import java.io.Serializable;
import java.util.List;

public interface DataModel extends Serializable{

	
	List<Data> getDataList();

	void addData(Data data);

	void addDataList(List<Data> datas);

	void clearAndSetDataList(List<Data> datas);
}
