package autoTrader;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.MouseInfo;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.Util;

/**
 * @author Nicholas Contreras
 */

public class Locations {
	
	public static boolean hasCalibrated;

	private static JDialog calibrateDialog;

	private static JPanel cardPanel;
	private static CardLayout cardLayout;

	private static int numCalibrates;
	
	
	public static Point undockButton = new Point(1177, 148);

	public static Point mapOpenButton = new Point(20, 293);
	public static Point mapSearchButton = new Point(1125, 111);
	public static Point mapFirstItemButton = new Point(1064, 133);
	public static Point mapSetDestinationButton = new Point(1114, 159);
	public static Point mapCloseButton = new Point(1127, 90);
	
	public static Point autopilotButton = new Point(526, 690);
	
	public static Point currentStationLabel = new Point(83, 123);
	public static Point currentStationCopyButton = new Point(117, 208);
	
	public static Point openMarketButton = new Point(20, 225);
	public static Point marketFirstItem;

	public static void runAutoCalibrate(JFrame frame) {
		calibrateDialog = new JDialog(frame, "Calibrate Wizzard", ModalityType.DOCUMENT_MODAL);

		cardPanel = new JPanel();
		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		addStep(mapOpenButton, "Place your mouse over the open map button");
		addStep(mapSearchButton, "Place your mouse over the search button in the map");

		addStep(openMarketButton, "Place your mouse over the open market button");
//		addStep(marketSearchButton, "Place your mouse over the search button in the market");
		
		calibrateDialog.add(cardPanel);
		calibrateDialog.pack();
		calibrateDialog.setResizable(false);
		calibrateDialog.setVisible(true);
	}

	private static void addStep(Point toSet, String prompt) {

		numCalibrates++;

		JPanel curPanel = new JPanel(new BorderLayout(5, 5));
		curPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		curPanel.add(new JLabel(prompt), BorderLayout.NORTH);
		JButton startButton = new JButton("Begin Countdown");
		startButton.setActionCommand(numCalibrates + "");
		startButton.addActionListener((ActionEvent) -> {
			startButton.setEnabled(false);
			countdownAndRecordLocation(toSet);
			if (startButton.getActionCommand().equals(numCalibrates + "")) {
				hasCalibrated = true;
				calibrateDialog.dispose();
			} else {
				cardLayout.next(cardPanel);
			}
		});
		curPanel.add(startButton, BorderLayout.SOUTH);
		cardPanel.add(curPanel);
	}

	private static void countdownAndRecordLocation(Point toSet) {
		for (int i = 0; i < 10; i++) {
			Toolkit.getDefaultToolkit().beep();
			Util.sleep(1000);
		}

		toSet = MouseInfo.getPointerInfo().getLocation();
		System.out.println("recorded " +  toSet);
		
		for (int i = 0; i < 3; i++) {
			Toolkit.getDefaultToolkit().beep();
			Util.sleep(250);
		}
	}
	
	public static void main(String[] args) {
		Util.sleep(10000);
		
		System.out.println(MouseInfo.getPointerInfo().getLocation());
	}
}
