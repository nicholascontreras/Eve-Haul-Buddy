package engine;

import java.util.ArrayList;
import java.util.HashMap;

import util.Util;

/**
 * @author Nicholas Contreras
 */

public class RouteManager {

	private static final int NUM_THREADS = 25;
	private static final HashMap<String, Integer> ROUTE_CACHE;
	private static final ArrayList<Request> REQUESTS;
	private static final Thread[] ROUTE_THREADS;

	static {
		ROUTE_THREADS = new Thread[NUM_THREADS];
		ROUTE_CACHE = new HashMap<String, Integer>();
		REQUESTS = new ArrayList<Request>();

		for (int i = 0; i < ROUTE_THREADS.length; i++) {
			new Thread(() -> {
				while (true) {
					Request request = null;

					while (request == null) {
						synchronized (REQUESTS) {
							if (!REQUESTS.isEmpty()) {
								request = REQUESTS.remove(0);
							}
						}
						if (request == null) {
							Util.sleep(2000);
						}
					}

					if (ROUTE_CACHE.containsKey(request.call)) {
						request.callback.length(ROUTE_CACHE.get(request.call));
					} else {
						String route = Util.makeAPIRequest("route/" + request.call);
						int routeLength = route.split(",").length - 1;
						ROUTE_CACHE.put(request.call, routeLength);
						request.callback.length(routeLength);
					}
				}
			}, "Route-Thread-" + i).start();
		}
	}

	public static void makeRequest(int startSystem, int endSystem, String mode, RouteResult callback) {
		String requestString = startSystem + "/" + endSystem + "?flag=" + mode;
		synchronized (REQUESTS) {
			REQUESTS.add(new Request(requestString, callback));
		}
	}

	private static class Request {

		private final String call;
		private final RouteResult callback;

		private Request(String call, RouteResult callback) {
			this.call = call;
			this.callback = callback;
		}
	}

	public interface RouteResult {
		void length(int length);
	}
}
