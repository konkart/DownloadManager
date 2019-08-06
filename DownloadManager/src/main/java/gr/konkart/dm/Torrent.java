package gr.konkart.dm;

import java.io.File;
import java.text.NumberFormat;
import java.time.Duration;
import bt.Bt;
import bt.data.Storage;
import bt.data.file.FileSystemStorage;
import bt.dht.DHTModule;
import bt.protocol.crypto.EncryptionPolicy;
import java.nio.file.*;
import bt.dht.*;
import bt.runtime.BtClient;
import bt.runtime.Config;
import bt.torrent.selector.PieceSelector;
import bt.torrent.selector.SequentialSelector;




public class Torrent implements Runnable{
	String fileLoc;
	long startTime;
	int complete; //Completion flag
	int downloadId; //Download ID
	public Torrent(String fileLoc,int downloadID){//Torrent constructor
		this.fileLoc = fileLoc;
		this.downloadId=downloadID;
		this.complete=0;

		}
	volatile boolean paused=false; //Paused Flag
	long downloaded;//Downloaded Bytes
	long downloadedS;//Variable used in download speed(gets 'downloaded's value)
	int total;//total torrent pieces
	int piece;//completed pieces
	int perce;//percentage
	@Override
	public void run() {
			String home = System.getProperty("user.home");
			System.setProperty("java.net.preferIPv4Stack" , "true");
		
		//torrent client config	
		Config config = new Config() {
			//number of threads to use
		    @Override
		    public int getNumOfHashingThreads() {
		        return Runtime.getRuntime().availableProcessors() * 4;
		    }
		};
		Duration dur = Duration.ofSeconds(60);
		config.setPeerConnectionTimeout(dur);
		config.setPeerConnectionRetryCount(1);
		config.setMaxPieceReceivingTime(Duration.ofSeconds(240)); 
		config.setEncryptionPolicy(EncryptionPolicy.REQUIRE_ENCRYPTED);
		config.setTimeoutedAssignmentPeerBanDuration(Duration.ofMinutes(15));
		config.setMaxPeerConnections(100);
		config.setNumberOfPeersToRequestFromTracker(30);
		DHTModule dhtModule = new DHTModule(new DHTConfig() {
		    @Override
		    public boolean shouldUseRouterBootstrap() {
		        return true;
		    }
		});

		// get download directory
		Path targetDirectory = new File(home+"\\Downloads\\").toPath();

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
		        .selector(selector)
		        .build();
		startTime = System.currentTimeMillis();
		//start client with callback every 1000ms
		client.startAsync(state -> {
			total = state.getPiecesTotal();
			piece = state.getPiecesComplete();
			perce = (piece*100)/(total);
			//if no pieces remaining stop client and flag as completed
		    if (state.getPiecesRemaining() == 0) {
		        client.stop();
		        complete = 1;
		    }
		    else if(paused==true) {
		    	client.stop();
		    }
		  //get downloaded bytes
		    downloaded=state.getDownloaded();
		}, 1000).join();
		}
	//get downloaded data in Megabytes
	public long getDownloaded() {
		return (downloaded/1024)/1024;
	}
	//download speed calculation
	public String getDownloadSpeed() {
		float current_speed;
		downloadedS=downloaded;
		if (downloadedS > 0 ) {
		current_speed = (float)( downloadedS / (System.currentTimeMillis() - startTime));
		downloadedS=0;
		}
		else {
		current_speed = 0;
		}
		NumberFormat formatter = NumberFormat.getNumberInstance() ;
		formatter.setMaximumFractionDigits(2);
		if (current_speed>1000) {
			current_speed=current_speed/1000;
			return " "+formatter.format(current_speed)+" MB/s ";
		}
		else {
			return " "+formatter.format(current_speed)+" KB/s ";
		}
	}

	public int getSize() {//gets the torrent's data size
		return total;
	}
	public int getPerc() {//get percentage completed
		int p=1;
		if (perce!=0) {
		
		return perce;
		}
		else {
			perce = p;
			return perce;
		}
		
	}
	public void setTorPaused() {//funtion that is called to pause/stop the torrent
		paused = true;
	}
	public boolean getPaused() {
		return paused;
	}
	
}


