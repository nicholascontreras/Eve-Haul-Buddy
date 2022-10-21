package util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Nicholas Contreras
 */

public class Util {

	private static final String BASE_REQUEST_URL = "https://esi.evetech.net/latest/";

	private static final int NUM_THREADS = 10;

	public static void makeBulkAPIRequests(String request, JSONArray substutions, APICallback callback) {
		Thread[] threads = new Thread[NUM_THREADS];

		for (int i = 0; i < NUM_THREADS; i++) {
			final int startIndex = i;
			Thread t = new Thread(() -> {
				int curIndex = startIndex;

				while (true) {
					if (curIndex >= substutions.length()) {
						break;
					}

					String curSub = substutions.get(curIndex).toString();
					String curData = makeAPIRequest(request.replace("!bulk!", curSub));
					boolean doAgain = callback.callback(curData);
					if (!doAgain) {
						break;
					}
					curIndex += NUM_THREADS;
				}
			});
			threads[i] = t;
			t.setDaemon(true);
			t.start();
		}

		for (Thread t : threads) {
			while (t.isAlive()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static int getNumPagesForAPIRequest(String request) {
		int low = 2;
		int high = 2;

		while (true) {
			String curData = makeAPIRequest(request + (request.contains("?") ? "&" : "?") + "page=" + high);

			if (curData == null) {
				while (true) {
					int cur = (low + high) / 2;

					if (cur == low) {
						return cur;
					} else {
						curData = makeAPIRequest(request + (request.contains("?") ? "&" : "?") + "page=" + cur);
						if (curData == null) {
							high = cur;
						} else {
							low = cur;
						}
					}
				}
			} else {
				low = high;
				high *= 2;
			}
		}
	}

	public static void makeAllPagesAPIRequest(String request, APICallback callback) {

		Thread[] threads = new Thread[NUM_THREADS];

		for (int i = 0; i < NUM_THREADS; i++) {
			final int startPage = i + 1;
			Thread t = new Thread(() -> {
				int curPage = startPage;

				while (true) {
					String curData = makeAPIRequest(request + (request.contains("?") ? "&" : "?") + "page=" + curPage);
					if (curData == null) {
						break;
					}
					boolean doAgain = callback.callback(curData);
					if (!doAgain) {
						break;
					}
					curPage += NUM_THREADS;
				}
			});
			threads[i] = t;
			t.setDaemon(true);
			t.start();
		}

		for (Thread t : threads) {
			while (t.isAlive()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String makeAPIRequest(String request) {

		for (int retryTimes = 0; retryTimes < 5; retryTimes++) {
			try {
				URL url = new URL(BASE_REQUEST_URL + request);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				if (conn.getResponseCode() == 200) {
					InputStreamReader isr = new InputStreamReader(conn.getInputStream());
					StringBuilder response = new StringBuilder();
					int curRead = isr.read();

					while (curRead != -1) {
						response.append((char) curRead);
						curRead = isr.read();
					}

					return response.toString();
				} else {
					continue;
				}
			} catch (IOException e) {
				System.err.println("Error making API call (" + e.getMessage() + "), attempt: " + (retryTimes + 1));
				sleep(1000);
			}
		}
		return null;
	}

	public static <T> T getRandomItem(Collection<T> collection) {
		synchronized (collection) {
			int rand = (int) (Math.random() * collection.size());
			Iterator<T> it = collection.iterator();
			for (int i = 0; i < rand; i++) {
				it.next();
			}
			return it.next();
		}
	}

	public static JSONObject copy(JSONObject object) {
		JSONObject copy = new JSONObject();

		for (String curKey : object.keySet()) {
			copy.put(curKey, object.get(curKey));
		}

		return copy;
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public interface APICallback {
		boolean callback(String data);
	}
}
