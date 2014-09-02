package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Elif;
import com.nosolojava.fsm.runtime.executable.Else;
import com.nosolojava.fsm.runtime.executable.Executable;
import com.nosolojava.fsm.runtime.executable.If;

public class BasicIf extends BasicConditional implements If {
	private static final long serialVersionUID = -415238773021486012L;
	private final List<Elif> elifs = new ArrayList<Elif>();
	private final Else elseOperation;

	public BasicIf(String condition) {
		this(condition, null, null, null);
	}

	public BasicIf(String condition, List<Executable> executables) {
		this(condition, null, null, executables);
	}

	public BasicIf(String condition, Else elseOperation, List<Executable> executables) {
		this(condition, null, elseOperation, executables);
	}

	public BasicIf(String condition, List<Elif> elifs, Else elseOperation, List<Executable> executables) {
		super(condition, executables);
		if (elifs != null) {
			this.elifs.addAll(elifs);
		}
		this.elseOperation = elseOperation;
		
	}

	@Override
	public boolean runIf(Context context) {
		boolean result = false;

		// if condition fails
		if (super.runIf(context)) {
			result = true;
		} else {
			// try with elifs
			boolean enterElif = false;
			Iterator<Elif> iterElif = elifs.iterator();
			Elif elif;
			while (!enterElif && iterElif.hasNext()) {
				elif = iterElif.next();
				enterElif = elif.runIf(context);
			}
			// if no elif and else
			if (!enterElif && this.elseOperation != null) {
				elseOperation.run(context);
			}
		}
		return result;
	}

	public List<Elif> getElifs() {
		return this.elifs;
	}

	public void addElif(Elif elif) {
		this.elifs.add(elif);
	}

	public void addElifs(List<Elif> elifs) {
		this.elifs.addAll(elifs);
	}

	public void clearAndSetElifs(List<Elif> elifs) {
		this.elifs.clear();
		addElifs(elifs);
	}
}
