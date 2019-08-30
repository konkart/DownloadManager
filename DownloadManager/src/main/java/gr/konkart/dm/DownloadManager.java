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
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

public class DownloadManager {
	JPopupMenu popup; 
	static ClassLoader classLoader = DownloadManager.class.getClassLoader();
	static String path  = classLoader.getResource("NekoAtsumeFace.png").getPath();
	static Image image = Toolkit.getDefaultToolkit().getImage(path);
	public static int downloadID = 0;
	public static int torrentID = 0;
	static TrayIcon trayIcon = new TrayIcon(image, "Tester2");
	static JFrame frame;
	public Download df[] = new  Download[0];
	public Torrent tr[] =new Torrent[0];
	static TrayFrame trayframe;
	private JTextField textField;
	private JTable table_1;
	private JTable table_2;
	public String fileLoc;
	static DownloadManager window;
	private DefaultTableModel model;
	private DefaultTableModel model2;
	public int totalConnections = 5;
	public boolean rateState=false;
	JMenuItem pause;
	JMenuItem resume;
	JMenuItem delete;
	JMenuItem open;
	JMenuItem copyToCl;
	JMenuItem move;
	JMenuItem openFolder;
	JMenu sectionsMenu;
	JMenuItem mp4;
	JMenuItem webm;
	JMenuItem avi;
	JMenuItem flv;
	JMenuItem mp3;
	JMenuItem ogg;
	JMenuItem acc;
	JMenuItem wav;
	JMenuItem jpg;
	JMenuItem gif;
	JMenuItem bmp;
	JMenuItem png;
	JTabbedPane tabbedPane;
	String homeDefault = System.getProperty("user.home")+"\\Downloads\\";
	volatile double speedLimitNumber=0;
	int active=0;
	ExecutorService pool = Executors.newCachedThreadPool();
	public static void main(String[] args) throws IOException {
		System.setProperty("http.agent", "Chrome");
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
			public void run() {
				try {
					
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					window = new DownloadManager();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//set ups and calls the tray icon and creates the trayframe object
		if (SystemTray.isSupported()) {
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
		
		
	}
	

	public DownloadManager() {
		initialize();
	}

	/*
	 * main window init
	 */
	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 530, 450);
		frame.setDefaultCloseOperation(JFrame.ICONIFIED);
		frame.getContentPane().setLayout(null);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 64, 496, 336);
		frame.getContentPane().add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Direct", null, panel, null);
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 23, 490, 285);
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
				"ID", "Name", "Progress", "Size", "Date", "URL","Location"
			}
		));
		table_1.getColumnModel().getColumn(0).setPreferredWidth(20);
		scrollPane.setViewportView(table_1);
		model = (DefaultTableModel) table_1.getModel();
		table_1.setDefaultEditor(Object.class, null);
		//model.addRow(new Object[]{"Column 1", "Column 2", "Column 3","Column 4"});
		
		textField = new JTextField();
		textField.setBounds(47, 11, 329, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.setBounds(386, 10, 119, 23);
		frame.getContentPane().add(btnDownload);
		
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
			String type;
			if(URLHandler.isUrl(textField.getText())=="URL") {
				type = "URL";
				
				fileLoc = textField.getText();
				textField.setText("");
				String fileN = URLHandler.getFilename(fileLoc);
				String[] item={""+downloadID+"",fileN,"0%  0 KB/s","0",getDateTime(),fileLoc,homeDefault};
				String[] itemTray={fileN,"0%"};
				
				model.addRow(item);
				trayframe.modelTray.addRow(itemTray);
				/*-------OOOOO-------*/
				Download download = new Download(downloadID,fileLoc,totalConnections,homeDefault);
			    addDownload(download);
			    
					//df[DownloadID].start();
					Monitor dMonitor = new Monitor(window,downloadID,type);
					pool.execute(df[downloadID]);
					pool.execute(dMonitor);
					downloadID = downloadID + 1;
					active=active+1;
					if (rateState==true) {
						downloadSpeedLimit(speedLimitNumber);
					}
				/*--------------*/
			}
			else if (URLHandler.isUrl(textField.getText())=="Torrent") {
				type="Torrent";
				fileLoc = textField.getText();
				textField.setText("");
				String[] magnetParts = fileLoc.split("&"); 
				String toName = magnetParts[1];
				String[] Split_toEq = toName.split("=");
				String NameTo = Split_toEq[1];
				try {
					NameTo = URLDecoder.decode(NameTo,StandardCharsets.UTF_8.name()).replaceAll("[^a-zA-Z0-9]+","");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				String[] item={""+torrentID+"",NameTo,"0%  0 KB/s","0",getDateTime(),fileLoc,homeDefault+NameTo+"\\"};
				String[] itemTray={NameTo,"0%"};
				
				model2.addRow(item);
				trayframe.modelTray.addRow(itemTray);
				Monitor dMonitor = new Monitor(window,torrentID,type);
				System.out.println(NameTo);
				Torrent to = new Torrent(fileLoc,torrentID,NameTo,homeDefault);
				addTorrent(to);
				
				pool.execute(tr[torrentID]);
				pool.execute(dMonitor);
				torrentID=torrentID+1;
				
			}
			else {
				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);
				return;
			}
			btnDownload.setEnabled(true);	
		  }
		});
		
		/*---------------------*/
		JSpinner amoun = new JSpinner();
		amoun.setModel(new SpinnerNumberModel(new Double(0), new Double(0), null, new Double(1)));
		amoun.setBounds(319, 1, 55, 20);
		amoun.setUI(new BasicSpinnerUI() {
            protected Component createNextButton() {
                return null;
            }

            protected Component createPreviousButton() {
                return null;
            }
        });
		panel.add(amoun);
		
		JToggleButton speedLimitBut = new JToggleButton("Speed Limit");
		//Button that sets the state of the ratelimit and calls the downloadSpeedLimit
		 speedLimitBut.addItemListener(new ItemListener() {
			   public void itemStateChanged(ItemEvent ev) {
			      if (ev.getStateChange()==ItemEvent.SELECTED){
			    	speedLimitNumber = (double) amoun.getValue();
			    	speedLimitNumber = Math.round(1000*speedLimitNumber);
			    	downloadSpeedLimit(speedLimitNumber);
			    	rateState=true;
			        System.out.println("button is selected");
			      } else if (ev.getStateChange()==ItemEvent.DESELECTED){
			    	downloadSpeedLimit(0);
			    	rateState=false;
			        System.out.println("button is not selected");
			      }
			   }
			});
		speedLimitBut.setBounds(384, 0, 94, 23);
		panel.add(speedLimitBut);
		
		
		
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
					"ID","Name", "Progress", "Session bytes", "Date" , "Magnet","Location"
			}
		));
		table_2.getColumnModel().getColumn(0).setPreferredWidth(20);
		scrollPane_1.setViewportView(table_2);
		
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(1), new Long(1)));
		spinner.setBounds(238, 42, 34, 20);
		frame.getContentPane().add(spinner);
		
		JLabel lblDay = new JLabel("D");
		lblDay.setBounds(228, 45, 11, 14);
		frame.getContentPane().add(lblDay);
		
		JLabel lblH = new JLabel("H");
		lblH.setBounds(274, 45, 11, 14);
		frame.getContentPane().add(lblH);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(24), new Long(1)));
		spinner_1.setBounds(285, 42, 39, 20);
		frame.getContentPane().add(spinner_1);
		
		JLabel lblM = new JLabel("M");
		lblM.setBounds(326, 45, 11, 14);
		frame.getContentPane().add(lblM);
		
		JSpinner spinner_2 = new JSpinner();
		spinner_2.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(60), new Long(1)));
		spinner_2.setBounds(337, 42, 39, 20);
		frame.getContentPane().add(spinner_2);
		model2 = (DefaultTableModel) table_2.getModel();
		
		JButton btnSchedule = new JButton("Schedule");
		btnSchedule.setToolTipText("Schedule");
		btnSchedule.setBounds(386, 41, 119, 23);
		frame.getContentPane().add(btnSchedule);
		
		JButton	chooseFolder = new JButton("Set downloads folder");
		
		//button that opens downloads folder
		chooseFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fchooser = new JFileChooser(new File(homeDefault));
				fchooser.setAcceptAllFileFilterUsed(false);
				fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fchooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					homeDefault = fchooser.getSelectedFile().getAbsolutePath()+"\\";
				}
			}
		});
		chooseFolder.setBounds(10, 11, 34, 20);
		frame.getContentPane().add(chooseFolder);

		//button that calls the scheduled method with the time given by the spinner componments
		btnSchedule.addActionListener(new ActionListener()
		{
		  public void actionPerformed(ActionEvent e)
		  {		
			  if (textField.getText().equals("")){
  				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);

  				return;
  			}
			  	Monitor dMonitor = null;
			  	Long DateToS = (((Long) spinner.getValue() * 24)*60)*60;
			  	Long HoursToS = ((Long) spinner_1.getValue() *60)*60;
			  	Long MinToS = (Long) spinner_2.getValue() *60;
			  	System.out.println(DateToS+" "+HoursToS+" "+MinToS);
			  	Long timer =  DateToS+HoursToS+MinToS;
			  	String type = null;
			  	int tmpID = 0;
    			if (URLHandler.isUrl(textField.getText())=="URL") {
    				type = "URL";
    				System.out.println("it is a URL");
    				fileLoc = textField.getText();
    				String fileN = URLHandler.getFilename(fileLoc);
    				String[] item={""+downloadID+"",fileN,"Scheduled","Scheduled",getDateTime(),fileLoc,homeDefault};
    				String[] itemTray={fileN,"0%"};
    				
    				model.addRow(item);
    				trayframe.modelTray.addRow(itemTray);
    				/*-------OOOOO-------*/
    				Download download = new Download(downloadID,fileLoc,totalConnections,homeDefault);
    			    addDownload(download);
    			    dMonitor = new Monitor(window,downloadID,type);
    			    tmpID = downloadID;
    			    downloadID = downloadID+1;
    			    textField.setText("");
    			}
    			else if (URLHandler.isUrl(textField.getText())=="Torrent") {
    				type="Torrent";
    				fileLoc = textField.getText();
    				String[] magnetParts = fileLoc.split("&"); 
    				String toName = magnetParts[1];
    				String[] Split_toEq = toName.split("=");
    				String NameTo = Split_toEq[1];
    				NameTo = NameTo.replaceAll("[^a-zA-Z0-9]+","");
    				try {
    					NameTo = URLDecoder.decode(NameTo,StandardCharsets.UTF_8.name()).replaceAll("[^a-zA-Z0-9]+","");
    				} catch (UnsupportedEncodingException e1) {
    					e1.printStackTrace();
    				}
    				String[] item={""+torrentID+"",NameTo,"0%  0 KB/s","0",getDateTime(),fileLoc,homeDefault+NameTo+"\\"};
    				String[] itemTray={NameTo,"0%"};
    				
    				model2.addRow(item);
    				trayframe.modelTray.addRow(itemTray);
    				dMonitor = new Monitor(window,torrentID,type);
    				System.out.println(NameTo);
    				Torrent to = new Torrent(fileLoc,torrentID,NameTo,homeDefault);
    				addTorrent(to);
    				tmpID = torrentID;
    				torrentID = torrentID+1;
    				textField.setText("");
    			}
    			else {
    				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);
    				return;
    			}
    			if (dMonitor!=null && type!=null) {
			  	scheduled(timer,type,dMonitor,tmpID);
    			}
		  }
		});
		//popup menu items
		popup = new JPopupMenu();
		open = new JMenuItem("Open");
		popup.add(open);
		delete = new JMenuItem("Delete");
		popup.add(delete);
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
	    copyToCl = new JMenuItem("Copy URL");
	    popup.add(copyToCl);
	    //Convertion menu
	    sectionsMenu = new JMenu("Convert to..");
	    sectionsMenu.setVisible(false);
	    //Videoformats
	    mp4 = new JMenuItem("mp4");
	    sectionsMenu.add(mp4);
	    flv = new JMenuItem("flv");
	    sectionsMenu.add(flv);
	    avi = new JMenuItem("avi");
	    sectionsMenu.add(avi);
	    webm = new JMenuItem("webm");
	    sectionsMenu.add(avi);
	    //audioFormats
	    mp3 = new JMenuItem("mp3");
	    sectionsMenu.add(mp3);
	    wav = new JMenuItem("wav");
	    sectionsMenu.add(wav);
	    ogg = new JMenuItem("ogg");
	    sectionsMenu.add(ogg);
	    acc = new JMenuItem("acc");
	    sectionsMenu.add(acc);
	    //image formats
	    png = new JMenuItem("png");
	    sectionsMenu.add(png);
	    jpg = new JMenuItem("jpg");
	    sectionsMenu.add(jpg);
	    gif = new JMenuItem("gif");
	    sectionsMenu.add(gif);
	    bmp = new JMenuItem("bmp");
	    sectionsMenu.add(bmp);

	    popup.add(sectionsMenu);
	    
	    MouseListener popupListener = new PopupListener();
	    table_1.addMouseListener(popupListener);
	    table_2.addMouseListener(popupListener);
		}	
	
	
	
	class PopupListener extends MouseAdapter {
		String file;
		String progress;
		String type = null ;
		String percent;
		String fileMIME[];
		String filetype;
		String location = homeDefault;
		String value;
		int row;
		/*
		 * method that will change our popup menu appearance,depending on the status of the download
		 */
	    public void mousePressed(MouseEvent e) {
	    	sectionsMenu.setVisible(false);
			open.setVisible(false);
			delete.setVisible(false);
			move.setVisible(false);
			pause.setVisible(false);
			resume.setVisible(true);
	    	if (tabbedPane.getSelectedIndex()==1) {
	    		row = table_2.getSelectedRow();
	    		value = table_2.getModel().getValueAt(row, 0).toString();
	    		progress = table_2.getModel().getValueAt(row, 2).toString();
	    		String status = table_2.getModel().getValueAt(row, 3).toString();
				String percentSplit[] = progress.split("%");
				percent = percentSplit[0];
				int per = Integer.parseInt(percent);
				if (per>=1 && per<100) {
					openFolder.setVisible(true);
					pause.setVisible(true);
					if (tr[Integer.parseInt(value)].getStopped()==true) {
						delete.setVisible(true);
						move.setVisible(true);
					}
				}
				else if (per==100){
					openFolder.setVisible(true);
					resume.setVisible(false);
					delete.setVisible(true);
					pause.setVisible(false);
				}
				else if(status.equals("Deleted")) {
					resume.setText("Redownload");
					openFolder.setVisible(false);
				}
				else if (status.equals("Scheduled")) {
					resume.setVisible(false);
					openFolder.setVisible(false);
				}
	    	}
	        if (tabbedPane.getSelectedIndex()==0) {
	        	delete.setVisible(true);
		        row = table_1.getSelectedRow();
		        file = table_1.getModel().getValueAt(row, 1).toString();
		        location = table_1.getModel().getValueAt(row, 6).toString();
		        try {
		        	Path source = Paths.get(location+file);
			        if(source.toFile().exists()) {
				        
							fileMIME = Files.probeContentType(source).split("/");
							filetype = fileMIME[0].toString();
						
			        }
		        } catch (Exception e1) {
		        	System.out.println("Exception FileMIME");
				}
		        
				progress = table_1.getModel().getValueAt(row, 2).toString();
				String status = table_1.getModel().getValueAt(row, 3).toString();
				String percentSplit[] = progress.split("%");
				percent = percentSplit[0];
				int per;
				try {
					per = Integer.parseInt(percent);
				}catch(Exception error) {
					per=0;
				}
				if (per==100 ){
					resume.setText("Redownload");
					sectionsMenu.setVisible(true);
					open.setVisible(true);
					move.setVisible(true);
					if (filetype.equals("image")) {
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
					if (filetype.equals("video")) {
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
					if (filetype.equals("audio")) {
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
					
				}
				else if (status.equals("Deleted") || status.equals("Failed")){
					resume.setText("Redownload");
					delete.setVisible(false);
				}
				else if (status.equals("Scheduled")) {
					resume.setVisible(false);
				}
				else {
					pause.setVisible(true);
					resume.setText("Resume");
				}
				
	        }
	        maybeShowPopup(e);
	    }
	        

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }
	    
	    public PopupListener() {
	    //method do delete the download from the driver
	    delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(tabbedPane.getSelectedIndex()==0) {
					row = table_1.getSelectedRow();
					value = table_1.getModel().getValueAt(row, 0).toString();
			        file = table_1.getModel().getValueAt(row, 1).toString();
			        location = table_1.getModel().getValueAt(row, 6).toString();
					File filetoDelete= new File(location+file);
					if(filetoDelete.exists()) {
						FileUtils de = new FileUtils();
						de.deleteFiles(filetoDelete);
						table_1.getModel().setValueAt("Deleted",row, 3);
						table_1.getModel().setValueAt("0%  0 KB/s",row, 2);
			        }
					else {
						df[Integer.parseInt(value)].PauseDownload();
						while (df[Integer.parseInt(value)].pool.isTerminated()==false) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {e.printStackTrace();}
						}
						df[Integer.parseInt(value)].deleteSubFiles();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {e.printStackTrace();}
						table_1.getModel().setValueAt("Deleted",row, 3);
						table_1.getModel().setValueAt("0%  0 KB/s",row, 2);
					}
				}
				if(tabbedPane.getSelectedIndex()==1) {
					row = table_2.getSelectedRow();
					value = table_2.getModel().getValueAt(row, 1).toString();
			        location = table_2.getModel().getValueAt(row, 6).toString();
					File foldertoDelete= new File(location);
					if(foldertoDelete.exists()) {
						FileUtils futil = new FileUtils();
						System.out.println(value+" "+foldertoDelete.getParent());
						futil.deleteFiles(foldertoDelete);
						table_2.getModel().setValueAt("Deleted", row, 3);
						table_2.getModel().setValueAt("0%  0 KB/s", row, 2);
			        }
				}
			}
	    	
	    });
	    /* method to move file to different location in the drive */
	    move.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(tabbedPane.getSelectedIndex()==0) {
					row = table_1.getSelectedRow();
			        file = table_1.getModel().getValueAt(row, 1).toString();
			        location = table_1.getModel().getValueAt(row, 6).toString();
			        value = table_1.getModel().getValueAt(row, 0).toString();
					File filetoMove = new File(location+file);
					JFileChooser fchooser = new JFileChooser(new File(location));
					fchooser.setAcceptAllFileFilterUsed(false);
					fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (fchooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
						try {
							Files.move(Paths.get(location+file), Paths.get(fchooser.getSelectedFile().getAbsolutePath()+"\\"+file),StandardCopyOption.REPLACE_EXISTING);
							table_1.getModel().setValueAt(fchooser.getSelectedFile().getAbsolutePath()+"\\",row,6);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				else if(tabbedPane.getSelectedIndex()==1) {
					row = table_2.getSelectedRow();
			        location = table_2.getModel().getValueAt(row, 6).toString();
			        file = table_2.getModel().getValueAt(row, 1).toString();
			        value = table_2.getModel().getValueAt(row, 0).toString();
					File foldertoMove = new File(location);
					JFileChooser fchooser = new JFileChooser(foldertoMove.getParent());
					fchooser.setAcceptAllFileFilterUsed(false);
					fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (fchooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
						try {
							FileUtils futils = new FileUtils();
							futils.moveDir(foldertoMove, new File(fchooser.getSelectedFile().getAbsolutePath()+"\\"+file));
							futils.deleteFiles(foldertoMove);
							table_2.getModel().setValueAt(fchooser.getSelectedFile().getAbsolutePath()+"\\"+file+"\\",row,6);
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
				if(tabbedPane.getSelectedIndex()==0) {
				row = table_1.getSelectedRow();
		        file = table_1.getModel().getValueAt(row, 1).toString();
		        location = table_1.getModel().getValueAt(row, 6).toString();
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
				if(tabbedPane.getSelectedIndex()==0) {
				row = table_1.getSelectedRow();
		        file = table_1.getModel().getValueAt(row, 1).toString();
		        location = table_1.getModel().getValueAt(row, 6).toString();
				File filetoOpen = new File(location);
				Desktop desktop = Desktop.getDesktop();
		        if(filetoOpen.exists()) {
					try {
						desktop.open(filetoOpen);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
				}
				else if(tabbedPane.getSelectedIndex()==1) {
					row = table_2.getSelectedRow();
			        file = table_2.getModel().getValueAt(row, 1).toString();
			        location = table_2.getModel().getValueAt(row, 6).toString();
					File filetoOpen = new File(location);
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
				}
				else {
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
				int column = 5;
				if(tabbedPane.getSelectedIndex()==0) {
					type = "URL";
				}
				else {type="Torrent";}
				if (type=="URL") {
					row = table_1.getSelectedRow();
					value = table_1.getModel().getValueAt(row, column).toString();
					StringSelection stringSelection = new StringSelection(value);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
				}
				else if (type=="Torrent") {
					row = table_2.getSelectedRow();
					value = table_2.getModel().getValueAt(row, column).toString();
					StringSelection stringSelection = new StringSelection(value);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
				}
				
				
			}
	    });
	    /*
	     * Pause button,sets the pause flag variable to true,stopping the download,
	     * whose flag variable "complete" is not changed
	     */
	    pause.addActionListener(new ActionListener()
		{
			
			public void actionPerformed(ActionEvent e)
			  {
					if(tabbedPane.getSelectedIndex()==0) {
						type = "URL";
					}
					else {type="Torrent";}
					int column = 0;
					if (type=="URL") {
						
						row = table_1.getSelectedRow();
	    				value = table_1.getModel().getValueAt(row, column).toString();
	    				//pool.shutdownNow();
	    				df[Integer.parseInt(value)].PauseDownload();
	    				if(rateState==true) {
							downloadSpeedLimit(speedLimitNumber);
						}
					}
					else {
						row = table_2.getSelectedRow();
						value = table_2.getModel().getValueAt(row, column).toString();
						tr[Integer.parseInt(value)].setTorPaused();
					}
		
			  }
		});
	    /*
	     * Resume button,creates new download object with the same args as the one from the selected row
	     * and starts the download,SubDownload class has file checks to achieve the resume fuctionality,
	     * torrent is handled by the library
	     */
	    resume.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e)
			  {
				  if(tabbedPane.getSelectedIndex()==0) {
						type = "URL";
					}
				  else {type="Torrent";}
				  int column = 0;
				  if (type=="URL") {
					  row = table_1.getSelectedRow();
					  value = table_1.getModel().getValueAt(row, column).toString();
					  String url = table_1.getModel().getValueAt(row, 5).toString();
					  location = table_1.getModel().getValueAt(row, 6).toString();
					  try {
						  if(df[Integer.parseInt(value)].getPause()==true || df[Integer.parseInt(value)].getComplete()==1) {
						  Download download = new Download(Integer.parseInt(value),url,totalConnections,location);
						  df[Integer.parseInt(value)] = download;
						  Monitor dMonitor = new Monitor(window,Integer.parseInt(value),type);
						  pool.execute(df[Integer.parseInt(value)]);
						  pool.execute(dMonitor);
						  active=active+1;
						  if(rateState==true) {
								downloadSpeedLimit(speedLimitNumber);
							}
						  }
					  }catch(Exception e1) {
						  e1.printStackTrace();
					  }
				  }
				  else if(type=="Torrent"){
					  row = table_2.getSelectedRow();
    				  value = table_2.getModel().getValueAt(row, column).toString();
    				  String magnet = table_2.getModel().getValueAt(row, 5).toString();
    				  String NameTo = table_2.getModel().getValueAt(row, 1).toString();
    				  location = table_2.getModel().getValueAt(row, 6).toString();
    				  if(tr[Integer.parseInt(value)].getPaused()==true || tr[Integer.parseInt(value)].getComplete()==true) {
    				  Torrent to = new Torrent(magnet,Integer.parseInt(value),NameTo,Paths.get(location).getParent().toString()+"\\");
    				  tr[Integer.parseInt(value)]=to;
    				  Monitor dMonitor = new Monitor(window,Integer.parseInt(value),"Torrent");
    				  pool.execute(tr[Integer.parseInt(value)]);
    				  pool.execute(dMonitor);
    				  }
				  }
			  }

			
		});
	    }
	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	        	popup.show(e.getComponent(),e.getX(), e.getY());
	        }
	    }
	}
	private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	 // Monitor class monitors the activity of the download and does actions depending on that activity
	class Monitor extends Thread{

		DownloadManager gui;
		int threadIndex;
		String typeof;
		public Monitor(DownloadManager dm_frame,int idx,String type){
			gui=dm_frame;
			threadIndex = idx;
		  	typeof = type;
		}

		public void run(){
			int currThread=0;
			int dconnections=0;
			boolean downloadcomplete = true;
			boolean failed = false;
			boolean failCheck = true;
			System.out.println("Monitor");
			currThread= threadIndex;
			if (typeof=="URL") {
			//gui.updateStatus(currThread);
			if (gui.df[currThread].getFileSize() == -1){
				JOptionPane.showMessageDialog(
					      	 null,"Download Failed.Try Downloading Again or verify URL is Correct" ,
	              		 "INFORMATION",JOptionPane.INFORMATION_MESSAGE);
	              gui.updateStatus(currThread,true,typeof);
	              failed=true;
	              gui.df[currThread].setComplete(-1);
			} 			
			   
			while(gui.df[currThread].getComplete() == 0)
			{
				dconnections = gui.df[currThread].getTotConnections();
				if (gui.df[currThread].getComplete() == 0 && gui.df[currThread].getActiveSubConn() == dconnections ){
					if(failCheck=true) {
						failed = gui.df[currThread].isDownloadFailed();
						failCheck=false;
						if(failed==true) {
							gui.updateStatus(currThread,true,typeof);
							break;
						}
					}
					gui.updateStatus(currThread,false,typeof);
					downloadcomplete = gui.df[currThread].isDownloadComplete();//Flag
					if (downloadcomplete == true) {
						gui.df[currThread].concatSub();
						active = active-1;
						if(rateState==true) {
							downloadSpeedLimit(speedLimitNumber);
						}
						break;
					}
					else if(gui.df[currThread].getPause()) {						
						active=active-1;
						if(rateState==true) {
							downloadSpeedLimit(speedLimitNumber);
						}
						break;
					}
					gui.updateStatus(currThread,false,typeof);
					   
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(failed==false) {
				gui.model.setValueAt(String.valueOf(gui.df[currThread].DownloadProgress())+"%  0 KB/s",currThread,2);
				DownloadManager.trayframe.modelTray.setValueAt(String.valueOf(gui.df[currThread].DownloadProgress())+"%",currThread,1);
			}
			}
			else if(typeof=="Torrent") {
				System.out.println("monitoring");
				while(gui.tr[currThread].getComplete() == false && gui.tr[currThread].getStopped()==false){
					gui.updateStatus(currThread,false,typeof);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				gui.model2.setValueAt(String.valueOf(gui.tr[currThread].getPerc())+"%  0 KB/s",currThread,2);
				gui.model2.setValueAt("Paused",currThread,3);
			}
		}
		   
	}
	
	//updateStatus method updates our table data
	public void updateStatus( int currThread, boolean dFailed,String type){
		if (type=="URL"){
			if (!dFailed){
			this.model.setValueAt(String.valueOf(this.df[currThread].DownloadProgress())+"% "+this.df[currThread].getDownloadSpeed(),currThread,2);	
	   		this.model.setValueAt(this.df[currThread].getBytesDownloaded()+"MB",currThread,3);
	   		DownloadManager.trayframe.modelTray.setValueAt(String.valueOf(this.df[currThread].DownloadProgress())+"%",currThread,1);
			}
			else {
			this.model.setValueAt("Failed",currThread,2);
	   		this.model.setValueAt("Failed",currThread,3);
	   		DownloadManager.trayframe.modelTray.setValueAt("Failed",currThread,1);
			}
		}
		else if(type=="Torrent") {
			if (!dFailed){
				this.model2.setValueAt(String.valueOf(this.tr[currThread].getPerc())+"% "+this.tr[currThread].getDownloadSpeed(),currThread,2);	
		   		this.model2.setValueAt(this.tr[currThread].getDownloaded()+"MB",currThread,3);
		   		DownloadManager.trayframe.modelTray.setValueAt(String.valueOf(this.tr[currThread].getPerc())+"%",currThread,1);
				}
				else {
				this.model2.setValueAt("Failed",currThread,2);
		   		this.model2.setValueAt("Failed",currThread,3);
		   		DownloadManager.trayframe.modelTray.setValueAt("Failed",currThread,1);
				}
			
		}
	}
	
	
	//addDownload increases the array's size and inserts the DownloadFile object
	public void addDownload(Download d) {
	       
        Download[] dab = new Download[df.length+1];
        System.arraycopy(df, 0, dab, 0, df.length);
        dab[dab.length-1] = d;
        df = dab;
        
    	}
	//like addDownload but for torrent array
	public void addTorrent(Torrent d) {
	       
        Torrent[] dab = new Torrent[tr.length+1];
        System.arraycopy(tr, 0, dab, 0, tr.length);
        dab[dab.length-1] = d;
        tr = dab;
        
    	}
	//sets the download limit to all
	public void downloadSpeedLimit(double speedLimitNumber2) {
		try {
			Thread.sleep(300);
		}catch(Exception e) {}
		if(df.length!=0 && active!=0) {
		double rateper = speedLimitNumber2/active;
		for (int i=0;i<df.length;i++) {
			if(df[i].getComplete()==0) {
			df[i].setRateLimit(rateper);
			}
			}
		}
	}
	
	//scheduled method created a download task that will be executed after x Seconds
	public void scheduled(Long x,String t,Monitor d,int ID) {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(new Runnable() {
            @Override
            public void run() {
            	
    			
    			if (t=="URL") {
    					active=active+1;
    					pool.execute(df[ID]);
    					pool.execute(d);
    					
    					if(rateState==true) {
    						downloadSpeedLimit(speedLimitNumber);
    					}
    				/*--------------*/
    			}else if(t=="Torrent"){
    				pool.execute(tr[ID]);
    				pool.execute(d);
    			}
    			System.out.println("Out of time!");
            }}, x, TimeUnit.SECONDS);
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
}
