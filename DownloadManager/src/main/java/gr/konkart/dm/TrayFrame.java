package gr.konkart.dm;

import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

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
		
		getContentPane().setLayout(null);
            
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 314, 189);
		getContentPane().add(scrollPane);
            
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
            
		scrollPane.setViewportView(table);
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
