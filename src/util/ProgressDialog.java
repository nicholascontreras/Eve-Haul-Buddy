package util;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * @author Nicholas Contreras
 */

public class ProgressDialog {

	private final JDialog dialog;
	private final JProgressBar progressBar;
	private final JButton cancelButton;

	private double preciseProgress;

	private boolean taskCancelled;

	public ProgressDialog(Window parent, String title, String message, boolean moveable) {
		dialog = new JDialog(parent, title, ModalityType.DOCUMENT_MODAL);
		dialog.setIconImage(parent.getIconImages().get(0));
		dialog.setUndecorated(!moveable);

		JPanel outerPanel = new JPanel(new BorderLayout(5, 5));
		outerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		outerPanel.add(new JLabel(message), BorderLayout.NORTH);
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		outerPanel.add(progressBar, BorderLayout.CENTER);

		Box cancelButtonBox = new Box(BoxLayout.X_AXIS);
		cancelButtonBox.add(Box.createHorizontalGlue());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((ActionEvent) -> {
			taskCancelled = true;
			dialog.dispose();
		});
		cancelButtonBox.add(cancelButton);
		cancelButtonBox.add(Box.createHorizontalGlue());
		outerPanel.add(cancelButtonBox, BorderLayout.SOUTH);
		dialog.add(outerPanel);

		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parent);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		if (moveable) {
			dialog.addWindowListener(new WindowListener() {
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
					taskCancelled = true;
					dialog.dispose();
				}

				@Override
				public void windowClosed(WindowEvent e) {
				}
			});
		}
	}

	public void show() {
		dialog.setVisible(true);
	}

	public void setProgress(double progress) {
		preciseProgress = progress;
		updateProgress();
	}

	public void changeProgress(double deltaProgress) {
		preciseProgress += deltaProgress;
		updateProgress();
	}
	
	public double getProgress() {
		return preciseProgress;
	}

	private void updateProgress() {
		SwingUtilities.invokeLater(() -> progressBar.setValue((int) (preciseProgress * 100)));
	}

	public void setIndeterminateMode(boolean b) {
		progressBar.setIndeterminate(b);
		progressBar.setStringPainted(!b);
	}

	public boolean isCancelled() {
		return taskCancelled;
	}

	public void remove() {
		dialog.dispose();
	}
}
