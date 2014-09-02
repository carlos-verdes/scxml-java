package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Executable;
import com.nosolojava.fsm.runtime.executable.ForEach;

public class BasicForEach implements ForEach {
	private static final long serialVersionUID = -2108800262588663830L;

	protected final List<Executable> executables = new ArrayList<Executable>();
	private final String arrayName;
	private final String itemName;
	private final String indexName;

	public BasicForEach(String array, String item, String index,
			List<Executable> executables) {
		super();
		this.arrayName = array;
		this.itemName = item;
		this.indexName = index;
		this.executables.addAll(executables);
	}
	public BasicForEach(String array, String item, 
			List<Executable> executables) {
		super();
		this.arrayName = array;
		this.itemName = item;
		this.indexName = null;
		this.executables.addAll(executables);
	}

	@Override
	public void run(Context context) {
		Serializable arrayObj = context.getDataByName(arrayName);
		context.createVarIfDontExist(itemName, null);
		if(this.indexName!=null&&!"".equals(this.indexName)){
			context.createVarIfDontExist(this.indexName, null);
		}

		// TODO validate array and item class

		Class<?> arrayClass = arrayObj.getClass();
		if (arrayClass.isArray()) {

			int arrayLength = Array.getLength(arrayObj);
			Object arrayItem;
			for (int i = 0; i < arrayLength; i++) {
				arrayItem = Array.get(arrayObj, i);
				processItem(i,context, arrayItem);
			}
		} else if (Collection.class.isAssignableFrom(arrayClass)) {
			@SuppressWarnings("unchecked")
			Collection<Object> arrayList = (List<Object>) arrayObj;

			int i=0;
			for (Object arrayItem : arrayList) {
				processItem(i,context, arrayItem);
				i++;
			}

		}
		

	}

	private void processItem(int index, Context context, Object arrayItem) {
		if(this.indexName!=null&&!"".equals(this.indexName)){
			context.updateData(this.indexName, index);
		}
		context.updateData(itemName, arrayItem);
		executeAll(context);
	}

	private void executeAll(Context context) {
		for (Executable exec : executables) {
			exec.run(context);
		}
	}

}
