package tritechplugins.detect.veto.polygon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamNorthPanel;

public class PolygonVetoDialogPanel implements PamDialogPanel {

	private JPanel mainPanel;
	
	private JPanel tablePanel;
	
	private JTable table;
	
	private PointsTableModel tableModel;
	
	private JButton addButton, removeButton;
	
	private ArrayList<Point2D.Double> polyPoints = new ArrayList<>();

	private PolygonVeto polygonVeto;
	
	public PolygonVetoDialogPanel(PolygonVeto polygonVeto) {
		this.polygonVeto = polygonVeto;
		
		mainPanel = new JPanel(new BorderLayout());
		tablePanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, tablePanel);
		JPanel buttonPanel = new PamNorthPanel();
		mainPanel.add(BorderLayout.EAST, buttonPanel);
		
		tableModel = new PointsTableModel();
		table = new JTable(tableModel);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.setBorder(new TitledBorder("Polygon points"));
		tablePanel.add(BorderLayout.CENTER, scrollPane);
		Dimension pSize = scrollPane.getPreferredSize();
		if (pSize != null) {
			Font anyFont = buttonPanel.getFont();
			if (anyFont != null) {
				pSize.width = buttonPanel.getFontMetrics(anyFont).getMaxAdvance() * 15;
			}
			else {
				pSize.width = 150;
			}
			scrollPane.setPreferredSize(pSize);
		}
		
		addButton = new JButton("Insert");
		removeButton = new JButton("Remove");
		buttonPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		buttonPanel.add(addButton, c);
		c.gridy++;
		buttonPanel.add(removeButton, c);
			
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addRow();
			}
		});
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeRow();
			}
		});
		
	}

	protected void removeRow() {
		int selRow = table.getSelectedRow();
		if (selRow >= 0) {
			polyPoints.remove(selRow);
		}
		tableModel.fireTableDataChanged();		
	}

	protected void addRow() {
		int selRow = table.getSelectedRow();
		if (selRow < 0) {
			polyPoints.add(null);
		}
		else {
			polyPoints.add(selRow+1, null);
		}
		tableModel.fireTableDataChanged();
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		PolygonParams params = polygonVeto.getParams();
		if (params.getNumPoints() == 0) {
			return;
		}
		double xx[] = params.getX();
		double yy[] = params.getY();
		for (int i = 0; i < xx.length; i++) {
			polyPoints.add(new Point2D.Double(xx[i], yy[i]));
		}
		tableModel.fireTableDataChanged();
	}

	@Override
	public boolean getParams() {
		deEditTable();
		//  check for null entries and that the path doesn't cross itself. 
		Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD, polyPoints.size());
		for (Point2D.Double aPoint : polyPoints) {
			if (aPoint == null) {
				return PamDialog.showWarning(null, "Invalid point", "null rows are not allowed");
			}
			path.moveTo(aPoint.x, aPoint.y);
		}
		if (polyPoints.size() < 3) {
			return PamDialog.showWarning(null, "Invalid polygon", "at least three points are required");
		}
		// can't find a function to see if the path crosses itself. 
		// guess I could take a segment at a time and see if it crosses other segments ? 
		
		// ok here, so make two arrays and set them inparams. 
		PolygonParams params = polygonVeto.getParams();
		double xx[] = new double[polyPoints.size()];
		double yy[] = new double[polyPoints.size()];
		for (int i = 0; i < polyPoints.size(); i++) {
			Point2D.Double aPoint = polyPoints.get(i);
			xx[i] = aPoint.x;
			yy[i] = aPoint.y;
		}
		params.setX(xx);
		params.setY(yy);
		
		return true;
	}

	/**
	 * Try to force table to accept half edited text. 
	 */
	private void deEditTable() {
		TableCellEditor ce = table.getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
			int eRow = table.getEditingRow();
			int eCol = table.getEditingColumn();
			if (eRow >= 0) {
			table.setValueAt(ce.getCellEditorValue(), eRow, eCol);
			}
			tableModel.fireTableDataChanged();
		}
		
	}

	private class PointsTableModel extends AbstractTableModel {

		private String[] colNames = {"x (m)", "y (m)"};
		
		@Override
		public int getRowCount() {
			return polyPoints.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getValueAt(int rowIndex, int columnIndex) {
			Point2D.Double point = polyPoints.get(rowIndex);
			if (point == null) {
				return null;
			}
			switch (columnIndex) {
			case 0:
				return String.format("%3.2f", point.getX());
			case 1:
				return String.format("%3.2f", point.getY());
			}
			return null;
		}
		
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (aValue instanceof String == false) {
				return;
			}
			double value;
			try {
				value = java.lang.Double.valueOf((String) aValue);
			}
			catch (NumberFormatException e) {
				return;
			}
			Point2D.Double point = polyPoints.get(rowIndex);
			if (point == null) {
				point = new Point2D.Double(0,0);
				polyPoints.set(rowIndex, point);
			}
			switch (columnIndex) {
			case 0:
				point.x = value;
				break;
			case 1:
				point.y = value;
				break;
			}
			
			
		}
		
	}
}
