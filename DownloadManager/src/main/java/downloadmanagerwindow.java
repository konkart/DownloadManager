import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

public class downloadmanagerwindow {
	JPopupMenu popup;
	static Image image = Toolkit.getDefaultToolkit().getImage("C:\\Users\\wcwra\\Desktop\\emojis\\icon.png");

	static TrayIcon trayIcon = new TrayIcon(image, "Tester2");
	static JFrame frame;

	static TrayFrame trayframe;
	private JTextField textField;
	private JTable table_1;
	private JTable table_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					downloadmanagerwindow window = new downloadmanagerwindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		if (SystemTray.isSupported()) {
		      SystemTray tray = SystemTray.getSystemTray();
		      final PopupMenu popup = new PopupMenu();
		      
		      MenuItem exitItem = new MenuItem("Exit");
		      MenuItem openmain = new MenuItem("Open");
		      popup.add(openmain);
		      popup.add(exitItem).addActionListener(e-> {
		    	  frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
		    	  System.exit(0);
		      });
		      
		      trayIcon.setImageAutoSize(true);
		      trayIcon.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		          System.out.println("In here");
		          frame.setVisible(true);
		          //trayIcon.displayMessage("Tester!", "Some action performed", TrayIcon.MessageType.INFO);
		        }
		      });
		      trayIcon.addMouseListener(new MouseAdapter() {
		    	    public void mouseClicked(MouseEvent e) {
		    	    	int modifiers = e.getModifiers();
		    	    	
		    	        if ( (modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
		    	        	if (e.getClickCount() == 1) {
		    	        	
		    	        			
		    	        		trayframe.setVisible(true);
		    	        	
		    	        	}
		    	        }
		    	    }
		    	}); 
		      trayIcon.setPopupMenu(popup);
		      try {
		        tray.add(trayIcon);
		      } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		      }
		    }
		
	}
	
	/**
	 * Create the application.
	 */
	public downloadmanagerwindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 387);
		frame.setDefaultCloseOperation(JFrame.ICONIFIED);
		frame.getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(10, 11, 365, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.setBounds(385, 10, 89, 23);
		frame.getContentPane().add(btnDownload);
		
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 42, 464, 295);
		frame.getContentPane().add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Direct", null, panel, null);
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 23, 459, 244);
		panel.add(scrollPane);
		
		table_1 = new JTable();
		table_1.setRowHeight(30);
		table_1.setShowGrid(false);
		table_1.setShowVerticalLines(false);
		table_1.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
					"Name", "Progress", "Size", "Date"
			}
		));
		scrollPane.setViewportView(table_1);
		DefaultTableModel model = (DefaultTableModel) table_1.getModel();
		model.addRow(new Object[]{"Column 1", "Column 2", "Column 3","Column 4"});
		
		JToggleButton tglbtnNewToggleButton = new JToggleButton("Speed Limit");
		tglbtnNewToggleButton.setBounds(365, 0, 94, 23);
		panel.add(tglbtnNewToggleButton);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		tabbedPane.addTab("Torrents", null, scrollPane_1, null);
		
		table_2 = new JTable();
		table_2.setRowHeight(30);
		table_2.setShowVerticalLines(false);
		table_2.setShowGrid(false);
		table_2.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Name", "Progress", "Size", "Date"
			}
		));
		scrollPane_1.setViewportView(table_2);
		
		

	    //...where the GUI is constructed:
	    //Create the popup menu.
	    popup = new JPopupMenu();
	    JMenuItem menuItem = new JMenuItem("A popup menu item");
	    menuItem.addActionListener(null);
	    popup.add(menuItem);
	    menuItem = new JMenuItem("Another popup menu item");
	    menuItem.addActionListener(null);
	    popup.add(menuItem);

	    //Add listener to components that can bring up popup menus.
	    MouseListener popupListener = new PopupListener();
	    table_1.addMouseListener(popupListener);
	    trayframe = new TrayFrame();
	    trayframe.setVisible(false);
        trayframe.addR(new Object[]{"Column 1", "Column 2", "Column 3","Column 4"});
	   
		}	
	
	
	
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
	/*---------------------*/
}

