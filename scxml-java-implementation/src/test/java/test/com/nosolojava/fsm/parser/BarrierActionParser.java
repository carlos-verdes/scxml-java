package test.com.nosolojava.fsm.parser;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nosolojava.fsm.parser.XppActionParser;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.CustomAction;

public class BarrierActionParser implements XppActionParser {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private static final String NS = "http://com.nosolojava.schemas.fsm/testAction";

	private final Map<String, CyclicBarrier> barriers = new ConcurrentHashMap<String, CyclicBarrier>();

	public void blockUntilAction(String tag) {
		//logger.entering(this.getClass().getName(), "blockUntilAction");
		waitForAll(tag);
	}

	private void waitForAll(String barrierName) {
		try {
			//logger.entering(this.getClass().getName(), "waitForAll");
			//logger.log(Level.INFO,"waiting barrier {0}",new Object[]{barrierName});
			CyclicBarrier barrier = barriers.get(barrierName);
			barrier.await();
			logger.log(Level.INFO, "exiting barrier {0}", new Object[] { barrierName });
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Can't wait for barrier, barriername: {0} " + barrierName, new Object[] { barrierName });
			logger.log(Level.SEVERE, "stacktrace: ", e);

		}
	}

	@Override
	public CustomAction parseAction(XmlPullParser xpp) throws XmlPullParserException, IOException {
		CustomAction result = null;
		if (xpp.getName().equals("barrierAction")) {
			final CyclicBarrier barrier = new CyclicBarrier(2);
			xpp.nextTag();
			final String tag = xpp.nextText();
			barriers.put(tag, barrier);

			result = new CustomAction() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run(Context context) {
					try {
						//logger.log(Level.INFO,"entering barrier from scm {0}",new Object[]{tag});
						barrier.await();
						logger.log(Level.INFO, "exiting barrier from scm {0}", new Object[] { tag });

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public String getNamespace() {
					return NS;
				}
			};
		}
		return result;
	}

	@Override
	public String getNamespace() {
		return NS;
	}

}
