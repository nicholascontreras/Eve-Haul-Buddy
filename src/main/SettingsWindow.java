package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import org.json.JSONObject;

import util.Util;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class SettingsWindow extends JDialog {

	private JSONObject currentSettings, stagedSettings;

	private final JCheckBox[] regionCheckBoxes;

	private final JSpinner cargoCapacitySpinner;
	private final JSpinner maximumInvestmentSpinner;
	private final JSpinner minSecuritySpinner;
	private final JComboBox<String> distanceCalcModeSelector;

	public SettingsWindow(JFrame parent, Set<String> regionNames, ConfirmSettingsChange confirmCall) {

		currentSettings = new JSONObject();
		stagedSettings = new JSONObject();

		setIconImage(parent.getIconImage());
		setTitle("Settings");

		setLayout(new BorderLayout());
		setLocationRelativeTo(parent);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(false);

		JPanel outerPanel = new JPanel(new BorderLayout());

		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel outerRegionSelectPanel = new JPanel(new BorderLayout());
		outerRegionSelectPanel.add(new JLabel("Select Regions To Search", JLabel.CENTER), BorderLayout.NORTH);

		JPanel regionCheckBoxPanel = new JPanel(new GridLayout(0, 1));

		regionCheckBoxes = new JCheckBox[regionNames.size()];
		int counter = 0;
		int maxCheckBoxWidth = 0;
		String[] sortedRegionNames = regionNames.toArray(new String[0]);
		Arrays.sort(sortedRegionNames);
		for (String curRegionName : sortedRegionNames) {
			JCheckBox curCheckBox = new JCheckBox(curRegionName);
			curCheckBox.addActionListener((ActionEvent) -> stagedSettings.put(curRegionName, curCheckBox.isSelected()));
			regionCheckBoxes[counter] = curCheckBox;
			curCheckBox.getActionListeners()[0].actionPerformed(null);
			regionCheckBoxPanel.add(curCheckBox);
			maxCheckBoxWidth = Math.max(maxCheckBoxWidth, curCheckBox.getPreferredSize().width);
			counter++;
		}

		JScrollPane regionScrollPane = new JScrollPane(regionCheckBoxPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar regionScrollBar = regionScrollPane.getVerticalScrollBar();
		regionScrollBar.setUnitIncrement(regionScrollBar.getUnitIncrement() * 4);
		regionScrollPane.setPreferredSize(new Dimension(maxCheckBoxWidth + regionScrollBar.getPreferredSize().width * 2,
				Toolkit.getDefaultToolkit().getScreenSize().height / 3));
		outerRegionSelectPanel.add(regionScrollPane, BorderLayout.CENTER);
		settingsPanel.add(outerRegionSelectPanel);
		
		settingsPanel.add(Box.createHorizontalStrut(5));

		JPanel outerAdditonalOptionsPanel = new JPanel();
		outerAdditonalOptionsPanel.setLayout(new BoxLayout(outerAdditonalOptionsPanel, BoxLayout.Y_AXIS));
		JLabel additionalSettingsLabel = new JLabel("Additional Settings", JLabel.CENTER);
		additionalSettingsLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		outerAdditonalOptionsPanel.add(additionalSettingsLabel);

		JPanel additionalOptionsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
		
		additionalOptionsPanel.add(new JLabel("Cargo Capacity"));
		cargoCapacitySpinner = new JSpinner(new SpinnerNumberModel(5000, 1, Integer.MAX_VALUE, 1));
		cargoCapacitySpinner.addChangeListener((ChangeEvent) -> {
			int value = (int) cargoCapacitySpinner.getValue();
			stagedSettings.put("cargo_capacity", value);
		});
		cargoCapacitySpinner.getChangeListeners()[0].stateChanged(null);
		additionalOptionsPanel.add(cargoCapacitySpinner);
		
		additionalOptionsPanel.add(new JLabel("Maximum Investment"));
		maximumInvestmentSpinner = new JSpinner(new SpinnerNumberModel(5000000, 1, Integer.MAX_VALUE, 1));
		maximumInvestmentSpinner.addChangeListener((ChangeEvent) -> {
			int value = (int) maximumInvestmentSpinner.getValue();
			stagedSettings.put("max_invest", value);
		});
		maximumInvestmentSpinner.getChangeListeners()[0].stateChanged(null);
		additionalOptionsPanel.add(maximumInvestmentSpinner);
		
		additionalOptionsPanel.add(new JLabel("Minimum Security Level"));
		minSecuritySpinner = new JSpinner(new SpinnerNumberModel(0.5, -1, 1, 0.1));
		minSecuritySpinner.setEditor(new JSpinner.NumberEditor(minSecuritySpinner,"0.0"));
		minSecuritySpinner.addChangeListener((ChangeEvent) -> {
			double value = (double) minSecuritySpinner.getValue();
			stagedSettings.put("min_security", value);
		});
		minSecuritySpinner.getChangeListeners()[0].stateChanged(null);
		additionalOptionsPanel.add(minSecuritySpinner);
		additionalOptionsPanel.add(new JLabel("Distance Calculation Mode"));
		distanceCalcModeSelector = new JComboBox<String>(new String[] { "Shortest Route", "Safest Route" });
		distanceCalcModeSelector.addActionListener((ActionEvent) -> {
			String value = (String) distanceCalcModeSelector.getSelectedItem();
			stagedSettings.put("dist_calc_mode", value.equals("Shortest Route") ? "shortest" : "secure");
		});
		distanceCalcModeSelector.getActionListeners()[0].actionPerformed(null);
		additionalOptionsPanel.add(distanceCalcModeSelector);
		outerAdditonalOptionsPanel.add(additionalOptionsPanel);

		for (int i = 0; i < 100; i++) {
			outerAdditonalOptionsPanel.add(Box.createVerticalGlue());
		}

		settingsPanel.add(outerAdditonalOptionsPanel);

		outerPanel.add(settingsPanel, BorderLayout.CENTER);

		JPanel bottomButtonPanel = new JPanel();
		bottomButtonPanel.setLayout(new BoxLayout(bottomButtonPanel, BoxLayout.X_AXIS));
		bottomButtonPanel.add(Box.createHorizontalGlue());

		JButton acceptSettingsButton = new JButton("Accept");
		acceptSettingsButton.addActionListener((ActionEvent) -> {
			if (confirmCall.confirm(stagedSettings)) {
				applySettings();
				setVisible(false);
			} else {
				resetSettings();
			}
		});
		bottomButtonPanel.add(acceptSettingsButton);
		bottomButtonPanel.add(Box.createHorizontalGlue());
		JButton cancelSettingsButton = new JButton("Cancel");
		cancelSettingsButton.addActionListener((ActionEvent) -> {
			setVisible(false);
			resetSettings();
		});
		bottomButtonPanel.add(cancelSettingsButton);
		bottomButtonPanel.add(Box.createHorizontalGlue());

		outerPanel.add(bottomButtonPanel, BorderLayout.SOUTH);

		this.add(outerPanel, BorderLayout.CENTER);
		this.pack();

		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				resetSettings();
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}
		});

		applySettings();
	}

	private void applySettings() {
		currentSettings = Util.copy(stagedSettings);
	}

	private void resetSettings() {
		for (JCheckBox curRegionCheckBox : regionCheckBoxes) {
			curRegionCheckBox.setSelected(currentSettings.getBoolean(curRegionCheckBox.getText()));
		}

		stagedSettings = Util.copy(currentSettings);
	}

	public JSONObject getCurentSettings() {
		return currentSettings;
	}

	interface ConfirmSettingsChange {
		boolean confirm(JSONObject stagedSettings);
	}
}
