package com.nosolojava.fsm.impl.runtime.executable.externalcomm.invokeHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.AbstractBasicInvokeHandler;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicMessage;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.io.ConsoleIOProcessor;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.io.ScxmlIOProcessor;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeInfo;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class ConsoleInvokeHandler extends AbstractBasicInvokeHandler {
	private static final long serialVersionUID = 1106621957118194732L;
	public static String TYPE = "console";

	private ConcurrentMap<String, Thread> taskMap = new ConcurrentHashMap<String, Thread>();
	private Lock lock = new ReentrantLock();

	@Override
	public String getType() {
		return "console";
	}

	class ReadConsoleTask implements Runnable {

		private final String invokeId;
		private final Context context;

		public ReadConsoleTask(String invokeId, Context context) {
			super();
			this.invokeId = invokeId;
			this.context = context;
		}

		private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		@Override
		public void run() {
			String result = null;

			while (!Thread.interrupted() && taskMap.containsKey(invokeId)) {
				try {
					result = null;
					result = this.br.readLine();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}

				if (result != null) {
					Message message = BasicMessage.createSimpleSCXMLMessage(result, context.getParentSessionId(),
							context);
					IOProcessor ioProcessor = context.searchIOProcessor(ScxmlIOProcessor.NAME);
					ioProcessor.sendMessage(message);

				}

			}

			System.out.println("Ending thread " + invokeId);
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ConsoleInvokeHandler.this.taskMap.remove(invokeId);
		}

	}

	@Override
	public void invokeServiceInternal(InvokeInfo invokeInfo, Context context) {

		if (!this.taskMap.containsKey(context.getSessionId())) {
			this.lock.lock();
			if (!this.taskMap.containsKey(context.getSessionId())) {

				ReadConsoleTask readConsoleTask = new ReadConsoleTask(invokeInfo.getInvokeId(), context);
				Thread t = new Thread(readConsoleTask);
				t.start();

				this.taskMap.put(invokeInfo.getInvokeId(), t);
			}
			this.lock.unlock();
		}

	}

	@Override
	public void sendMessageToService(Message message, Context context) {

		IOProcessor ioProcessor = context.searchIOProcessor(ConsoleIOProcessor.NAME);
		ioProcessor.sendMessage(message);

	}

	@Override
	public void onEndSession(String invokeId, Context context) {
		if (this.taskMap.containsKey(invokeId)) {
			Thread thread = this.taskMap.get(invokeId);
			if (thread.isAlive()) {
				System.out.println("Interrupting thread " + invokeId);
				thread.interrupt();
				System.out.println("Please introduce control+D in your keyboard to finish session.");
			}
		}
	}

}
