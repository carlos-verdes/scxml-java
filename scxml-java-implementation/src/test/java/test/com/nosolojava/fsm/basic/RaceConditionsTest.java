package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public class RaceConditionsTest {

//	@Test
//	public void testIncrementalRaceCondition() throws ConfigurationException,
//			IOException, InterruptedException, SCXMLParserException {
//
//		int iterations = 100;
//		int instances = 100;
//
//		BasicStateMachineEngine.CHECK_AVAILABLE_SESSIONS_PERIOD_IN_MILLIS
//				.set(1);
//
//		StateMachineEngine engine = new BasicStateMachineEngine();
//		engine.start();
//
//		Context[] contexts = new Context[instances];
//
//		System.out.println(String.format("Start contexts: %s", new Date()));
//		// init all the instances (this is synch)
//		for (int i = 0; i < contexts.length; i++) {
//			Context context = engine.startFSMSession(URI
//					.create("classpath:incrementalRaceConditionSM.xml"));
//			contexts[i] = context;
//		}
//
//		System.out.println("Start init events: " + new Date());
//		Context context;
//		// raise all the starting events
//		for (int i = 0; i < contexts.length; i++) {
//			context = contexts[i];
//			engine.pushEvent(context.getSessionId(), new BasicEvent("start",
//					iterations));
//
//		}
//
//		System.out.println("Start increment events: " + new Date());
//		// send all the increment events
//		for (int j = 0; j < iterations; j++) {
//			for (int i = 0; i < contexts.length; i++) {
//				context = contexts[i];
//				engine.pushEvent(context.getSessionId(), new BasicEvent(
//						"incrementEvent"));
//
//			}
//		}
//
//		long init = Calendar.getInstance().getTimeInMillis();
//		System.out.println("All events sent!!");
//		boolean ended = engine.shutdownAndWait(10000, TimeUnit.MILLISECONDS);
//		long end = Calendar.getInstance().getTimeInMillis();
//		System.out.println("Ended in: " + ((end - init) / 1000) + " seconds");
//		Assert.assertTrue("The FSM hasn't finished", ended);
//
//		for (int i = 0; i < instances; i++) {
//			Assert.assertEquals("not expected iterations, instance: " + i,
//					iterations, (int) contexts[i].getDataByExpression("x"));
//		}
//
//	}
}
