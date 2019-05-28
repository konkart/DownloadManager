package gr.konkart.dm;

import java.awt.AWTException;
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
import javax.swing.JTable;
import javax.swing.UIManager;
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
	public int DownloadID = 0;
	public int TorrentID = 0;
	static TrayIcon trayIcon = new TrayIcon(image, "Tester2");
	static JFrame frame;
	public DownloadFile df[] = new  DownloadFile[0];
	public Torrent tr[] =new Torrent[0];
	static TrayFrame trayframe;
	private JTextField textField;
	private JTable table_1;
	private JTable table_2;
	public String ls_FileLoc;
	static DownloadManager window;
	private DefaultTableModel model;
	private DefaultTableModel model2;
	public int li_TotalConnections = 5;
	public int li_BufferLen = 4;
	public boolean RateState=false;
	public volatile int wantedDownloadSpeed=0; //0 is unlimited
	JMenuItem pause;
	JMenuItem resume;
	JMenuItem delete;
	JMenuItem open;
	JMenuItem copyToCl;
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
	ExecutorService pool = Executors.newCachedThreadPool();
	public static void main(String[] args) {
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
	

	public DownloadManager() {
		initialize();
	}


	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 450);
		frame.setDefaultCloseOperation(JFrame.ICONIFIED);
		frame.getContentPane().setLayout(null);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 64, 564, 336);
		frame.getContentPane().add(tabbedPane);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Direct", null, panel, null);
		panel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 23, 559, 285);
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
				"ID", "Name", "Progress", "Size", "Date", "URL"
			}
		));
		table_1.getColumnModel().getColumn(0).setPreferredWidth(27);
		scrollPane.setViewportView(table_1);
		model = (DefaultTableModel) table_1.getModel();
		table_1.setDefaultEditor(Object.class, null);
		//model.addRow(new Object[]{"Column 1", "Column 2", "Column 3","Column 4"});
		
		textField = new JTextField();
		textField.setBounds(10, 11, 366, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.setBounds(386, 10, 119, 23);
		frame.getContentPane().add(btnDownload);
		
		btnDownload.addActionListener(new ActionListener()
		{
		  public void actionPerformed(ActionEvent e)
		  {
			
			
			if (textField.getText().equals("")){
				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);

				return;
			}
			
			String type;
			if(URLHandler.isUrl(textField.getText())=="URL") {
				type = "URL";
				System.out.println("it is a URL");
				ls_FileLoc = textField.getText();
				try {
					String ext = URLHandler.gContentTypeA(ls_FileLoc);
					System.out.println(ext);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				String[] item={""+DownloadID+"",URLHandler.getFilename(ls_FileLoc),"0% 0KB/s","0",getDateTime(),ls_FileLoc};
				String[] itemTray={URLHandler.getFilename(ls_FileLoc),"0%"};
				
				model.addRow(item);
				trayframe.modelTray.addRow(itemTray);
				/*-------OOOOO-------*/
				DownloadFile download = new DownloadFile(DownloadID,ls_FileLoc,li_TotalConnections,li_BufferLen);
			    addDownload(download);
			    
					//df[DownloadID].start();
					Monitor dMonitor = new Monitor(window,DownloadID,type);
					pool.execute(df[DownloadID]);
					pool.execute(dMonitor);
					DownloadID = DownloadID + 1;
					textField.setText("");
					if(RateState==true) {
						 try {
								Thread.sleep(300);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						DownloadSpeedLimit(100);
					}
				/*--------------*/
			}
			else if (URLHandler.isUrl(textField.getText())=="Torrent") {
				type="Torrent";
				ls_FileLoc = textField.getText();
				String[] magnetParts = ls_FileLoc.split("&"); 
				String toName = magnetParts[1];
				String[] Split_toEq = toName.split("=");
				String NameTo = Split_toEq[1];
				try {
					NameTo = URLDecoder.decode(NameTo,StandardCharsets.UTF_8.name());
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String[] item={""+TorrentID+"",NameTo,"0% 0KB/s","0",getDateTime(),ls_FileLoc};
				String[] itemTray={NameTo,"0%"};
				
				model2.addRow(item);
				trayframe.modelTray.addRow(itemTray);
				Monitor dMonitor = new Monitor(window,TorrentID,type);
				System.out.println(NameTo);
				Torrent to = new Torrent(ls_FileLoc,TorrentID);
				addTorrent(to);
				
				pool.execute(tr[TorrentID]);
				pool.execute(dMonitor);
				TorrentID=TorrentID+1;
				textField.setText("");
			}
			else {
				JOptionPane.showMessageDialog(null,"URL is Invalid or Empty.Please enter valid URL","ERROR",JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			
			
			
			
		    
				
		  }
		});
		
		/*---------------------*/
		
		
		JToggleButton tglbtnNewToggleButton = new JToggleButton("Speed Limit");
		 tglbtnNewToggleButton.addItemListener(new ItemListener() {
			   public void itemStateChanged(ItemEvent ev) {
			      if(ev.getStateChange()==ItemEvent.SELECTED){
			    	DownloadSpeedLimit(100);
			    	RateState=true;
			        System.out.println("button is selected");
			      } else if(ev.getStateChange()==ItemEvent.DESELECTED){
			    	DownloadSpeedLimit(0);
			    	RateState=false;
			        System.out.println("button is not selected");
			      }
			   }
			});
		tglbtnNewToggleButton.setBounds(465, 0, 94, 23);
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
					"ID","Name", "Progress", "Session bytes", "Date" , "Magnet"
			}
		));
		scrollPane_1.setViewportView(table_2);
		
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(1), new Long(1)));
		spinner.setBounds(370, 44, 34, 20);
		frame.getContentPane().add(spinner);
		
		JLabel lblDay = new JLabel("D");
		lblDay.setBounds(360, 47, 11, 14);
		frame.getContentPane().add(lblDay);
		
		JLabel lblH = new JLabel("H");
		lblH.setBounds(410, 47, 11, 14);
		frame.getContentPane().add(lblH);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(24), new Long(1)));
		spinner_1.setBounds(418, 44, 39, 20);
		frame.getContentPane().add(spinner_1);
		
		JLabel lblM = new JLabel("M");
		lblM.setBounds(461, 47, 11, 14);
		frame.getContentPane().add(lblM);
		
		JSpinner spinner_2 = new JSpinner();
		spinner_2.setModel(new SpinnerNumberModel(new Long(0), new Long(0), new Long(60), new Long(1)));
		spinner_2.setBounds(472, 44, 39, 20);
		frame.getContentPane().add(spinner_2);
		model2 = (DefaultTableModel) table_2.getModel();
		
		JButton btnSchedule = new JButton("Schedule");
		btnSchedule.setToolTipText("Schedule");
		btnSchedule.setBounds(515, 10, 59, 54);
		frame.getContentPane().add(btnSchedule);
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
    			if(URLHandler.isUrl(textField.getText())=="URL") {
    				type = "URL";
    				System.out.println("it is a URL");
    				ls_FileLoc = textField.getText();
    				try {
    					String ext = URLHandler.gContentTypeA(ls_FileLoc);
    					System.out.println(ext);
    				} catch (IOException e1) {
    					e1.printStackTrace();
    				}
    				String[] item={""+DownloadID+"",URLHandler.getFilename(ls_FileLoc),"0% 0KB/s","0",getDateTime(),ls_FileLoc};
    				String[] itemTray={URLHandler.getFilename(ls_FileLoc),"0%"};
    				
    				model.addRow(item);
    				trayframe.modelTray.addRow(itemTray);
    				/*-------OOOOO-------*/
    				DownloadFile download = new DownloadFile(DownloadID,ls_FileLoc,li_TotalConnections,li_BufferLen);
    			    addDownload(download);
    			    dMonitor = new Monitor(window,DownloadID,type);
    			    tmpID = DownloadID;
    			    DownloadID = DownloadID+1;
    			    textField.setText("");
    			}
    			else if (URLHandler.isUrl(textField.getText())=="Torrent") {
    				type="Torrent";
    				ls_FileLoc = textField.getText();
    				String[] magnetParts = ls_FileLoc.split("&"); 
    				String toName = magnetParts[1];
    				String[] Split_toEq = toName.split("=");
    				String NameTo = Split_toEq[1];
    				try {
    					NameTo = URLDecoder.decode(NameTo,StandardCharsets.UTF_8.name());
    				} catch (UnsupportedEncodingException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
    				String[] item={""+TorrentID+"",NameTo,"0% 0KB/s","0",getDateTime(),ls_FileLoc};
    				String[] itemTray={NameTo,"0%"};
    				
    				model2.addRow(item);
    				trayframe.modelTray.addRow(itemTray);
    				dMonitor = new Monitor(window,TorrentID,type);
    				System.out.println(NameTo);
    				Torrent to = new Torrent(ls_FileLoc,TorrentID);
    				addTorrent(to);
    				tmpID = TorrentID;
    				TorrentID = TorrentID+1;
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

		popup = new JPopupMenu();
		open = new JMenuItem("Open");
		popup.add(open);
		delete = new JMenuItem("Delete");
		popup.add(delete);
		delete.setVisible(false);
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
		String home = System.getProperty("user.home");
	    public void mousePressed(MouseEvent e) {
	    	if (tabbedPane.getSelectedIndex()==1) {
	    		sectionsMenu.setVisible(false);
	    		open.setVisible(false);
	    		delete.setVisible(false);
	    		int row = table_2.getSelectedRow();
	    		progress = table_2.getModel().getValueAt(row, 2).toString();
				String percentSplit[] = progress.split("%");
				percent = percentSplit[0];
				int per = Integer.parseInt(percent);
				if (per==100 ){
					open.setVisible(true);
				}
	    	}
	        if (tabbedPane.getSelectedIndex()==0) {
	        int row = table_1.getSelectedRow();
	        file = table_1.getModel().getValueAt(row, 1).toString();
	        Path source = Paths.get(home+"\\Downloads\\"+file);
	        try {
				fileMIME = Files.probeContentType(source).split("/");
				filetype = fileMIME[0].toString();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			progress = table_1.getModel().getValueAt(row, 2).toString();
			String percentSplit[] = progress.split("%");
			percent = percentSplit[0];
			int per = Integer.parseInt(percent);
			if (per==100 ){
				pause.setVisible(false);
				resume.setText("Redownload");
				sectionsMenu.setVisible(true);
				open.setVisible(true);
				delete.setVisible(true);
				System.out.println(filetype);
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
	        }
	        
			
			maybeShowPopup(e);
	    }
	        

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }
	    public PopupListener() {
	    delete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tabbedPane.getSelectedIndex()==0) {
					int rowtoDelete = table_1.getSelectedRow();
			        file = table_1.getModel().getValueAt(rowtoDelete, 1).toString();
					File filetoDelete= new File(home+"\\Downloads\\"+file);
					if(filetoDelete.exists()) {
						try {
							FileUtils de = new FileUtils();
							de.delete(file);
							table_1.getModel().setValueAt("Deleted",rowtoDelete, 3);
						} catch (IOException e) {
							e.printStackTrace();
						}
			        }
				}
				if(tabbedPane.getSelectedIndex()==1) {
					int rowtoDelete = table_2.getSelectedRow();
			        file = table_2.getModel().getValueAt(rowtoDelete, 1).toString();
					File filetoDelete= new File(home+"\\Downloads\\"+file);
					if(filetoDelete.exists()) {
						try {
							FileUtils de = new FileUtils();
							de.delete(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
			        }
				}
			}
	    	
	    });
	    open.addActionListener(new ActionListener() {
	    	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(tabbedPane.getSelectedIndex()==0) {
				int row = table_1.getSelectedRow();
		        file = table_1.getModel().getValueAt(row, 1).toString();
				File filetoOpen = new File(home+"\\Downloads\\"+file);
				Desktop desktop = Desktop.getDesktop();
		        if(filetoOpen.exists()) {
					try {
						desktop.open(filetoOpen);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }
				}
				if(tabbedPane.getSelectedIndex()==1) {
					int row = table_2.getSelectedRow();
			        file = table_2.getModel().getValueAt(row, 1).toString();
					File foldertoOpen = new File(home+"\\Downloads\\");
					Desktop desktop = Desktop.getDesktop();
			        if(foldertoOpen.exists())
						try {
							desktop.open(foldertoOpen);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
			}
	    	
	    });	
	    mp4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.videoConv(home+"\\Downloads\\"+file,"mp4");
			}
	    });
	    webm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.videoConv(home+"\\Downloads\\"+file,"webm");	
			}
	    });
	    avi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.videoConv(home+"\\Downloads\\"+file,"avi");
			}
	    });
	    flv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.videoConv(home+"\\Downloads\\"+file,"flv");
			}
	    });
	    mp3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.audioConv(home+"\\Downloads\\"+file,"mp3");
			}
	    });
	    ogg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.audioConv(home+"\\Downloads\\"+file,"ogg");
			}
	    });
	    acc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.audioConv(home+"\\Downloads\\"+file,"acc");
			}
	    });
	    wav.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.audioConv(home+"\\Downloads\\"+file,"wav");
			}
	    });
	    png.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.imgConv(home+"\\Downloads\\"+file,"png");	
			}
	    });
	    bmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.imgConv(home+"\\Downloads\\"+file,"bmp");	
			}
	    });
	    gif.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.imgConv(home+"\\Downloads\\"+file,"gif");
			}
	    });
	    jpg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					int row = table_1.getSelectedRow();
					String file = table_1.getModel().getValueAt(row, 1).toString();
					FileUtils conv = new FileUtils();
					conv.imgConv(home+"\\Downloads\\"+file,"jpg");
			}
	    });
	    	
	    copyToCl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int column = 5;
				if(tabbedPane.getSelectedIndex()==0) {
					type = "URL";
				}
				else {type="Torrent";}
				if (type=="URL") {
					int row = table_1.getSelectedRow();
					String value = table_1.getModel().getValueAt(row, column).toString();
					StringSelection stringSelection = new StringSelection(value);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
				}
				else if (type=="Torrent") {
					int row = table_2.getSelectedRow();
					String value = table_2.getModel().getValueAt(row, column).toString();
					StringSelection stringSelection = new StringSelection(value);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
				}
				
				
			}
	    });
	    	
	    pause.addActionListener(new ActionListener()
		{
			
			public void actionPerformed(ActionEvent e)
			  {
					if(tabbedPane.getSelectedIndex()==0) {
						type = "URL";
					}
					else {type="Torrent";}
					int column = 0;
					System.out.println(type);
					if (type=="URL") {
						
						int row = table_1.getSelectedRow();
	    				String value = table_1.getModel().getValueAt(row, column).toString();
	    				System.out.println(value);
	    				System.out.println(df[Integer.parseInt(value)]);
	    				//pool.shutdownNow();
	    				df[Integer.parseInt(value)].PauseDownload();
	    				System.out.println(df.length);
	    				if(RateState==true) {
							 try {
									Thread.sleep(300);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							DownloadSpeedLimit(100);
						}
					}
					else {
						int row = table_2.getSelectedRow();
						String value = table_2.getModel().getValueAt(row, column).toString();
						tr[Integer.parseInt(value)].setTorPaused();
					}
		
			  }
		});
	    resume.addActionListener(new ActionListener()
		{
			  public void actionPerformed(ActionEvent e)
			  {
				  
				  int column = 0;
				  if (type=="URL") {
				  int row = table_1.getSelectedRow();
				  String value = table_1.getModel().getValueAt(row, column).toString();
				  String url = table_1.getModel().getValueAt(row, 5).toString();
				  DownloadFile download = new DownloadFile(Integer.parseInt(value),url,li_TotalConnections,li_BufferLen);
				  df[Integer.parseInt(value)] = download;
				  Monitor dMonitor = new Monitor(window,Integer.parseInt(value),type);
				  pool.execute(df[Integer.parseInt(value)]);
				  pool.execute(dMonitor);
				  if(RateState==true) {
					  try {
							Thread.sleep(300);
						} catch (InterruptedException s) {
							// TODO Auto-generated catch block
							s.printStackTrace();
						}
						DownloadSpeedLimit(100);
					}
				  //df[Integer.parseInt(value)].ResumeDownload();
				  }
				  else if(type=="Torrent"){
					  int row = table_2.getSelectedRow();
    				  String value = table_2.getModel().getValueAt(row, column).toString();
    				  String magnet = table_2.getModel().getValueAt(row, 5).toString();
    				  Torrent to = new Torrent(magnet,Integer.parseInt(value));
    				  tr[Integer.parseInt(value)]=to;
    				  Monitor dMonitor = new Monitor(window,Integer.parseInt(value),"Torrent");
    				  pool.execute(tr[Integer.parseInt(value)]);
    				  pool.execute(dMonitor);
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
	/*---------------------*/
	
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
		   int Downloadcomplete = 1;
		   String[] files;
		   FileUtils futils = new FileUtils();
		   System.out.println("Monitor");
		   currThread= threadIndex;
		   if (typeof=="URL") {
		   //gui.updateStatus(currThread);
		   if (gui.df[currThread].FileSize == -1){
		   		gui.df[currThread].Complete =-1;
				JOptionPane.showMessageDialog(
				      	 null,"Download Failed.Try Downloading Again or verify URL is Correct" ,
              		 "INFORMATION",JOptionPane.INFORMATION_MESSAGE);
              gui.updateStatus(currThread,true,typeof);
              		 				
		   		}
		   			
		   
		   while(gui.df[currThread].Complete == 0)
		   {
			   dconnections = gui.df[currThread].TotConnections;
			   if (gui.df[currThread].Complete == 0 && gui.df[currThread].ActiveSubConn == dconnections ){
				   gui.updateStatus(currThread,false,typeof);
				   Downloadcomplete = 1;//Flag
				   files =  new String[dconnections];
				   for(int subDown = 0; subDown < dconnections ; subDown ++){
					   files[subDown]= gui.df[currThread].getSubDownId(subDown);
					   if(gui.df[currThread].isSubDownComplete(subDown) == 0){
						   Downloadcomplete = 0; //Download Incomplete
						   break;
					   }
				   }
				   if (Downloadcomplete == 1 || gui.df[currThread].getPause() == true){
					   if(!gui.df[currThread].getPause()) {
						   try {
							   futils.concat(files,gui.df[currThread].FilePath);
								} catch (IOException e) {
										e.printStackTrace();
								}
							for(int fileid=0;fileid < dconnections;fileid++) {
			   						try {
										futils.delete(files[fileid]);
										gui.df[currThread].Complete = 1;
			   						} catch (IOException e) {
										e.printStackTrace();
									}
		   						}
		   					}
					   else {
						   gui.df[currThread].Complete = 1;
						   if(RateState==true) {
							   try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
								DownloadSpeedLimit(100);
							}
					   }
					   gui.updateStatus(currThread,false,typeof);
					   System.out.println("Monitor");
				   }
			   }
			   try {
				   Thread.sleep(1000);
			   } catch (InterruptedException e) {
				   e.printStackTrace();
			   }
		   }
		   gui.model.setValueAt(String.valueOf(gui.df[currThread].DownloadProgress())+"% 0KB/s",currThread,2);
		   }
		   else if(typeof=="Torrent") {
			   System.out.println("monitoring");
			   while(gui.tr[currThread].Complete == 0){
				   
				   gui.updateStatus(currThread,false,typeof);
				   try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			   }
			   gui.model2.setValueAt(String.valueOf(gui.tr[currThread].getPerc())+"%",currThread,2);
		   }
		}
		   
	}
	
	//Method to update our data
	public void updateStatus( int currThread, boolean dFailed,String type){
		if (type=="URL"){
			if (!dFailed){
			this.model.setValueAt(String.valueOf(this.df[currThread].DownloadProgress())+"% "+this.df[currThread].getDownloadSpeed(),currThread,2);	
	   		this.model.setValueAt(this.df[currThread].getBytesDownloaded()+"MB",currThread,3);
	   		DownloadManager.trayframe.modelTray.setValueAt(String.valueOf(this.df[currThread].DownloadProgress())+"%",currThread,1);
			}
			else {
			String str="Failed";
			this.model.setValueAt((Object)str,currThread,2);
	   		this.model.setValueAt((Object)str,currThread,3);
	   		DownloadManager.trayframe.modelTray.setValueAt((Object)str,currThread,1);
			}
		}
		else if(type=="Torrent") {
			if (!dFailed){
				this.model2.setValueAt(String.valueOf(this.tr[currThread].getPerc())+"%",currThread,2);	
		   		this.model2.setValueAt(this.tr[currThread].getDownloaded()+"MB",currThread,3);
		   		DownloadManager.trayframe.modelTray.setValueAt(String.valueOf(this.tr[currThread].getPerc())+"%",currThread,1);
				}
				else {
				String str="Failed";
				this.model2.setValueAt((Object)str,currThread,2);
		   		this.model2.setValueAt((Object)str,currThread,3);
		   		DownloadManager.trayframe.modelTray.setValueAt((Object)str,currThread,1);
				}
			
		}
	}
	
	
	
	public void addDownload(DownloadFile d) {
	       
        DownloadFile[] dab = new DownloadFile[df.length+1];
        System.arraycopy(df, 0, dab, 0, df.length);
        dab[dab.length-1] = d;
        df = dab;
        
    	}
	public void addTorrent(Torrent d) {
	       
        Torrent[] dab = new Torrent[tr.length+1];
        System.arraycopy(tr, 0, dab, 0, tr.length);
        dab[dab.length-1] = d;
        tr = dab;
        
    	}
	//RATELIMIT
	public void DownloadSpeedLimit(int s) {
		if(df.length!=0) {
		float rateper = s/df.length;
		for (int i=0;i<df.length;i++) {
			df[i].setRateLimit(rateper);
			}
		}
	}
	
	
	public void scheduled(Long x,String t,Monitor d,int ID) {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(new Runnable() {
            @Override
            public void run() {
            	
    			
    			if (t=="URL") {
    					pool.execute(df[ID]);
    					pool.execute(d);
    					
    					if(RateState==true) {
    						 try {
    								Thread.sleep(300);
    							} catch (InterruptedException e1) {
    								// TODO Auto-generated catch block
    								e1.printStackTrace();
    							}
    						DownloadSpeedLimit(100);
    					}
    				/*--------------*/
    			}else if(t=="Torrent"){
    				pool.execute(tr[ID]);
    				pool.execute(d);
    			}
    			System.out.println("Out of time!");
            }}, x, TimeUnit.SECONDS);
	}
}
