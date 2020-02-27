package main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import autoTrader.AutoTrader;
import autoTrader.Locations;
import engine.HaulJob;
import engine.Item;
import engine.Order;
import engine.RouteManager;
import engine.Station;
import javafx.scene.control.ProgressBar;
import util.JSwitchBox;
import util.ProgressDialog;
import util.Util;

/**
 * @author Nicholas Contreras
 */

public class Main {

	private BufferedImage logo;

	private final HashMap<Integer, Item> items;
	private final HashMap<String, Integer> regionNames;
	private final HashSet<Integer> regionsLoaded;
	private final HashMap<Integer, Station> stations;

	private HaulJobTable haulJobTable;

	private JFrame frame;
	private JPanel outerPanel;

	private JSwitchBox autoTraderSwitchBox;

	private JButton openSettingsButton;
	private SettingsWindow settingsWindow;

	public static void main(String[] args) {
		new Main();
	}

	private Main() {
		items = new HashMap<Integer, Item>();
		regionNames = new HashMap<String, Integer>();
		regionsLoaded = new HashSet<Integer>();
		stations = new HashMap<Integer, Station>();

		createMainGUI();

		doStartupLoading();

		createSecondaryGUIs();

		run();
	}

	private void doStartupLoading() {
		ProgressDialog progressDialog = new ProgressDialog(frame, "Connecting", "Connecting to server, please wait",
				false);

		Thread thread = new Thread(() -> {
			loadItems(progressDialog);
			loadRegionNames(progressDialog);
			Util.sleep(250);
			progressDialog.remove();
		}, "Startup-Load-Thread");
		thread.start();

		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				if (progressDialog.isCancelled()) {
					System.exit(0);
				}
			}
		}, 0, 100);
		progressDialog.show();
	}

	private void createMainGUI() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		frame = new JFrame("Eve Haul Buddy");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setPreferredSize(new Dimension(screenSize.width / 2, screenSize.height / 2));

		try {
			logo = ImageIO.read(getClass().getResourceAsStream("/logo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		frame.setIconImage(logo);

		outerPanel = new JPanel(new BorderLayout());

		haulJobTable = new HaulJobTable();
		JScrollPane tableScrollPane = new JScrollPane(haulJobTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		outerPanel.add(tableScrollPane, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel autoTraderSettingsPanel = new JPanel();
		autoTraderSettingsPanel.setLayout(new BoxLayout(autoTraderSettingsPanel, BoxLayout.Y_AXIS));

		JPanel autoTraderSwitchPanel = new JPanel(new BorderLayout(5, 5));

		JLabel autoTraderSwitchBoxLabel = new JLabel("Automatic Trader");
		autoTraderSwitchPanel.add(autoTraderSwitchBoxLabel, BorderLayout.WEST);

		autoTraderSwitchBox = new JSwitchBox("ON", "OFF");
		autoTraderSwitchBox.addChangeListener((ChangeEvent) -> {
			if (autoTraderSwitchBox.isSelected() && !Locations.hasCalibrated) {
				int input = JOptionPane.showConfirmDialog(frame,
						"You have not completed the automatic calibration!" + System.lineSeparator()
								+ "Do you want to run the automatic trader anyways?",
						"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (input == JOptionPane.YES_OPTION) {
				} else {
					autoTraderSwitchBox.setSelected(false);
				}
			}
		});
		autoTraderSwitchPanel.add(autoTraderSwitchBox, BorderLayout.EAST);

		autoTraderSettingsPanel.add(autoTraderSwitchPanel);
		autoTraderSettingsPanel.add(Box.createVerticalStrut(5));

		Box autoTraderCalibrateButtonBox = new Box(BoxLayout.X_AXIS);
		autoTraderCalibrateButtonBox.add(Box.createHorizontalGlue());
		JButton autoTraderCalibrateButton = new JButton("Automatic Trader Calibration");
		autoTraderCalibrateButton.addActionListener((ActionEvent) -> Locations.runAutoCalibrate(frame));
		autoTraderCalibrateButtonBox.add(autoTraderCalibrateButton);
		autoTraderCalibrateButtonBox.add(Box.createHorizontalGlue());

		autoTraderSettingsPanel.add(autoTraderCalibrateButtonBox);
		autoTraderSettingsPanel.add(Box.createVerticalStrut(5));

		Box apiKeyEntryBox = new Box(BoxLayout.X_AXIS);
		JLabel apiKeyEntryLabel = new JLabel("API Key");
		apiKeyEntryBox.add(apiKeyEntryLabel);

		apiKeyEntryBox.add(Box.createHorizontalStrut(5));

		JTextField apiKeyEntryField = new JTextField();
		apiKeyEntryBox.add(apiKeyEntryField);

		autoTraderSettingsPanel.add(apiKeyEntryBox);

		rightPanel.add(autoTraderSettingsPanel, BorderLayout.NORTH);

		Box settingsButtonBox = new Box(BoxLayout.X_AXIS);
		settingsButtonBox.add(Box.createHorizontalGlue());
		openSettingsButton = new JButton("Settings");
		openSettingsButton.addActionListener((ActionEvent) -> {
			settingsWindow.setLocationRelativeTo(frame);
			settingsWindow.setVisible(true);
		});
		settingsButtonBox.add(openSettingsButton);
		settingsButtonBox.add(Box.createHorizontalGlue());
		rightPanel.add(settingsButtonBox, BorderLayout.SOUTH);

		outerPanel.add(rightPanel, BorderLayout.EAST);

		frame.add(outerPanel);

		frame.pack();

		haulJobTable.setColumnStartingWidths();

		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void createSecondaryGUIs() {
		settingsWindow = new SettingsWindow(frame, regionNames.keySet(), (JSONObject stagedSettings) -> {
			for (String curRegionName : regionNames.keySet()) {
				if (stagedSettings.getBoolean(curRegionName)) {
					if (!regionsLoaded.contains(regionNames.get(curRegionName))) {
						ProgressDialog progressDialog = new ProgressDialog(settingsWindow, "Loading " + curRegionName,
								"Loading region '" + curRegionName + "', please wait", true);

						new Thread(() -> {
							loadRegion(curRegionName, progressDialog);
						}, "Load-Region-Thread").start();
						progressDialog.show();

						if (progressDialog.isCancelled()) {
							System.out.println("aborted loading: " + curRegionName);
							return false;
						}
					}
				}
			}
			return true;
		});
	}

	private void loadItems(ProgressDialog progressDialog) {
		System.out.println("Starting item loading");
		progressDialog.setIndeterminateMode(true);
		int numPages = Util.getNumPagesForAPIRequest("universe/types");
		double progressToUse = ((double) numPages) / (numPages + 1);
		progressDialog.setIndeterminateMode(false);
		
		Util.makeAllPagesAPIRequest("universe/types", (String itemDataArray) -> {
			JSONArray itemDataJSONArray = new JSONArray(itemDataArray);
			Util.makeBulkAPIRequests("universe/types/!bulk!", itemDataJSONArray, (String itemInfoString) -> {
				JSONObject itemInfo = new JSONObject(itemInfoString);

				if (itemInfo.getBoolean("published")) {
					Item curItem = new Item(itemInfo.getString("name"), itemInfo.getInt("type_id"),
							itemInfo.getDouble("volume"));
					synchronized (items) {
						items.put(curItem.getItemID(), curItem);
					}
				}
				progressDialog.changeProgress((progressToUse / numPages) / itemDataJSONArray.length());
				return true;
			});
			return true;
		});
		System.out.println("Finished item loading");
	}

	private void loadRegionNames(ProgressDialog progressDialog) {
		System.out.println("Starting region name loading");
		double progressRemaining = 1 - progressDialog.getProgress();
		JSONArray allRegions = new JSONArray(Util.makeAPIRequest("universe/regions"));
		Util.makeBulkAPIRequests("universe/regions/!bulk!", allRegions, (String regionInfoString) -> {
			JSONObject regionInfo = new JSONObject(regionInfoString);
			int regionID = regionInfo.getInt("region_id");
			String regionName = regionInfo.getString("name");

			if (regionID < 11000000) {
				regionNames.put(regionName, regionID);
			}
			progressDialog.changeProgress(progressRemaining / allRegions.length());
			return true;
		});
		System.out.println("Finished region name loading");
	}

	private void loadRegion(String regionName, ProgressDialog progressDialog) {

		int curRegionID = regionNames.get(regionName);

		JSONObject regionInfo = new JSONObject(Util.makeAPIRequest("universe/regions/" + curRegionID));

		JSONArray allConstellations = regionInfo.getJSONArray("constellations");
		Util.makeBulkAPIRequests("universe/constellations/!bulk!", allConstellations,
				(String constellationInfoString) -> {
					JSONObject constellationInfo = new JSONObject(constellationInfoString);
					JSONArray allSystems = constellationInfo.getJSONArray("systems");

					Util.makeBulkAPIRequests("universe/systems/!bulk!", allSystems, (String systemInfoString) -> {
						JSONObject systemInfo = new JSONObject(systemInfoString);

						if (systemInfo.has("stations")) {
							int curSystemID = systemInfo.getInt("system_id");
							double securityLevel = systemInfo.getDouble("security_status");
							JSONArray allStations = systemInfo.getJSONArray("stations");
							Util.makeBulkAPIRequests("universe/stations/!bulk!", allStations,
									(String stationInfoString) -> {
										JSONObject stationInfo = new JSONObject(stationInfoString);
										String curStationName = stationInfo.getString("name");
										int curStationID = stationInfo.getInt("station_id");
										Station newStation = new Station(curStationName, curStationID, curSystemID,
												curRegionID, securityLevel);
										stations.put(curStationID, newStation);
										return !progressDialog.isCancelled();
									});
						}
						return !progressDialog.isCancelled();
					});
					progressDialog.changeProgress(1.0 / allConstellations.length());
					return !progressDialog.isCancelled();
				});
		regionsLoaded.add(curRegionID);
		progressDialog.remove();
	}

	private void run() {
		while (true) {
			long startTime = System.currentTimeMillis();
			HashMap<Item, HashSet<Order>> orders = findOrders();
			HashSet<HaulJob> haulJobs = generateHaulJobs(orders);
			System.out.println("Number of haul jobs: " + haulJobs.size());
			haulJobTable.removeStaleHaulJobs(haulJobs);

			if (autoTraderSwitchBox.isSelected()) {
				ArrayList<HaulJob> sortedHaulJobs = new ArrayList<>(haulJobs);
				sortedHaulJobs.sort((HaulJob hj1, HaulJob hj2) -> {
					return (int) (hj1.getProfitPerJump() - hj2.getProfitPerJump());
				});
//				AutoTrader.run(sortedHaulJobs.get(0));
			}

			long endTime = System.currentTimeMillis();
			int elapsedTime = (int) (endTime - startTime);

			Util.sleep(Math.min(Math.max(elapsedTime, 15000), 60000 * 5));
		}
	}

	private HashMap<Item, HashSet<Order>> findOrders() {
		ArrayList<Integer> regionsToRequest = new ArrayList<Integer>();
		JSONObject currentSettings = settingsWindow.getCurentSettings();
		for (String regionName : regionNames.keySet()) {
			if (currentSettings.getBoolean(regionName)) {
				System.out.println("Will request " + regionName + " this round");
				regionsToRequest.add(regionNames.get(regionName));
			}
		}

		double minSecurity = settingsWindow.getCurentSettings().getDouble("min_security");

		System.out.println("Creating order map");

		HashMap<Item, HashSet<Order>> allOrders = new HashMap<Item, HashSet<Order>>();

		for (Item curItem : items.values()) {
			allOrders.put(curItem, new HashSet<Order>());
		}

		System.out.println("About to request orders for " + regionsToRequest.size() + " regions");

		for (int curRegionID : regionsToRequest) {
			System.out.println("Requesting orders for region " + curRegionID);

			Util.makeAllPagesAPIRequest("markets/" + curRegionID + "/orders?order_type=all",
					(String curOrdersString) -> {
						JSONArray curOrders = new JSONArray(curOrdersString);
						for (int i = 0; i < curOrders.length(); i++) {
							JSONObject curOrderInfo = curOrders.getJSONObject(i);

							Item item;
							synchronized (items) {
								item = items.get(curOrderInfo.getInt("type_id"));
							}
							Station station = stations.get(curOrderInfo.getInt("location_id"));
							boolean isBuy = curOrderInfo.getBoolean("is_buy_order");
							double price = curOrderInfo.getDouble("price");
							int volume = curOrderInfo.getInt("volume_remain");

							if (item != null && station != null) {
								if (station.getSecurityLevel() >= minSecurity) {
									Order newOrder = new Order(item, station, isBuy, price, volume);
									allOrders.get(item).add(newOrder);
								}
							}
						}
						return true;
					});
		}
		System.out.println("Finished all order requests");
		return allOrders;
	}

	private HashSet<HaulJob> generateHaulJobs(HashMap<Item, HashSet<Order>> orders) {
		int maxVolume = settingsWindow.getCurentSettings().getInt("cargo_capacity");
		int maxInvestment = settingsWindow.getCurentSettings().getInt("max_invest");
		String routeMode = settingsWindow.getCurentSettings().getString("dist_calc_mode");

		HashSet<HaulJob> haulJobs = new HashSet<HaulJob>();
		for (Item curItem : orders.keySet()) {
			HashSet<Order> curOrders = orders.get(curItem);
			for (Order order1 : curOrders) {
				if (!order1.isBuy()) {
					for (Order order2 : curOrders) {
						if (order2.isBuy()) {
							HaulJob newHaulJob = new HaulJob(curItem, order1, order2, maxVolume, maxInvestment);
							if (newHaulJob.isViable()) {
								RouteManager.makeRequest(order1.getStation().getSystemID(),
										order2.getStation().getSystemID(), routeMode, (int routeLength) -> {
											newHaulJob.setRouteLength(routeLength);
											haulJobTable.addHaulJob(newHaulJob);
										});
								haulJobs.add(newHaulJob);
							}
						}
					}
				}
			}
		}

		System.out.println("Finished making haul jobs, waiting on routes");

		for (HaulJob curHaulJob : haulJobs) {
			while (curHaulJob.getRouteLength() == -1) {
				Util.sleep(100);
			}
		}

		return haulJobs;
	}
}
