package gr.konkart.dm;

import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class TrayFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	JTable table;
	protected DefaultTableModel modelTray;
	Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	public TrayFrame() {
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.ICONIFIED);
		setBounds(100, 100, 340, 240);
            
		JScrollPane scrollPane = new JScrollPane();
            
		table = new JTable();
		table.setRowHeight(30);
		table.setShowVerticalLines(false);
		table.setShowGrid(false);
		table.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						"Name", "Progress"
				}
				));
		table.getColumnModel().getColumn(0).setPreferredWidth(135);
		modelTray = (DefaultTableModel) table.getModel();
		table.setDefaultEditor(Object.class, null);
		scrollPane.setViewportView(table);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
					.addContainerGap())
		);
		getContentPane().setLayout(groupLayout);
		pack();
		setVisible(true);
		setToBottomRight();
	}
	
	/* screen position check to always place tray window near tray*/
	public void setToBottomRight() {
		Point location = MouseInfo.getPointerInfo().getLocation();
		
		if (location.x + this.getWidth() > screen.width) {
			location.x = screen.width - this.getWidth();
		}
		if (location.y + this.getHeight() > screen.height) {
			location.y = screen.height - this.getHeight();
		}
		this.setLocation(location);
	}
}
