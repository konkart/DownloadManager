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

public TrayFrame() {
		setType(Type.UTILITY);
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.ICONIFIED);
		setBounds(100, 100, 340, 240);
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
    	        .getMaximumWindowBounds();
    	Point location = MouseInfo.getPointerInfo().getLocation();
    	
    	 if (location.x + this.getWidth() > screen.x + screen.width) {
    		 location.x = screen.x + screen.width - this.getWidth();
            }
            if (location.x < screen.x) {
            	location.x = screen.x;
            }

            if (location.y + this.getHeight() > screen.y + screen.height) {
            	location.y = screen.y + screen.height - this.getHeight();
            }
            if (location.y < screen.y) {
            	location.y = screen.y;
            }
            this.setLocation(location);
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
            
	}
	public void addR(Object object) {
		modelTray.addRow(new Object[]{"Column 1", "Column 2", "Column 3","Column 4"});
		
	}
	public void setValueAt(String string, int currThread, int i) {
	}
}
