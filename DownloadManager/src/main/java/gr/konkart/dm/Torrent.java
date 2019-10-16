package gr.konkart.dm;

import java.io.File;
import java.time.Duration;
import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTModule;
import java.nio.file.*;
import bt.dht.*;
import bt.runtime.BtClient;
import bt.runtime.Config;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.SequentialSelector;

public class Torrent implements Runnable{
	String fileLoc;
	long startTime;
	private boolean complete; //Completion flag
	private String location,folderName;
	private boolean stopped;
	private int trayRow,downloadID;
	public Torrent(int id,String fileLoc,String nameFolder,String location,int trayRow) {
		this.fileLoc = fileLoc;
		this.location = location;
		this.folderName = nameFolder;
		this.trayRow = trayRow;
		this.downloadID = id;
		stopped = true;
	}
	volatile boolean paused=false;	// Paused Flag
	private long downloaded;		// Downloaded Bytes
	private long downloadedS;		// Variable used in download speed(gets 'downloaded's value)
	private int total;				// total torrent pieces
	private int piece;				// completed pieces
	private int perce;				// percentage
	@Override
	public void run() {
		downloaded = 0;
		System.setProperty("java.net.preferIPv4Stack" , "true");
		complete=false;
		stopped=false;
		paused=false;
		//torrent client config	
		Config config = new Config() {
			//number of threads to use
			@Override
			public int getNumOfHashingThreads() {
				return Runtime.getRuntime().availableProcessors() * 2;
			}
		};
		
		config.setPeerConnectionTimeout(Duration.ofSeconds(60));
		DHTModule dhtModule = new DHTModule(new DHTConfig() {
		    @Override
		    public boolean shouldUseRouterBootstrap() {
		        return true;
		    }
		});

		// get download directory
		Path targetDirectory = new File(location+folderName+"\\").toPath();

		// create file system based backend for torrent data
		Storage storage = new FileSystemStorage(targetDirectory);
		PieceSelector selector= SequentialSelector.sequential();
		
		// create client with a private runtime
		BtClient client = Bt.client()
				.config(config)
				.storage(storage)
				.magnet(fileLoc)
				.autoLoadModules()
				.stopWhenDownloaded()
				.module(dhtModule)
				.build();
		startTime = System.currentTimeMillis();
		//start client with callback every 1000ms
		client.startAsync(state -> {
			total = state.getPiecesTotal();
			piece = state.getPiecesComplete();
			perce = (piece*100) / total;
			System.out.println(total + " " + piece + " "+ perce);
			//if no pieces remaining stop client and flag as completed
			if (perce == 100) {
				client.stop();
				complete = true;
				stopped = true;
				System.out.println(stopped);
		    } else if(paused==true) {
		    	client.stop();
		    	stopped = true;
		    	System.out.println(stopped);
		    }
			//get downloaded bytes
			downloaded=state.getDownloaded();
			if (downloaded == 0) {
				startTime = System.currentTimeMillis();
			}
		}, 1000).join();
	}
	//get downloaded data
	public String getDownloaded() {
		if(downloaded>1024000) {
			return String.valueOf((downloaded/1024)/1024)+"MB";
		} else if (downloaded>1024) {
			return String.valueOf((downloaded/1024))+"KB";
		} else {
			return String.valueOf(downloaded)+"B";
		}
	}
	//download speed calculation
	public String getDownloadSpeed() {
		float current_speed;
		downloadedS=downloaded;
		if (downloadedS > 0 ) {
		current_speed = (float)( downloadedS / (System.currentTimeMillis() - startTime));
		downloadedS=0;
		} else {
		current_speed = 0;
		}
		if (current_speed>1000) {
			current_speed=current_speed/1000;
			return " "+String.format("%.1f",current_speed)+" MB/s ";
		} else {
			return " "+String.format("%.0f",current_speed)+" KB/s ";
		}
	}

	public int getSize() {//gets the torrent's data size
		return total;
	}
	public void setSize(int size) {
		this.total = size;
	}
	public int getPerc() {//get percentage completed
		return perce;	
	}
	public boolean getComplete() {
		return complete;
	}
	public boolean getStopped() {
		return stopped;
	}
	public void setTorPaused() {
		paused = true;
	}
	public void setTorStopped(boolean stopped) {
		this.stopped = stopped;
	}
	public boolean getPaused() {
		return paused;
	}
	public String getFolderName() {
		return folderName;
	}
	public int getTrayRow() {
		return trayRow;
	}
	public String getMagnetURI() {
		return fileLoc;
	}
	public String getLocation() {
		return location;
	}
	public int getDownloadID() {
		return downloadID;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public void setTrayRow(int tRow) {
		this.trayRow = tRow;
	}
	
}


