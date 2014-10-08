package com.nosolojava.fsm.impl.model.basic.datamodel;

import java.util.ArrayList;
import java.util.List;

import com.nosolojava.fsm.model.datamodel.Data;
import com.nosolojava.fsm.model.datamodel.DataModel;

public class BasicDataModel implements DataModel {
	private static final long serialVersionUID = 845317073938669202L;

	final List<Data> dataList = new ArrayList<Data>();
	
	@Override
	public List<Data> getDataList() {
		return this.dataList;
	}

	@Override
	public void addData(Data data) {
		this.dataList.add(data);

	}

	@Override
	public void addDataList(List<Data> dataList) {
		this.dataList.addAll(dataList);

	}

	@Override
	public void clearAndSetDataList(List<Data> dataList) {
		this.dataList.clear();
		addDataList(dataList);

	}

}
