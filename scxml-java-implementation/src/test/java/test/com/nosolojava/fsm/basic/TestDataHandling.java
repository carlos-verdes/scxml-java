package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Test;

import com.nosolojava.fsm.impl.model.basic.BasicStateMachineModel;
import com.nosolojava.fsm.impl.model.basic.jexl.JexlFSMContext;
import com.nosolojava.fsm.impl.model.basic.state.BasicState;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.Context;

public class TestDataHandling {

	@Test
	public void thatFileContentIsLoadedFromURI() throws ConfigurationException,
			URISyntaxException, IOException, InterruptedException,
			SCXMLParserException {

		BasicState root = BasicState.createRootState();
		StateMachineModel model = new BasicStateMachineModel(root);

		Context ctx = new JexlFSMContext("sessionId", null, model, null, null);
		for (Entry<String, String> entry : System.getenv().entrySet()) {
			System.out.println(String.format("%s: %s", entry.getKey(),
					entry.getValue()));
		}
		
		URL textFileUrl = ClassLoader.getSystemClassLoader().getResource("someJSONtest.txt");
		
		//the result would be an InputStream
		InputStream result = ctx.getDataFromURL(textFileUrl);
		Assert.assertNotNull(result);
	
	}

}
