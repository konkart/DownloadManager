package gr.konkart.dm;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Desktop;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class DownloadManager {
	JPopupMenu popup; 
	static String path = System.getProperty("user.dir") + "\\image\\icon.png";
	static Image image = Toolkit.getDefaultToolkit().getImage(path);
	public static int downloadID = 0 , torrentID = 0;
	private static int tDownID = 0 , tTorID = 0;
	static TrayIcon trayIcon = new TrayIcon(image, "Download Manager");
	static JFrame frame;
	public ArrayList<Download> df = new ArrayList<Download>(1000);
	public ArrayList<Torrent> tr = new ArrayList<Torrent>(1000);
	public ArrayList<Schedule> sc = new ArrayList<Schedule>();
	public static Map<Download, Schedule> Dmap = new HashMap<>();
	public static Map<Torrent, Schedule> Tmap = new HashMap<>();
	static TrayFrame trayframe;
	private JTextField textField;
	private JTable table_1,table_2;
	public String fileLoc;
	static DownloadManager window;
	public static DefaultTableModel model,model2;
	public boolean rateState=false;
	private static DatabaseHandler db = new DatabaseHandler();
	JMenuItem pause,remove,resume,delete,scheduleCancel,open,copyToCl,move,
		openFolder,mp4,webm,avi,flv,mp3,ogg,acc,wav,jpg,gif,bmp,png;
	JMenu convertMenu;
	JTabbedPane tabbedPane;
	static String homeDefault = System.getProperty("user.home")+"\\Downloads\\";
	JComboBox<Object> speedCmb;
	volatile double speedLimitNumber=0;
	volatile int active = 0;
	int scheduled = 0 , trayFrameRow = 0;
	ExecutorService pool = Executors.newCachedThreadPool();
	public static void main(String[] args) throws IOException {
		System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					window = new DownloadManager();
					if ( new File(System.getProperty("user.dir")+"\\Downloads.db").exists()==false ) {
						db.createDB();
					}
					ArrayList<String[]> t = db.importDownloadDir(window);
					for (String[] t1 : t) {
						model.addRow(t1);
					}
					downloadID = db.getDownCount();
					tDownID = db.getDownID();
					ArrayList<String[]> t1 = db.importDownloadTor(window);
					for (String[] t2 : t1) {
						model2.addRow(t2);
					}
					torrentID = db.getTorCount();
					tTorID = db.getTorID();
					String tmpFolder;
					tmpFolder = db.getDefaultFolder();

					if (tmpFolder!=null) {
						homeDefault = tmpFolder;
						
					}
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//set ups and calls the tray icon and creates the trayframe object
		SystemTray tray = SystemTray.getSystemTray();
		final PopupMenu popup = new PopupMenu();
		      
		MenuItem exitItem = new MenuItem("Exit");
		MenuItem openmain = new MenuItem("Open");
		popup.add(openmain).addActionListener(e-> {
			frame.setVisible(true);
		});
		popup.add(exitItem).addActionListener(e-> {
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			System.exit(0);
		});
			      
		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(true);
			}
		});
		trayIcon.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int modifiers = e.getModifiers();
		    	    	
				if ( (modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
					if (e.getClickCount() == 1) {
		    	        	
						trayframe.setToBottomRight();
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

	

	public DownloadManager() {
		initialize();
	}

	/*
	 * main window init
	 */
	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 520, 438);
		frame.setDefaultCloseOperation(JFrame.ICONIFIED);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Direct", null, panel, null);
		
		JScrollPane scrollPane = new JScrollPane();
		
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
				"ID", "Name", "Progress", "Size", "Date", "URL"
			}
		));
		table_1.getColumnModel().getColumn(0).setPreferredWidth(20);
		scrollPane.setViewportView(table_1);
		model = (DefaultTableModel) table_1.getModel();
		table_1.setDefaultEditor(Object.class, null);
		
		textField = new JTextField();
		textField.setColumns(10);
		
		JButton btnDownload = new JButton("Download");
		
		//creates the download and monitor objects from the given url,adds the download to the main window's table and on the trayframe
		btnDownload.addActionListener(new ActionListener()
		{
		  public void actionPerformed(ActionEvent e)
		  {
			
			
			if (textField.getText().equals("")){
				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);

				return;
			}
			btnDownload.setEnabled(false);
			String type = URLHandler.getUriType(textField.getText().trim());
			if(type=="URL") {
				fileLoc = textField.getText().trim();
				textField.setText("");
				String fileN = URLHandler.getFilename(fileLoc);
				if (new File(homeDefault+fileN).exists()==false) {
					String[] item={""+tDownID+"",fileN,"0%  0 KB/s","0",getDateTime(),fileLoc};
					String[] itemTray={fileN,"0%"};
					model.addRow(item);
					trayframe.modelTray.addRow(itemTray);
					/*-------OOOOO-------*/
					Download download = new Download(tDownID,fileLoc,fileN,homeDefault,trayFrameRow);
					df.add(download);
					Monitor dMonitor = new Monitor(window,downloadID,type,trayFrameRow);
					db.insertDownloadDir(df, downloadID);
					
					pool.execute(df.get(downloadID));
					pool.execute(dMonitor);
					downloadID = downloadID + 1;
					active = active + 1;
					tDownID = tDownID + 1;
					trayFrameRow = trayFrameRow + 1;
					if (rateState==true) {
						downloadSpeedLimit(speedLimitNumber);
					}
				} else {
					int dialogResult = JOptionPane.showConfirmDialog (null, "File already exists,open on explorer?","Warning",JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.YES_OPTION) {
						openFolderS(homeDefault , fileN);
					}
				}
				/*--------------*/
			} else if (type=="Torrent") {
				fileLoc = textField.getText().trim();
				textField.setText("");
				String NameTo = fileLoc.split("&dn=")[1].split("&")[0];
				try {
					NameTo = URLDecoder.decode(NameTo,StandardCharsets.UTF_8.name()).replaceAll("[^a-zA-Z0-9]+","");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				if (new File(homeDefault+NameTo+"\\").exists()==false) {
					String[] item={""+tTorID+"",NameTo,"0%  0 KB/s","Starting",getDateTime(),fileLoc,homeDefault+NameTo+"\\"};
					String[] itemTray={NameTo,"0%"};
					
					model2.addRow(item);
					trayframe.modelTray.addRow(itemTray);
					Monitor dMonitor = new Monitor(window,torrentID,type,trayFrameRow);
					Torrent to = new Torrent(tTorID,fileLoc,NameTo,homeDefault,trayFrameRow);
					tr.add(to);
					db.insertDownloadTor(tr,torrentID);
					
					pool.execute(tr.get(torrentID));
					pool.execute(dMonitor);
					torrentID = torrentID + 1;
					tTorID = tTorID + 1;
					trayFrameRow = trayFrameRow + 1;
				} else {
					int dialogResult = JOptionPane.showConfirmDialog (null, "Folder already exists,open on explorer?","Warning",JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.YES_OPTION) {
						openFolderS(homeDefault , NameTo);
					}
				}
				
			} else {
				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);
			}
			btnDownload.setEnabled(true);	
		  }
		});
		
		/*---------------------*/
		JSpinner amoun = new JSpinner();
		amoun.setModel(new SpinnerNumberModel(new Double(0), new Double(0), null, new Double(1)));
		amoun.setUI(new BasicSpinnerUI() {
            protected Component createNextButton() {
                return null;
            }

            protected Component createPreviousButton() {
                return null;
            }
        });
		
		JToggleButton speedLimitBut = new JToggleButton("Speed Limit");
		//Button that sets the state of the ratelimit and calls the downloadSpeedLimit
		speedLimitBut.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange()==ItemEvent.SELECTED){
					speedLimitNumber = (double) amoun.getValue();
					if (speedCmb.getSelectedItem().equals("KB/s")) {
						speedLimitNumber = Math.round(1000*speedLimitNumber);
					} else if (speedCmb.getSelectedItem().equals("MB/s")) {
						speedLimitNumber = Math.round(1000000*speedLimitNumber);
					}
					downloadSpeedLimit(speedLimitNumber);
					rateState=true;
				} else if (ev.getStateChange()==ItemEvent.DESELECTED){
					downloadSpeedLimit(0);
					rateState=false;

				}
			}
		});
		
		speedCmb = new JComboBox<Object>();
		speedCmb.setModel(new DefaultComboBoxModel<Object>(new String[] {"KB/s", "MB/s"}));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap(290, Short.MAX_VALUE)
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(44)
							.addComponent(speedCmb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(amoun, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
					.addGap(2)
					.addComponent(speedLimitBut, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
					.addGap(13))
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
					.addGap(1))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(2)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(speedCmb, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(amoun, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)))
						.addComponent(speedLimitBut))
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		
		
		
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
					"ID","Name", "Progress", "Session bytes", "Date" , "Magnet"
			}
		));
		table_2.getColumnModel().getColumn(0).setPreferredWidth(20);
		table_2.setDefaultEditor(Object.class, null);
		scrollPane_1.setViewportView(table_2);
		
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(1), new Long(1)));
		
		JLabel lblDay = new JLabel("D");
		
		JLabel lblH = new JLabel("H");
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(24), new Long(1)));
		
		JLabel lblM = new JLabel("M");
		
		JSpinner spinner_2 = new JSpinner();
		spinner_2.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(60), new Long(1)));
		model2 = (DefaultTableModel) table_2.getModel();
		
		JButton btnSchedule = new JButton("Schedule");
		btnSchedule.setToolTipText("Schedule");
		
		JButton	chooseFolder = new JButton("Set downloads folder");
		
		//button that opens downloads folder
		chooseFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fchooser = new JFileChooser(new File(homeDefault));
				fchooser.setAcceptAllFileFilterUsed(false);
				fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fchooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					String oldFolder = homeDefault;
					homeDefault = fchooser.getSelectedFile().getAbsolutePath()+"\\";
					db.setDefaultFolder(homeDefault,oldFolder);
				}
			}
		});
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(chooseFolder, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addGap(3)
					.addComponent(textField, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
					.addGap(10)
					.addComponent(btnDownload, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
					.addGap(9))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap(224, Short.MAX_VALUE)
					.addComponent(lblDay, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(spinner, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(lblH, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
					.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(2)
					.addComponent(lblM, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
					.addComponent(spinner_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(10)
					.addComponent(btnSchedule, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
					.addGap(9))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)
					.addGap(8))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(chooseFolder, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(btnDownload))
					.addGap(8)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblDay)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(lblH))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(spinner_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(lblM))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(spinner_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(btnSchedule))
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
					.addGap(9))
		);
		frame.getContentPane().setLayout(groupLayout);

		//button that calls the scheduled method with the time given by the spinner componments
		btnSchedule.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if (textField.getText().equals("")) {
					JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);
					return;
				}
				Monitor dMonitor = null;
				long dateToS = (((Long) spinner.getValue() * 24)*60)*60;
				long hoursToS = ((Long) spinner_1.getValue() *60)*60;
				long minToS = (Long) spinner_2.getValue() *60;
				long timer =  dateToS+hoursToS+minToS;
				int tmpID = 0;
				String type = URLHandler.getUriType(textField.getText().trim());
    			if (type=="URL") {
    				fileLoc = textField.getText().trim();
    				String fileN = URLHandler.getFilename(fileLoc);
    				if (new File(homeDefault+fileN).exists()==false) {
	    				String[] item={""+tDownID+"",fileN,"Scheduled","Scheduled",getScheduledDate(timer),fileLoc,homeDefault};
	    				String[] itemTray={fileN,"Scheduled"};
	
	    				model.addRow(item);
	    				trayframe.modelTray.addRow(itemTray);
	    				/*-------OOOOO-------*/
	    				Download download = new Download(tDownID,fileLoc,fileN,homeDefault,trayFrameRow);
	    			    df.add(download);
	    			    dMonitor = new Monitor(window,downloadID,type,trayFrameRow);
	    			    db.insertDownloadDir(df, downloadID);
	    			    tmpID = downloadID;
	    			    downloadID = downloadID+1;
						active=active+1;
						tDownID = tDownID + 1;
						trayFrameRow = trayFrameRow + 1;
	    			    textField.setText("");
    				} else {
    					int dialogResult = JOptionPane.showConfirmDialog (null, "File already exists,open on explorer?","Warning",JOptionPane.YES_NO_OPTION);
    					if (dialogResult == JOptionPane.YES_OPTION) {
    						openFolderS(homeDefault , fileN);
    					}
    					type = null;
    				}
    			} else if (type=="Torrent") {
    				fileLoc = textField.getText().trim();
    				String NameTo = fileLoc.split("&")[1].split("=")[1];
    				try {
    					NameTo = URLDecoder.decode(NameTo,StandardCharsets.UTF_8.name()).replaceAll("[^a-zA-Z0-9]+","");
    				} catch (UnsupportedEncodingException e1) {
    					e1.printStackTrace();
    				}
    				if (new File(homeDefault+NameTo+"\\").exists()==false) {
	    				String[] item={""+tTorID+"",NameTo,"Scheduled","Scheduled",getScheduledDate(timer),fileLoc,homeDefault+NameTo+"\\"};
	    				String[] itemTray={NameTo,"Scheduled"};
	
	    				model2.addRow(item);
	    				trayframe.modelTray.addRow(itemTray);
	    				dMonitor = new Monitor(window,torrentID,type,trayFrameRow);
	    				Torrent to = new Torrent(tTorID,fileLoc,NameTo,homeDefault,trayFrameRow);
	    				tr.add(to);
	    				db.insertDownloadTor(tr,torrentID);
	    				tmpID = torrentID;
	    				torrentID = torrentID+1;
	    				tTorID = tTorID + 1;
	    				trayFrameRow = trayFrameRow + 1;
	    				textField.setText("");
	    			} else {
						int dialogResult = JOptionPane.showConfirmDialog (null, "Folder already exists,open on explorer?","Warning",JOptionPane.YES_NO_OPTION);
						if (dialogResult == JOptionPane.YES_OPTION) {
							openFolderS(homeDefault , NameTo);
						}
						type=null;
					}
    			} else {
    				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);
    				return;
    			}
    			if (dMonitor!=null && type!=null) {
    				Schedule schedule = new Schedule(timer,type,dMonitor,tmpID);
    				sc.add(schedule);
    				if (type.equals("URL")) {
    					Dmap.put(df.get(tmpID),sc.get(scheduled));
    					
    				} else {
    					Tmap.put(tr.get(tmpID),sc.get(scheduled));
    				}
    				scheduled = scheduled + 1;
    			}
		  }
		});
		//popup menu items
		popup = new JPopupMenu();
		open = new JMenuItem("Open");
		popup.add(open);
		delete = new JMenuItem("Delete");
		popup.add(delete);
		scheduleCancel = new JMenuItem("Cancel Schedule");
		popup.add(scheduleCancel);
		delete.setVisible(false);
		move = new JMenuItem("Move to...");
		popup.add(move);
		move.setVisible(false);
		openFolder = new JMenuItem("Open folder");
		popup.add(openFolder);
		move.setVisible(false);
		pause = new JMenuItem("Pause");
		popup.add(pause);
		resume = new JMenuItem("Resume");
		popup.add(resume);
		remove = new JMenuItem("Remove");
		popup.add(remove);
		copyToCl = new JMenuItem("Copy URL");
		popup.add(copyToCl);
	    //Convertion menu
		convertMenu = new JMenu("Convert to..");
		convertMenu.setVisible(false);
		//Videoformats
		mp4 = new JMenuItem("mp4");
		convertMenu.add(mp4);
		flv = new JMenuItem("flv");
		convertMenu.add(flv);
		avi = new JMenuItem("avi");
		convertMenu.add(avi);
		webm = new JMenuItem("webm");
		convertMenu.add(webm);
		//audioFormats
		mp3 = new JMenuItem("mp3");
		convertMenu.add(mp3);
		wav = new JMenuItem("wav");
		convertMenu.add(wav);
		ogg = new JMenuItem("ogg");
		convertMenu.add(ogg);
		acc = new JMenuItem("acc");
		convertMenu.add(acc);
		//image formats
		png = new JMenuItem("png");
		convertMenu.add(png);
	    jpg = new JMenuItem("jpg");
	    convertMenu.add(jpg);
	    gif = new JMenuItem("gif");
	    convertMenu.add(gif);
	    bmp = new JMenuItem("bmp");
	    convertMenu.add(bmp);

	    popup.add(convertMenu);
	    
	    MouseListener popupListener = new PopupListener();
	    table_1.addMouseListener(popupListener);
	    table_2.addMouseListener(popupListener);
	    frame.pack();
	}	
	
	
	
	class PopupListener extends MouseAdapter {
		String file,progress,percent,fileMIME[],filetype,value;
		String type = null;
		String location = homeDefault;
		int row;
		/*
		 * method that will change our popup menu appearance,depending on the status of the download
		 */
		public void mousePressed(MouseEvent e) {
			int tabActive = tabbedPane.getSelectedIndex();
			if (tabActive==0) {
				int row = table_1.rowAtPoint(e.getPoint());
				if (row > -1) {
					table_1.setRowSelectionInterval(row, row);
				} else {
					table_1.clearSelection();
				}
			} else {
				int row = table_2.rowAtPoint(e.getPoint());
				if (row > -1) {
					table_2.setRowSelectionInterval(row, row);
				} else {
					table_2.clearSelection();
				}
			}
			convertMenu.setVisible(false);
			open.setVisible(false);
			delete.setVisible(false);
			move.setVisible(false);
			pause.setVisible(false);
			resume.setVisible(true);
			resume.setText("Resume");
			remove.setVisible(false);
			scheduleCancel.setVisible(false);
			if (tabActive==1) {
				int per;
				row = table_2.getSelectedRow();
				per = tr.get(row).getPerc();
				int size = tr.get(row).getSize();
				String status = table_2.getModel().getValueAt(row, 3).toString();
				if (tr.get(row).getPaused() || tr.get(row).getStopped()) {
					remove.setVisible(true);
				}
				if (per>=0 && per<100) {
					if (per > 0 || size>1) {
						openFolder.setVisible(true);
					} else {
						openFolder.setVisible(false);
					}
					pause.setVisible(true);
					if (tr.get(row).getStopped()==true) {
						pause.setVisible(false);
						
						if (size>1) {
							delete.setVisible(true);
							move.setVisible(true);
						}
					} else {
						resume.setVisible(false);
					}
				} else if (per==100){
					openFolder.setVisible(true);
					delete.setVisible(true);
					pause.setVisible(false);
					move.setVisible(true);
				}
				if (status.equals("Not Found")) {
					resume.setText("Redownload");
					openFolder.setVisible(false);
				}
				if(status.equals("Deleted")) {
					delete.setVisible(false);
					resume.setText("Redownload");
					openFolder.setVisible(false);
					remove.setVisible(true);
					move.setVisible(false);
				} else if (status.equals("Scheduled")) {
					resume.setVisible(false);
					openFolder.setVisible(false);
					scheduleCancel.setVisible(true);
					delete.setVisible(false);
				}
			} else {
				delete.setVisible(true);
				row = table_1.getSelectedRow();
				file = df.get(row).getNameOfFile();
				location = df.get(row).getLocation();
				try {
					Path source = Paths.get(location+file);
					if(source.toFile().exists()) {
				        
						fileMIME = Files.probeContentType(source).split("/");
						filetype = fileMIME[0].toString();
					}
				} catch (Exception e1) {
					System.out.println("Exception FileMIME");
					e1.printStackTrace();
				}
		        
				progress = table_1.getModel().getValueAt(row, 2).toString();
				String status = table_1.getModel().getValueAt(row, 3).toString();
				String percentSplit[] = progress.split("%");
				percent = percentSplit[0];
				int per;
				try {
					per = Integer.parseInt(percent);
				} catch(Exception error) {
					per=0;
				}
				if (per==100 ){
					resume.setText("Redownload");
					
					open.setVisible(true);
					move.setVisible(true);
					remove.setVisible(true);
					if ( "image".equals(filetype) ) {
						convertMenu.setVisible(true);
						png.setVisible(true);
						jpg.setVisible(true);
						gif.setVisible(true);
						bmp.setVisible(true);
						mp4.setVisible(false);
						flv.setVisible(false);
						avi.setVisible(false);
						webm.setVisible(false);
						acc.setVisible(false);
						ogg.setVisible(false);
						mp3.setVisible(false);
						wav.setVisible(false);
					}
					if ( "video".equals(filetype) ) {
						convertMenu.setVisible(true);
						mp4.setVisible(true);
						flv.setVisible(true);
						avi.setVisible(true);
						gif.setVisible(true);
						webm.setVisible(true);
						acc.setVisible(false);
						ogg.setVisible(false);
						mp3.setVisible(false);
						wav.setVisible(false);
						png.setVisible(false);
						jpg.setVisible(false);
						bmp.setVisible(false);
					}
					if ( "audio".equals(filetype) ) {
						convertMenu.setVisible(true);
						acc.setVisible(true);
						ogg.setVisible(true);
						mp3.setVisible(true);
						wav.setVisible(true);
						mp4.setVisible(false);
						flv.setVisible(false);
						avi.setVisible(false);
						gif.setVisible(false);
						webm.setVisible(false);
						png.setVisible(false);
						bmp.setVisible(false);
					}
					
				} else if (status.equals("Deleted") || status.equals("Failed") || status.equals("Not Found")){
					if ( status.equals("Failed") ) {
						resume.setText("Retry");
					} else {
						resume.setText("Redownload");
					}
					delete.setVisible(false);
					remove.setVisible(true);
				} else if (status.equals("Scheduled")) {
					resume.setVisible(false);
					scheduleCancel.setVisible(true);
					delete.setVisible(false);
				} else {
					pause.setVisible(true);
					resume.setText("Resume");
				}
			}
		}
	        

		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}
	    
		public PopupListener() {
			
			//method do delete the download from the driver
			delete.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int tabAcive = tabbedPane.getSelectedIndex();
					if(tabAcive==0) {
						row = table_1.getSelectedRow();
						file = df.get(row).getNameOfFile();
						location = df.get(row).getLocation();
						File filetoDelete= new File(location+file);
						if(filetoDelete.exists()) {
							FileUtils de = new FileUtils();
							if (de.deleteFiles(filetoDelete)) {
								table_1.getModel().setValueAt("Deleted",row, 3);
								table_1.getModel().setValueAt("0%  0 KB/s",row, 2);
							}
				        } else {
							df.get(row).PauseDownload();
							df.get(row).deleteSubFiles();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {e.printStackTrace();}
							table_1.getModel().setValueAt("Deleted",row, 3);
							table_1.getModel().setValueAt("0%  0 KB/s",row, 2);
						}
					}
					if(tabAcive==1) {
						row = table_2.getSelectedRow();
				        location = tr.get(row).getLocation();
				        file = tr.get(row).getFolderName();
						File foldertoDelete= new File(location+file+"\\");
						if(foldertoDelete.exists()) {
							FileUtils futil = new FileUtils();
							futil.deleteFiles(foldertoDelete);
				        }
						table_2.getModel().setValueAt("Deleted", row, 3);
						table_2.getModel().setValueAt("0%  0 KB/s", row, 2);
					}
				}
		    	
		    });
	    
			remove.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					int tabAcive = tabbedPane.getSelectedIndex();
					if(tabAcive==0) {
						row = table_1.getSelectedRow();
						model.removeRow(row);
						db.deleteDir(df.get(row).getDownloadID());
						df.remove(row);
						downloadID = downloadID -1;
					} else if(tabAcive==1) {
						row = table_2.getSelectedRow();
						model2.removeRow(row);
						db.deleteTor(tr.get(row).getDownloadID());
						tr.remove(row);
						torrentID = torrentID-1;
					}
				}
			});
			/* method to move file to different location in the drive */
			move.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					int tabAcive = tabbedPane.getSelectedIndex();
					if(tabAcive==0) {
						row = table_1.getSelectedRow();
						file = df.get(row).getNameOfFile();
						location = df.get(row).getLocation();
						JFileChooser fchooser = new JFileChooser(new File(location));
						fchooser.setAcceptAllFileFilterUsed(false);
						fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if (fchooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
							try {
								Files.move(Paths.get(location+file), Paths.get(fchooser.getSelectedFile().getAbsolutePath()+"\\"+file),StandardCopyOption.REPLACE_EXISTING);
								df.get(row).setLocation(fchooser.getSelectedFile().getAbsolutePath()+"\\");
								db.updateLocDir(location, fchooser.getSelectedFile().getAbsolutePath(), df.get(row).getDownloadID());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					} else if(tabAcive==1) {
						row = table_2.getSelectedRow();
				        file = tr.get(row).getFolderName();
				        location = tr.get(row).getLocation();
						File foldertoMove = new File(location+file+"\\");
						JFileChooser fchooser = new JFileChooser(foldertoMove.getParent());
						fchooser.setAcceptAllFileFilterUsed(false);
						fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if (fchooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
							try {
								FileUtils futils = new FileUtils();
								futils.moveDir(foldertoMove, new File(fchooser.getSelectedFile().getAbsolutePath()+"\\"+file));
								futils.deleteFiles(foldertoMove);
								tr.get(row).setLocation(fchooser.getSelectedFile().getAbsolutePath()+"\\");
								db.updateLocTor(location, fchooser.getSelectedFile().getAbsolutePath(), tr.get(row).getDownloadID());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
		    	
		    });
		    /*
		     * method to open the file ,or the download folder in case of the torrent
		     */
			open.addActionListener(new ActionListener() {
		    	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int tabAcive = tabbedPane.getSelectedIndex();
					if(tabAcive==0) {
					row = table_1.getSelectedRow();
					file = df.get(row).getNameOfFile();
					location = df.get(row).getLocation();
					File filetoOpen = new File(location+file);
					Desktop desktop = Desktop.getDesktop();
			        if(filetoOpen.exists()) {
						try {
							desktop.open(filetoOpen);
						} catch (IOException e) {
							e.printStackTrace();
						}
			        }
					}
				}
		    	
		    });
	    
			openFolder.addActionListener(new ActionListener() {
		    	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int tabAcive = tabbedPane.getSelectedIndex();
					if(tabAcive==0) {
						row = table_1.getSelectedRow();
						file = df.get(row).getNameOfFile();
						location = df.get(row).getLocation();
						File filetoOpento = new File(location+file);
						if (filetoOpento.exists()) {
								openFolderS(location,file);
								//desktop.open(filetoOpen);
						}
					} else if(tabAcive==1) {
						row = table_2.getSelectedRow();
						file = tr.get(row).getFolderName();
						location = tr.get(row).getLocation();
						File filetoOpen = new File(location+file);
						if(filetoOpen.exists()) {
							openFolderS(location,file);
						}
					}
				}
			});
	    
		    //cancels the scheduled download
			scheduleCancel.addActionListener(new ActionListener() {
		    	
		    	@Override
		    	public void actionPerformed(ActionEvent arg0) {
		    		int tabAcive = tabbedPane.getSelectedIndex();
		    		if(tabAcive==0) {
		    			row = table_1.getSelectedRow();
		    			Dmap.get(df.get(row)).getScheduler().cancel(true);
		    			Dmap.remove(df.get(row));
		    			scheduled = scheduled - 1;
		    			table_1.getModel().setValueAt("Deleted", row, 2);
		    			table_1.getModel().setValueAt("Deleted", row, 3);
		    		} else if (tabAcive==1) {
		    			row = table_2.getSelectedRow();
		    			Tmap.get(tr.get(row)).getScheduler().cancel(true);
		    			Tmap.remove(tr.get(row));
		    			scheduled = scheduled - 1;
		    			table_2.getModel().setValueAt("Deleted", row, 2);
		    			table_2.getModel().setValueAt("Deleted", row, 3);
		    		}
		    	}
			});
	    
		    /*
		     * methods to convert the file to the format that is clicked
		     */
			mp4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(0,"mp4",location);
				}
			});
			webm.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(0,"webm",location);
				}
			});
			avi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(0,"avi",location);
				}
			});
			flv.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(0,"flv",location);
				}
			});
			mp3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(2,"mp3",location);
				}
			});
			ogg.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(2,"ogg",location);
				}
			});
			acc.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(2,"acc",location);
				}
			});
			wav.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(2,"wav",location);
				}
			});
			png.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(1,"png",location);
				}
			});
			bmp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(1,"bmp",location);
				}
			});
			gif.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (filetype.equals("video")) {
						getFileandConv(0,"gif",location);
					} else {
						getFileandConv(1,"gif",location);
					}
				}
			});
			jpg.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getFileandConv(1,"jpg",location);
				}
			});
			//method to copy URL to clipboard	
			copyToCl.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					int tabAcive = tabbedPane.getSelectedIndex();
					if (tabAcive==0) {
						row = table_1.getSelectedRow();
						StringSelection stringSelection = new StringSelection(df.get(row).getFileLoc());
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(stringSelection, null);
					} else if (tabAcive==1) {
						row = table_2.getSelectedRow();
						StringSelection stringSelection = new StringSelection(tr.get(row).getMagnetURI());
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						clipboard.setContents(stringSelection, null);
					}
					
					
				}
			});
			/*
			 * Pause button,sets the pause flag variable to true,stopping the download,
			 * whose flag variable "complete" is not changed
			 */
			pause.addActionListener(new ActionListener() {
				
		    	public void actionPerformed(ActionEvent e) {
		    		int tabAcive = tabbedPane.getSelectedIndex();
						if (tabAcive==0) {
							row = table_1.getSelectedRow();
		    				//pool.shutdownNow();
		    				df.get(row).PauseDownload();
		    				if(rateState==true) {
								downloadSpeedLimit(speedLimitNumber);
							}
						} else {
							row = table_2.getSelectedRow();
							tr.get(row).setTorPaused();
						}
			
				  }
			});
			/*
			 * Resume button,creates new download object with the same args as the one from the selected row
			 * and starts the download,SubDownload class has file checks to achieve the resume fuctionality,
			 * torrent is handled by the library
			 */
			resume.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					int tabAcive = tabbedPane.getSelectedIndex();
					if (tabAcive==0) {
						type = "URL";
						row = table_1.getSelectedRow();
						String fileN = df.get(row).getNameOfFile();
						int tRow = df.get(row).getTrayRow();
						if (tRow==-1) {
							tRow = trayFrameRow;
							String[] item = {fileN,"0%"};
							trayframe.modelTray.addRow(item);
							trayFrameRow = trayFrameRow +1;
						}
						df.get(row).setTrayRow(tRow);
						try {
							if(df.get(row).getPause()==true || df.get(row).getComplete()==1) {
								Monitor dMonitor = new Monitor(window,row,type,tRow);
								df.get(row).setComplete(0);
								pool.execute(df.get(row));
								pool.execute(dMonitor);
								active=active+1;
								if(rateState==true) {
									downloadSpeedLimit(speedLimitNumber);
								}
							}
						}catch(Exception e1) {
							e1.printStackTrace();
						}
					} else if (tabAcive==1) {
						type = "Torrent";
						
						row = table_2.getSelectedRow();
						String NameTo = tr.get(row).getFolderName();
						int tRow = tr.get(row).getTrayRow();
						if (tRow==-1) {
							tRow = trayFrameRow;
							String[] item = {NameTo,"0%"};
							trayframe.modelTray.addRow(item);
							trayFrameRow = trayFrameRow +1;
						}
						tr.get(row).setTrayRow(tRow);
						System.out.println(tr.get(row).getStopped());
						if(tr.get(row).getStopped()) {
							Monitor dMonitor = new Monitor(window,row,type,tRow);
							pool.execute(tr.get(row));
							pool.execute(dMonitor);
						}
					}
				}
			});
		}
		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(),e.getX(), e.getY());
			}
		}
	}
	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm dd-MM-yy");
		Date date = new Date();
		return dateFormat.format(date);
	}
	private String getScheduledDate(long x) {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm dd-MM-yy");
		long ftrtime  = System.currentTimeMillis() + (x*1000);
		Date date = new Date(ftrtime);
		return dateFormat.format(date);
	}
	
	 /*
	  *  Monitor class monitors the activity of the download and does actions depending on that activity,
	  *  updateStatus()
	  *  
	  *  @author MAHESH KAREKAR
	  *  @author KONSTANTINOS KARTOFIS
	  */
	class Monitor extends Thread{

		DownloadManager gui;
		int threadIndex;
		String typeof;
		private int trayFrameRow;
		public Monitor(DownloadManager dm_frame,int idx,String type,int trayFrameRow){
			gui=dm_frame;
			threadIndex = idx;
		  	typeof = type;
		  	this.trayFrameRow = trayFrameRow;
		}

		public void run(){
			int currThread=0;
			int dconnections=0;
			boolean downloadcomplete = true;
			boolean failed = false;
			boolean failCheck = true;
			currThread= threadIndex;
			String failMsg = "Download Failed.Try Downloading Again or verify URL is Correct";
			if (typeof=="URL") {
				while (df.get(currThread).getComplete() == 0) {
					
					dconnections = df.get(currThread).getTotConnections();
					if (df.get(currThread).getComplete() == 0 && df.get(currThread).getActiveSubConn() == dconnections ){
						if(failCheck==true) {
							failed = df.get(currThread).isDownloadFailed();
							failCheck=false;
							if(failed==true) {
								gui.updateStatus(currThread,true,typeof,trayFrameRow);
								JOptionPane.showMessageDialog(null,failMsg,"INFORMATION",JOptionPane.INFORMATION_MESSAGE);
								break;
							}
						}
						gui.updateStatus(currThread,false,typeof,trayFrameRow);
						downloadcomplete = df.get(currThread).isDownloadComplete();//Flag
						if (downloadcomplete == true) {
							df.get(currThread).concatSub();
							model.setValueAt("100%  0 KB/s",currThread,2);
							trayframe.modelTray.setValueAt("100%",trayFrameRow,1);
							System.out.println("done");
							break;
						} else if(df.get(currThread).getPause()) {						
							model.setValueAt(df.get(currThread).DownloadProgress()+"%  0 KB/s",currThread,2);
							break;
						}
						gui.updateStatus(currThread,false,typeof,trayFrameRow);   
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
					active = active - 1;
					if(rateState==true) {
						downloadSpeedLimit(speedLimitNumber);
					}
			} else if (typeof=="Torrent") {
				while(tr.get(currThread).getComplete() == false && tr.get(currThread).getPaused()==false){
					gui.updateStatus(currThread,false,typeof,trayFrameRow);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				model2.setValueAt(String.valueOf(tr.get(currThread).getPerc())+"%  0 KB/s",currThread,2);
				if (tr.get(currThread).getComplete()==false && tr.get(currThread).getPaused()==true) {
					model2.setValueAt("Paused",currThread,3);
				}
			}
		}
		   
	}
	
	//method updates our table data
	public void updateStatus( int currThread, boolean dFailed,String type,int trayR) {
		if (type=="URL") {
			if (!dFailed) {
				int progress = df.get(currThread).DownloadProgress();
				model.setValueAt(progress+"% "+df.get(currThread).getDownloadSpeed(),currThread,2);	
				model.setValueAt(df.get(currThread).getBytesDownloaded(),currThread,3);
				trayframe.modelTray.setValueAt(progress+"%",trayR,1);
			} else {
				model.setValueAt("Failed",currThread,2);
				model.setValueAt("Failed",currThread,3);
				trayframe.modelTray.setValueAt("Failed",trayR,1);
			}
		} else if(type=="Torrent") {
			if (!dFailed){
				int progress = tr.get(currThread).getPerc();
				model2.setValueAt(progress+"% "+tr.get(currThread).getDownloadSpeed(),currThread,2);	
				model2.setValueAt(tr.get(currThread).getDownloaded(),currThread,3);
				trayframe.modelTray.setValueAt(progress+"%",trayR,1);
			} else {
				model2.setValueAt("Failed",currThread,2);
				model2.setValueAt("Failed",currThread,3);
				trayframe.modelTray.setValueAt("Failed",trayR,1);
			}
		}
	}
	//sets the download limit to all
	public synchronized void downloadSpeedLimit(double speedLimitNumber2) {
		try {
			Thread.sleep(300);
		} catch (Exception e) {}
		if(df.size()!=0 && active!=0) {
			double rateper = speedLimitNumber2/active;
			for (int i=0;i<df.size();i++) {
				if(df.get(i).getComplete()==0) {
					df.get(i).setRateLimit(rateper);
				}
			}
		}
	}
	
	//scheduled method created a download task that will be executed after x Seconds
	class Schedule {
		private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
		private ScheduledFuture<?> sch;
		public Schedule(long x,String t,Monitor d,int ID) {
			scheduler.setRemoveOnCancelPolicy(true);
			sch = scheduler.schedule(new Runnable() {
				@Override
				public void run() {
					if (t=="URL") {
						
						pool.execute(df.get(ID));
						pool.execute(d);
						active = active + 1;
						if(rateState==true) {
							downloadSpeedLimit(speedLimitNumber);
						}
	    				/*--------------*/
					} else if(t=="Torrent"){
						
						pool.execute(tr.get(ID));
						pool.execute(d);
					}
				}}, x, TimeUnit.SECONDS);
		}
		public ScheduledFuture<?> getScheduler() {
			return sch;
		}
	}
	/*
	 * method to grab file from selected row and convert it to given format
	 * type: 0 for video,1 for images,2 for audio
	 */
	public void getFileandConv(int type,String ext,String alocation) {
		int row = table_1.getSelectedRow();
		String file = table_1.getModel().getValueAt(row, 1).toString();
		FileUtils conv = new FileUtils();
		String location = alocation;
		switch(type) {
		case 0:
			conv.videoConv(location+file,ext);
			break;
		case 1:
			conv.imgConv(location+file,ext);
			break;
		case 2:
			conv.audioConv(location+file,ext);
		break;
		}
	}
	
	/* addDown() and addTor() are called by DatabaseHandler to add
	 * the new objects to the ArrayLists
	 */
	public void addDown(Download d) {
		df.add(d);
	}
	
	public void addTor(Torrent t) {
		tr.add(t);
	}
	
	/*
	 * opens Folder where download is located and highlights it
	 */
	public void openFolderS(String location,String file) {
		String command = "explorer.exe /select," + location + file;
		try {
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
