package test.com.nosolojava.fsm.basic.executable;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import com.nosolojava.fsm.impl.model.basic.BasicStateMachineModel;
import com.nosolojava.fsm.impl.model.basic.datamodel.BasicData;
import com.nosolojava.fsm.impl.model.basic.jexl.JexlFSMContext;
import com.nosolojava.fsm.impl.model.basic.state.BasicState;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicAssign;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicElif;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicElse;
import com.nosolojava.fsm.impl.runtime.executable.basic.BasicIf;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.DataModel;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Elif;
import com.nosolojava.fsm.runtime.executable.Else;
import com.nosolojava.fsm.runtime.executable.Executable;
import com.nosolojava.fsm.runtime.executable.If;

public class TestConditionalsAssignAndSimpleInvokeHandler {

	private static final String IF = "if";
	private static final String ELSE = "else";
	private static final String ELIF = "elif";
	private static final String INIT = "init";
	private static final String X_VAR = "x";
	private static final String Y_VAR = "y";
	private Context context;

	public TestConditionalsAssignAndSimpleInvokeHandler() throws ConfigurationException {
		super();

		BasicState root = BasicState.createRootState();
		StateMachineModel model = new BasicStateMachineModel(root);
		DataModel dm = model.getRootState().getDataModel();
		dm.addData(BasicData.createExpressionData(X_VAR, "" + 1));
		dm.addData(BasicData.createValueData(Y_VAR, INIT));

		context = new JexlFSMContext(null, null, model, null, null);
	}

	@Test
	public void testIfElifElseAssign() throws ConfigurationException {

		If ifClause = createMockup();
		String value = (String) context.getDataByName(Y_VAR);
		Assert.assertEquals(INIT, value);

		// execute if --> assign if
		ifClause.run(context);
		value = (String) context.getDataByName(Y_VAR);
		Assert.assertEquals(IF, value);

		ifClause.run(context);
		value = (String) context.getDataByName(Y_VAR);
		Assert.assertEquals(ELIF, value);

		ifClause.run(context);
		value = (String) context.getDataByName(Y_VAR);
		Assert.assertEquals(ELSE, value);

	}

	private If createMockup() throws ConfigurationException {

		Elif elif = new BasicElif("x>=2 && x<3", createExecutables(3, ELIF));
		ArrayList<Elif> elifs = new ArrayList<Elif>();
		elifs.add(elif);

		Else elseClause = new BasicElse(createExecutables(4, ELSE));

		If ifClause = new BasicIf("x<2", elifs, elseClause, createExecutables(2, IF));

		return ifClause;
	}

	private ArrayList<Executable> createExecutables(int x, String y) {
		ArrayList<Executable> executables = new ArrayList<Executable>();
		BasicAssign assign = BasicAssign.assignByValue(X_VAR, "" + x);
		executables.add(assign);

		assign = BasicAssign.assignByValue(Y_VAR, y);
		executables.add(assign);

		return executables;
	}
}
