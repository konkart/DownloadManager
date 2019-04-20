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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

public class downloadmanagerwindow {
	JPopupMenu popup; 
	static ClassLoader classLoader = downloadmanagerwindow.class.getClassLoader();
	static String path  = classLoader.getResource("NekoAtsumeFace.png").getPath();
	static Image image = Toolkit.getDefaultToolkit().getImage(path);
	public int DownloadID = 0;
	static TrayIcon trayIcon = new TrayIcon(image, "Tester2");
	static JFrame frame;
	public DownloadFile df[] = new  DownloadFile[0];
	static TrayFrame trayframe;
	private JTextField textField;
	private JTable table_1;
	private JTable table_2;
	static downloadmanagerwindow window;
	private DefaultTableModel model;
	JMenuItem pause;
	JMenuItem resume;
	public static void main(String[] args) {
		System.setProperty("http.agent", "Chrome");
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					window = new downloadmanagerwindow();
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
	

	public downloadmanagerwindow() {
		initialize();
	}

/*χτισιμο του GUI*/
	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 387);
		frame.setDefaultCloseOperation(JFrame.ICONIFIED);
		frame.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 42, 464, 295);
		frame.getContentPane().add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Direct", null, panel, null);
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 23, 459, 244);
		panel.add(scrollPane);
		
		trayframe = new TrayFrame();
	    trayframe.setVisible(false);
        //trayframe.addR(new Object[]{"Column 1", "Column 2"});
        
		table_1 = new JTable();
		table_1.setRowHeight(30);
		table_1.setShowGrid(false);
		table_1.setShowVerticalLines(false);
		table_1.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"ID", "Name", "Progress", "Size", "Date"
			}
		));
		table_1.getColumnModel().getColumn(0).setPreferredWidth(27);
		scrollPane.setViewportView(table_1);
		model = (DefaultTableModel) table_1.getModel();
		table_1.setDefaultEditor(Object.class, null);
		//model.addRow(new Object[]{"Column 1", "Column 2", "Column 3","Column 4"});
		
		textField = new JTextField();
		textField.setBounds(10, 11, 365, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.setBounds(385, 10, 89, 23);
		frame.getContentPane().add(btnDownload);
		/*κουμπί dowload*/
		btnDownload.addActionListener(new ActionListener()
		{
		  public void actionPerformed(ActionEvent e)
		  {
			String ls_FileLoc;
			int li_TotalConnections = 5;
			int li_BufferLen = 4;
			if (textField.getText().equals("")){
				JOptionPane.showMessageDialog(
			             null,"URL is Invalid or Empty.Please enter valid URL",
         				 "ERROR",JOptionPane.ERROR_MESSAGE);

				return;
			}
			ls_FileLoc = textField.getText();
			try {
				String ext = getContentTypeA.gContentTypeA(ls_FileLoc);
				System.out.println(ext);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String[] item={""+DownloadID+"",ls_FileLoc,"B","C",getDateTime()};
			String[] itemTray={ls_FileLoc,"B"};
			
			model.addRow(item);
			trayframe.modelTray.addRow(itemTray);
			/*li_TotalConnections = Integer.parseInt((String)cmbConnection.getSelectedItem()); 
			li_BufferLen = Integer.parseInt((String)cmbMemory.getSelectedItem());*/ 
			
			/*νέο download και πρόσθεση στον πίνακα*/
		    DownloadFile download = new DownloadFile(DownloadID,ls_FileLoc,li_TotalConnections,li_BufferLen);
		    addDownload(download);
			
				df[DownloadID].start();
				Monitor dMonitor = new Monitor(window,DownloadID);
				dMonitor.start();
				DownloadID = DownloadID + 1;
				textField.setText("");
				
		  }
		});
		
		/*---------------------*/
		
		/*το κουμπί Speed limit δεν κάνει ακόμα κάτι*/
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
					"ID","Name", "Progress", "Size", "Date"
			}
		));
		scrollPane_1.setViewportView(table_2);
		
	
		popup = new JPopupMenu();
	    pause = new JMenuItem("Pause");
	    popup.add(pause);
	    resume = new JMenuItem("resume");
	    
	    popup.add(resume);
	    MouseListener popupListener = new PopupListener();
	    table_1.addMouseListener(popupListener);
	    
	   
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
	        	//PAUSE DOWNLOAD BUTTON ACTION
	        	
	    	    pause.addActionListener(new ActionListener()
	    		{
	    			  public void actionPerformed(ActionEvent e)
	    			  {
	    				  int column = 0;
	    				  int row = table_1.getSelectedRow();
	    				  String value = table_1.getModel().getValueAt(row, column).toString();
	    				  System.out.println(value);
	    				  df[Integer.parseInt(value)].PauseDownload();
	    			  }
	    		});
	    	    resume.addActionListener(null);
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	        }
	    }
	}
	private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }
	/*---------------------*/
	//Monitor για κάθε λήψη
	class Monitor extends Thread{

			downloadmanagerwindow gui;
		   int threadIndex;
		   
		   public Monitor(downloadmanagerwindow dm_frame,int idx){
		   	gui=dm_frame;
		   	threadIndex = idx;
		  	
		   }

		   public void run(){
		   int dThreadCount=0;
		   int currThread=0;
		   int dconnections=0;
		   int Downloadcomplete;
		   String[] files;
		   FileUtils futils = new FileUtils();

		   currThread= threadIndex;
		   //gui.updateStatus(currThread);
		   if (gui.df[currThread].FileSize == -1){
		   		gui.df[currThread].Complete =-1;
				JOptionPane.showMessageDialog(
				      	 null,"Download Failed.Try Downloading Again or verify URL is Correct" ,
              		 "INFORMATION",JOptionPane.INFORMATION_MESSAGE);
              gui.updateStatus(currThread,true);
              		 				
		   		}
		   			
		   
		   while(gui.df[currThread].Complete == 0) //Monitor Loop για το download
		   {
		   			
		   				dconnections = gui.df[currThread].TotConnections;
		   				//Τσεκ για αν τέλειωσε το κατέβασμα και αν οι συνδέσεις είναι ακόμα ενεργές
		   				if (gui.df[currThread].Complete == 0 && gui.df[currThread].ActiveSubConn == dconnections ){
		   				
		   				gui.updateStatus(currThread,false);
		   				
		   				
		   				
		   				Downloadcomplete = 1;//Flag
		   				files =  new String[dconnections]; //Πίνακας με τα ονόματα των thread
		   				
		   				
		   				for(int subDown = 0; subDown < dconnections ; subDown ++){
		   					
		   				    files[subDown]= gui.df[currThread].getSubDownId(subDown);
		   				   		
		   				   		//Βεβαιωση ότι δεν έχει ολοκληρωθεί το κατέβασμα
		   				   		if(gui.df[currThread].isSubDownComplete(subDown) == 0){
		   				      	
		   				      		Downloadcomplete = 0; //Download Incomplete
		   				      		
		   				      		break;
		   				      		
		   				      	}
		   				      
		   				     }
		   				      
		   				if (Downloadcomplete == 1 && gui.df[currThread].getPause() == false)
		   					{
		   					System.out.println(Downloadcomplete);
		   					//Ένωση των κατεβασμένων αρχείων και διαγραφή των "κωμματιων"
								try {
									futils.concat(files,gui.df[currThread].FilePath);
								} catch (IOException e) {
									e.printStackTrace();
								}
							for(int fileid=0;fileid < dconnections;fileid++) {
		   						try {
									futils.delete(files[fileid]);
								} catch (IOException e) {
									e.printStackTrace();
								}
		   					}
		   					gui.df[currThread].Complete = 1; 
		   					gui.updateStatus(currThread,false);//Ενημέρωση GUI
		   					
		   					}     
		   				  
		   				  }
		   		}
			}
	}
	
	//Method to update our data
	public void updateStatus( int currThread, boolean dFailed){
		if (!dFailed){
		//TODO FILESIZE
		this.model.setValueAt(String.valueOf(this.df[currThread].DownloadProgress())+"%",currThread,2);	
   		this.model.setValueAt(this.df[currThread].getBytesDownloaded(),currThread,3);
   		//TODO DOWNLOAD SPEED
   		downloadmanagerwindow.trayframe.modelTray.setValueAt(String.valueOf(this.df[currThread].DownloadProgress())+"%",currThread,1);
		}
		else {
		String str="Failed";
		//this.model.setValueAt((Object)str,currThread,1);
		this.model.setValueAt((Object)str,currThread,2);
   		this.model.setValueAt((Object)str,currThread,3);
   		//this.model.setValueAt((Object)str,currThread,4);
   		downloadmanagerwindow.trayframe.modelTray.setValueAt((Object)str,currThread,1);
		}


}
	
	//Πρόσθεση λήψης στον πίνακα ληψεων
	public void addDownload(DownloadFile d) {
	       
        DownloadFile[] dab = new DownloadFile[df.length+1];
        System.arraycopy(df, 0, dab, 0, df.length);
        dab[dab.length-1] = d;
        df = dab;
        
    	}
}

