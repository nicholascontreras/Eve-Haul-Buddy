package main;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import engine.HaulJob;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class HaulJobTable extends JTable {

	private final HaulJobTableModel hjtm;

	private final ArrayList<HaulJob> jobsDisplayed;

	public HaulJobTable() {
		hjtm = new HaulJobTableModel();
		this.setModel(hjtm);

		String[] columnNames = new String[] { "Product Name", "Purchase Location", "Purchase Price", "Selling Location",
				"Selling Price", "Num of Units", "Total Profit", "Num of Jumps", "Profit per Jump" };
		for (String s : columnNames) {
			hjtm.addColumn(s);
		}

		this.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		this.setRowSelectionAllowed(true);
		this.setColumnSelectionAllowed(false);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setAutoCreateRowSorter(true);
		this.getColumnModel().getColumn(2).setCellRenderer(new DecimalFormatRenderer());
		this.getColumnModel().getColumn(4).setCellRenderer(new DecimalFormatRenderer());

		HaulJobTable me = this;
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getButton() == MouseEvent.BUTTON3) {
					int row = me.rowAtPoint(evt.getPoint());
					int col = me.columnAtPoint(evt.getPoint());
					System.out.println(row + ", " + col);
					if (row >= 0 && col >= 0 && row <= hjtm.getRowCount() && col <= hjtm.getColumnCount()) {
						String toCopy = hjtm.getValueAt(row, col).toString();
						StringSelection stringSelection = new StringSelection(toCopy);
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(stringSelection, null);
					}
				}
			}
		});

		jobsDisplayed = new ArrayList<HaulJob>();
	}

	public void setColumnStartingWidths() {
		float[] columnWidthPercentage = { 0.15f, 0.225f, 0.05f, 0.225f, 0.05f, 0.05f, 0.1f, 0.05f, 0.1f };

		int tW = this.getColumnModel().getTotalColumnWidth();
		TableColumn column;
		TableColumnModel jTableColumnModel = this.getColumnModel();
		int cantCols = jTableColumnModel.getColumnCount();
		for (int i = 0; i < cantCols; i++) {
			column = jTableColumnModel.getColumn(i);
			int pWidth = Math.round(columnWidthPercentage[i] * tW);
			column.setPreferredWidth(pWidth);
		}
	}

	public void addHaulJob(HaulJob haulJob) {
		if (!jobsDisplayed.contains(haulJob)) {
			SwingUtilities.invokeLater(() -> {
				synchronized (jobsDisplayed) {
					jobsDisplayed.add(haulJob);
					hjtm.addRow(haulJob.seperateForTable());
				}
			});
		}
	}

	public void removeStaleHaulJobs(HashSet<HaulJob> haulJobs) {

		ArrayList<Integer> rowsToRemove = new ArrayList<Integer>();
		for (int i = jobsDisplayed.size() - 1; i >= 0; i--) {
			if (!haulJobs.contains(jobsDisplayed.get(i))) {
				rowsToRemove.add(i);
			}
		}

		try {
			for (int rowToRemove : rowsToRemove) {
				SwingUtilities.invokeAndWait(() -> {
					hjtm.removeRow(rowToRemove);
					jobsDisplayed.remove(rowToRemove);
				});
			}

		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class HaulJobTableModel extends DefaultTableModel {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return String.class;
			case 1:
				return String.class;
			case 2:
				return Double.class;
			case 3:
				return String.class;
			case 4:
				return Double.class;
			default:
				return Integer.class;
			}
		}
	}

	private static class DecimalFormatRenderer extends DefaultTableCellRenderer {
		private static final DecimalFormat formatter = new DecimalFormat("#.00");

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// First format the cell value as required
			value = formatter.format((Number) value);
			// And pass it on to parent class
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
}
